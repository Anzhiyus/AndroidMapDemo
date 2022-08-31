package com.example.myapplication;


import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myapplication.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements View.OnClickListener,OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<Marker> MarkerList = new ArrayList<>();
    private PolygonOptions AreaOfInterest;
    private Polygon polygon;
    public Button bt_rewind;

    ArrayList<Marker> markerList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        registerButton();
    }
    private void registerButton(){
        bt_rewind = findViewById(R.id.bt_rewind);
        bt_rewind.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_rewind: {
                withDrawMarker();
                break;
            }
            default:
                break;
        }
    }

    private void withDrawMarker(){
        if (MarkerList.size() == 0){
            setResultToToast("There is no points to rewind");
            return;
        }
        setResultToToast("rewind last point");
        Marker lst_marker = MarkerList.get(MarkerList.size()-1);
        MarkerList.remove(MarkerList.size()-1);
        lst_marker.remove();
        if (MarkerList.size()>0){
            renderPolygon();
        }

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
    private void setResultToToast(final String string){
        MapsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MapsActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        // Add a marker in Sydney and move the camera
        LatLng Mizzou = new LatLng(38.940381, -92.327738);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Mizzou,15.0f));

    }
    private void setUpMap() {
        mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        mMap.setOnMapClickListener(this);// add the listener for click for map object
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                renderPolygon();
            }
        });
    }
    @Override
    public void onMapClick(LatLng point) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        Marker marker = mMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_icon)).draggable(true).title(String.valueOf(MarkerList.size()+1)));
        MarkerList.add(marker);
        renderPolygon();
    }
    public void renderPolygon(){
        try{
            polygon.remove();
        }
        catch(Exception e){

        }
        AreaOfInterest = new PolygonOptions().clickable(true);
        for (Marker m:MarkerList){
            AreaOfInterest.add(m.getPosition());
        }
        polygon = mMap.addPolygon(AreaOfInterest);
        polygon.setStrokeColor(Color.argb(100,71,227,58));
        polygon.setFillColor(Color.argb(100,55,201,43));
    }

}