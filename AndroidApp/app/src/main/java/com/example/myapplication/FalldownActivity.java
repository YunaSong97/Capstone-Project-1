package com.example.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FalldownActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MaterialToolbar toolbar;
    private ActionBar actionBar;
    private MaterialTextView toolbartext;
    private MaterialTextView PhoneCall;

    private GoogleMap GMap;

    private ArrayList<Hospital> HospitalDataList = new ArrayList<>();
    private RecycleAdaptors_hospital recycleAdaptors;
    private RecyclerView Hospital_recycler_view;
    private TextView Phone_call;

    private LinearLayoutManager layoutManager;

    private Double start_latitude;
    private Double start_longitude;
    private String target_phone;
    private String data_array;
    private JSONArray Hospital_array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_falldown);

        Utils.setStatusBarColor(this, Utils.StatusBarcolorType.BLACK_STATUS_BAR);

        toolbar = (MaterialToolbar)findViewById(R.id.MainActiviy_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        toolbartext = (MaterialTextView)findViewById(R.id.toolbar_textview);
        toolbartext.setText("?????? ??????");

        Hospital_recycler_view = findViewById(R.id.Hospital_Recycler_view);
        layoutManager =  new LinearLayoutManager(FalldownActivity.this);
        if(layoutManager != null){

            Hospital_recycler_view.setLayoutManager(layoutManager);
        }
        else{
            Log.e("SensorFragment", "Error");
        }


        Intent intent = getIntent();
        start_latitude = Double.parseDouble(intent.getStringExtra("latitude"));
        start_longitude = Double.parseDouble(intent.getStringExtra("longitude"));

        Log.e("start_latitude", Double.toString(start_latitude));
        Log.e("start_longitude", Double.toString(start_longitude));
        target_phone = intent.getStringExtra("phone_number");
        data_array = intent.getStringExtra("data_array");
        Hospital_array = null;

        try {
            Hospital_array = new JSONArray(data_array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0 ; i < Hospital_array.length() ; i++) {
            try {
                JSONObject hospital_data = Hospital_array.getJSONObject(i);
                String hospital_name = hospital_data.getString("hospital_name");
                String hospital_location = hospital_data.getString("hospital_location");
                String hospital_phone_num = hospital_data.getString("hospital_phone_num");
                String category = hospital_data.getString("category");
                String hospital_time = hospital_data.getString("time");
                String latitude = hospital_data.getString("latitude");
                String longitude = hospital_data.getString("longitude");

                HospitalDataList.add(new Hospital(hospital_name, hospital_location, hospital_phone_num
                        ,category,hospital_time, latitude, longitude));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        // ?????? ???????????? onMapReady ??????
        mapFragment.getMapAsync(this);

        PhoneCall = (MaterialTextView)findViewById(R.id.phone_call);
        PhoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tel="tel:"+target_phone;
                startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
            }
        });

        /**
         * ?????? ?????? ?????? ??? ????????????
         * */

    }

    public com.google.android.gms.maps.GoogleMap getGoogleMap() {
        return GMap;
    }

    public void setGoogleMap(com.google.android.gms.maps.GoogleMap googleMap) {
        GMap = googleMap;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        setGoogleMap(googleMap);
        GMap.setOnMarkerClickListener(this);
        // ???????????? ????????? api??? ????????????
        // ??????????????? ?????? ???????????? ??????
        /**
         * ?????? ?????? ?????? ?????? ????????????
         * */


//        start_latitude = Double.parseDouble("35.86464630089416");
//        start_longitude = Double.parseDouble("128.59296105844146");
        start_latitude = 35.86464630089416;
        start_longitude = 128.59296105844146;
        //Toast.makeText(getApplicationContext(), String.format("%s %s", Double.toString(start_latitude), Double.toString(start_longitude)
       // ), Toast.LENGTH_LONG).show();
        LatLng Starting_Point = new LatLng(start_latitude, start_longitude); // ????????? ??????
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(Starting_Point, 14);
        //GMap.moveCamera(CameraUpdateFactory.newLatLng(Starting_Point));
        GMap.animateCamera(cameraUpdate);// ????????? ??? ??????

        // ????????? ?????? ???????????? ????????? title??? snippet??? ??????.
        MarkerOptions markerOptions = new MarkerOptions();
        double Latitude;
        double Longtitude;


//        HospitalDataList.add(new Hospital("?????? ????????? ??????",
//                "??????????????? ?????? ????????? 99",
//                "053-940-7114", "24?????? ??????", true, "35.88422977202024", "128.6242076358278"));
//
//        HospitalDataList.add(new Hospital("?????? ?????? ??????",
//                "??????????????? ?????? ????????? ????????? 270",
//                "053-959-7114", "24?????? ??????", true, "35.893712463929305", "128.61971185982367"));
//
//        HospitalDataList.add(new Hospital("????????? ??????",
//                "??????????????? ?????? ????????? ????????? 120",
//                "053-940-3000", "09:00~18:00", true, "35.88103054660772", "128.61258938252828"));
//
//        HospitalDataList.add(new Hospital("??????????????????????????????",
//                "??????????????? ?????? ????????? ????????????6??? 11",
//                "053-242-0119", "09:00~18:00", true, "35.88122783453655", "128.608410057474"));
//
//        HospitalDataList.add(new Hospital("?????????????????????",
//                "??????????????? ?????? ??????3??? 1287-4",
//                "+82539547771", "09:00~18:00", true, "35.897941271089294", "128.613653306951"));


        Marker marker;
        for(int i = 0 ; i < HospitalDataList.size() ; i++){
            Latitude = Double.parseDouble(HospitalDataList.get(i).getLatitude());
            Longtitude = Double.parseDouble(HospitalDataList.get(i).getLongtitude());

            markerOptions.position(new LatLng(Latitude,Longtitude));
            markerOptions.title(HospitalDataList.get(i).getHospital_name());
            //markerOptions.snippet(Hos);
            // ????????? ?????? ????????? ????????? ??????

            marker = GMap.addMarker(markerOptions);
            marker.showInfoWindow();
        }

        // ??????????????????

        // ?????? DB??? ???????????? ????????? ????????? ??????.
//        LatLng plaza = new LatLng(37.565785, 126.978056);
//        markerOptions.position(plaza);
//        markerOptions.title("??????");
//        markerOptions.snippet("?????? ??????");
//        GMap.addMarker(markerOptions);

        //??? ?????? ??? ??????
        GMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
               // Toast.makeText(FalldownActivity.this, "Map????????????", Toast.LENGTH_SHORT).show();
            }
        });

        //????????? ?????? ??????
        GMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                //Log.d("set>>","start");
            }
        });

        // ????????? ?????? ???
        GMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                //Log.d("set>>","move");
            }
        });

        // ????????? ???????????? ???????????? ?????????
        GMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // ?????? ?????? ??????
                //googleMap.clear();
                // ????????? ????????? ?????? ????????????
                GMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                // ?????? ?????? ??????
//                MarkerOptions newMarker=new MarkerOptions();
//                newMarker.position(latLng);
//                googleMap.addMarker(newMarker);
            }
        });

        recycleAdaptors = new RecycleAdaptors_hospital(getGoogleMap());
        //HospitalDataList = recycleAdaptors.getItems();


//        HospitalDataList.add(new Hospital("????????????",
//                "?????? ????????? ???????????????4??? ????????? 130-11",
//                "053-986-153", "??????", true, latitude, longtitude));
//        HospitalDataList.add(new Hospital("??????????????? ??????",
//                "?????? ????????? ???????????????99??? ????????? 130-123",
//                "053-941-823", "?????????", true, latitude, longtitude));
        //ArrayList<Sensor_list> sensorDataList = recycleAdaptors.getItems();
        //sensorDataList.add(new Sensor_list("12:34:56:78", "????????? ??????", "????????????" ));
        recycleAdaptors.setItems(HospitalDataList);
        Hospital_recycler_view.setAdapter(recycleAdaptors);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //System.out.println(layoutManager.canScrollVertically());
                int position = 0;
                for( Hospital hospital : HospitalDataList ){
                    if(hospital.getHospital_name().equals(marker.getTitle())){
                        System.out.println(hospital.getHospital_name());
                        Hospital_recycler_view.smoothScrollToPosition(position);
                    }
                    position++;
                }
            }
        }, 200);
        return false;
    }
}