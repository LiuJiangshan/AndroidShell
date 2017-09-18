package ljs.androidshell;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

import ljs.shell.Command;
import ljs.shell.Shell;

public class ShellExcuteTestActivity extends AppCompatActivity implements View.OnClickListener
{
    EditText textInput;
    Button excuteButton;
    TextView textShow;
    Shell shell;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shellexcutetest);

        textInput = (EditText) findViewById(R.id.textInput);
        excuteButton = (Button) findViewById(R.id.excuteButton);
        textShow = (TextView) findViewById(R.id.textShow);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        excuteButton.setOnClickListener(this);

        try
        {
            shell = Shell.getAndroidShell(false);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v)
    {
        String inputStr = textInput.getText().toString();
        Command command = new Command(inputStr)
        {
            @Override
            public void commandOutput(String line)
            {
                appendLine(line);
            }
        };
        try
        {
            shell.addCommand(command,false);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void appendLine(final String line)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                textShow.append(line + "\n");
                scrollView.scrollTo(0, scrollView.getMaxScrollAmount());
            }
        });
    }
}
