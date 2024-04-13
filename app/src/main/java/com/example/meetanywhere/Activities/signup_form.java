package com.example.meetanywhere.Activities;

import android.text.Editable;
import android.text.TextWatcher;
import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meetanywhere.Modules.Sha256_hash;
import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.google.android.material.textfield.TextInputEditText;
import com.example.meetanywhere.R;

public class signup_form extends AppCompatActivity {
    private String screenName = "[ACTIVITY]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = this;
    private View rootLayout;

    private TextInputEditText nameInput;
    private TextInputEditText passwordInput;
    private TextInputEditText passwordCheckInput;
    private Button signupBtn;

    private String name = "";
    private String password = "";
    private String passwordCheck = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_signup_form);

        // 바깥 영역 터치 시 EditText 키패드 내리기
        rootLayout = findViewById(R.id.signupFormLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (this.getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            return false;
        });

        nameInput = findViewById(R.id.signupFormNameInput);
        passwordInput = findViewById(R.id.signupFormPwInput);
        passwordCheckInput = findViewById(R.id.signupFormPwCheckInput);
        signupBtn = findViewById(R.id.signupFormBtn);

        nameInput.addTextChangedListener(nameWatcher);
        passwordInput.addTextChangedListener(passwordWatcher);
        passwordCheckInput.addTextChangedListener(passwordCheckWatcher);
    }

    public void signup(View view) {
        if (!password.equals(passwordCheck)) {
            dialog_confirm.show(this, "비밀번호가 일치하지 않습니다.", null);
        } else {
            String hashedPassword = Sha256_hash.hexString(password);

            // dialog_confirm 확인 버튼 터치 시 실행할 callback
            class Callee_Confirm extends dialog_confirm.Callee_Confirm {
                public void call() {
                    Intent intent = new Intent(context, main_activity.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }

            // 요청 성공 시 실행할 callback
            class Callee_success extends httpRequestAPIs.Callee_success {
                public void call(httpRequestAPIs.ResponseObject responseObj) {
                    Log.d(tag_check, "success. data : " + responseObj.data);

                    dialog_confirm.show(context, "가입이 완료되었습니다.", new Callee_Confirm());
                }
            }

            // 요청 실패 시 실행할 callback
            class Callee_failed extends httpRequestAPIs.Callee_failed {
                public void call(int statusCode) {
                    Log.d(tag_check, "failed statusCode : " + statusCode);

                    if (statusCode == 404) {
                        dialog_confirm.show(context, "잘못된 이메일 또는 비밀번호입니다.", null);
                    } else {
                        dialog_confirm.show(context, "요청 중 에러가 발생하였습니다. 잠시 후 다시 시도해 주세요.", null);
                    }
                }
            }
            String email = getIntent().getStringExtra("email");
            httpRequestAPIs.signup(name, email, hashedPassword, new Callee_success(), new Callee_failed());
        }
    }

    // 이름 watcher
    public final TextWatcher nameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            name = s.toString();
            if (name.length() != 0 && password.length() != 0 && passwordCheck.length() != 0) {
                signupBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_001));
                signupBtn.setEnabled(true);
            }
            else {
                signupBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray_001));
                signupBtn.setEnabled(false);
            }
        }
    };

    // 비밀번호 watcher
    public final TextWatcher passwordWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            password = s.toString();
            if (name.length() != 0 && password.length() != 0 && passwordCheck.length() != 0) {
                signupBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_001));
                signupBtn.setEnabled(true);
            }
            else {
                signupBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray_001));
                signupBtn.setEnabled(false);
            }
        }
    };

    // 비밀번호 확인 watcher
    public final TextWatcher passwordCheckWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            passwordCheck = s.toString();
            if (name.length() != 0 && password.length() != 0 && passwordCheck.length() != 0) {
                signupBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_001));
                signupBtn.setEnabled(true);
            }
            else {
                signupBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray_001));
                signupBtn.setEnabled(false);
            }
        }
    };
}
