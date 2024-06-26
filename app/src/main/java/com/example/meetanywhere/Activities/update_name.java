package com.example.meetanywhere.Activities;

import android.content.SharedPreferences;

import android.text.Editable;
import android.text.TextWatcher;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.google.android.material.textfield.TextInputEditText;
import com.example.meetanywhere.R;

public class update_name extends AppCompatActivity {
    private String screenName = "[ACTIVITY]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = this;
    private View rootLayout;
    private TextInputEditText nameInput;
    private Button editNameBtn;
    private String name = "";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_update_name);

        // 필요한 부분에 선언
        sharedPref = getSharedPreferences(getString(R.string.sharedPreferenceMain), MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        // 바깥 영역 터치 시 EditText 키패드 내리기
        rootLayout = findViewById(R.id.editNameLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (this.getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            return false;
        });

        nameInput = findViewById(R.id.editNameInput);
        editNameBtn = findViewById(R.id.editNameBtn);
        nameInput.addTextChangedListener(textWatcher);
    }

    public void editName(View view) {
        // dialog_confirm 확인 버튼 터치 시 실행할 callback
        class Callee_Confirm extends dialog_confirm.Callee_Confirm {
            public void call() {
                finish();
            }
        }

        // 요청 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                Log.d(tag_check, "success. data : " + responseObj.data);
                sharedPrefEditor.putString(getString(R.string.store_U_Name), name);
                sharedPrefEditor.apply();
                dialog_confirm.show(context, "변경되었습니다.", new Callee_Confirm());
            }
        }

        // 요청 실패 시 실행할 callback
        class Callee_failed extends httpRequestAPIs.Callee_failed {
            public void call(int statusCode) {
                Log.d(tag_check, "failed statusCode : " + statusCode);
                dialog_confirm.show(context, "요청 중 에러가 발생하였습니다. 잠시 후 다시 시도해 주세요.", null);
            }
        }
        String userId = sharedPref.getString(getString(R.string.store_U_Id), "");
        httpRequestAPIs.updateName(userId, name, new Callee_success(), new Callee_failed());
    }

    public final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            name = s.toString();

            if (name.length() != 0) {
                editNameBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_001));
                editNameBtn.setEnabled(true);
            } else {
                editNameBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray_001));
                editNameBtn.setEnabled(false);
            }
        }
    };
}
