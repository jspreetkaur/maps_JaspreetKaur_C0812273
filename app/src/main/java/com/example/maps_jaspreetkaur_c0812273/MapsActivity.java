package com.example.maps_jaspreetkaur_c0812273;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {



    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;


    Polyline line;
    Polygon shape;

    private Marker homeMarker;
    private Marker destMarker;
    LatLng userLocation ;


    private static final int POLYGON_SIDES = 4;
    List<Marker> markers = new ArrayList();
    List<Polyline> lines = new ArrayList();

    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setHomeMarker(location);
            }

        };

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick( Polyline polyline) {
                
               Toast.makeText(getApplicationContext(),"address2",Toast.LENGTH_LONG).show();

            }
        });
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                //getting distance b/w all points
                double dist_AB = calculateDistance(markers.get(0).getPosition(),markers.get(1).getPosition());
                double dist_BC = calculateDistance(markers.get(1).getPosition(),markers.get(2).getPosition());
                double dist_CD = calculateDistance(markers.get(2).getPosition(),markers.get(3).getPosition());
                double dist_DA = calculateDistance(markers.get(3).getPosition(),markers.get(0).getPosition());
                //adding all distances to calculate Total distance
                double totalDistance = dist_AB + dist_BC + dist_CD + dist_DA ;
              Toast.makeText(getApplicationContext(),"Total Distance = " + String.format("%.2f", totalDistance*0.001)+ " KM",Toast.LENGTH_LONG).show();

            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //removing the polygon
                removeAllShapes();
                //getting index of dragged marker
                int mindex = 0;
                switch(marker.getTitle())
                {
                    case "A" : mindex = 0 ;
                        break;
                    case "B" : mindex = 1;
                        break;
                    case "C" : mindex = 2;
                        break;
                    case "D" : mindex = 3;
                        break;
                }
                //updating the new position of marker
                markers.set(mindex,marker);
                //redrawing the polygon
                drawShape();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
               showAddressToast(marker);
                return false;
            }
        });

        // apply long press gesture
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // set marker
                setMarker(latLng);
            }

        });

    }

    private void showAddressToast(Marker marker)
    {
        Geocoder geocoder = new Geocoder(getApplicationContext(),
                Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            String postal = addresses.get(0).getPostalCode();
            String city = addresses.get(0).getLocality();
            String province = addresses.get(0).getAddressLine(1);
            Toast.makeText(getApplicationContext(),address,Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDistance(Marker marker)
    {
        Double distance =calculateDistance(userLocation,marker.getPosition());
        marker.setSnippet("Distance = " + String.format("%.2f", distance));
    }

    private void setMarker(LatLng latLng) {
        String markerTitle = "";
        switch(markers.size())
        {
            case 0 : markerTitle = "A";
                break;
            case 1 : markerTitle = "B";
                break;
            case 2 : markerTitle = "C";
                break;
            case 3 : markerTitle = "D";
                break;

        }

        Double distance =calculateDistance(userLocation,latLng);
        MarkerOptions options = new MarkerOptions().position(latLng)
                .title(markerTitle).snippet("Distance = " + String.format("%.2f", distance*0.001) + "KM").icon(BitmapDescriptorFactory.fromResource(R.drawable.locationmarker)).draggable(true);


        if (markers.size() == POLYGON_SIDES)
        {
        removeAllMarkers();
        removeAllShapes();
        }

        markers.add(mMap.addMarker(options));
        drawShape();

    }

    private void drawShape() {

        if (markers.size() == POLYGON_SIDES) {
            PolygonOptions options = new PolygonOptions()
                    .fillColor(0x3500FF00)
                    .strokeColor(Color.RED)
                    .strokeWidth(1).clickable(true);

            for (int i = 0; i < POLYGON_SIDES; i++) {
                options.add(markers.get(i).getPosition());
                if(i<POLYGON_SIDES - 1) {
                    drawLines(markers.get(i), markers.get(i + 1));
                }
                else
                {
                    drawLines(markers.get(i), markers.get(0));
                }
            }

            shape = mMap.addPolygon(options);

        }

    }
    private void drawLines(Marker startMarker , Marker endMarker)
    {
        PolylineOptions options = new PolylineOptions()
                .color(Color.RED)
                .width(12).clickable(true);
        options.add(startMarker.getPosition(), endMarker.getPosition());
        lines.add(mMap.addPolyline(options));

    }

    private void removeAllMarkers()
    {
        for (Marker marker: markers)
            marker.remove();
        markers.clear();
    }

    private void removeAllShapes() {

        if(shape != null)
        shape.remove();
        shape = null;
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {
         userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");
        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    private double calculateDistance(LatLng source ,LatLng destination) {

        Location start = new Location("Start");
        start.setLatitude(source.latitude);
        start.setLongitude(source.longitude);
        Location end = new Location("End");
        end.setLatitude(destination.latitude);
        end.setLongitude(destination.longitude);
       float dist = start.distanceTo(end);
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }



}