package com.example.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Thread.ThreadTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NearHospital extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

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

    String Email;
    private SharedPreferences login_information_pref;

    private String ip;
    private String input;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_hospital);
        ip = getString(R.string.server_ip);

        Utils.setStatusBarColor(this, Utils.StatusBarcolorType.BLACK_STATUS_BAR);

        toolbar = (MaterialToolbar)findViewById(R.id.MainActiviy_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        toolbartext = (MaterialTextView)findViewById(R.id.toolbar_textview);
        toolbartext.setText("???????????? ????????? ???????????????");
        login_information_pref = getSharedPreferences("login_information", Context.MODE_PRIVATE);
        Email = login_information_pref.getString("email", Email);


        Hospital_recycler_view = findViewById(R.id.Hospital_Recycler_view);
        layoutManager =  new LinearLayoutManager(NearHospital.this);
        if(layoutManager != null){

            Hospital_recycler_view.setLayoutManager(layoutManager);
        }
        else{
            Log.e("SensorFragment", "Error");
        }

        ThreadTask<Object> result = getThreadTask(Email, "/id_duplication_check");
        result.execute(ip);


        JSONObject input_object = null;
        try {
            input_object = new JSONObject(input);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //Intent intent = getIntent();
        try {
            start_latitude = Double.parseDouble(input_object.getString("latitude"));
            start_longitude = Double.parseDouble(input_object.getString("longitude"));
            Log.e("NearHospital", Double.toString(start_latitude));
            Log.e("NearHospital", Double.toString(start_longitude));
            target_phone = input_object.getString("phone_number");
            data_array = input_object.getString("data_array");
            Hospital_array = null;
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
                Log.e("tqtqtqtqtqtq", "asdfsdgfsdgfsdfgsdfgd");
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


        /**
         * ?????? ?????? ?????? ??? ????????????
         * */

    }
    private ThreadTask<Object> getThreadTask(String email, String Router_name){

        return new ThreadTask<Object>() {
            private int response_result;
            private String error_code;
            @Override
            protected void onPreExecute() {// excute ??????

            }

            @Override
            protected void doInBackground(String... urls) throws IOException, JSONException {//background??? ????????????
                HttpURLConnection con = null;
                JSONObject sendObject = new JSONObject();
                BufferedReader reader = null;

                URL url = new URL(urls[0] +Router_name);

                con = (HttpURLConnection) url.openConnection();

                sendObject.put("email_address", email);

                con.setRequestMethod("POST");//POST???????????? ??????
                con.setRequestProperty("Cache-Control", "no-cache");//?????? ??????
                con.setRequestProperty("Content-Type", "application/json");//application JSON ???????????? ??????
                con.setRequestProperty("Accept", "application/json");//????????? response ???????????? html??? ??????
                con.setDoOutput(true);//Outstream?????? post ???????????? ?????????????????? ??????
                con.setDoInput(true);//Inputstream?????? ??????????????? ????????? ???????????? ??????

                OutputStream outStream = con.getOutputStream();
                outStream.write(sendObject.toString().getBytes());
                outStream.flush();

                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    InputStream stream = con.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] byteBuffer = new byte[1024];
                    byte[] byteData = null;
                    int nLength = 0;
                    while ((nLength = stream.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                        baos.write(byteBuffer, 0, nLength);
                    }
                    byteData = baos.toByteArray();
                    String response = new String(byteData);
                    JSONObject responseJSON = new JSONObject(response);

                    this.response_result = (Integer) responseJSON.get("key");
                    this.error_code = (String) responseJSON.get("err_code");
                    input = (String) responseJSON.getString("input");
                }
            }

            @Override
            protected void onPostExecute() {

            }

            @Override
            public int getResult() {
                return response_result;
            }

            @Override
            public String getErrorCode() {
                return error_code;
            }
        };
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

        LatLng Starting_Point = new LatLng(start_latitude, start_longitude); // ????????? ??????
        GMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Starting_Point, 14));
        //GMap.animateCamera(CameraUpdateFactory.zoomTo(14));// ????????? ??? ??????

        // ????????? ?????? ???????????? ????????? title??? snippet??? ??????.
        MarkerOptions markerOptions = new MarkerOptions();
        double Latitude;
        double Longtitude;

        Marker marker = null;
        for(int i = 0 ; i < HospitalDataList.size() ; i++){
            Latitude = Double.parseDouble(HospitalDataList.get(i).getLatitude());
            Longtitude = Double.parseDouble(HospitalDataList.get(i).getLongtitude());

//            markerOptions.position(new LatLng(Latitude,Longtitude));
//            markerOptions.title(HospitalDataList.get(i).getHospital_name());
            //markerOptions.snippet(Hos);
            // ????????? ?????? ????????? ????????? ??????

            //marker = GMap.addMarker(markerOptions);
            //marker.showInfoWindow();
            marker = GMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Latitude,Longtitude))
                    .title(HospitalDataList.get(i).getHospital_name() ));
        }
        marker.showInfoWindow();
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
                //Toast.makeText(NearHospital.this, "Map????????????", Toast.LENGTH_SHORT).show();
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
        recycleAdaptors.setItems(HospitalDataList);
        Hospital_recycler_view.setAdapter(recycleAdaptors);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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