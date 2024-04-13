package com.example.meetanywhere.Activities;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.example.meetanywhere.R;

public class signup_check_code extends AppCompatActivity {
    private String screenName = "[ACTIVITY]signup_check_code:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = this;
    private InputMethodManager inputManager;
    private View rootLayout;

    private EditText codeTxt1;
    private EditText codeTxt2;
    private EditText codeTxt3;
    private EditText codeTxt4;
    private EditText codeTxt5;
    private EditText codeTxt6;

    private TextView guideText;
    private TextView codeErrorText;
    private TextView resendCodeText;
    private String codeInput = "";
    private String receivedCode;
    private String userEmail;
    private boolean isCodeEntered = false;  // 인증코드가 완전히 입력되었는지 여부

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag_execute, "onCreate");
        setContentView(R.layout.screen_signup_check_code);

        inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);

        // 바깥 영역 터치 시 EditText 키패드 내리기
        rootLayout = findViewById(R.id.signupCheckCodeLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (this.getCurrentFocus() != null) {
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            return false;
        });

        setEachInputBlankFunction();

        guideText = findViewById(R.id.signupCheckCodeGuideText);
        codeErrorText = findViewById(R.id.signupCheckCodeErrorText);
        resendCodeText = findViewById(R.id.signupCheckCodeResendTxt);

        codeErrorText.setVisibility(View.INVISIBLE);

        receivedCode = getIntent().getStringExtra("code");
        userEmail = getIntent().getStringExtra("email");
        Log.d(tag_check, "receivedCode : " + receivedCode);
        Log.d(tag_check, "userEmail : " + userEmail);

        guideText.setText("이메일 주소 " + userEmail + " 에 전송된 인증 코드를 입력하세요.");

        resendCodeText.setOnClickListener(view -> {
                Log.d(tag_execute, "sendVerificationCode");
                // 요청 성공 시 실행할 callback
                class Callee_success extends httpRequestAPIs.Callee_success {
                    public void call(httpRequestAPIs.ResponseObject responseObj) {
                        Log.d(tag_check, "success. verification code : " + responseObj.data);
                        receivedCode = responseObj.data;
                        dialog_confirm.show(context,"새 인증코드가 전송되었습니다.", null);
                    }
                }

                // 요청 실패 시 실행할 callback
                class Callee_failed extends httpRequestAPIs.Callee_failed {
                    public void call(int statusCode) {
                        Log.d(tag_check, "failed. statusCode : " + statusCode);
                        dialog_confirm.show(context, "요청 중 에러가 발생하였습니다. 잠시 후 다시 시도해 주세요.",null);
                    }
                }
                httpRequestAPIs.sendVerificationCode(userEmail, new Callee_success(), new Callee_failed());
        });
    }

    // 인증코드 각 자리 입력 부분 설정
    public void setEachInputBlankFunction() {
        // findViewById
        codeTxt1 = findViewById(R.id.signupCheckCodeCodeTxt1);
        codeTxt2 = findViewById(R.id.signupCheckCodeCodeTxt2);
        codeTxt3 = findViewById(R.id.signupCheckCodeCodeTxt3);
        codeTxt4 = findViewById(R.id.signupCheckCodeCodeTxt4);
        codeTxt5 = findViewById(R.id.signupCheckCodeCodeTxt5);
        codeTxt6 = findViewById(R.id.signupCheckCodeCodeTxt6);

        // set input type
        codeTxt1.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeTxt2.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeTxt3.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeTxt4.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeTxt5.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeTxt6.setInputType(InputType.TYPE_CLASS_NUMBER);

        // set input value limits
        codeTxt1.setMaxLines(1);
        codeTxt2.setMaxLines(1);
        codeTxt3.setMaxLines(1);
        codeTxt4.setMaxLines(1);
        codeTxt5.setMaxLines(1);
        codeTxt6.setMaxLines(1);

        // register input change watcher
        codeTxt1.addTextChangedListener(new GenericTextWatcher(null, codeTxt1, codeTxt2));
        codeTxt2.addTextChangedListener(new GenericTextWatcher(codeTxt1, codeTxt2, codeTxt3));
        codeTxt3.addTextChangedListener(new GenericTextWatcher(codeTxt2, codeTxt3, codeTxt4));
        codeTxt4.addTextChangedListener(new GenericTextWatcher(codeTxt3, codeTxt4, codeTxt5));
        codeTxt5.addTextChangedListener(new GenericTextWatcher(codeTxt4, codeTxt5, codeTxt6));
        codeTxt6.addTextChangedListener(new GenericTextWatcher(codeTxt5, codeTxt6, null)); // The last one has no next EditText


        // register onTouchListener
        codeTxt1.setOnTouchListener((v, event) -> {
            inputManager.showSoftInput(rootLayout, InputMethodManager.SHOW_FORCED);
            return false;
        });
        codeTxt2.setOnTouchListener((v, event) -> {
            inputManager.showSoftInput(rootLayout, InputMethodManager.SHOW_FORCED);
            return false;
        });
        codeTxt3.setOnTouchListener((v, event) -> {
            inputManager.showSoftInput(rootLayout, InputMethodManager.SHOW_FORCED);
            return false;
        });
        codeTxt4.setOnTouchListener((v, event) -> {
            inputManager.showSoftInput(rootLayout, InputMethodManager.SHOW_FORCED);
            return false;
        });
        codeTxt5.setOnTouchListener((v, event) -> {
            inputManager.showSoftInput(rootLayout, InputMethodManager.SHOW_FORCED);
            return false;
        });
        codeTxt6.setOnTouchListener((v, event) -> {
            inputManager.showSoftInput(rootLayout, InputMethodManager.SHOW_FORCED);
            return false;
        });
    }

    // 인증번호 확인
    public void checkVerificationCode() {
        if (receivedCode.equals(codeInput)) {
            Intent intent = new Intent(this, signup_form.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
        } else {
            codeErrorText.setVisibility(View.VISIBLE);
            codeInput = "";

            codeTxt1.requestFocus();
            codeTxt1.setText("");
            codeTxt2.setText("");
            codeTxt3.setText("");
            codeTxt4.setText("");
            codeTxt5.setText("");
            codeTxt6.setText("");

            isCodeEntered = false;
        }
    }

    public class GenericTextWatcher implements TextWatcher {
        private final EditText previousEditText;
        private final EditText currentEditText;
        private final EditText nextEditText;

        public GenericTextWatcher(EditText previousEditText, EditText currentEditText, EditText nextEditText) {
            this.previousEditText = previousEditText;
            this.currentEditText = currentEditText;
            this.nextEditText = nextEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!isCodeEntered) {
                if (editable.length() == 1 && nextEditText != null) {
                    nextEditText.requestFocus();
                    codeInput += editable.toString();
                }
                // 인증코드 마지막 자리
                else if (nextEditText == null) {
                    codeInput += editable.toString();
                    isCodeEntered = true;
                    checkVerificationCode();
                }
                Log.d(tag_check, "codeInput : " + codeInput);
            }
        }
    }
}
