package my.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A io util, use to read or save content.
 *
 * @author djh on  2018/7/30 14:58
 * @E-Mail 1544579459@qq.com
 */
public class IoUtil {

    /**
     * Use to read content from file, the file default location at files dir.
     */
    public static byte[] readFile(String fileName) {
        try {
            return read(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Use to save content to the files dir, if success return true.
     */
    public static boolean saveToFile(String fileName, byte[] content) {
        return save(fileName, content);
    }

    private static byte[] read(String fileName) throws FileNotFoundException {

        File file = new File(MyApplication.getContext().getFilesDir(), fileName);

        InputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        int hasRead;
        byte[] bytes = new byte[1024];
        try {
            while ((hasRead = bufferedInputStream.read(bytes)) != -1) {
                byteArrayOutputStream.write(bytes, 0, hasRead);
            }
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                bufferedInputStream.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static boolean save(String fileName, byte[] content) {
        File file = new File(MyApplication.getContext().getFilesDir(), fileName);
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            bufferedOutputStream.write(content);
            bufferedOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
