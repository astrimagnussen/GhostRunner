package mycompany.ghostrunner;
// mycket kod från här: https://developer.android.com/training/location/retrieve-current.html
import android.Manifest;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.content.Context;
import android.content.pm.PackageManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.widget.Button;
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
    private TextView speedText;
   // private TextView showsaved;

    //The audio for save
    private MediaPlayer save;
    private boolean calculateRun;
    
    //for the time counting
    Integer hourToSave= 0;
    Integer minutesToSave = 0;
    Integer secToSave = 0;

    private String date;

    private Button saveBtn;
    private Button stopBtn;
    private Button startBtn;
    private Button menuBtn;

    //for time calc from http://stackoverflow.com/questions/4597690/android-timer-how
    private TextView timerTextView;
    private long startTime = 0;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis/1000);
            int minutes = seconds/60;
            seconds = seconds%60;
            int hour = minutes/60;

            hourToSave = hour;
            minutesToSave = minutes;
            secToSave = seconds;

            if (hour>0){
                timerTextView.setText(String.format("%d:%02d:%03d", hour, minutes, seconds));
            }
            else {
                timerTextView.setText(String.format("%d:%02d", minutes, seconds));
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
        getSupportActionBar().setTitle("New Run");
        calculateRun = false;

        //Creates the mediaPlayer
        save = MediaPlayer.create(getApplicationContext(), R.raw.saved);

        //Finds all the objects by Id
        distText = (TextView) findViewById(R.id.showDistance);
        timerTextView = (TextView) findViewById(R.id.showTime);


        //Finds TextViews the objects by Id
        timeText = (TextView) findViewById(R.id.showTime);
        distText = (TextView) findViewById(R.id.showDistance);
        speedText = (TextView) findViewById(R.id.showSpeed);

        //Find Buttons from id
        saveBtn = (Button) findViewById(R.id.saveRun);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        startBtn = (Button) findViewById(R.id.startBtn);
        menuBtn = (Button) findViewById(R.id.goHome);

        //Sets visibility for buttons
        saveBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        startBtn.setVisibility(View.VISIBLE);
        menuBtn.setVisibility(View.GONE);

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
        startBtn.setVisibility(View.GONE);
        calculateRun = true;
        //Checks permissions
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        }

        //Gets the last location
        startLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        //for time calculation start
        startTime = System.currentTimeMillis();
        handler.postDelayed(runnable, 0);

    }

    // spara saker globalt
    public void stopRun(View view) {
        saveBtn.setVisibility(View.VISIBLE);
        menuBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.GONE);
        calculateRun= false;


        //for time calculation stop
        handler.removeCallbacks(runnable);




        Toast.makeText(getApplicationContext(), "Run stopped", Toast.LENGTH_SHORT).show();
        //showTime.setText(Long.toString(stopTime));
    }
    public void calcDist (){
        distance += mCurrentLocation.distanceTo(startLocation);
        startLocation = mCurrentLocation;
        distText = (TextView) findViewById(R.id.showDistance);
        distText.setText(Float.toString(distance));
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    //Spara till fil
    public void saveRun(View view){
        saveBtn.setVisibility(View.GONE);
        menuBtn.setVisibility(View.VISIBLE);

        date =  getDateTime();

        String file_name = "runs";
        try {
            //Skickas, distans, new line, hour, new line, min, new line, sec, new line, date
            FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_PRIVATE);

            fileOutputStream.write( Integer.toString(distance).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.write(Integer.toString(hourToSave).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.write(Integer.toString(minutesToSave).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.write(Integer.toString(secToSave).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.write((date).getBytes());

            fileOutputStream.close();
            Toast.makeText(getApplicationContext(), "Run saved", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        save.start();

    }

    //Läser in från fil och visa stuff
    public void goHome(View view){

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

    }

}
