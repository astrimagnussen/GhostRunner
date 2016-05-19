package mycompany.ghostrunner;
// mycket kod från här: https://developer.android.com/training/location/retrieve-current.html
        import android.Manifest;

        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Color;
        import android.media.MediaPlayer;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.SystemClock;
        import android.os.Vibrator;
        import android.speech.tts.TextToSpeech;
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

        import com.google.android.gms.appindexing.Action;
        import com.google.android.gms.appindexing.AppIndex;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.location.LocationSettingsRequest;
        import com.google.android.gms.location.LocationListener;
        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;

        import java.io.BufferedReader;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.text.DateFormat;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.Locale;

public class GhostCompete extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, TextToSpeech.OnInitListener {

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

    //Shows the titles for the person
    private TextView distTextPersonTitle;
    private TextView timeTextPersonTitle;
    private TextView paceTextPersonTitle;

    //Shows the distance and the saved values for ghost
    private TextView nameTextGhost;
    private TextView distTextGhost;
    private TextView timeTextGhost;
    private TextView paceTextGhost;

    //The audio for save
    private MediaPlayer save;
    private MediaPlayer ghostBusters;
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
    private Button updateBtn;

    private int avgPaceSec;
    private int avgPaceMin;

    private Run ghost;
    public Boolean secondTry = false;
    private Boolean statusIsGreen;

    private Vibrator vib;
    private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;


    //All the timestuff!
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
                timeTextPerson.setText(String.format("%d:%02d:%02d", hour, minutes, seconds));
            }
            else {
                timeTextPerson.setText(String.format("%d:%02d", minutes, seconds));
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
        setContentView(R.layout.activity_ghost_compete);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        calculateRun = false;

        //Get the ghosts stuff
        Intent intent = getIntent();
        ghost = (Run) intent.getSerializableExtra("Run");

        //Creates the mediaPlayer
        save = MediaPlayer.create(getApplicationContext(), R.raw.saved);
        ghostBusters = MediaPlayer.create(getApplicationContext(), R.raw.ghostBustersSound);

        //Find Buttons from id
        saveBtn = (Button) findViewById(R.id.saveRunGhostCompeteBtn);
        stopBtn = (Button) findViewById(R.id.stopGhostCompeteBtn);
        startBtn = (Button) findViewById(R.id.startGhostCompeteBtn);
        menuBtn = (Button) findViewById(R.id.menuGhostCompeteBtn);
        deleteBtn = (Button) findViewById(R.id.deleteGhostCompeteBtn);
        pauseBtn = (Button) findViewById(R.id.pauseBtn);
        continueBtn = (Button) findViewById(R.id.continueBtn);
        updateBtn = (Button) findViewById(R.id.updateGhostCompeteBtn);


        //Finds TextViews the objects by Id for person
        timeTextPerson = (TextView) findViewById(R.id.showTimePerson);
        distTextPerson = (TextView) findViewById(R.id.showDistancePerson);
        paceTextPerson = (TextView) findViewById(R.id.showSpeedPerson);

        //Finds TextViews for the titles
        distTextPersonTitle = (TextView) findViewById(R.id.distanceTitlePerson);
        timeTextPersonTitle = (TextView) findViewById(R.id.timeTitlePerson);
        paceTextPersonTitle = (TextView) findViewById(R.id.speedTitlePerson);


        //Finds TextViews the objects by Id for ghost
        nameTextGhost = (TextView) findViewById(R.id.nameTextGhost);
        timeTextGhost = (TextView) findViewById(R.id.showTimeGhost);
        distTextGhost = (TextView) findViewById(R.id.showDistanceGhost);
        paceTextGhost = (TextView) findViewById(R.id.showSpeedGhost);

        vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        showGhost(ghost);
        statusIsGreen = true;
        setPersonFasterThanGhost(true);

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
        updateBtn.setVisibility(View.GONE);

        nameTextGhost.setVisibility(View.VISIBLE);
        nameTextGhost.setText(ghost.getName());
        //Gets the locationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

    }

    public void showGhost(Run ghost) {
        float hour = ghost.getHours();
        if(hour != 0) {
            timeTextGhost.setText(String.format("%d:%02d:%02d", hour, (int)ghost.getMinutes(), (int)ghost.getSeconds()));
        }else{
            timeTextGhost.setText(String.format("%d:%02d", (int)ghost.getMinutes(), (int)ghost.getSeconds()));
        }
        float km = ghost.getDistance()/1000;
        String dist =  Float.toString(km) + "0000";
        distTextGhost.setText(dist.substring(0, 4));

        float avgPaceSec;
        float avgPaceMin;
        float totSeconds = ghost.getSeconds() + 60 * (ghost.getMinutes() + (ghost.getHours() * 60));
        if(ghost.getDistance() != 0) {
            avgPaceSec = totSeconds/km;
            avgPaceMin = avgPaceSec/60;
            avgPaceSec = avgPaceSec%60;
        }
        else {
            avgPaceMin = 0;
            avgPaceSec = 0;
        }
        paceTextGhost.setText(String.format("%d:%02d %s", (int)avgPaceMin, (int)avgPaceSec, " min/km"));
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
//        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    //When the location is changed
    public void onLocationChanged(Location location) {
        //updates the currentLocation
        float ghostPaceMin = 0;
        float ghostPaceSec = 0;
        mCurrentLocation = location;
        if(calculateRun) {
            calcDist();
            calcAvgPace();

            ghostPaceSec = ghost.getSeconds()/(ghost.getDistance()/1000);
            ghostPaceMin = ghostPaceSec/60;

            System.out.println("ghostpacemin = "+ ghostPaceMin);
            System.out.println("avgPaveMin = " + avgPaceMin);

            if(avgPaceMin < ghostPaceMin || avgPaceMin == ghostPaceMin && avgPaceSec < ghostPaceSec ){
                if(!statusIsGreen){
                    setPersonFasterThanGhost(true);
                    speakWords("You just passed your ghost, good job!");
                    ghostBusters.start();
                    statusIsGreen = true;
                }
            }else{
                if(statusIsGreen) {
                    setPersonFasterThanGhost(false);
                    speakWords("The ghost ran past you, keep running!");
                    statusIsGreen = false;

                }
            }
        }
    }

    public void setPersonFasterThanGhost(Boolean green){


        //sets the color
        Color color;
        if(green){
            timeTextPersonTitle.setTextColor(ContextCompat.getColor(this, R.color.colorButtonGreenLightBackground));
            timeTextPerson.setTextColor(ContextCompat.getColor(this, R.color.colorButtonGreenLightBackground));

            distTextPersonTitle.setTextColor(ContextCompat.getColor(this, R.color.colorButtonGreenLightBackground));
            distTextPerson.setTextColor(ContextCompat.getColor(this, R.color.colorButtonGreenLightBackground));

            paceTextPersonTitle.setTextColor(ContextCompat.getColor(this, R.color.colorButtonGreenLightBackground));
            paceTextPerson.setTextColor(ContextCompat.getColor(this, R.color.colorButtonGreenLightBackground));
        } else{
            timeTextPersonTitle.setTextColor(ContextCompat.getColor(this, R.color.colorButtonRedLightBackground));
            timeTextPerson.setTextColor(ContextCompat.getColor(this, R.color.colorButtonRedLightBackground));

            distTextPersonTitle.setTextColor(ContextCompat.getColor(this, R.color.colorButtonRedLightBackground));
            distTextPerson.setTextColor(ContextCompat.getColor(this, R.color.colorButtonRedLightBackground));

            paceTextPersonTitle.setTextColor(ContextCompat.getColor(this, R.color.colorButtonRedLightBackground));
            paceTextPerson.setTextColor(ContextCompat.getColor(this, R.color.colorButtonRedLightBackground));
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
        vibrateNow();
        speakWords("Run started!");

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
        speakWords("Run paused");

        handler.removeCallbacks(runnable);
        pausedTimeAt = SystemClock.elapsedRealtime();

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
        updateBtn.setText("Replace your ghost: " + ghost.getName());

        saveBtn.setVisibility(View.VISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);
        updateBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        calculateRun= false;
        speakWords("Run stopped");
        vibrateNow();

        //for time calculation stop
        handler.removeCallbacks(runnable);

        Toast.makeText(getApplicationContext(), "Run stopped", Toast.LENGTH_SHORT).show();
        //showTime.setText(Long.toString(stopTime));

        //turn off TextToSpeech
        if (!myTTS.isSpeaking()) myTTS.shutdown();
    }
    public void calcDist (){
        distance += mCurrentLocation.distanceTo(startLocation);
        int tenMeters = (distance/10)%100;
        int km = distance/1000;
        startLocation = mCurrentLocation;
        distTextPerson.setText(String.format("%d.%02d %s", km, tenMeters, " km"));
    }

    public void calcAvgPace () {
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
    public void saveRun(final View view){
        saveBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
        updateBtn.setVisibility(View.GONE);
        menuBtn.setVisibility(View.VISIBLE);

        //kod från https://stackoverflow.com/questions/10903754/input-text-dialog-android , taget 2016-05-06
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(secondTry){
            builder.setTitle("Sorry, your name was not unique, choose another name");
        }else{
            builder.setTitle("Well done! Please name this new run");
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
                    updateBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);
                    menuBtn.setVisibility(View.GONE);
                    secondTry = true;
                    saveAndContinue("Wrongnamen", false);
                }else {
                    secondTry= false;
                    myTTS.shutdown();
                    saveAndContinue(input.getText().toString(), false);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                saveBtn.setVisibility(View.VISIBLE);
                updateBtn.setVisibility(View.VISIBLE);
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
        }else {
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
        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(500);
        Toast.makeText(getApplicationContext(), "Run deleted", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Läser in från fil och visa stuff
    public void menu (View view){
        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(500);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Updates the ghost
    public void updateGhost(View view){

        saveBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
        updateBtn.setVisibility(View.GONE);
        menuBtn.setVisibility(View.VISIBLE);

        saveAndContinue(ghost.getName(), true);
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

    public void speakWords(String speech){
        if(Settings.sound) {
            myTTS.speak(speech, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public void giveFeedback(){
        vibrateNow();
        speakWords("Time " + timeTextPerson.getText() + "       Total distance  " + distTextPerson.getText() + "     Average Pace  " + paceTextPerson.getText());
    }
}
