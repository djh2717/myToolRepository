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
     * Use to read content from internet.
     */
    public static byte[] readFromInternet(InputStream inputStream) {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        return read(bufferedInputStream);
    }

    /**
     * Use to read content from file.
     */
    public static byte[] readFile(String path) {
        File file = new File(path);
        try {
            return read(new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Use to save content to the path, if success return true.
     */
    public static boolean saveToFile(String path, byte[] content) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return save(new BufferedOutputStream(new FileOutputStream(file)), content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static byte[] read(BufferedInputStream bufferedInputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
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
                bufferedInputStream.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static boolean save(BufferedOutputStream bufferedOutputStream, byte[] content) {
        try {
            bufferedOutputStream.write(content);
            bufferedOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
