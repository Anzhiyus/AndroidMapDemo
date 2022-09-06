package com.example.myapplication;


import static java.lang.Double.parseDouble;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.databinding.ActivityMapsBinding;
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;


public class MapsActivity extends FragmentActivity implements View.OnClickListener,OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private GoogleMap mMap;
    private final ArrayList<Marker> MarkerList = new ArrayList<>();
    private Polygon polygon;
    public Button bt_rewind;
    private String rootPath = "DJI_APP_Mizzou";
    private String wayPointFileName = "/Waypoint_Log";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.myapplication.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        registerButton();
    }
    private void registerButton(){
        bt_rewind = findViewById(R.id.bt_rewind);
        bt_rewind.setOnClickListener(this);
        Button bt_upload = findViewById(R.id.bt_upload);
        bt_upload.setOnClickListener(this);
        findViewById(R.id.bt_generate_path).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_rewind) {
            withDrawMarker();
        }
        else if (v.getId()==R.id.bt_upload){
            saveBoundary();
        }
        else if (v.getId()==R.id.bt_generate_path){
            loadBoundary();
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
        MapsActivity.this.runOnUiThread(() -> Toast.makeText(MapsActivity.this, string, Toast.LENGTH_SHORT).show());
    }
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    private void writeFiles(String inputText,String filename,String filepath) {
        File myExternalFile = new File(getExternalFilesDir(filepath), filename);
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            setResultToToast("Not able to write logs");
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(myExternalFile);
                fos.write(inputText.getBytes());
                fos.close();
            } catch (IOException e) {
                setResultToToast(e.getStackTrace().toString());
            }
        }
    }
    private void saveBoundary(){
        String points = "";
        String fileName = "Project_"+java.text.DateFormat.getDateTimeInstance().format(new Date())+".txt";
        fileName = "Latest_boundary";
        for (Marker m:MarkerList){
            points = points+String.valueOf(m.getPosition().latitude)+"/"+String.valueOf(m.getPosition().longitude)+",";
        }
        writeFiles(points,fileName,rootPath+wayPointFileName);
    }
    private String readFiles(String filename,String filepath){
        String outData = "";
        File myExternalFile = new File(getExternalFilesDir(filepath), filename);
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                outData = outData + strLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outData;
    }
    private void loadBoundary(){
        MarkerList.clear();
        mMap.clear();
        String fileName = "Project_"+java.text.DateFormat.getDateTimeInstance().format(new Date())+".txt";
        fileName = "Latest_boundary";
        String data = readFiles(fileName,rootPath+wayPointFileName);
        for (String value:data.split(",")){
            double lat = parseDouble(value.split("/")[0]);
            double lot = parseDouble(value.split("/")[1]);
            setResultToToast("recovering"+(MarkerList.size())+new LatLng(lat,lot).toString());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(lat,lot));
            Marker marker = mMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_icon)).draggable(true).title(String.valueOf(MarkerList.size()+1)));
            MarkerList.add(marker);
            renderPolygon();
        }

    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        // Add a marker in Sydney and move the camera
        LatLng Mizzou = new LatLng(38.940381, -92.327738);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Mizzou,15.0f));

    }
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setOnMapClickListener(this);// add the listener for click for map object
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                renderPolygon();
            }
        });
    }
    @Override
    public void onMapClick(@NonNull LatLng point) {
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
        catch(Exception ignored){

        }
        PolygonOptions areaOfInterest = new PolygonOptions().clickable(true);
        for (Marker m:MarkerList){
            areaOfInterest.add(m.getPosition());
        }
        polygon = mMap.addPolygon(areaOfInterest);
        polygon.setStrokeColor(Color.argb(100,71,227,58));
        polygon.setFillColor(Color.argb(100,55,201,43));
    }

}