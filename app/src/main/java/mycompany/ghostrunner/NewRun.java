package mycompany.ghostrunner;
// mycket kod från här: https://developer.android.com/training/location/retrieve-current.html

import android.Manifest;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
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

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewRun extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    //Used in the mapview
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mapLocation;

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
    // private TextView showsaved;

    //The audio for save
    private MediaPlayer save;
    private boolean calculateRun;

    //for the time counting
    private Integer milliSeconds;
    Integer hourToSave = 0;
    Integer minutesToSave = 0;
    Integer secToSave = 0;

    private String date;

    private Button saveBtn;
    private Button stopBtn;
    private Button startBtn;
    private Button pauseBtn;
    private Button continueBtn;
    private Button menuBtn;
    private Button deleteBtn;

    //for time calc from http://stackoverflow.com/questions/4597690/android-timer-how
    private long startTime = 0;
    private long pauseTime = 0;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long millis;
            if (pauseTime != 0) {
                millis = System.nanoTime() - (pauseTime + startTime);
            } else {
                millis = System.nanoTime() - startTime;
            }
            milliSeconds = (int) millis;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int hour = minutes / 60;

            hourToSave = hour;
            minutesToSave = minutes;
            secToSave = seconds;

            if (hour > 0) {
                timeText.setText(String.format("%d:%02d:%03d", hour, minutes, seconds));
            } else {
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

        //for the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //Finds TextViews the objects by Id
        timeText = (TextView) findViewById(R.id.showTime);
        distText = (TextView) findViewById(R.id.showDistance);
        paceText = (TextView) findViewById(R.id.showSpeed);

        //Find Buttons from id
        saveBtn = (Button) findViewById(R.id.saveRunBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        startBtn = (Button) findViewById(R.id.startBtn);
        pauseBtn = (Button) findViewById(R.id.pauseBtn);
        continueBtn = (Button) findViewById(R.id.continueBtn);
        menuBtn = (Button) findViewById(R.id.menuBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);

        //Sets visibility for buttons
        saveBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.GONE);
        startBtn.setVisibility(View.VISIBLE);
        menuBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);


        //Creates locationRequests
        createLocationRequest();


    }

    //Runs when GoogleApiClient connects
    @Override
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //for the map
        mMap = googleMap;
        //Gets the locationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mapLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng latLng = new LatLng(mapLocation.getLatitude(), mapLocation.getLongitude());
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
    }
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
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
//        stopLocationUpdates();
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
        //Checks permissions
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        }

        //Gets the last location
        startLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        //for time calculation start
        startTime = System.nanoTime();
        handler.postDelayed(runnable, 0);
    }

    public void pauseRun(View view) {
        pauseBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.VISIBLE);
        calculateRun = false;

        handler.removeCallbacks(runnable);
        pauseTime = System.nanoTime();

        Toast.makeText(getApplicationContext(), "Run paused", Toast.LENGTH_SHORT).show();
    }

    public void continueRun(View view) {
        continueBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
        calculateRun = true;

        pauseTime = System.nanoTime() - pauseTime;
        handler.postDelayed(runnable, 0);

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

        //for time calculation stop
        handler.removeCallbacks(runnable);

        Toast.makeText(getApplicationContext(), "Run stopped", Toast.LENGTH_SHORT).show();
        //showTime.setText(Long.toString(stopTime));
    }
    public void calcDist (){
        distance += mCurrentLocation.distanceTo(startLocation);
        int tenMeters = (distance/10)%100;
        int km = distance/1000;
        startLocation = mCurrentLocation;
        distText.setText(String.format("%d.%02d %s", km, tenMeters, " km"));
    }
    public void calcAvgPace (){
        int avgPaceSec = milliSeconds/distance;
        int avgPaceMin = avgPaceSec/60;
        avgPaceSec = avgPaceSec%60;
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
            Toast.makeText(getApplicationContext(), "Run saved", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        save.start();
    }
    public void afterDelete(View view){
        Toast.makeText(getApplicationContext(), "Run deleted", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Läser in från fil och visa stuff
    public void menu(View view){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
    }

}
