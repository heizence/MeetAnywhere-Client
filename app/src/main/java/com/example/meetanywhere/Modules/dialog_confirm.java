package com.example.meetanywhere.Modules;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.meetanywhere.R;

public class dialog_confirm {
    // activity 에서 dialog 열 때 호출하는 매서드
    public static void show(Context context,
                            String contents, Callee_Confirm param_calleeConfirm  // 확인 버튼 클릭 시 실행할 콜백 함수
    ) {
        dialogInstance dialog = new dialogInstance(context, contents, param_calleeConfirm);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    // 확인 버튼 dialog 생성하는 class
    static class dialogInstance extends Dialog {
        private String dialogContentsTxt;
        private TextView dialogContentsTextView;
        private TextView confirmBtn;

        private Caller_Confirm callerSelectConfirm;
        private Callee_Confirm calleeSelectConfirm;

        public dialogInstance(@NonNull Context context, String dialogContentsTxt, Callee_Confirm param_calleeConfirm) {
            super(context);
            this.dialogContentsTxt = dialogContentsTxt;

            this.callerSelectConfirm = new Caller_Confirm();
            this.calleeSelectConfirm = param_calleeConfirm;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_confirm);

            dialogContentsTextView = findViewById(R.id.dialogConfirmContents);
            confirmBtn = findViewById(R.id.dialogConfirmOkBtn);
            dialogContentsTextView.setText(dialogContentsTxt);

            confirmBtn.setOnClickListener(view -> {
                dismiss();
                if (calleeSelectConfirm != null) {
                    callerSelectConfirm.register(calleeSelectConfirm);
                }
            });
        }
    }

    // 콜백 함수 전달을 위한 interface(확인 버튼 선택)
    interface Callback_Confirm {
        void call();
    }

    // 호출을 하는 주체(확인 버튼 선택)
    static class Caller_Confirm {
        public void register(dialog_confirm_or_cancel.Callback_Confirm callback) {
            callback.call();
        }
    }

    // 요청 후 콜백 함수를 실행시키는 주체(확인 버튼 선택)
    public static class Callee_Confirm implements dialog_confirm_or_cancel.Callback_Confirm {
        public void call() {
        }
    }
}