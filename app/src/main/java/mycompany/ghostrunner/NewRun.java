package mycompany.ghostrunner;
// mycket kod från här: https://developer.android.com/training/location/retrieve-current.html
import android.Manifest;

import android.media.MediaPlayer;
import android.os.Bundle;
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




public class NewRun extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Used to access the API
    private GoogleApiClient mGoogleApiClient;

    private Location mCurrentLocation;

    //Shows the coordinates
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;

    //If we should request loctionUpdates
    private boolean mRequestingLocationUpdates = true;

    private LocationRequest mLocationRequest;
    private LocationManager locationManager;

    //Start and stop locations for distance calculations
    public Location startLocation;
    private Location stopLocation;
    private float distance;

    //Shows the distance and the saved values
    private TextView distText;
    private TextView showsaved;

    //The audio for save
    private MediaPlayer save;
    private boolean calculateRun;
    
    //for the time counting
    private long startTime;
    private long stopTime;
    private TextView showTime;

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
        mLatitudeTextView = (TextView) findViewById(R.id.TextView02);
        mLongitudeTextView = (TextView) findViewById(R.id.TextView04);
        showTime = (TextView) findViewById(R.id.showTime);
        showsaved = (TextView) findViewById(R.id.showsaved);
        distText = (TextView) findViewById(R.id.showdistance);

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

        //Displays the currentlocation
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
        }

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
        //displays the currentLocation
        mCurrentLocation = location;
        float lat = (float) (mCurrentLocation.getLatitude());
        float lng = (float) (mCurrentLocation.getLongitude());
        mLatitudeTextView.setText(String.valueOf(lat));
        mLongitudeTextView.setText(String.valueOf(lng));
        if(calculateRun) {
            calcDist();
        }
    }


    //@Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    //@Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    //Have to exist and do nothing...
    public void onConnectionSuspended( int i ){}

    //Creates a locationRequest
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        //Sets the intervall for updating
        mLocationRequest.setInterval(5000);

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
        calculateRun = true;
        startTime = System.currentTimeMillis();
        //Checks permissions
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //locationManager.removeUpdates(GPSListener.this);
            }
        }

        //Gets the last location
        startLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        //Displays the location
        /*if(startLocation != null) {
            showsaved.setText(String.valueOf(startLocation.getLatitude()) + " " + String.valueOf(startLocation.getLongitude()));

        }else{
            showsaved.setText("startLocation är null");
        }*/

    }

    public void stopRun(View view) {
        calculateRun= false;
        save.start();
        stopTime = System.currentTimeMillis() - startTime;
        stopTime = stopTime/1000;
        showTime.setText(Long.toString(stopTime));

        //Checks permission
       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Gets the last location
        stopLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        //Displays the distance
        if(stopLocation != null) {
            //Calculate the distance
            distance = stopLocation.distanceTo(startLocation);
            distText = (TextView) findViewById(R.id.showdistance);
            distText.setText(Float.toString(distance));
        }else{
            distText.setText("stoplocation är null");
        }*/

    }
    public void calcDist (){
       distance += mCurrentLocation.distanceTo(startLocation);
        startLocation = mCurrentLocation;
        distText = (TextView) findViewById(R.id.showdistance);
        distText.setText(Float.toString(distance));
    }


}
