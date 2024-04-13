package com.example.meetanywhere.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.dialog_signup_terms;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.example.meetanywhere.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signup_verify_email extends AppCompatActivity {
    private String screenName = "[ACTIVITY]signup_verify_email:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = this;
    private View rootLayout;
    private TextInputEditText emailInput;
    private Button sendCodeBtn;
    private String email = "";

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag_execute, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_signup_verify_email);

        // 바깥 영역 터치 시 EditText 키패드 내리기
        rootLayout = findViewById(R.id.signupVerifyEmailLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (this.getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            return false;
        });
        emailInput = findViewById(R.id.signupVerifyEmailInput);
        sendCodeBtn = findViewById(R.id.signupVerifyEmailSendCodeBtn);

        // Textwatcher 등록
        emailInput.addTextChangedListener(emailTextWatcher);
        sendCodeBtn.setOnClickListener(view -> {
            sendVerificationCode();
        });

        openTermModal();
    }

    public void openTermModal() {
        // 취소 버튼 클릭 시 실행할 콜백 함수
        class Callee_Cancel extends dialog_signup_terms.Callee_Cancel {
            public void call() {
                finish();
                return;
            }
        }
        // 확인 버튼 클릭 시 실행할 콜백 함수
        class Callee_Confirm extends dialog_signup_terms.Callee_Confirm {
            public void call() {
                return;
            }
        }
        dialog_signup_terms.show(context, new Callee_Cancel(), new Callee_Confirm());
    }

    // for test
    public void sendVerificationCode2() {
        Intent intent = new Intent(context, signup_check_code.class);
        intent.putExtra("code", "123456");
        intent.putExtra("email", email);
        startActivity(intent);
    }

    // 인증번호 전송
    public void sendVerificationCode() {
        Log.d(tag_execute, "sendVerificationCode");

        // 요청 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                Log.d(tag_check, "success. verification code : " + responseObj.data);

                Intent intent = new Intent(context, signup_check_code.class);
                intent.putExtra("code", responseObj.data);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        }

        // 요청 실패 시 실행할 callback
        class Callee_failed extends httpRequestAPIs.Callee_failed {
            public void call(int statusCode) {
                Log.d(tag_check, "failed. statusCode : " + statusCode);
                dialog_confirm.show(context, "요청 중 에러가 발생하였습니다. 잠시 후 다시 시도해 주세요.", null);
            }
        }
        httpRequestAPIs.sendVerificationCode(email, new Callee_success(), new Callee_failed());
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
                emailInput.setText(email);
                emailInput.setSelection(email.length());
                Toast toast = Toast.makeText(getBaseContext(), "특수문자는 사용할 수 없습니다", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                email = s.toString();
                String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
                Pattern pattern = Pattern.compile(regexPattern);
                Matcher matcher = pattern.matcher(email);

                if (matcher.matches()) {
                    sendCodeBtn.setEnabled(true);
                    sendCodeBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_001));
                } else {
                    sendCodeBtn.setEnabled(false);
                    sendCodeBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray_001));
                }
            }
        }
    };
}
