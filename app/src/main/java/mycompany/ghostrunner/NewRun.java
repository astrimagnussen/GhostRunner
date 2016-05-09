package mycompany.ghostrunner;
// mycket kod från här: https://developer.android.com/training/location/retrieve-current.html
import android.Manifest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;

import android.content.Context;
import android.content.pm.PackageManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewRun extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Used to access the API
    private GoogleApiClient mGoogleApiClient;

    private Location mCurrentLocation;

    //If we should request loctionUpdates
    private boolean mRequestingLocationUpdates = true;

    private LocationRequest mLocationRequest;
    private LocationManager locationManager;

    //Start and stop locations for distance calculations
    public Location startLocation;
    private int distance;

    //Shows the distance and the saved values
    private TextView distText;
    private TextView timeText;
    private TextView paceText;

   // private TextView nameOfRun;

    //The audio for save
    private MediaPlayer save;
    private boolean calculateRun;
    
    //for the time counting
   private Integer milliSeconds;
    Integer hourToSave= 0;
    Integer minutesToSave = 0;
    Integer secToSave = 0;

    private String date;
    private String m_Text = "";

    private Button saveBtn;
    private Button stopBtn;
    private Button startBtn;
    private Button pauseBtn;
    private Button continueBtn;
    private Button menuBtn;
    private Button deleteBtn;

    //for time calc from http://stackoverflow.com/questions/4597690/android-timer-how
    private long startTime = 0;
    private long pausedTimeAt = 0;
    private long totalPauseTime = 0;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long millis = SystemClock.elapsedRealtime() - startTime - totalPauseTime;
            milliSeconds =  (int) millis;
            int seconds = (int) (millis/1000);
            int minutes = seconds/60;
            seconds = seconds%60;
            int hour = minutes/60;

            hourToSave = hour;
            minutesToSave = minutes;
            secToSave = seconds;

            if (hour>0){
                timeText.setText(String.format("%d:%02d:%02d", hour, minutes, seconds));
            }
            else {
                timeText.setText(String.format("%d:%02d", minutes, seconds));
            }

            handler.postDelayed(this, 500);
        }
    };

    @Override //Runs when the Activity starts
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_run);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        calculateRun = false;

        //Creates the mediaPlayer
        save = MediaPlayer.create(getApplicationContext(), R.raw.saved);

        //Finds TextViews the objects by Id
        timeText = (TextView) findViewById(R.id.showTime);
        distText = (TextView) findViewById(R.id.showDistance);
        paceText = (TextView) findViewById(R.id.showSpeed);

        //Find namefield for the run
        //nameOfRun = (TextView) findViewById(R.id.nameOfRun);

        //Find Buttons from id
        saveBtn = (Button) findViewById(R.id.saveRunGhostCompeteBtn);
        stopBtn = (Button) findViewById(R.id.stopGhostCompeteBtn);
        startBtn = (Button) findViewById(R.id.startGhostCompeteBtn);
        menuBtn = (Button) findViewById(R.id.menuGhostCompeteBtn);
        deleteBtn = (Button) findViewById(R.id.deleteGhostCompeteBtn);
        pauseBtn = (Button) findViewById(R.id.pauseBtn);
        continueBtn = (Button) findViewById(R.id.continueBtn);



        //Sets visibility for buttons
        saveBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.GONE);
        startBtn.setVisibility(View.VISIBLE);
        menuBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);

        //nameOfRun.setVisibility(View.GONE);

        //Creates locationRequests
        createLocationRequest();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Gets the locationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    //Runs when GoogleApiClient connects
    public void onConnected(Bundle connectionHint) {
        //checks the locationManager
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //locationManager.removeUpdates(GPSListener.this);
            }
        }
        //Gets the currentlocation
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        //Starts the locationUpdates
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    // @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    //When the location is changed
    public void onLocationChanged(Location location) {
        //updates the currentLocation
        mCurrentLocation = location;
        if(calculateRun) {
            calcDist();
            calcAvgPace();
        }
    }

    //Have to exist and do nothing...
    public void onConnectionSuspended( int i ){}

    //Creates a locationRequest
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        //Sets the intervall for updating
        mLocationRequest.setInterval(2000);

        //Sets a max limit of how fast it can be updated without overflow of data
        mLocationRequest.setFastestInterval(1000);

        //Sets priority on accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

    }

    protected void startLocationUpdates() {

        //Checks permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Requests for updates
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }

    public void startRun(View view) {
        stopBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.GONE);
        calculateRun = true;

        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(200);

        //Checks permissions
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        }

        //Gets the last location
        startLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        //for time calculation start
        startTime = SystemClock.elapsedRealtime();
        handler.postDelayed(runnable, 0);
    }

    public void pauseRun(View view) {
        pauseBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.VISIBLE);
        calculateRun = false;

        handler.removeCallbacks(runnable);
        pausedTimeAt = SystemClock.elapsedRealtime();

        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(200);

        Toast.makeText(getApplicationContext(), "Run paused", Toast.LENGTH_SHORT).show();
    }

    public void continueRun(View view) {
        continueBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
        calculateRun = true;

        totalPauseTime += SystemClock.elapsedRealtime() - pausedTimeAt;
        pausedTimeAt = 0;
        handler.postDelayed(runnable, 0);

        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(200);

        Toast.makeText(getApplicationContext(), "Run continued", Toast.LENGTH_SHORT).show();
    }

    // spara saker globalt
    public void stopRun(View view) {
        saveBtn.setVisibility(View.VISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        calculateRun = false;
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(200);


        //for time calculation stop
        handler.removeCallbacks(runnable);

        Toast.makeText(getApplicationContext(), "Run stopped", Toast.LENGTH_SHORT).show();
        //showTime.setText(Long.toString(stopTime));
    }
    public void calcDist () {
        distance += mCurrentLocation.distanceTo(startLocation);
        int tenMeters = (distance/10)%100;
        int km = distance/1000;
        startLocation = mCurrentLocation;
        distText.setText(String.format("%d.%02d %s", km, tenMeters, " km"));
    }
    public void calcAvgPace () {
        int avgPaceSec;
        int avgPaceMin;
        if(distance != 0){
            avgPaceSec = milliSeconds/distance;
            avgPaceMin = avgPaceSec/60;
            avgPaceSec = avgPaceSec%60;
        }
        else{
            avgPaceMin = 0;
            avgPaceSec = 0;
        }
        paceText.setText(String.format("%d:%02d %s", avgPaceMin, avgPaceSec, " min/km"));
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    //Spara till fil
    public void saveRun(View view){
        saveBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
        menuBtn.setVisibility(View.VISIBLE);
        //nameOfRun.setVisibility(View.VISIBLE);

        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(200);

        //kod från https://stackoverflow.com/questions/10903754/input-text-dialog-android , taget 2016-05-06
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Well done! Please name this run (leave blank for default)");

        // Set up the input
        final EditText input = new EditText(this);
        input.setTextColor(Color.BLACK);
        // Specify the type of input expected;
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Set name", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveAndContinue(input.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                saveBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);
                menuBtn.setVisibility(View.GONE);
            }
        });

        builder.show();
    }

    public void saveAndContinue(String runName) {
        date = getDateTime();

        /*System.out.println("hourToSave = " + hourToSave);
        System.out.println("minutesToSave = " + minutesToSave);
        System.out.println("secToSave = " + secToSave);
        System.out.println("distance = " + distance);
        System.out.println("date = " + date);*/

        //System.out.println("Given runName = " + runName);

        String file_name = "runs";
        try {
            //Skriver till namnet på rundan i runs filen
            FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_APPEND);
            System.out.print("input when saved: ");
            System.out.println(runName);

            fileOutputStream.write(runName.getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.close();

            FileOutputStream fileOutputStream2 = openFileOutput(runName, MODE_PRIVATE);

            //Skickas: hour, new line, min, new line, sec, new line, distans, new line, date, new line
            fileOutputStream2.write(Integer.toString(hourToSave).getBytes());
            fileOutputStream2.write("\n".getBytes());
            fileOutputStream2.write(Integer.toString(minutesToSave).getBytes());
            fileOutputStream2.write("\n".getBytes());
            fileOutputStream2.write(Integer.toString(secToSave).getBytes());
            fileOutputStream2.write("\n".getBytes());
            fileOutputStream2.write(Integer.toString(distance).getBytes());
            fileOutputStream2.write("\n".getBytes());
            fileOutputStream2.write((date).getBytes());
            fileOutputStream2.write("\n".getBytes());

            fileOutputStream2.close();
            Toast.makeText(getApplicationContext(), "Run saved", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        save.start();

        Intent intent = new Intent(this, ListRun.class);
        startActivity(intent);
    }

    public void afterDelete(View view){
        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(200);
        Toast.makeText(getApplicationContext(), "Run deleted", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Läser in från fil och visa stuff
    public void menu(View view){
        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(200);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
    }
}
