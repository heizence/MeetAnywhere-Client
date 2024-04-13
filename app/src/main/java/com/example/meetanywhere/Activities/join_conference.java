package com.example.meetanywhere.Activities;
import android.content.SharedPreferences;

import android.view.ViewGroup;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meetanywhere.Modules.Sha256_hash;
import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.example.meetanywhere.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputEditText;

public class join_conference extends AppCompatActivity {
    private String screenName = "[ACTIVITY join_conference]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = this;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

    private View rootLayout;
    private TextInputEditText conferenceIdInput;
    private TextInputEditText userNameInput;
    private FlexboxLayout joinConferenceTermsTxt;
    private Button joinBtn;

    private String conferenceId = "";
    private String userName = "";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_join_conference);

        sharedPref = getSharedPreferences(getString(R.string.sharedPreferenceMain), MODE_PRIVATE);

        // 바깥 영역 터치 시 EditText 키패드 내리기
        rootLayout = findViewById(R.id.joinConferenceLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (this.getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            return false;
        });
        conferenceIdInput = findViewById(R.id.conferenceIdInput);
        userNameInput = findViewById(R.id.userNameInput);
        joinConferenceTermsTxt = findViewById(R.id.joinConferenceTermsTxt);
        joinBtn = findViewById(R.id.joinConferenceBtn);

        // Textwatcher 등록
        conferenceIdInput.addTextChangedListener(conferenceIdTextWatcher);
        userNameInput.addTextChangedListener(userNameTextWatcher);

        boolean isSignedIn = getIntent().getBooleanExtra(getString(R.string.is_signedin), true);
        String userNameSaved = sharedPref.getString(getString(R.string.store_U_Name), "");

        if (!userNameSaved.equals("")) {
            userNameInput.setText(userNameSaved);
        }

        Log.d(tag_check, "isSignedIn : " + isSignedIn);

        // 로그인 된 상태에서 회의 참석 시 약관 안내 텍스트 부분 가려주기
        if (isSignedIn) {
            joinConferenceTermsTxt.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams layoutParams = joinConferenceTermsTxt.getLayoutParams();
            layoutParams.width = 0;
            layoutParams.height = 0;
            joinConferenceTermsTxt.setLayoutParams(layoutParams);
        }
    }

    // 서비스 약관 보여주기
    public void showServiceTerms(View view) {
    }

    // 개인정보 처리방침 보여주기
    public void showPolicy(View view) {
    }

    // 회의 참가하기
    public void join(View view) {
        Log.d(tag_check, "join");
        String hashedConferenceId = Sha256_hash.hexString(conferenceId);

        // 요청 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                Log.d(tag_check, "success. data : " + responseObj.data);

                //Intent intent = new Intent(context, conference_room_participant.class);
                Intent intent = new Intent(context, conference_room.class);    // for test
                intent.putExtra(getString(R.string.conferenceId), conferenceId); // Add a string
                intent.putExtra(getString(R.string.isHost), false);
                intent.putExtra(getString(R.string.userName), userName);
                startActivity(intent);
                finish();
            }
        }

        // 요청 실패 시 실행할 callback
        class Callee_failed extends httpRequestAPIs.Callee_failed {
            public void call(int statusCode) {
                System.out.println("## failed statusCode : " + statusCode);

                if (statusCode == 404) {
                    dialog_confirm.show(context, "잘못된 회의 ID 입니다.", null);
                } else {
                    dialog_confirm.show(context, "요청 중 에러가 발생하였습니다. 잠시 후 다시 시도해 주세요.", null);
                }
            }
        }
        httpRequestAPIs.checkConferenceId(hashedConferenceId, new Callee_success(), new Callee_failed());
    }

    // 회의 ID 입력 TextWatcher
    public final TextWatcher conferenceIdTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            conferenceId = s.toString();
            //Log.d(tag_check, "conferenceId : " + conferenceId);
            if (conferenceId.length() == 11 && !userName.equals("")) {
                joinBtn.setEnabled(true);
                joinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_001));
            } else {
                joinBtn.setEnabled(false);
                joinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray_001));
            }
        }
    };

    // 참가자 이름 입력 TextWatcher
    public final TextWatcher userNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            userName = s.toString();
            //Log.d(tag_check, "userName : " + userName);
            if (conferenceId.length() == 11 && !userName.equals("")) {
                joinBtn.setEnabled(true);
                joinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_001));
            } else {
                joinBtn.setEnabled(false);
                joinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray_001));
            }
        }
    };
}
