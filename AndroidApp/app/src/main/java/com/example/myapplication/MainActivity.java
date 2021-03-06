package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;

import android.widget.Toast;

import com.example.myapplication.Thread.ThreadTask;
import com.example.myapplication.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.nhn.android.naverlogin.OAuthLogin;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MainActivity<pirvate> extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentCallback{

    private static final String CHANNEL_ID = "1000" ;

    private String [] notepad_title = new String[100];
    private String [] notepad_content = new String[100];
    private String[] temp = new String[2];
    private ConstraintLayout Parent_layout;
    private int i = 0;
    private String title_line = null;
    private String Content_line = null;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recycle_adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecycleAdaptors recycleAdaptors;

    private Home_fragment home_fragment;
    private Sensor_fragment sensor_fragment;
    private Mypage_fragment mypage_fragment;
    private Hospital_fragment hospital_fragment;

    private String ip;
    DrawerLayout drawer;

    String Title_filename = "title.txt";
    String Content_filename = "content.txt";
    private ItemTouchHelper helper;
    ArrayList<SampleData> notepadDataList = new ArrayList<>();

    private MaterialToolbar toolbar;
    private ActionBar actionBar;
    private MaterialTextView toolbartext;

    private SharedPreferences login_information_pref;
    private String Email;
    private Class<LoginActivity> loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.setStatusBarColor(this, Utils.StatusBarcolorType.BLACK_STATUS_BAR);
        home_fragment = new Home_fragment();
        sensor_fragment = new Sensor_fragment();
        mypage_fragment = new Mypage_fragment();
        hospital_fragment = new Hospital_fragment();

        login_information_pref = getSharedPreferences("login_information", Context.MODE_PRIVATE);
        Email = login_information_pref.getString("email", "");

        ip = getString(R.string.server_ip);

        /*??? fragment??? ?????? ?????????*/
        getSupportFragmentManager().beginTransaction().replace(R.id.container, home_fragment).commit();

        toolbar = (MaterialToolbar)findViewById(R.id.MainActiviy_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        toolbartext = (MaterialTextView)findViewById(R.id.toolbar_textview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        //actionBar.setDisplayHomeAsUpEnabled(true); ???????????? ??????, ?????? ??????????????? ???????????? ???????????? ?????? ????????????

        //getSupportFragmentManager().beginTransaction().replace(R.id.container , home_fragment).commit();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.tab1:
                                //Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_SHORT).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, home_fragment).commit();
                                toolbartext.setText("???????????????!");
                                return true;
                            case R.id.tab2 :
                                //Toast.makeText(getApplicationContext(), "2", Toast.LENGTH_SHORT).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, sensor_fragment).commit();
                                toolbartext.setText("????????? ??????????????????");
                                return true;
                            case R.id.tab3 :
                                //Toast.makeText(getApplicationContext(), "3", Toast.LENGTH_SHORT).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, mypage_fragment).commit();
                                toolbartext.setText("???????????? ???????????????");
                                return true;
                            case R.id.tab4 :
                                //Toast.makeText(getApplicationContext(), "3", Toast.LENGTH_SHORT).show();
                                //getSupportFragmentManager().beginTransaction().replace(R.id.container, hospital_fragment).commit();
                                Intent intent = new Intent(MainActivity.this, NearHospital.class);
                                startActivity(intent);
                                //toolbartext.setText("???????????? ????????? ???????????????");
                                return true;
                        }
                        return false;
                    }
                }
        );
        Navinit(); // navigation drawer ?????????

        //getHashKey(); // fire base ?????? ??? ????????????


        //createNotificationChannel();

        FirebaseMessaging.getInstance().subscribeToTopic("falldown");
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( MainActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken(); //?????? ?????? ????????? new token call back
                Log.e("Fire base Token", newToken.toString());
                System.out.println("Fire base Token :" + newToken);
                //new JSONTask(newToken).execute("http://10.0.2.2:3000/post");//AsyncTask ????????????
                makeThread(newToken);
                int response = send_token_response(Email, newToken);

                if(response == 1){
                    Log.e("?????? ?????? : ", "????????? ?????? ??????");
                }
                else if(response == 2){
                    Log.e("?????? ?????? : ", "??????");
                }
                else if(response == 3){
                    Log.e("?????? ?????? : ", "??????3");
                }
                else if(response == 4){
                    Log.e("?????? ?????? : ", "??????4");
                }
                else if(response == 0){
                    Log.e("?????? ?????? : ", "???????????????");
                }

            }

        });

    }
    private void getHeadup(String title, String body){
        Intent snoozeIntent = new Intent(this,RegisterActivity.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("EXTRA_NOTIFICATION_ID", 0);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_menu_slideshow)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setFullScreenIntent(snoozePendingIntent, false)
                .setContentIntent(snoozePendingIntent);
        //.setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        int notificationId = 15;
        notificationManager.notify(notificationId, builder.build());
    }

    private int send_token_response(String request_email, String token){

        ThreadTask<Object> result = new ThreadTask<Object>() {

            String Request_email = request_email;
            String Token = token;

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

                URL url = new URL(urls[0] +"/firebase_token_save");
               // URL url = new URL(getString(R.string.test_FCM_ip) +"/firebase_token_save");
                con = (HttpURLConnection) url.openConnection();

                sendObject.put("email_address",Request_email);
                sendObject.put("fcm_token",Token);

                Log.e("?????? ??????", "?????? ?????? ?????????.");
                con.setRequestMethod("POST");//POST???????????? ??????
                con.setRequestProperty("Cache-Control", "no-cache");//?????? ??????
                con.setRequestProperty("Content-Type", "application/json");//application JSON ???????????? ??????
                con.setRequestProperty("Accept", "application/json");//????????? response ???????????? html??? ??????
                con.setDoOutput(true);//Outstream?????? post ???????????? ?????????????????? ??????
                con.setDoInput(true);//Inputstream?????? ??????????????? ????????? ???????????? ??????

                OutputStream outStream = con.getOutputStream();
                outStream.write(sendObject.toString().getBytes());
                outStream.flush();
                Log.e("?????? ??????", "?????? ?????? ?????????.");
                int responseCode = con.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    InputStream stream = con.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] byteBuffer = new byte[1024];
                    byte[] byteData = null;
                    int nLength = 0;

                    while ((nLength = stream.read(byteBuffer, 0, byteBuffer.length)) != -1){
                        baos.write(byteBuffer, 0, nLength);
                    }
                    byteData = baos.toByteArray();

                    String response = new String(byteData);

                    JSONObject responseJSON = new JSONObject(response);
                    this.response_result = (Integer) responseJSON.get("result");
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

        result.execute(ip);
        return result.getResult();
    }

    public void makeThread(Object newToken){
        ThreadTask<Object> result = new ThreadTask<Object>() {
            Object NewToken = newToken;
            @Override
            protected void onPreExecute() {// excute ??????

            }

            @Override
            protected void doInBackground(String... urls) {//background??? ????????????
                try {
                    //JSONObject??? ????????? key value ???????????? ?????? ???????????????.
                    JSONObject jsonObject = new JSONObject();

                    Object jsonStr = "tlqkftlqkf";

                    Log.e("jsonTask", "????????????4");
                    //jsonObject.accumulate("name", "yun");
                    HttpURLConnection con = null;
                    BufferedReader reader = null;

                    jsonObject.accumulate("token", NewToken);

                    try{
                        //URL url = new URL("http://10.0.2.2:3000/post");

                        URL url = new URL(urls[0]+"/post");
                        Log.e("Mainactivity", urls[0]+"/post");
                        con = (HttpURLConnection) url.openConnection();

                        con.setRequestMethod("POST");//POST???????????? ??????
                        con.setRequestProperty("Cache-Control", "no-cache");//?????? ??????
                        con.setRequestProperty("Content-Type", "application/json");//application JSON ???????????? ??????

                        con.setRequestProperty("Accept", "text/html");//????????? response ???????????? html??? ??????
                        con.setDoOutput(true);//Outstream?????? post ???????????? ?????????????????? ??????
                        con.setDoInput(true);//Inputstream?????? ??????????????? ????????? ???????????? ??????

                        con.setConnectTimeout(15000);
                        con.connect();
                        //????????? ?????????????????? ????????? ??????

                        OutputStream outStream = con.getOutputStream();
                        //????????? ???????????? ??????
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                        writer.write(jsonObject.toString());
                        writer.flush();
                        writer.close();//????????? ?????????

                        //????????? ?????? ???????????? ??????
                        InputStream stream = con.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(stream));
                        StringBuilder buffer = new StringBuilder();
                        String line = "";
                        while((line = reader.readLine()) != null){
                            buffer.append(line);
                        }

                        //return buffer.toString();//????????? ?????? ?????? ?????? ???????????? ?????? OK!!??? ???????????????

                    } catch (MalformedURLException e){
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if(con != null){
                            con.disconnect();
                        }
                        try {
                            if(reader != null){
                                reader.close();//????????? ?????????
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();//
                }
            }

            @Override
            protected void onPostExecute() {

            }

            @Override
            public int getResult() {
                return 0;
            }

            @Override
            public String getErrorCode() {
                return null;
            }

        };
        result.execute(ip);

    }
    public void wrtieToFile(){

        File Title_file = new File(MainActivity.this.getFilesDir(), Title_filename);
        File Content_file = new File(MainActivity.this.getFilesDir(), Content_filename);

        StringBuilder stringBuffer = new StringBuilder();
        BufferedReader Title_reader = null;
        try {
            Title_reader = new BufferedReader(new FileReader(Title_file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try (
                BufferedReader Content_reader =
                        new BufferedReader(new FileReader(Content_file))) {

            while (true) {
                assert Title_reader != null;
                title_line = Title_reader.readLine();
                if (title_line == null ) {
                    break;
                }

                while (!(Content_line = Content_reader.readLine()).equals("=============") && Content_line != null) {
                    stringBuffer.append(Content_line).append('\n');
                    System.out.println("gdgdgd " + stringBuffer.toString());;
                }

                Content_line = stringBuffer.toString();
                System.out.println("Content_line : " + Content_line);

                notepad_title[i] = title_line;
                notepad_content[i] = Content_line;
                System.out.println("????????? ??????" + i + " : " + notepad_title[i] + " " + notepad_content[i]);

                notepadDataList.add(new SampleData(notepad_title[i] , notepad_content[i],i)) ;
                recycleAdaptors.notifyDataSetChanged();
                stringBuffer.delete(0, stringBuffer.length());
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }

    public void InitializeNotepadData() {
        notepadDataList = new ArrayList<>();

       // notepadDataList.add(new SampleData("?????????", 1));
    }
    public void Navinit(){
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        drawer = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
    }

    private long time = 0;
    @Override
    public void onBackPressed() {
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
        if(System.currentTimeMillis() - time >= 2000){
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "?????? ????????? ?????? ??? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
        }
        else if(System.currentTimeMillis() - time < 2000){
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu1) {
            Toast.makeText(this, "????????? ?????? ?????????.", Toast.LENGTH_LONG).show();
            onFragmentSelected(0, null);
        } else if (id == R.id.menu2) {
            Toast.makeText(this, "????????? ?????? ?????????.", Toast.LENGTH_LONG).show();
            onFragmentSelected(1, null);
        } else if (id == R.id.menu3) {
            Toast.makeText(this, "????????? ?????? ?????????.", Toast.LENGTH_LONG).show();
            onFragmentSelected(2, null);
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onFragmentSelected(int position, Bundle bundle) {
        Fragment curFragment = null;

        if (position == 0) {
           System.out.println("????????? menu");
            UserManagement.getInstance()
                    .requestLogout(new LogoutResponseCallback() {
                        @Override
                        public void onCompleteLogout() {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        } else if (position == 1) {
            System.out.println("????????? menu");
            OAuthLogin mOAuthLogin = OAuthLogin.getInstance();
            String loginState = mOAuthLogin.getState(MainActivity.this).toString();
            if(!loginState.equals("NEED_LOGIN")){
                Log.e("Main Logout", "???????????? ??????");
                mOAuthLogin.logout(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Log.e("Main Logout", "???????????? ??????");
            }
        } else if (position == 2) {
            System.out.println("????????? menu");
            /*???????????? ??? ??? ????????? ??????*/
            UserManagement.getInstance()
                    .requestUnlink(new UnLinkResponseCallback() {
                        @Override
                        public void onSessionClosed(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "????????? ?????? ??????: " + errorResult);
                        }

                        @Override
                        public void onFailure(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "?????? ?????? ??????: " + errorResult);

                        }
                        @Override
                        public void onSuccess(Long result) {
                            Log.i("KAKAO_API", "?????? ?????? ??????. id: " + result);
                        }
                    });
        }

        /*
            //getSupportFragmentManager().beginTransaction().replace(R.id.container, curFragment).commit();
            ????????? ?????? ????????? fragment ???????????? ????????????.
         */

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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @TargetApi(Build.VERSION_CODES.M)

    private void checkPermission() {

        String[] permissions = {
                // Manifest??? android??? import
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (String permission : permissions) {
            permissionCheck = this.checkSelfPermission(permission);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                //break;
            }
        }
        //?????? ??????
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            this.requestPermissions(permissions, 1);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE )!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)){
                /**
                 * ?????? ?????? ???
                 * */
            }
            else{
                /**
                 * ???????????? ???
                 * */
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                        1000);
            }
        }
    }
}



