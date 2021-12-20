package nl.inholland.lafkiri.covid_19measure;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.KeyEventDispatcher;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15;
    private boolean locationPermissionGranted = false;

//  widgets
    private EditText searchText;
    private ImageView mGps;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

//  this is probably not needed at the end
    private CountryStatisticsViewModel countryStatisticsViewModel;
//    private CountryStatisticsAdapter adapter;
    private List<CountryStatistics> stats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        searchText = (EditText) findViewById(R.id.search_text) ;
        mGps = (ImageView) findViewById(R.id.ic_gps);

//      getting the country statistics list from main activity
        stats = (ArrayList<CountryStatistics>)getIntent().getSerializableExtra("statsList");

//      getting location permissions and initializing the map
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(5000);
        locationCallback = new LocationCallback();
        getLocationPermission();
    }

    /**
     * initializes searching on map listener
     * and rest of implementation
     */
    private void initSearching() {
        Log.d(TAG, "init: initializing searching feature on map");

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId ==EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
//                  Execute searching method here
                    geoLocate();
                }
                return false;
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked gps icon, to get current location ");
                getLocation();
            }
        });
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");
        String searchString = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: Found Location: " + address.toString());
//            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }

    }

    private void getLocation() {
        Log.d(TAG, "getLocation: getting the devices location");
        try {
            if (locationPermissionGranted) {
                Task<Location> task = fusedLocationProviderClient.getLastLocation();
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, MapActivity.this.getMainLooper());
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d(TAG, "onComplete: found Location");
                            currentLocation = location;
//                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "onComplete: current location is null, last location is not found");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getLocation: Security Exception: " + e.getMessage() );
        }
    }
    
    private void moveCamera (LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to : lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
        map.addMarker(markerOptions);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        Toast.makeText(this, getResources().getString(R.string.map_ready), Toast.LENGTH_SHORT).show();
        map = googleMap;
//      Adding markers on the map for each country/state with its Covid-19 statistics
//      For Efficiency: add an if statement checking the size of stats list to check whether the list if passed or not, else do nothing
        for (int i = 0; i < stats.size(); i++) {
            CountryStatistics statsItem = stats.get(i);
            String countryState = statsItem.getCountryState();
            String totalCases = statsItem.getTotalCases();
            String recovered = statsItem.getRecovered();
            String deaths = statsItem.getDeaths();
            String activeCases = statsItem.getActiveCases();
            String source = statsItem.getSource();
            double latitude = statsItem.getLatitude();
            double longitude = statsItem.getLongitude();

            LatLng coordinates = new LatLng(latitude, longitude);

            String snippet = getResources().getString(R.string.total_cases) + ":    " + totalCases + "\n" +
                    getResources().getString(R.string.recovered) + ":    " + recovered + "\n" +
                    getResources().getString(R.string.deaths) + ":    " + deaths + "\n" +
                    getResources().getString(R.string.active_cases) + ":    " + activeCases + "\n" +
                    getResources().getString(R.string.source) + ":    " + source + "\n";

            map.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));



            map.addMarker(new MarkerOptions().position(coordinates).title(countryState).snippet(snippet));

//            map.addCircle(new CircleOptions()
//                    .center(new LatLng(latitude, longitude))
//                    .radius(10000)
//                    .strokeColor(Color.RED)
//                    .fillColor(Color.BLUE));
//            map.moveCamera(CameraUpdateFactory.newLatLng());
        }

        if (locationPermissionGranted) {
            getLocation();
//          Marking current device location on the map...
            map.setMyLocationEnabled(true);
//          disabling the GPS Icon that centers on current location automatically
//          this is done becuase an search field is implemented and which block the view of that Icon
//          So, the default one is removed and a custom one will be implemented
            map.getUiSettings().setMyLocationButtonEnabled(false );
            initSearching();
        }
    }

    private void initializeMap() {
        Log.d(TAG, "initializeMap: Initializing the map here");
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Getting the location's permission
     */
    private void getLocationPermission() {
        String [] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                initializeMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     *Verifying Location permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Calling");
        locationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if(grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: Permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: Permission Granted");
                    locationPermissionGranted = true;
//                  initialize the map here
                    initializeMap();
                }
            }
        }
    }
}
