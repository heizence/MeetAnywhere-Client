package com.example.meetanywhere.Modules;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.meetanywhere.R;

public class dialog_delete_account {
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
        private TextView selectCancelBtn;
        private TextView selectConfirmBtn;

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
            setContentView(R.layout.dialog_delete_account);

            selectCancelBtn = findViewById(R.id.dialogDeleteAccount_Contents_cancelBtn);
            selectConfirmBtn = findViewById(R.id.dialogDeleteAccount_Contents_confirmBtn);

            // 취소 버튼 터치 시 콜백 함수 실행
            selectCancelBtn.setOnClickListener(view -> {
                dismiss();
            });

            // 모두 버튼 터치 시 실행할 매서드 등록
            selectConfirmBtn.setOnClickListener(view -> {
                callerConfirm.register(calleeConfirm);
                dismiss();
            });
        }
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
