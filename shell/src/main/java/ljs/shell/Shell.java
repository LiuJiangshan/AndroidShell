package ljs.shell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * android终端
 */
public class Shell
{
    //命令开始标记
    public final String START_MARK = "ljs_cmd_mark_start:" + hashCode();
    //命令结束标记
    public final String END_MARK = "ljs_cmd_mark_end:" + hashCode();

    //是否已经关闭
    private boolean closed = false;
    //是否正在运行
    private boolean running = false;
    //是否正在读取
    private boolean reading = false;

    public Runtime runtime;
    public Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private BufferedReader error;
    Command command;
    Runnable readThread = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    if (command == null || command.finish)
                        synchronized (Shell.this)
                        {
                            Shell.this.wait();
                        }
                    if (closed)
                        break;
                    String line = reader.readLine();
                    if (line == null)
                    {
                        reading = false;
                        running = false;
                        command.running = false;
                        synchronized (Shell.this)
                        {
                            Shell.this.wait();
                        }
                        break;
                    } else if (START_MARK.contains(line))
                        reading = true;
                    else if (END_MARK.contains(line))
                    {
                        running = false;
                        reading = false;
                        command.running = false;
                        command.finish = true;
                        command.commandFinish();
                    } else
                        command.commandOutput(line);
                }
            } catch (Exception e)
            {
            } finally
            {
                close();
            }
        }
    };
    Runnable errorThread = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                String line = null;
                synchronized (Shell.this)
                {
                    Shell.this.wait();
                }
                while ((line = error.readLine()) != null)
                {
                    if (command != null && !command.interrupted)
                    {
                        command.interrupted = true;
                        command.commandInterrupted(line);
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            } finally
            {

            }
        }
    };
    Runnable writeThread = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    if (command == null)
                        synchronized (Shell.this)
                        {
                            Shell.this.wait();
                        }
                    if (closed)
                        break;
                    else if (command == null || command.running)
                        synchronized (Shell.this)
                        {
                            try
                            {
                                Shell.this.wait();
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    else
                    {
                        running = true;
                        command.running = true;
                        writer.write("echo " + START_MARK + "\n");
                        writer.write(command.getCmd() + "\n");
                        writer.write("echo " + END_MARK + "\n");
                        writer.flush();
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                close();
            }
        }
    };

    private Shell(String initCommand) throws IOException
    {
        runtime = Runtime.getRuntime();
        process = runtime.exec(initCommand);
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
        error = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
        new Thread(readThread, "ljs_shell_read_thread").start();
        new Thread(writeThread, "ljs_shell_write_thread").start();
        new Thread(errorThread, "ljs_shell_error_read_thread").start();
    }

    /**
     * 获取Android终端命令行
     *
     * @param isRoot 是否获取一个拥有root的终端
     */
    public static Shell getAndroidShell(boolean isRoot) throws IOException
    {
        return new Shell(isRoot ? "su\n" : "sh\n");
    }

    /**
     * 获取linux终端命令行
     */
    public static Shell getLinuxShell() throws IOException
    {
        return new Shell("sh\n");
    }

    /**
     * 关闭Shell
     */
    public void close()
    {
        closed = true;
        running = false;
        reading = false;
        Util.close(writer);
        Util.close(reader);
        process.destroy();
    }

    /**
     * 执行命令，不会堵塞当前线程
     *
     * @param command 需要执行的命令
     */
    public synchronized void addCommand(Command command) throws Exception
    {
        addCommand(command, false);
    }

    /**
     * 执行命令
     *
     * @param command 需要执行的命令
     * @param join    是否让当前线程等待命令结束
     */
    public void addCommand(Command command, boolean join) throws Exception
    {
        if (closed)
            throw new Exception("该终端已被关闭");
        this.command = command;
        synchronized (Shell.this)
        {
            Shell.this.notifyAll();
        }
        if (join)
            while (command.isRunning())
                Util.sleep(100);
    }
}
