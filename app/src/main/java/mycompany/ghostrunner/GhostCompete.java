package mycompany.ghostrunner;
// mycket kod från här: https://developer.android.com/training/location/retrieve-current.html
        import android.Manifest;

        import android.content.Intent;
        import android.media.MediaPlayer;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.SystemClock;
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

        import com.google.android.gms.appindexing.Action;
        import com.google.android.gms.appindexing.AppIndex;
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

public class GhostCompete extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

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

    //Shows the distance and the saved values for person
    private TextView distTextPerson;
    private TextView timeTextPerson;
    private TextView paceTextPerson;

    //Shows the distance and the saved values for ghost
    private TextView distTextGhost;
    private TextView timeTextGhost;
    private TextView paceTextGhost;

    //The audio for save
    private MediaPlayer save;
    private boolean calculateRun;

    //for the time counting
    private Integer milliSeconds;
    Integer hourToSave= 0;
    Integer minutesToSave = 0;
    Integer secToSave = 0;

    private String date;

    private Button saveBtn;
    private Button pauseBtn;
    private Button continueBtn;
    private Button stopBtn;
    private Button startBtn;
    private Button menuBtn;
    private Button deleteBtn;

    //All the timestuff!
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
                timeTextPerson.setText(String.format("%d:%02d:%02d", hour, minutes, seconds));
            }
            else {
                timeTextPerson.setText(String.format("%d:%02d", minutes, seconds));
            }

            handler.postDelayed(this, 500);
        }
    };


    @Override //Runs when the Activity starts
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost_compete);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        calculateRun = false;

        //Get the ghosts stuff
        Intent intent = getIntent();
        Run ghost = (Run) intent.getSerializableExtra("Run");

        //Creates the mediaPlayer
        save = MediaPlayer.create(getApplicationContext(), R.raw.saved);

        //Find Buttons from id
        saveBtn = (Button) findViewById(R.id.saveRunGhostCompeteBtn);
        stopBtn = (Button) findViewById(R.id.stopGhostCompeteBtn);
        startBtn = (Button) findViewById(R.id.startGhostCompeteBtn);
        menuBtn = (Button) findViewById(R.id.menuGhostCompeteBtn);
        deleteBtn = (Button) findViewById(R.id.deleteGhostCompeteBtn);
        pauseBtn = (Button) findViewById(R.id.pauseBtn);
        continueBtn = (Button) findViewById(R.id.continueBtn);

        //Finds TextViews the objects by Id for person
        timeTextPerson = (TextView) findViewById(R.id.showTimePerson);
        distTextPerson = (TextView) findViewById(R.id.showDistancePerson);
        paceTextPerson = (TextView) findViewById(R.id.showSpeedPerson);

        //Finds TextViews the objects by Id for ghost
        timeTextGhost = (TextView) findViewById(R.id.showTimeGhost);
        distTextGhost = (TextView) findViewById(R.id.showDistanceGhost);
        paceTextGhost = (TextView) findViewById(R.id.showSpeedGhost);

        showGhost(ghost);

        //Creates locationRequests
        createLocationRequest();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }

        //Sets visibility for buttons
        saveBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        startBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.GONE);
        menuBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);

        //Gets the locationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void showGhost(Run ghost) {
        int hour = ghost.getHours();
        if(hour != 0) {
            timeTextGhost.setText(String.format("%d:%02d:%02d", hour, ghost.getMinutes(), ghost.getSeconds()));
        }else{
            timeTextGhost.setText(String.format("%d:%02d", ghost.getMinutes(), ghost.getSeconds()));
        }
        distTextGhost.setText(Float.toString(ghost.getDistance()));

        int avgPaceSec;
        int avgPaceMin;
        int totSeconds = ghost.getSeconds() + 60 * (ghost.getMinutes() + (ghost.getHours() * 60));
        if(ghost.getDistance() != 0) {
            avgPaceSec = totSeconds/Float.floatToIntBits(ghost.getDistance());
            avgPaceMin = avgPaceSec/60;
            avgPaceSec = avgPaceSec%60;
        }
        else {
            avgPaceMin = 0;
            avgPaceSec = 0;
        }
        paceTextGhost.setText(String.format("%d:%02d %s", avgPaceMin, avgPaceSec, " min/km"));
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TOD choose an action type.
                "GhostCompete Page", // TOD Define a title for the content shown.
                // TOD: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TOD: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://mycompany.ghostrunner/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TOD: choose an action type.
                "GhostCompete Page", // TOD: Define a title for the content shown.
                // TOD: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TOD: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://mycompany.ghostrunner/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
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

        Toast.makeText(getApplicationContext(), "Run paused", Toast.LENGTH_SHORT).show();
    }

    public void continueRun(View view) {
        continueBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
        calculateRun = true;

        totalPauseTime += SystemClock.elapsedRealtime() - pausedTimeAt;
        pausedTimeAt = 0;
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
        calculateRun= false;

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
        distTextPerson.setText(String.format("%d.%02d %s", km, tenMeters, " km"));
    }
    public void calcAvgPace (){
        int avgPaceSec;
        int avgPaceMin;
        if(distance!=0){
        avgPaceSec = milliSeconds/distance;
        avgPaceMin = avgPaceSec/60;
        avgPaceSec = avgPaceSec%60;
        }
        else{
            avgPaceMin = 0;
            avgPaceSec = 0;
        }
        paceTextPerson.setText(String.format("%d:%02d %s", avgPaceMin, avgPaceSec, " min/km"));

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

        date = getDateTime();

        String file_name = "runs";
        try {
            //Skickas: hour, new line, min, new line, sec, new line, distans, new line, date, new line
            FileOutputStream fileOutputStream = openFileOutput(file_name, MODE_APPEND);

            fileOutputStream.write(Integer.toString(hourToSave).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.write(Integer.toString(minutesToSave).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.write(Integer.toString(secToSave).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.write(Integer.toString(distance).getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.write((date).getBytes());
            fileOutputStream.write("\n".getBytes());

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
