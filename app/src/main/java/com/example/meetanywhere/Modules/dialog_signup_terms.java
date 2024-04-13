package com.example.meetanywhere.Modules;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.meetanywhere.R;

public class dialog_signup_terms {
    // activity 에서 dialog 열 때 호출하는 매서드
    public static void show(Context context,
                            Callee_Cancel param_calleeCancel,   // 취소 버튼 클릭 시 실행할 콜백 함수
                            Callee_Confirm param_calleeConfirm  // 확인 버튼 클릭 시 실행할 콜백 함수
    ) {
        dialogInstance dialog = new dialogInstance(context, param_calleeCancel, param_calleeConfirm);
        dialog.show();
    }

    // dialog 생성하는 class
    static class dialogInstance extends Dialog {
        private CheckBox checkbox1; // 개인정보 처리방침 및 서비스 약관 동의 여부
        private CheckBox checkbox2; // 데이터 수집 및 사용 동의 여부

        private boolean isChecked1 = false;
        private boolean isChecked2 = false;

        private TextView selectCancelBtn;
        private TextView selectAllBtn;

        // 콜백 함수 사용을 위한 변수 정의
        private Caller_Cancel callerSelectCancel;
        private Callee_Cancel calleeSelectCancel;

        private Caller_Confirm callerConfirm;
        private Callee_Confirm calleeConfirm;

        public dialogInstance(@NonNull Context context, Callee_Cancel param_calleeCancel, Callee_Confirm param_calleeConfirm) {
            super(context);

            this.callerSelectCancel = new Caller_Cancel();
            this.calleeSelectCancel = param_calleeCancel;

            this.callerConfirm = new Caller_Confirm();
            this.calleeConfirm = param_calleeConfirm;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_signup_terms);

            checkbox1 = findViewById(R.id.dialogSignupTerms_Contents_checkbox1);
            checkbox2 = findViewById(R.id.dialogSignupTerms_Contents_checkbox2);
            selectCancelBtn = findViewById(R.id.dialogSignupTerms_Contents_cancelBtn);
            selectAllBtn = findViewById(R.id.dialogSignupTerms_Contents_selectAllBtn);

            // 각 동의 체크박스 버튼 선택 시 콜백 함수 실행
            checkbox1.setOnClickListener(view -> {
                onCheckboxClicked(view);
            });

            // 취소 버튼 선택 시 콜백 함수 실행
            checkbox2.setOnClickListener(view -> {
                onCheckboxClicked(view);
            });

            // 취소 버튼 터치 시 콜백 함수 실행
            selectCancelBtn.setOnClickListener(view -> {
                callerSelectCancel.register(calleeSelectCancel);
                dismiss();
            });

            // 모두 버튼 터치 시 실행할 매서드 등록
            selectAllBtn.setOnClickListener(view -> {
                selectAll();
            });
        }

        public void onCheckboxClicked(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            switch (view.getId()) {
                case R.id.dialogSignupTerms_Contents_checkbox1:
                    isChecked1 = checked;
                    checkIsAllChecked();
                    break;
                case R.id.dialogSignupTerms_Contents_checkbox2:
                    isChecked2 = checked;
                    checkIsAllChecked();
                    break;
                default:
                    break;
            }
        }

        // 체크박스가 모두 선택되었는지 체크하기. 경우에 따라 텍스트 및 onclick listener 다르게 설정
        public void checkIsAllChecked() {
            if (isChecked1 && isChecked2) {
                selectAllBtn.setText("확인");
                selectAllBtn.setOnClickListener(view -> {
                    callerConfirm.register(calleeConfirm);
                    dismiss();
                });
            }
            else {
                selectAllBtn.setText("모두 선택");
                selectAllBtn.setOnClickListener(view -> {
                    selectAll();
                });
            }
        }

        // 모두 선택하기
        public void selectAll() {
            if (isChecked1 && isChecked2) {
                isChecked1 = false;
                isChecked2 = false;
                checkbox1.setChecked(false);
                checkbox2.setChecked(false);
                selectAllBtn.setText("모두 선택");
                selectAllBtn.setOnClickListener(view -> {
                    selectAll();
                });
            }
            else {
                isChecked1 = true;
                isChecked2 = true;
                checkbox1.setChecked(true);
                checkbox2.setChecked(true);
                selectAllBtn.setText("확인");
                selectAllBtn.setOnClickListener(view -> {
                    callerConfirm.register(calleeConfirm);
                    dismiss();
                });
            }
        }

        public void showTermsOrPolicy1(View view) {}
        public void showTermsOrPolicy2(View view) {}
        public void showTermsOrPolicy3() {}
        public void showTermsOrPolicy4() {}
    }

    // 콜백 함수 전달을 위한 interface(취소 버튼)
    interface Callback_Cancel {
        void call();
    }

    // 콜백 함수 전달을 위한 interface(확인 버튼)
    interface Callback_Confirm {
        void call();
    }

    // 호출을 하는 주체(취소 버튼)
    static class Caller_Cancel {
        public void register(Callback_Cancel callback) {
            callback.call();
        }
    }

    // 호출을 하는 주체(확인 버튼)
    static class Caller_Confirm {
        public void register(Callee_Confirm callback) {
            callback.call();
        }
    }

    // 요청 후 콜백 함수를 실행시키는 주체(취소 버튼)
    public static class Callee_Cancel implements Callback_Cancel {
        public void call() {
        }
    }

    // 요청 후 콜백 함수를 실행시키는 주체(확인 버튼)
    public static class Callee_Confirm implements Callback_Confirm {
        public void call() {
        }
    }
}
