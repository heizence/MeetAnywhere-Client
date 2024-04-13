package com.example.meetanywhere.Activities;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.dialog_delete_account;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.example.meetanywhere.R;

public class delete_account extends AppCompatActivity {
    private String screenName = "[ACTIVITY]deleteAccount:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = this;
    private SharedPreferences sharedPref;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag_execute, "onCreate");
        sharedPref = getSharedPreferences(getString(R.string.sharedPreferenceMain), MODE_PRIVATE);
        setContentView(R.layout.screen_delete_account);
    }

    public void openDeleteAccountModal(View view) {
        // 취소 버튼 클릭 시 실행할 콜백 함수
        class Callee_Cancel extends dialog_delete_account.Callee_Cancel {
            public void call() { return; }
        }
        // 확인 버튼 클릭 시 실행할 콜백 함수
        class Callee_Confirm extends dialog_delete_account.Callee_Confirm {
            public void call() {
                Log.d(tag_check, "openDeleteAccountModal call");
                deleteAccount();
                return;
            }
        }
        dialog_delete_account.show(context, new Callee_Cancel(), new Callee_Confirm());
    }

    // 계정 삭제
    public void deleteAccount() {
        Log.d(tag_execute, "deleteAccount");
        // 확인 버튼 클릭 시 실행할 콜백 함수
        class Callee_Confirm extends dialog_confirm.Callee_Confirm {
            public void call() {
                Context context = delete_account.this;
                SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.sharedPreferenceMain), Context.MODE_PRIVATE);
                SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
                sharedPrefEditor.putString(getString(R.string.store_app_token), "");
                sharedPrefEditor.putString(getString(R.string.store_U_Id), "");
                sharedPrefEditor.putString(getString(R.string.store_U_Email), "");
                sharedPrefEditor.putString(getString(R.string.store_U_Name), "");
                sharedPrefEditor.putString(getString(R.string.store_U_ProfileImg), "");
                sharedPrefEditor.apply();

                Intent intent = new Intent(context, main_activity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
        }

        // 요청 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                Log.d(tag_check, "success. data : " + responseObj.data);
                dialog_confirm.show(context, "삭제되었습니다.", new Callee_Confirm());
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

        String userId = sharedPref.getString(getString(R.string.store_U_Id), "");
        httpRequestAPIs.deleteAccount(userId, new Callee_success(), new Callee_failed());
    }
}
