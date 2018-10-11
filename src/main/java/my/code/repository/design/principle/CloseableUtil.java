package my.code.repository.design.principle;

import java.io.Closeable;
import java.io.IOException;

/**
 * A closeable util, use to close the closeable resource, main is io resource.
 * NOTICE : This is typical of interface segregation principle.
 *
 * @author djh on  2018/9/9 18:41
 * @E-Mail 1544579459@qq.com
 */
public class CloseableUtil {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
