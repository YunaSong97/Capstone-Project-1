package com.example.myapplication.ui.login;

import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.FindEmail;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.RegisterActivity;
import com.example.myapplication.ResetPW;
import com.example.myapplication.Thread.ThreadTask;
import com.example.myapplication.Tutorial;
import com.example.myapplication.Utils;
import com.example.myapplication.additional_inform_register;
import com.example.myapplication.data.LoginRepository;
import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthLoginState;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nhn.android.naverlogin.OAuthLogin.mOAuthLoginHandler;

public class LoginActivity extends AppCompatActivity  {
    /*
    * sns ????????? check ????????? ?????? :
    * 1. naver : mOAuthLoginModule.getState(mContext).toString() -> OK?????? ????????? session??? ??????.
    * 2. kakao : Kakao_session.checkAndImplicitOpen() -> true?????? ????????? session??? ??????.
    *
    * ????????? ????????????????????? ??????????????? ???????????? ????????????.
    *  ?????? ??????????????? api ?????? ????????? ???????????? 'id'?????? ?????? ???????????? ????????? ??????????????? ????????????.
    * 'id'?????? ??? ???????????????????????? ?????? ?????? ???????????? ?????????,
    * ?????? ????????? ??????????????? ???????????? ????????? ????????????????????? ????????? id?????? ?????? ??? ??????????????? ????????????
    */

    private LoginViewModel loginViewModel;
    private Button btn_custom_login;
    private Button btn_custom_login_out;
    private SessionCallback sessionCallback;
    public static Context loginContext;
    private static Activity loginActivity;
    static OAuthLogin mOAuthLoginModule;
    private static String OAUTH_CLIENT_ID = "Tu7qkulSTOAWYaEYnMyJ";
    private static String OAUTH_CLIENT_SECRET = "swpY8wkE0P";
    private static String OAUTH_CLIENT_NAME = "????????? ???????????? ?????????";
    public static Context mContext;
    private String apiURL;
    private String Naver_profile_ReadBody;
    private static String Email;
    private String first_naver_login;
    private String naver_access_token_check;
    private String ip;
    private SharedPreferences login_information_pref;
    private SharedPreferences login_log_pref;

    private SharedPreferences.Editor login_infromation_editor;
    private SharedPreferences.Editor login_log_editor;

    private Button find_Email_button;
    private Button find_pw_button;

    private String kakao_Email;
    Session Kakao_session;
    private LoginRepository KakaoTalkService;
    /*
    * sns ????????? ????????? ??????
    * ???????????? email??? ????????? ???????????? ?????? db??? ?????? ????????? check, ????????? ???????????? page??? ????????????
    * ????????? ?????? ?????????.
    *
    *
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Utils.setStatusBarColor(this, Utils.StatusBarcolorType.BLACK_STATUS_BAR);

        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        mContext = this;
        ip = getString(R.string.server_ip);
        //Toast.makeText(getApplication(),ip+"/naver", Toast.LENGTH_SHORT).show();
        //session ????????? ?????? ??????????????? ?????????.. ?

        find_Email_button = findViewById(R.id.find_id);
        find_pw_button = findViewById(R.id.find_pw);

        find_Email_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.myapplication", "com.example.myapplication.FindEmail"));
                startActivity(intent);
            }
        });

        find_pw_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.myapplication", "com.example.myapplication.ResetPW"));
                startActivity(intent);
            }
        });

        getHashKey();
        btn_custom_login = (Button) findViewById(R.id.btn_kakao_login_custom);

        sessionCallback = new SessionCallback(mContext);
        Kakao_session = Session.getCurrentSession();
        Kakao_session.addCallback(sessionCallback);
        Kakao_session.checkAndImplicitOpen();// ????????? ???????????????

        login_log_pref = getSharedPreferences("SNS_login_log", Activity.MODE_PRIVATE);
        first_naver_login = login_log_pref.getString("first_naver_login", "true");

        /*????????? ????????? ??????*/
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.showDevelopersLog(true);
        mOAuthLoginModule.init(LoginActivity.this
                ,OAUTH_CLIENT_ID
                ,OAUTH_CLIENT_SECRET
                ,OAUTH_CLIENT_NAME
        );
        OAuthLoginButton mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);
        @SuppressLint("HandlerLeak") OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
            @Override
            public void run(boolean success) {
                    if (success) {// naver access??? ?????????
                        String accessToken = mOAuthLoginModule.getAccessToken(mContext);
                        String refreshToken = mOAuthLoginModule.getRefreshToken(mContext);
                        Log.e("naver Token accessToken", accessToken);
                        Log.e("naver Token refreshToken", refreshToken);

                        if(!Kakao_session.checkAndImplicitOpen()){ // ????????? ????????? ????????? page??? ?????????
                            //????????? ??????
                            Log.e("Login Check", "kakao Session??? ?????? naver????????? ??????");
                            // ????????? ????????? ?????? ??????;
                            String header = "Bearer " + accessToken; // Bearer ????????? ?????? ??????
                            apiURL = "https://openapi.naver.com/v1/nid/me";

                            Map<String, String> requestHeaders = new HashMap<>();
                            requestHeaders.put("Authorization", header);

                            makeThread(apiURL, requestHeaders);
                            String responseBody = null;
                            responseBody = Naver_profile_ReadBody;

                            ThreadTask<Object> result = getThreadTask_social_sign_in(Email, "naver","/social_sign_in");
                            result.execute(ip);

                            if(result.getResult() == 1){
                                //????????? ?????? ??????
                            }
                            else if(result.getResult() == 2){
                                //???????????? ?????? ????????? // ???????????? ????????????
                                //?????? ?????? ??? ???????????? ????????? ??????
                                if(first_naver_login.equals("true")){
                                    Intent intent = new Intent(LoginActivity.this, additional_inform_register.class);
                                    intent.putExtra("email", Email);
                                    intent.putExtra("login_type","naver");

                                    login_log_pref = getSharedPreferences("SNS_login_log", Activity.MODE_PRIVATE);
                                    login_log_editor = login_log_pref.edit();
                                    login_log_editor.putString("first_naver_login" , "false");
                                    login_log_editor.commit();
                                    Log.e("Naver_login ?????? ", Email);
                                    //intent.putExtra("pw", PasswordConfirmText.getText().toString());
                                    startActivity(intent);
                                }
                            }
                            else if(result.getResult() == 3){ // ???????????? ?????? ????????? ???????????? ?????????
                                Toast.makeText(LoginActivity.this , "?????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                                mOAuthLoginModule.logoutAndDeleteToken(LoginActivity.mContext);
                                //                                login_information_pref = getSharedPreferences("login_information", Activity.MODE_PRIVATE);
//                                login_infromation_editor = login_information_pref.edit();

//                                if(first_naver_login.equals("true")){
//                                    Intent intent = new Intent(LoginActivity.this, additional_inform_register.class);
//                                    intent.putExtra("email", Email);
//                                    intent.putExtra("login_type","naver");
//
//                                    login_log_pref = getSharedPreferences("SNS_login_log", Activity.MODE_PRIVATE);
//                                    login_log_editor = login_log_pref.edit();
//                                    login_log_editor.putString("first_naver_login" , "false");
//                                    login_log_editor.commit();
//                                    Log.e("Naver_login ?????? ", Email);
//                                    //intent.putExtra("pw", PasswordConfirmText.getText().toString());
//                                    startActivity(intent);
//                                }
//                                else if(first_naver_login.equals("false")){
//                                    //????????? Email??? ????????? ??????, Email??? user table??? ????????? true??? ?????????
//                                    //true?????? main activity??? ?????????
//                                    //sns_login_request(Email, "naver");
//                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                    login_infromation_editor.putString("login_type" , "naver");
//                                    login_infromation_editor.putString("email" , Email);
//                                    login_infromation_editor.commit();
//                                    startActivity(intent);
//                                    finish();
//
//                                }
                            }
                            else if(result.getResult() == 4) { // ????????? ??????!
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                login_information_pref = getSharedPreferences("login_information", Activity.MODE_PRIVATE);
                                login_infromation_editor = login_information_pref.edit();
                                login_infromation_editor.putString("login_type" , "naver");
                                login_infromation_editor.putString("email" , Email);
                                login_infromation_editor.commit();
                                startActivity(intent);
                                finish();
                            }
                            else if(result.getResult() == 0) {
                                //????????? ??????
                            }
                    }
                } else { //????????? ????????? ?????????
                    /*String errorCode = mOAuthLoginModule.getLastErrorCode(mContext).getCode();
                    String errorDesc = mOAuthLoginModule.getLastErrorDesc(mContext);
                    Toast.makeText(mContext, "errorCode:" + errorCode
                            + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();*/
                    Log.e("naver_login", "????????? ??????");
                }
            };
        };
        mOAuthLoginButton.setOnClickListener( //?????? ????????? ????????? ??????, ????????? ?????? ?????????
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOAuthLoginModule.startOauthLoginActivity((Activity) mContext, mOAuthLoginHandler); //????????? ??????.Intent intent = new Intent(LoginActivity.this, additional_inform_register.class);
                    }
                }
        );

        //???????????? ????????? ??????????????? ??????
        if(!OAuthLoginState.NEED_LOGIN.equals(OAuthLogin.getInstance().getState(mContext)) && !Kakao_session.checkAndImplicitOpen()){
            mOAuthLoginModule.startOauthLoginActivity((Activity) mContext, mOAuthLoginHandler); //????????? ??????.
            Log.e("Naver Login", "?????? ?????????");
        }

        //????????? ????????? ?????????
        mOAuthLoginButton.setBgResourceId(R.drawable.ic_anyconv__naver_login_button);

        btn_custom_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Kakao_session.open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);

             }
        });

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final Button RegisterButton = findViewById(R.id.register);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) { //????????? form??? ????????? ????????????.
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid()); // data??? valid?????? ????????? ?????? ?????????.
                if (loginFormState.getUsernameError() != null) { //username??? ????????? ????????????
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) { //username??? ????????? ????????????
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) { //????????? ????????? ????????????
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) { // ????????????
                    updateUiWithUser(loginResult.getSuccess());
                    Email = usernameEditText.getText().toString();
                    login_information_pref = getSharedPreferences("login_information", Activity.MODE_PRIVATE);
                    login_infromation_editor = login_information_pref.edit();
                    login_infromation_editor.putString("login_type" , "general");
                    login_infromation_editor.putString("email" , Email);
                    login_infromation_editor.commit();

                    //Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    Intent intent = new Intent(LoginActivity.this, Tutorial.class);
                    startActivity(intent);
                    finish();
                }
                setResult(Activity.RESULT_OK);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), ip); // ?????????
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), ip); // ????????? ??????
            }
        });

        RegisterButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    private ThreadTask<Object> getThreadTask_social_sign_in(String email,String user_type, String Router_name){
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
                sendObject.put("user_type", user_type);

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

    //????????? ????????? ?????? ????????????
    public void makeThread(String apiURL, Map<String, String> requestHeaders){
        new ThreadTask<Object>() {
            String ApiURL = apiURL;
            Map<String, String> RequestHeaders = requestHeaders;

            @Override
            protected void onPreExecute() {// excute ??????

            }

            @Override
            protected void doInBackground(String... urls) {//background??? ????????????
                try {
                    //JSONObject??? ????????? key value ???????????? ?????? ???????????????.
                    HttpURLConnection con = null;
                    BufferedReader reader = null;
                    try{
                        //URL url = new URL("http://10.0.2.2:3000/post");
                        URL url = new URL(apiURL);
                        con = (HttpURLConnection) url.openConnection();

                        con.setRequestMethod("GET");//??????
                        for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                            con.setRequestProperty(header.getKey(), header.getValue());
                        }

                        int responseCode = con.getResponseCode();
                        //???????????? ????????? ?????????.
                        if (responseCode == HttpURLConnection.HTTP_OK) { // ?????? ??????
                            Log.e("Naver_login","?????? ?????????");
                            Naver_profile_ReadBody =  readBody(con.getInputStream());
                            JSONObject jsonObject = new JSONObject(Naver_profile_ReadBody);
                            JSONObject naver_object = jsonObject.getJSONObject("response");
                            // resonse ????????? ?????? ???????????? ?????? ????????? ????????????
                            Email = naver_object.getString("email");
                            sendProfile("naver", naver_object.getString("name"),
                                    naver_object.getString("age"), naver_object.getString("email"),
                                    naver_object.getString("gender"),naver_object.getString("birthday"));
                        } else { // ?????? ??????
                            Naver_profile_ReadBody = readBody(con.getErrorStream());
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    } finally {
                        if(con != null){
                            con.disconnect();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
        }.execute(apiURL);
    }

    /* naver ???????????? ??????*/
    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API ????????? ????????? ??????????????????.", e);
        }
    }

    private void sendProfile(String social_type, String name, String age, String email, String gender, String birthday) throws IOException, JSONException {
        /*????????? ????????? ?????? ????????? ?????????*/
        HttpURLConnection con = null;
        JSONObject sendObject = new JSONObject();
        BufferedReader reader = null;

        URL url = new URL(ip+"/"+social_type);
        Log.e("SendProfile : ", social_type);
        con = (HttpURLConnection) url.openConnection();

        sendObject.put("name", name);
        sendObject.put("age", age);
        sendObject.put("email",email);
        sendObject.put("gender", gender);
        sendObject.put("birthday", birthday);

        con.setRequestMethod("POST");//POST???????????? ??????
        con.setRequestProperty("Cache-Control", "no-cache");//?????? ??????
        con.setRequestProperty("Content-Type", "application/json");//application JSON ???????????? ??????

        con.setRequestProperty("Accept", "text/html");//????????? response ???????????? html??? ??????
        con.setDoOutput(true);//Outstream?????? post ???????????? ?????????????????? ??????
        con.setDoInput(true);//Inputstream?????? ??????????????? ????????? ???????????? ??????

        con.setConnectTimeout(3000);
        con.connect();
        Log.e("naver profile", "profile ?????? ?????? ?????????");

        OutputStream outStream = con.getOutputStream();
        //????????? ???????????? ??????
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
        writer.write(sendObject.toString());
        writer.flush();
        writer.close();//????????? ?????????
        Log.e("naver profile", "profile ?????? ?????? ?????????");
        //????????? ?????? ???????????? ??????
        InputStream stream = con.getInputStream();;
        ByteArrayOutputStream baos = null;

        reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder buffer = new StringBuilder();
        String line = "";
        while((line = reader.readLine()) != null){
            buffer.append(line);
        }
        Log.e("sns_?????? ?????????", buffer.toString());
        Log.e("naver profile", "profile ?????? ?????? ?????? ??????");
    }

    //????????????, ????????? social_sign_in
    private int sns_login_request(String request_email, String social_type){

        ThreadTask<Object> result = new ThreadTask<Object>() {
            String Request_email = request_email;
            String Social_type = social_type;
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

                URL url = new URL(urls[0] +"/social_sign_in");
                con = (HttpURLConnection) url.openConnection();

                sendObject.put("email",Request_email);

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

    private void updateUiWithUser(LoggedInUserView model) {
        //String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
      //  Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // ?????? ?????? ??????
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // ????????????|????????? ??????????????? ?????? ????????? ????????? SDK??? ??????
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
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
                Log.e("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
    //????????? ?????? ????????? ????????? ????????? ??? ??????.
    private void getAgree(){
        List<String> scopes = Arrays.asList("account_email", "profile", "gender", "age_range", "birthday");

        Session.getCurrentSession()
                .updateScopes(this, scopes, new AccessTokenCallback() {
                    @Override
                    public void onAccessTokenReceived(AccessToken accessToken) {
                        Log.e("KAKAO_SESSION", "????????? ???????????? ?????? ??????");

                        // ????????? scope??? ???????????? ????????? ????????? ???

                        // TODO: ????????? ?????? ?????? ?????? ????????????
                    }

                    @Override
                    public void onAccessTokenFailure(ErrorResult errorResult) {
                        Log.e("KAKAO_SESSION", "????????? ?????? ??????: " + errorResult);
                    }
                });
    }

    public class SessionCallback implements ISessionCallback { // ????????? ?????????
        private Context mContext;
        //private MeV2ResponseCallback responseCallback;

        private UserAccount kakaoAccount;
        private KakaoResponseCallback kakaoResponseCallback;

        public SessionCallback(Context mContext){
            this.mContext = mContext;
        }
        // ???????????? ????????? ??????
        @Override
        public void onSessionOpened() {
            //System.out.println("tlqkfktkqtkqktqk");
                if(OAuthLoginState.NEED_LOGIN.equals(OAuthLogin.getInstance().getState(mContext))) {
                    Log.e("Login Activitiy", "Naver login ???????????????.");
                    getAgree();
                    kakao_user_information_request();
            }
            //((LoginActivity) LoginActivity.loginContext).method1();
        }

        // ???????????? ????????? ??????
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
        }

        // ????????? ????????? ?????? ??????
        public void kakao_user_information_request() {
            kakaoResponseCallback = new KakaoResponseCallback();
            UserManagement.getInstance().me(kakaoResponseCallback);
        }

        public class KakaoResponseCallback extends MeV2ResponseCallback{
           // public String kakao_Email;
           // private UserAccount kakaoAccount;

            public UserAccount getKakaoAccount(){
                return kakaoAccount;
            }
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e("KAKAO_API", "????????? ?????? ??????: " + errorResult);
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e("KAKAO_API", "????????? ?????? ?????? ??????: " + errorResult);
            }

            @Override
            public void onSuccess(MeV2Response result) {
                Log.e("KAKAO_API", "????????? ?????????: " + result.getId());

                kakaoAccount = result.getKakaoAccount();
                System.out.println("kakaoAccount.emailNeedsAgreement()" + kakaoAccount.emailNeedsAgreement());

                if (kakaoAccount != null) {
                    kakao_Email = kakaoAccount.getEmail();
                    //setEmail(kakaoAccount.getEmail());
                    Log.e("KAKAO_API", "kakaAccount null ??????");
                    Log.e("KAKAO_API", kakao_Email);
                    if (kakao_Email != null) {
                        Log.e("KAKAO_API", "email: " + kakao_Email);
                    } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                        Log.e("kakao Profile", "email ?????? ??????");
                        // ?????? ?????? ??? ????????? ?????? ??????
                        // ???, ?????? ????????? ???????????? ????????? ????????? ?????? ???????????? ????????? ????????? ????????? ???????????? ???????????? ?????????.
                    } else {
                        // ????????? ?????? ??????
                        Log.e("kakao Profile", "email ?????? ??????");
                    }
                    // ?????????
                    Profile profile = kakaoAccount.getProfile();
                    if (profile != null) {
                        String age = kakaoAccount.getAgeRange().getValue();
                        String birthday = kakaoAccount.getBirthday();
                        String gender =kakaoAccount.getGender().getValue();
                        String name = kakaoAccount.getProfile().getNickname();

                        login_log_pref = getSharedPreferences("SNS_login_log", Activity.MODE_PRIVATE);
                        String first_kakao_login = login_log_pref.getString("first_kakao_login", "true");

                        login_log_editor = login_log_pref.edit();

                        ThreadTask<Object> social_sign_in_result = getThreadTask_social_sign_in(kakao_Email, "kakao","/social_sign_in");
                        social_sign_in_result.execute(ip);

                        if(social_sign_in_result.getResult() == 1){
                            //????????? ?????? ??????
                        }
                        else if(social_sign_in_result.getResult() == 2){
                            //???????????? ?????? ?????????
                            //????????? ?????? ??????

//                         Intent intent = new Intent(LoginActivity.this, additional_inform_register.class);
//                            intent.putExtra("email", kakao_Email);
//                            intent.putExtra("login_type","kakao");
//
//                            login_log_editor = login_log_pref.edit();
//                            login_log_editor.putString("first_kakao_login" , "false");
//                            login_log_editor.commit();
//                            Log.e("Kakao_login ?????? ", "tqtqtqtqtqtqtqtq");
//                            //intent.putExtra("pw", PasswordConfirmText.getText().toString());
//                            startActivity(intent);
//
                        if(first_kakao_login.equals("true")){
                            Intent intent = new Intent(LoginActivity.this, additional_inform_register.class);
                            intent.putExtra("email", Email);
                            intent.putExtra("login_type","kakao");

                            login_log_editor = login_log_pref.edit();
                            login_log_editor.putString("first_kakao_login" , "false");
                            login_log_editor.commit();
                            Log.e("Kakao_login ?????? ", "tqtqtqtqtqtqtqtq");
                            //intent.putExtra("pw", PasswordConfirmText.getText().toString());
                            startActivity(intent);
                         }
                        }
                        else if(social_sign_in_result.getResult() == 3){
                            //????????? ???????????? ??????
                            Toast.makeText(LoginActivity.this , "?????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();

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
                                        public void onSuccess(Long result) {// ????????? ???????????? ?????????
                                            Log.i("KAKAO_API", "?????? ?????? ??????. id: " + result);
                                        }
                                    });

//                            if(first_kakao_login.equals("true")){
//                                Intent intent = new Intent(LoginActivity.this, additional_inform_register.class);
//                                intent.putExtra("email",kakao_Email);
//                                intent.putExtra("login_type","kakao");
//
//                                login_log_editor = login_log_pref.edit();
//                                login_log_editor.putString("first_kakao_login" , "false");
//                                login_log_editor.commit();
//                                Log.e("Kakao_login ?????? ", "tqtqtqtqtqtqtqtq");
//                                //intent.putExtra("pw", PasswordConfirmText.getText().toString());
//                                startActivity(intent);
//                            }
//                            else{
//                                login_information_pref = getSharedPreferences("login_information", Activity.MODE_PRIVATE);
//                                login_infromation_editor = login_information_pref.edit();
//                                login_infromation_editor.putString("login_type" , "kakao");
//                                login_infromation_editor.putString("email" , kakao_Email);
//                                login_infromation_editor.commit();
//
//                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                startActivity(intent);//  ???????????? ?????????????????? ?????????
//                                finish();
//                            }
                        }
                        else if(social_sign_in_result.getResult() == 4){
                            //????????? ??????!
                        login_information_pref = getSharedPreferences("login_information", Activity.MODE_PRIVATE);
                        login_infromation_editor = login_information_pref.edit();
                        login_infromation_editor.putString("login_type" , "kakao");
                        login_infromation_editor.putString("email" , kakao_Email);
                        login_infromation_editor.commit();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);//  ???????????? ?????????????????? ?????????
                        finish();
                        }
                        else if(social_sign_in_result.getResult() == 0){
                            //????????? ??????
                        }

                    }
                    else{
                        Log.e("Login Activitiy", "Naver login ????????????.");
                    }

                }
                else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                        // ?????? ?????? ??? ????????? ?????? ?????? ??????

                    }
                else {
                        // ????????? ?????? ??????
                        Log.e("kakao Profile", "????????? ?????? ??????");
                    }
                }


            public String getEmail() {
                return kakao_Email;
            }

        }
    }



}