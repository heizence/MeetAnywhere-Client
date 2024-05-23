package com.example.meetanywhere.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.example.meetanywhere.R;
import com.google.gson.Gson;

public class main_activity extends AppCompatActivity {
    private String screenName = "[ACTIVITY main_activity]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = this;
    public static Context contextOfApplication; // activity class 가 아닌 다른 class 에서 context 에 접근해야 할 때 사용.
    private SharedPreferences sharedPref;
    private String app_token;   // 앱에 저장된 jwt token

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag_check, "onCreate");
        setContentView(R.layout.screen_main_activity);

        contextOfApplication = getApplicationContext();
        sharedPref = context.getSharedPreferences(getString(R.string.sharedPreferenceMain), Context.MODE_PRIVATE);
        app_token = sharedPref.getString(getString(R.string.store_app_token), "");

        // 저장된 토큰이 있을 때
        if (app_token.length() != 0) {
            Intent intent = new Intent(context, signedin_main.class);
            Log.d(tag_check, "has token : " + app_token);
            startActivity(intent);
            finish();
        }
        else {
            Log.d(tag_check, "no token. show start page.");
        }


        // for test
        Intent intent = getIntent();
        Uri data = intent.getData();
        Log.d(tag_check, "check uri data : " + data);

        if (data != null) {
            String url = data.toString();
            Log.d(tag_check, "url : " + url);
        }
    }

    public void moveToJoinConference(View view) {
        Intent intent = new Intent(this, join_conference.class);
        intent.putExtra(getString(R.string.is_signedin), false);
        startActivity(intent);
    }

    public void moveToSignup(View view) {
        Intent intent = new Intent(this, signup_verify_email.class);
        startActivity(intent);
    }

    public void moveToSignin(View view) {
        Intent intent = new Intent(this, signin.class);
        startActivity(intent);
    }

    // activity class 가 아닌 다른 class 에서 context 에 접근해야 할 때 사용.
    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

    public void test(View view) {
        Log.d(tag_execute, "test");
        // 요청 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                Log.d(tag_check, "success. data : " + responseObj.data);
                Gson gson = new Gson();
            }
        }

        // 요청 실패 시 실행할 callback
        class Callee_failed extends httpRequestAPIs.Callee_failed {
            public void call(int statusCode) {
                System.out.println("## failed statusCode : " + statusCode);
            }
        }

        //httpRequestAPIs.getTest(new Callee_success(), new Callee_failed());
        httpRequestAPIs.postTest(new Callee_success(), new Callee_failed());
    }
}
