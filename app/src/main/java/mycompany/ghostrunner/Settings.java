package mycompany.ghostrunner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Settings extends AppCompatActivity {

    public static boolean vibration = true;
    public static boolean sound = true;
    public static int feedback = 1;

    //http://www.mysamplecode.com/2013/04/android-switch-button-example.html
    private Switch vibText;
    private Switch soundText;

    ////http://javatechig.com/android/android-radio-button-example, http://www.tutorialspoint.com/android/android_radiogroup_control.htm
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        vibText = (Switch) findViewById(R.id.vibration);
        soundText = (Switch) findViewById(R.id.sound);
        radioGroup=(RadioGroup)findViewById(R.id.radioGroup);


        if(vibration){
            vibText.setChecked(true);
        }
        if(sound){
            soundText.setChecked(true);
        }
       switch(feedback){
           case 0 :
               RadioButton rb0 = (RadioButton) radioGroup.findViewById(R.id.radioButton1);
               rb0.setChecked(true);
               break;
           case 1 :
               RadioButton rb1 = (RadioButton) radioGroup.findViewById(R.id.radioButton2);
               rb1.setChecked(true);
               break;
           case 2 :
               RadioButton rb2 = (RadioButton) radioGroup.findViewById(R.id.radioButton3);
               rb2.setChecked(true);
               break;
           case 3 :
               RadioButton rb3 = (RadioButton) radioGroup.findViewById(R.id.radioButton4);
               rb3.setChecked(true);
               break;
           case 4 :
               RadioButton rb4 = (RadioButton) radioGroup.findViewById(R.id.radioButton5);
               rb4.setChecked(true);
               break;
           case 5 :
               RadioButton rb5 = (RadioButton) radioGroup.findViewById(R.id.radioButton6);
               rb5.setChecked(true);
               break;
           case 10 :
               RadioButton rb6 = (RadioButton) radioGroup.findViewById(R.id.radioButton7);
               rb6.setChecked(true);
               break;
           case 15 :
               RadioButton rb7 = (RadioButton) radioGroup.findViewById(R.id.radioButton8);
               rb7.setChecked(true);
               break;
           case 20 :
               RadioButton rb8 = (RadioButton) radioGroup.findViewById(R.id.radioButton9);
               rb8.setChecked(true);
               break;
           case 30 :
               RadioButton rb9 = (RadioButton) radioGroup.findViewById(R.id.radioButton10);
               rb9.setChecked(true);
               break;
       }

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
                if (isChecked) {
                    sound = true;
                } else {
                    sound = false;
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if(null!=rb && checkedId > -1){
                    switch (checkedId){
                        case 2131558586 : feedback = 0; break;
                        case 2131558587 : feedback = 1; break;
                        case 2131558588 : feedback = 2; break;
                        case 2131558589 : feedback = 3; break;
                        case 2131558590 : feedback = 4; break;
                        case 2131558591 : feedback = 5; break;
                        case 2131558592 : feedback = 10; break;
                        case 2131558593 : feedback = 15; break;
                        case 2131558594 : feedback = 20; break;
                        case 2131558595 : feedback = 30; break;
                    }
                }
            }
        });
    }

}
