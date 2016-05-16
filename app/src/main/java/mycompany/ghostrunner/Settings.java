package mycompany.ghostrunner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    public static boolean vibration = true;
    public static boolean sound = true;
    public static int feedback = 1;

    //http://www.mysamplecode.com/2013/04/android-switch-button-example.html
    private Switch vibText;
    private Switch soundText;
    private Switch feedbackText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        vibText = (Switch) findViewById(R.id.vibration);
        soundText = (Switch) findViewById(R.id.sound);
        feedbackText = (Switch) findViewById(R.id.feedback);

        vibText.setChecked(true);
        soundText.setChecked(true);
        vibText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    vibration = true;
                } else {
                    vibration = false;
                }
            }
        });
        soundText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sound = true;
                }
                else{
                    sound = false;
                }
            }
        });


    }







}
