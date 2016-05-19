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
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class NewRun extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback, TextToSpeech.OnInitListener {
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

    //The audio for save
    private MediaPlayer save;
    private boolean calculateRun;

    //for the time counting
    private Integer milliSeconds;
    Integer hourToSave = 0;
    Integer minutesToSave = 0;
    Integer secToSave = 0;

    private TextToSpeech myTTS;

    private String date;
    private String m_Text = "";

    private Button saveBtn;
    private Button stopBtn;
    private Button startBtn;
    private Button pauseBtn;
    private Button continueBtn;
    private Button menuBtn;
    private Button deleteBtn;

    private int MY_DATA_CHECK_CODE = 0;

    public Boolean secondTry = false;

    private Vibrator vib;

    //for time calc from http://stackoverflow.com/questions/4597690/android-timer-how
    private long startTime = 0;
    private long pausedTimeAt = 0;
    private long totalPauseTime = 0;
    private int feedbackInterval = Settings.feedback; //1 minute between feedbacks (audio)
    private int nextFeedback = feedbackInterval;
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

            if(minutes>=nextFeedback){
                giveFeedback();
                nextFeedback+=feedbackInterval;
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
     //   mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

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

        vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        //Creates locationRequests
        createLocationRequest();

        //Gets the locationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //Text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
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
        System.out.print("connection failed!");
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
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    //When the location is changed
    public void onLocationChanged(Location location) {
        //updates the currentLocation
        LatLng latLngBefore = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        mCurrentLocation = location;
        LatLng latLngAfter = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
          if(calculateRun) {
              PolylineOptions polylineOptions = new PolylineOptions().add(latLngBefore).add(latLngAfter).width(5).color(Color.GREEN).geodesic(true);
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

    //Check if the user has TTS, otherwise tell them to install it on the phone
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == MY_DATA_CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                myTTS = new TextToSpeech(this, this);
            }else{
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void onInit(int initStatus){
        if(initStatus == TextToSpeech.SUCCESS){
            myTTS.setLanguage(Locale.US);
        }
        else if(initStatus == TextToSpeech.ERROR){
            Toast.makeText(this, "TTS not working", Toast.LENGTH_LONG).show();
        }
    }

    public void startRun(View view) {
        stopBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.GONE);
        calculateRun = true;
        speakWords("Run started!");
        vibrateNow();

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
        speakWords("Run paused");

        Toast.makeText(getApplicationContext(), "Run paused", Toast.LENGTH_SHORT).show();
    }

    public void continueRun(View view) {
        continueBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
        calculateRun = true;
        speakWords("Run continued");

        totalPauseTime += SystemClock.elapsedRealtime() - pausedTimeAt;
        pausedTimeAt = 0;
        handler.postDelayed(runnable, 0);

        Toast.makeText(getApplicationContext(), "Run continued", Toast.LENGTH_SHORT).show();
    }

    // spara saker globalt
    public void stopRun(View view) {
        speakWords("Run stopped");
        saveBtn.setVisibility(View.VISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        calculateRun = false;
        vibrateNow();
        //for time calculation stop
        handler.removeCallbacks(runnable);

        //turn off TextToSpeech
        if (!myTTS.isSpeaking()) myTTS.shutdown();

        Toast.makeText(getApplicationContext(), "Run stopped", Toast.LENGTH_SHORT).show();
        //showTime.setText(Long.toString(stopTime));
    }

    public void calcDist () {
        distance += mCurrentLocation.distanceTo(startLocation);
        int tenMeters = (distance/10)%100;
        int km = distance / 1000;
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
    public void saveRun(final View view){
        saveBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
        menuBtn.setVisibility(View.VISIBLE);

        //kod från https://stackoverflow.com/questions/10903754/input-text-dialog-android , taget 2016-05-06
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (secondTry) {
            builder.setTitle("Sorry, your name was not unique, choose another name");
        } else {
            builder.setTitle("Well done! Please name this new run:");
        }

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
                if(checkIfExists(input.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Name not unique", Toast.LENGTH_LONG).show();
                    saveBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);
                    menuBtn.setVisibility(View.GONE);
                    secondTry = true;
                    saveAndContinue("Wrongnamen", false);
                } else if (input.getText().toString() == "") {

                }
                else {
                    myTTS.shutdown();
                    secondTry = false;
                    saveAndContinue(input.getText().toString(), false);
                }
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

    public void saveAndContinue(String runName, Boolean update) {
        date = getDateTime();
        if(secondTry){
            saveBtn.performClick();
        } else {
            System.out.println("Given runName = " + runName);

            String file_name = "runs";

            try {
                //Skriver till namnet på rundan i runs filen
                FileOutputStream fileOutputStream;
                if (!update) {
                    fileOutputStream = openFileOutput(file_name, MODE_APPEND);
                    fileOutputStream.write(runName.getBytes());
                    fileOutputStream.write("\n".getBytes());
                    fileOutputStream.close();
                }

                System.out.print("input when saved: ");
                System.out.println(runName);

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
                if (update) {
                    Toast.makeText(getApplicationContext(), "Ghost Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Ghost Saved", Toast.LENGTH_SHORT).show();
                    save.start();
                }

                Intent intent = new Intent(this, ListRun.class);
                startActivity(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void afterDelete(View view){

        Toast.makeText(getApplicationContext(), "Run deleted", Toast.LENGTH_SHORT).show();
        //turn off TextToSpeech
        myTTS.shutdown();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Läser in från fil och visa stuff
    public void menu(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void giveFeedback(){
        vibrateNow();
        speakWords("Time " + timeText.getText() + " Total distance " + distText.getText() + " Average Pace " + paceText.getText());
    }

    public void speakWords(String speech){
        if(Settings.sound) {
            myTTS.speak(speech, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public Boolean checkIfExists(String name){
        try {
            String input;
            FileInputStream fileInputStream = openFileInput("runs");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            //Reads all the names of files to read
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((input = bufferedReader.readLine()) != null) {
                if(name.toLowerCase().equals(input.toLowerCase())){
                    return true;
                }
            }
            fileInputStream.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public void vibrateNow(){
        if(Settings.vibration){
            vib.vibrate(200);
        }
    }

}