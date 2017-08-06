package ljs.shell;

import java.io.Closeable;
import java.io.IOException;

/**
 * 工具类
 */
public class Util
{
    public static void close(Closeable closeable)
    {
        if (closeable != null)
            try
            {
                closeable.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
    }

    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
