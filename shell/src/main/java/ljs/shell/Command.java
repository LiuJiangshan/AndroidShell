package ljs.shell;

import java.util.logging.Handler;

/**
 * 命令
 */
public class Command
{
    //命令是否运行
    public boolean running = false;
    //命令是否中断
    public boolean interrupted = false;
    //命令是否完成
    public boolean finish = false;
    Handler handler;

    String cmd;

    public String getCmd()
    {
        return cmd;
    }

    public void setCmd(String cmd)
    {
        this.cmd = cmd;
    }

    public Command(String cmd)
    {
        this.cmd = cmd;
    }

    /**
     * 命令输出
     */
    public void commandOutput(String line)
    {
    }

    /**
     * 命令中断
     */
    public void commandInterrupted(String error)
    {
    }

    /**
     * 命令正常完成
     */
    public void commandFinish()
    {
    }

    /**
     * 中断命令
     */
    public void interrupted()
    {
        interrupted = true;
    }

    /**
     * 命令是否结束
     *
     * @return boolean
     */
    public boolean isStop()
    {
        return interrupted || finish;
    }

    /**
     * 命令是否正在执行
     *
     * @return boolean
     */
    public boolean isRunning()
    {
        return !isStop();
    }
}
