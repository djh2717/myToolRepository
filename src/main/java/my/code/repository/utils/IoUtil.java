package my.code.repository.utils;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import my.code.repository.design.mode.CloseableUtil;


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

    /**
     * Save objects to file, if is append, will auto remove the null object
     * at then end of file.
     */
    public static void saveObjects(String fileName, Collection<Object> objects, boolean append) {
        try {
            saveObjectToFile(fileName, objects, append);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read objects from file, may return null if a exception is occur.
     */
    public static List<Object> readObjects(String fileName) {
        try {
            return readObjectFromFile(fileName);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * If external store is available, return the path, otherwise return null.
     */
    public static String isExternalStoreAvailable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {

            return Objects.requireNonNull(MyApplication.getContext().getExternalCacheDir()).getPath();
        }
        return null;
    }

    /**
     * Use nio copy file is faster, this may replace the toFile if already exists.
     */
    public static void copyFile(File fromFile, File toFile) {
        if (!fromFile.exists()) {
            throw new RuntimeException("FromFile is not exists!");
        }
        copy(fromFile, toFile);
    }

    /**
     * Use to search file from directory, may return null if no file is found.
     */
    public static List<File> searchFile(String directory, String fileName) {
        File file = new File(directory);
        if (!file.isDirectory() || !file.exists()) {
            throw new RuntimeException("Directory is not exists or not is a directory!");
        }
        List<File> fileList;
        // If the api is large than 26, use nio walkFileTree to search file,
        // otherwise use the custom recursion.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Path path = file.toPath();
//            try {
//                fileList = walkFileTree(path, fileName);
//                // If not search any file, return null.
//                if (fileList.size() == 0) {
//                    return null;
//                } else {
//                    return fileList;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
        fileList = new ArrayList<>();
        // Recursive search.
        recursiveSearch(fileList, file, fileName);
        // If no file is found, return null.
        if (fileList.size() == 0) {
            return null;
        }
        return fileList;
//        }
//        return null;
    }


    // ------------------ Internal API ------------------


    private static byte[] read(String fileName) throws FileNotFoundException {

        File file = new File(MyApplication.getContext().getFilesDir(), fileName);
        if (!file.exists()) {
            throw new RuntimeException("File is not exists!");
        }
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
            CloseableUtil.close(inputStream);
            CloseableUtil.close(bufferedInputStream);
            CloseableUtil.close(byteArrayOutputStream);
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
            CloseableUtil.close(bufferedOutputStream);
        }
        return false;
    }

    /**
     * Save objects to file, support for appending.
     */
    private static void saveObjectToFile(String fileName, Collection<Object> objects, boolean append) throws IOException, ClassNotFoundException {
        File file = new File(MyApplication.getContext().getFilesDir(), fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        ObjectInputStream objectInputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            if (append) {
                // If file length large than zero, need remove the null Object
                // at the end of file.
                if (file.length() > 0) {
                    List<Object> objectList = new ArrayList<>();
                    // Remove the null object at then end of file.
                    objectInputStream = new ObjectInputStream(new FileInputStream(file));
                    Object object;
                    while ((object = objectInputStream.readObject()) != null) {
                        objectList.add(object);
                    }
                    // Then save all object to file.
                    objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
                    for (Object o : objectList) {
                        objectOutputStream.writeObject(o);
                    }
                    // Append the new object to the file.
                    for (Object o : objects) {
                        objectOutputStream.writeObject(o);
                    }
                } else {
                    objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
                    // If file is empty, direct save objects to file.
                    for (Object object : objects) {
                        objectOutputStream.writeObject(object);
                    }
                }
            } else {
                objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
                // Save the objects to file.
                for (Object object : objects) {
                    objectOutputStream.writeObject(object);
                }
            }
            // Append a null object at the end of file.
            objectOutputStream.writeObject(null);
            objectOutputStream.flush();
        } finally {
            CloseableUtil.close(objectOutputStream);
            CloseableUtil.close(objectInputStream);
        }
    }

    private static List<Object> readObjectFromFile(String fileName) throws IOException, ClassNotFoundException {
        File file = new File(MyApplication.getContext().getFilesDir(), fileName);
        if (!file.exists()) {
            throw new RuntimeException("File is not exists!");
        }
        ObjectInputStream objectInputStream = null;
        try {
            // Read object from file and put it to list, then return.
            List<Object> objectList = new ArrayList<>();
            objectInputStream = new ObjectInputStream(new FileInputStream(file));
            Object object;
            while ((object = objectInputStream.readObject()) != null) {
                objectList.add(object);
            }
            return objectList;
        } finally {
            CloseableUtil.close(objectInputStream);
        }
    }

    private static void copy(File fromFile, File toFile) {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = new FileInputStream(fromFile).getChannel();
            toChannel = new FileOutputStream(toFile).getChannel();
            // Use channel to copy file.
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseableUtil.close(toChannel);
            CloseableUtil.close(fromChannel);
        }
    }

//    At Android 6.0, do not have the SimpleFileVisitor file.
//    @TargetApi(Build.VERSION_CODES.O)
//    private static List<File> walkFileTree(Path rootPath, final String fileName) throws IOException {
//        // Use to store the search result.
//        final List<File> fileList = new ArrayList<>();
//
//        // This is use to visitor the file tree.
//        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
//            @Override
//            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
//                // If the file name is contains the aim fileName, add to list.
//                if (file.getFileName().toString().contains(fileName)) {
//                    fileList.add(file.toFile());
//                }
//                return FileVisitResult.CONTINUE;
//            }
//
//            @Override
//            public FileVisitResult visitFileFailed(Path file, IOException exc) {
//                // If the file is visit failed, do not deal with.
//                return FileVisitResult.CONTINUE;
//            }
//        });
//        return fileList;
//    }

    /**
     * Recursive search the file.
     */
    private static void recursiveSearch(List<File> fileList, File directory, String fileName) {
        // If the directory is empty, return.
        if (directory.isDirectory() && directory.length() == 0) {
            return;
        }
        // If is a file, and if file name contains the aim fileName, add to fileList.
        if (directory.isFile()) {
            if (directory.getName().contains(fileName)) {
                fileList.add(directory);
            }
            return;
        }
        // Otherwise, recursive traversal.
        for (File file : directory.listFiles()) {
            recursiveSearch(fileList, file, fileName);
        }

    }

}
