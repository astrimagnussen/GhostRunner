package mycompany.ghostrunner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    private String provider;
    public Location startLocation;
    private Location stopLocation;
    private float distance;
    TextView textView;
    TextView distText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        textView = (TextView) findViewById(R.id.startDistance);

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void coordinate(View view){
        Intent intent =new Intent(this, DisplayCoordinates.class);
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

   public void saveLocation(View view ) {
       startLocation = DisplayCoordinates.mLastLocation;
       textView.setText(String.valueOf(startLocation.getLatitude()) + " " + String.valueOf(startLocation.getLongitude()));
//       textView.setText(Location.convert(startLocation.getLatitude(),Location.FORMAT_DEGREES) +" "+ Location.convert(startLocation.getLongitude(),Location.FORMAT_DEGREES));
       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (startLocation != null) {
            textView.setText(Location.convert(startLocation.getLatitude(),Location.FORMAT_DEGREES) +" "+ Location.convert(startLocation.getLongitude(),Location.FORMAT_DEGREES));
        }else{
            textView.setText("StartLocation var null");
        }*/
         //lastLocation = location;  Spara ner vår location
    }

    public void calcDistance(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        stopLocation = locationManager.getLastKnownLocation(provider);
        distance = stopLocation.distanceTo(startLocation);
        distText = (TextView) findViewById(R.id.showDistance);
        distText.setText(Float.toString(distance));
      //  Intent intent = new Intent(this, CalculateDistance.class);
      //  intent.putExtra("distance" , distance);
      //  startActivity(intent);

    }
}
