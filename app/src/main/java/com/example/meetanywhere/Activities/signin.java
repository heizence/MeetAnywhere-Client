package com.example.meetanywhere.Activities;

import android.util.Log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meetanywhere.Modules.Sha256_hash;
import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.example.meetanywhere.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signin extends AppCompatActivity {
    private String screenName = "[ACTIVITY signin]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = this;
    private View rootLayout;
    private TextInputEditText signinEmailInput;
    private TextInputEditText signinPwInput;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

    private String email = "";
    private String password = "";

    private int countdownToTestActivity = 2;    // for test.

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_signin);

        // 바깥 영역 터치 시 EditText 키패드 내리기
        rootLayout = findViewById(R.id.signinLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (this.getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            return false;
        });

        sharedPref = getSharedPreferences(getString(R.string.sharedPreferenceMain), MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        signinEmailInput = findViewById(R.id.signinEmailInput);
        signinPwInput = findViewById(R.id.signinPwInput);

        // Textwatcher 등록
        signinEmailInput.addTextChangedListener(emailTextWatcher);
        signinPwInput.addTextChangedListener(passwordTextWatcher);
    }

    // 원생 회원정보 데이터
    public class UserInfo {
        public String token;    // JWT 토큰
        public String U_Id;    // 회원 데이터 고유 id
        public String U_Email; // 이메일
        public String U_Name;  // 이름
        public String U_ProfileImg;    // 프로필 이미지
    }

    // 로그인 요청
    public void signin(View view) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(email);

        if (email.length() == 0 || !matcher.matches()) {
            dialog_confirm.show(this, "올바른 아이디 형식을 입력해 주세요.", null);
        } else if (password.length() == 0) {
            dialog_confirm.show(this, "비밀번호를 입력해 주세요", null);
        } else {
            checkUserEmail();
        }
    }

    // 로그인을 위한 이메일(아이디) 확인
    public void checkUserEmail() {
        String hashedPassword = Sha256_hash.hexString(password);

        // 요청 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                Log.d(tag_check, "success. data : " + responseObj.data);

                Gson gson = new Gson();
                UserInfo userInfo = gson.fromJson(responseObj.data, UserInfo.class);

                // 원생 회원정보 저장
                sharedPrefEditor.putString(getString(R.string.store_app_token), userInfo.token);
                sharedPrefEditor.putString(getString(R.string.store_U_Id), userInfo.U_Id);
                sharedPrefEditor.putString(getString(R.string.store_U_Email), userInfo.U_Email);
                sharedPrefEditor.putString(getString(R.string.store_U_Name), userInfo.U_Name);
                sharedPrefEditor.putString(getString(R.string.store_U_ProfileImg), userInfo.U_ProfileImg);
                sharedPrefEditor.apply();

                Intent intent = new Intent(context, signedin_main.class);
                startActivity(intent);
                finish();
            }
        }

        // 요청 실패 시 실행할 callback
        class Callee_failed extends httpRequestAPIs.Callee_failed {
            public void call(int statusCode) {
                System.out.println("## failed statusCode : " + statusCode);

                if (statusCode == 404) {
                    dialog_confirm.show(context, "잘못된 이메일 또는 비밀번호입니다.", null);
                } else {
                    dialog_confirm.show(context, "요청 중 에러가 발생하였습니다. 잠시 후 다시 시도해 주세요.", null);
                }
            }
        }

        httpRequestAPIs.signin(email, hashedPassword, new Callee_success(), new Callee_failed());
    }

    // 비밀번호 재발급 화면으로 이동
    public void moveToReissuePassword(View view) {
        Intent intent = new Intent(this, reissue_password.class);
        startActivity(intent);
    }

    // 이메일 입력 TextWatcher
    public final TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            Pattern regex = Pattern.compile("[$&+,:;=\\\\?#|/'<>^*()%!-]");

            if (regex.matcher(s.toString()).find()) {
                signinEmailInput.setText(email);
                signinEmailInput.setSelection(email.length());
                Toast toast = Toast.makeText(getBaseContext(), "특수문자는 사용할 수 없습니다", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                email = s.toString();
            }
        }
    };

    // 비밀번호 입력 TextWatcher
    public final TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            password = s.toString();
        }
    };

    // test activity 로 이동
    public void moveToTestActivity(View view) {
        if (countdownToTestActivity <= 0) {
            //Intent intent = new Intent(this, test_main.class);
            //Intent intent = new Intent(this, test_capture.class);
            //startActivity(intent);
            countdownToTestActivity = 2;
        } else {
            countdownToTestActivity -= 1;
        }
    }
}
