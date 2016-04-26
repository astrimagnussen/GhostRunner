package mycompany.ghostrunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class GhostRun extends AppCompatActivity {
    private TextView dateText;
    private TextView distText;
    private TextView timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost_run);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ghost Run");
        dateText = (TextView) findViewById(R.id.dateTextGhost);
        distText = (TextView) findViewById(R.id.distTextGhost);
        timeText = (TextView) findViewById(R.id.timeTextGhost);

        if(!read()){
            dateText.setText("Kaos");
        }

    }

    public boolean read() {
        try {
            String input;
            FileInputStream fileInputStream = openFileInput("runs");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);


            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //  StringBuffer stringBuffer = new StringBuffer();


            int counter = 0;
            int hour = 0;
            int min = 0;
            int sec = 0;

            while ((input = bufferedReader.readLine()) != null) {
                switch (counter) {
                    case 0:
                        dateText.setText(input);
                        break;
                    case 1:
                        distText.setText(input);
                        break;
                    case 2:
                        try {
                            hour = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        break;
                    case 3:
                        try {
                            min = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        break;
                    case 4:
                        try {
                            sec = Integer.parseInt(input);
                            if (hour > 0) {
                                timeText.setText(String.format("%d:%02d:%03d", hour, min, sec));
                            } else {
                                timeText.setText(String.format("%d:%02d", min, sec));
                            }
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        break;
                }
                counter ++;
            }
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
