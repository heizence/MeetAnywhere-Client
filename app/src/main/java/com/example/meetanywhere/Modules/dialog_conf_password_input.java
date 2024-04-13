package com.example.meetanywhere.Modules;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.meetanywhere.Activities.signedin_main;
import com.example.meetanywhere.Activities.signin;
import com.example.meetanywhere.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

public class dialog_conf_password_input {
    private static String screenName = "[Dialog]dialog_conf_password_input:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private static String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private static String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private static String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    // activity 에서 dialog 열 때 호출하는 매서드
    public static void show(Context context,
                            String conferenceId,    // 회의 ID
                            Callee_Cancel param_calleeCancel,   // 취소 버튼 클릭 시 실행할 콜백 함수
                            Callee_Confirm param_calleeConfirm  // 확인 버튼 클릭 시 실행할 콜백 함수
    ) {
        dialogInstance dialog = new dialogInstance(context, conferenceId, param_calleeCancel, param_calleeConfirm);
        dialog.show();
    }

    // 확인 버튼 dialog 생성하는 class
    static class dialogInstance extends Dialog {
        private TextView headerTextView;
        private TextInputEditText passwordInput;
        private TextView selectCancelBtn;
        private TextView selectConfirmBtn;

        private String conferenceId;
        private String conferencePassword;

        // 콜백 함수 사용을 위한 변수 정의
        private Caller_Cancel callerSelectCancel;
        private Callee_Cancel calleeSelectCancel;

        private Caller_Confirm callerSelectConfirm;
        private Callee_Confirm calleeSelectConfirm;

        public dialogInstance(@NonNull Context context, String conferenceId, Callee_Cancel param_calleeCancel, Callee_Confirm param_calleeConfirm) {
            super(context);
            this.conferenceId = conferenceId;
            this.callerSelectCancel = new Caller_Cancel();
            this.calleeSelectCancel = param_calleeCancel;

            this.callerSelectConfirm = new Caller_Confirm();
            this.calleeSelectConfirm = param_calleeConfirm;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_conf_password_input);

            headerTextView = findViewById(R.id.dialogConfPassword_header);
            passwordInput = findViewById(R.id.dialogConfPassword_passwordInput);
            passwordInput.addTextChangedListener(textWatcher);   // onCreate 내부에 설정해 주기

            selectCancelBtn = findViewById(R.id.dialogConfPassword_CancelBtn);
            selectConfirmBtn = findViewById(R.id.dialogConfPassword_ConfirmBtn);

            // 취소 버튼 선택 시 콜백 함수 실행
            selectCancelBtn.setOnClickListener(view -> {
                Log.d(tag_check, "selectCancelBtn onclick");
                callerSelectCancel.register(calleeSelectCancel);
                dismiss();
            });

            // 확인 버튼 선택 시 회의 비밀번호 확인하기
            selectConfirmBtn.setOnClickListener(view -> {
                Log.d(tag_check, "selectConfirmBtn onclick");
                checkPassword();
            });
        }

        // 회의 비밀번호 확인 요청
        public void checkPassword() {
            Log.d(tag_check, "checkPassword");
            String hashedConferenceId = Sha256_hash.hexString(conferenceId);
            String hashedPassword = Sha256_hash.hexString(conferencePassword);

            Log.d(tag_check, "hashedConferenceId : " + hashedConferenceId);
            Log.d(tag_check, "hashedPassword : " + hashedPassword);

            // 요청 성공 시 실행할 callback
            class Callee_success extends httpRequestAPIs.Callee_success {
                public void call(httpRequestAPIs.ResponseObject responseObj) {
                    Log.d(tag_check, "success. data : " + responseObj.data);

                    // 확인 후 activity 에서 전달받은 콜백 함수 실행
                    callerSelectConfirm.register(calleeSelectConfirm);
                    dismiss();
                }
            }

            // 요청 실패 시 실행할 callback
            class Callee_failed extends httpRequestAPIs.Callee_failed {
                public void call(int statusCode) {
                    System.out.println("## failed statusCode : " + statusCode);

                    if (statusCode == 404) {
                        headerTextView.setText("회의 암호가 잘못되었습니다.");
                    } else {
                        dialog_confirm.show(getContext(), "요청 중 에러가 발생하였습니다. 잠시 후 다시 시도해 주세요.", null);
                    }
                }
            }
            httpRequestAPIs.checkConferencePassword(hashedConferenceId, hashedPassword, new Callee_success(), new Callee_failed());
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
                conferencePassword = s.toString();
                //Log.d(tag_check, "conferencePassword : " + conferencePassword);
                if (conferencePassword.length() == 6) {
                    selectConfirmBtn.setClickable(true);
                    selectConfirmBtn.setTextColor(getContext().getColor(R.color.black));
                } else {
                    selectConfirmBtn.setClickable(false);
                    selectConfirmBtn.setTextColor(getContext().getColor(R.color.gray_002));
                }
            }
        };
    }


    // 콜백 함수 전달을 위한 interface(취소 버튼 선택)
    interface Callback_Cancel {
        void call();
    }

    // 콜백 함수 전달을 위한 interface(확인 버튼 선택)
    interface Callback_Confirm {
        void call();
    }

    // 호출을 하는 주체(취소 버튼 선택)
    static class Caller_Cancel {
        public void register(Callback_Cancel callback) {
            callback.call();
        }
    }

    // 호출을 하는 주체(확인 버튼 선택)
    static class Caller_Confirm {
        public void register(Callback_Confirm callback) {
            callback.call();
        }
    }

    // 요청 후 콜백 함수를 실행시키는 주체(취소 버튼 선택)
    public static class Callee_Cancel implements Callback_Cancel {
        public void call() {
        }
    }

    // 요청 후 콜백 함수를 실행시키는 주체(확인 버튼 선택)
    public static class Callee_Confirm implements Callback_Confirm {
        public void call() {
        }
    }
}
