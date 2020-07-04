package com.example.social_x;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.social_x.Model.MyPlaces;
import com.example.social_x.Model.Results;
import com.example.social_x.Remote.IGoogleAPIService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //private static final int MY_PERMISSION_CODE = 1000; //see if another number works / DELETE
    private GoogleMap _map_;
    private GoogleApiClient Google_API_Client;

    private double latitude;
    private double longitude;
    private Location last_location;
    private Marker _marker_;
    private LocationRequest location_request;

    IGoogleAPIService API_service;

    MyPlaces current_place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Service
        API_service = GoogleAPIService.getGoogleAPIService();

        //Request permission at Runtime
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
            //the method ^ is beneath this - change = move code inside here so we dont have a sep method for it

        }

        BottomNavigationView bottomNavigationItemView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int item_id;
                if((item_id = menuItem.getItemId()) != 0) {
                    if (R.id.action_hospital == item_id) {
                        nearByPlace("hospital");
                    } else if (R.id.action_market == item_id) {
                        nearByPlace("supermarket");
                    } else if (R.id.action_restaurant == item_id) {
                        nearByPlace("restaurant");
                    } else {
                        item_id = 0;
                    }

                    //return true;
                }
                return true;
            }


        });

        }

    private void nearByPlace(final String place) {

        _map_.clear();
        String url = getUrl(latitude,longitude,place);

        API_service.getNearByPlaces(url)
                .enqueue(new Callback<MyPlaces>() {
                    @Override
                    public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {

                        current_place = response.body();

                        if(response.isSuccessful())
                        {
                            for(int i = 0; i < response.body().getResults().length; i++)
                            {
                                MarkerOptions markerOptions = new MarkerOptions();
                                Results googlePlace = response.body().getResults()[i]; //this imported the androidx com.ex
                                double latitude = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                                double longitude = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());
                                String name_of_place = googlePlace.getName();
                                String vicinty = googlePlace.getVicinity();
                                LatLng latLng = new LatLng(latitude,longitude);
                                markerOptions.position(latLng);
                                markerOptions.title(name_of_place);

                                if(place.equals("hospital"))
                                {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital));
                                }

                                else if(place.equals("supermarket"))
                                {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_supermarket));
                                }

                                else if(place.equals("restaurant"))
                                {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_food));
                                }
                                else
                                {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                                }

                                markerOptions.snippet(String.valueOf(i)); //assign index for each marker on the app.

                                //add to map
                                _map_.addMarker(markerOptions);

                                //Moving camera
                                _map_.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                _map_.animateCamera(CameraUpdateFactory.zoomTo(11)); //see if could change



                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyPlaces> call, Throwable t) {

                    }
                });

    }

    private String getUrl (double latitude, double longitude, String place)
    {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location="+latitude+","+longitude);
        googlePlacesUrl.append("&radius="+10000);
        googlePlacesUrl.append("&type="+place);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key="+getResources().getString(R.string.browser_key));
        Log.d("getUrl" , googlePlacesUrl.toString()); //maybe could remove

        return googlePlacesUrl.toString();
    }

    private boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1000);
            return false;
        }

        else
        {
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1000)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (Google_API_Client == null) {
                        buildGoogleApiClient();
                        _map_.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();

                }

            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map_ = googleMap;


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                buildGoogleApiClient();
                _map_.setMyLocationEnabled(true);
            }

            //onClick event for markers on Map

            _map_.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    // when user selects marker, get result of place.
                    GoogleAPIService.currentResult = current_place.getResults() [Integer.parseInt(marker.getSnippet())];
                    //Start new activity

                    startActivity(new Intent(MapsActivity.this, ViewPlace.class));
                    return true;
                }
            });
        }


    }

    private synchronized void buildGoogleApiClient() {

        Google_API_Client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Google_API_Client.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        location_request = new LocationRequest();
        location_request.setInterval(1000);
        location_request.setFastestInterval(1000);
        location_request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(Google_API_Client,location_request,this);
        }

    }
    @Override
    public void onConnectionSuspended(int i) {

        Google_API_Client.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e("MapsActivity.java", "Connection Failed"); //added

    }

    @Override
    public void onLocationChanged(Location location) {

        last_location = location;
        if(_marker_ != null)
        {
            _marker_.remove();
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude,longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Your Current Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        _marker_ = _map_.addMarker(markerOptions);

        //Moving camera here

        _map_.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        _map_.animateCamera(CameraUpdateFactory.zoomTo(15)); //could change

        if(Google_API_Client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(Google_API_Client, this);
        }
    }
}