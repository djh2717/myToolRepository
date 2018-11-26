package my.code.repository.custom.view;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import my.demo.one.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import my.code.repository.utils.BitmapUtil;

/**
 * 此Activity用于封装从相册或者相机获取一张图片,可以选择对图片进行裁剪后返回或者直接返回图片.
 * 裁剪类别有:圆形,正方形. 通过公有字段传入数据到intent来决定裁剪类别.
 * notice: Remember to register in manifest.
 * notice: Also need a fileProvider if you use this after 7.0, authority is "avatar.author".
 *
 * @author djh
 */
public class PhotoActivity extends AppCompatActivity {

    @BindView(R.id.toolBar)
    Toolbar toolBar;
    @BindView(R.id.weChatImageView)
    WeChatCrop weChatCrop;


    /**
     * This component is for testing,a component that shows the cropped image.
     */
    @BindView(R.id.cropImageView)
    ImageView cropImageView;

    /**
     * For external selection of camera or album and crop type fields
     */
    public static final int TYPE_RAW = 2;
    public static final int TYPE_RECT = 0;
    public static final int TYPE_ALBUM = 1;
    public static final int TYPE_CAMERA = 0;
    public static final int TYPE_CIRCLE = 1;
    public static final String FROM_TYPE = "fromType";
    public static final String CLIP_TYPE = "clipType";

    /**
     * For external get image fields
     */
    public static final String IMAGE_RETURN_PATH = "imageReturn";
    public static final String AVATAR_FILE_NAME = "avatarFileName";

    /**
     * Open request code for album request permission
     */
    private static final int CAMERA = 0;
    private static final int ALBUM = 1;
    private static final int REQUEST_OPEN_ALBUM = 1;


    private int clipType;
    private File avatarFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        setTitle("");
        setSupportActionBar(toolBar);
        jumpTo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA:
                if (resultCode == RESULT_OK) {
                    //Pass ContentProvider get the input stream after taking a photo of the camera
                    //Compressed display cropped image
                    clipBitmap();
                } else {
                    finish();
                }
                break;
            case ALBUM:
                if (resultCode == RESULT_OK) {
                    //Get the image path returned by the album.
                    //Copy the image to the cache directory according to the path.
                    //Then compress and display,start cropping pictures
                    String avatarPath = parsePathFromAlbum(data.getData());
                    saveAlbumImageToCache(avatarPath);
                    clipBitmap();
                } else {
                    finish();
                }
                break;
            default:
        }
    }

    private void jumpTo() {
        Intent intent = getIntent();

        //According to Intent data to decide to jump to the camera or album.
        //Unspecified throw exception
        int fromType = intent.getIntExtra("fromType", -1);
        if (fromType == -1) {
            throw new RuntimeException("Image source not specified");
        }

        //Get the specified crop shape,unspecified throw exception
        clipType = intent.getIntExtra("clipType", -1);
        if (clipType == -1) {
            throw new RuntimeException("Clip category not specified");
        }

        //According to the file name obtained.
        //Create avatar file in cache directory,if file name not specified,throw an exception.
        String avatarFileName = intent.getStringExtra("avatarFileName");
        if (TextUtils.isEmpty(avatarFileName)) {
            throw new RuntimeException("File name not specified");
        }

        avatarFile = new File(getCacheDir(), avatarFileName + ".jpg");
        if (!avatarFile.exists()) {
            try {
                avatarFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Jump camera or album by fromType
        switch (fromType) {
            case CAMERA:
                openCamera();
                break;
            case ALBUM:
                openAlbum();
                break;
            default:
        }
    }

    private void openCamera() {
        Uri avatarUri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            avatarUri = Uri.fromFile(avatarFile);
        } else {
            avatarUri = FileProvider.getUriForFile(this, "avatar.author", avatarFile);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, avatarUri);
        startActivityForResult(intent, CAMERA);
    }

    private void openAlbum() {
        // Request for access
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_OPEN_ALBUM);
        } else {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            startActivityForResult(intent, ALBUM);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_OPEN_ALBUM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "拒绝权限,无法打开相册!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Parse the photo uri returned from album.
     * Then pass uri get photo path
     */
    private String parsePathFromAlbum(Uri uri) {
        String imagePath = null;
        //如果是document类型的Uri,则通过document id解析
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docuId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(Objects.requireNonNull(uri).getAuthority())) {
                //解析出数字格式的id
                String id = docuId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("name://downloads/public_downloads"), Long.valueOf(docuId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("name".equalsIgnoreCase(Objects.requireNonNull(uri).getScheme())) {
            //如果是content类型的Uri,使用普通方式解析
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri,直接获取图片路径即可
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    /**
     * According to uri get image path
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * Crop the captured image,load after compression processing
     * 此处发现,如果URi是被装饰过了的,Uri.getPath获取的路径也是被装饰过了的
     */
    private void clipBitmap() {
        weChatCrop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //获取返回的图片,要在OnGlobalLayoutListener内部加载bitmap
                //不然无法获取宽高进行压缩
                Bitmap bitmap = BitmapUtil.decodeFile(avatarFile.getPath(),
                        weChatCrop.getWidth(), weChatCrop.getHeight());
                // Rotate the image.
                // In order to adapt to the SamSung mobile phone after taking pictures to rotate
                bitmap = rotateBitmap(bitmap, avatarFile.getPath());
                //Determine if the djh needs to crop
                if (clipType == TYPE_RAW) {
                    //Save the rotated image
                    try {
                        Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(avatarFile));
                        //Hide cropping control
                        weChatCrop.setVisibility(View.GONE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //The djh decides that no cropping is required.
                    //Return image path directly.
                    Intent intent = new Intent();
                    intent.putExtra(IMAGE_RETURN_PATH, avatarFile.getPath());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    //Show picture starts cropping
                    weChatCrop.setImageBitmap(bitmap);
                    weChatCrop.setClipType(clipType);
                    weChatCrop.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

    }

    /**
     * Save the image selected from the album to the cache directory
     */
    private void saveAlbumImageToCache(String path) {
        File albumImageFile = new File(path);
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(albumImageFile));
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(avatarFile));
            int hasRead;
            byte[] bytes = new byte[1024];
            while ((hasRead = bufferedInputStream.read(bytes)) > 0) {
                bufferedOutputStream.write(bytes, 0, hasRead);
            }
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Solve the problem that some mobile phone rotating photo taken with the camera.
     * Such as SamSung mobile phone.
     */
    private Bitmap rotateBitmap(Bitmap bitmap, String path) {
        int degree = 0;
        try {
            //ExifInterface used to get some extra information for media pictures
            //如光圈,像素,等等,该字段包含Interface,但不是接口,是类,path代表路径
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
            }
            //After getting the angle,rotating picture.
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crop_menu, menu);
        return true;
    }

    /**
     * intent传递数据不能超过40kb;
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crop:
//                cropImageView.setImageBitmap(weChatCrop.getCropBitmap());
//                weChatCrop.setVisibility(View.GONE);

                //This code used when packaging,the above code is used to show the test.
                saveClipImage();
                Intent intent = new Intent();
                intent.putExtra(IMAGE_RETURN_PATH, avatarFile.getPath());
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存裁剪之后的图片,采用JPEG方式保存bitmap,透明的地方会被填充为黑色,png不会.
     * 但是png占用内存更大.
     */
    private void saveClipImage() {
        Bitmap bitmap = weChatCrop.getCropBitmap();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(avatarFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * If the djh does not press crop,directly pressed back.
     * To delete an avatar file,then finish.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (avatarFile.exists()) {
                    avatarFile.delete();
                }
                finish();
                break;
            default:
        }
        return super.onKeyDown(keyCode, event);
    }
}
