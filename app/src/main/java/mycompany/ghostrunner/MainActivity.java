package mycompany.ghostrunner;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("GhostRunner");


    }


    //Goes from new run button view to (NewRun)
    public void newRun(View view){
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
        Intent intent = new Intent(this, NewRun.class);
        startActivity(intent);
    }

    //Goes from ghost run button view
    public void listRun(View view){
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
        Intent intent = new Intent(this, ListRun.class);
        startActivity(intent);
    }

    public void onConnected( Bundle bun ){

    }
    public void onConnectionSuspended( int i ){}
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
    }


}
