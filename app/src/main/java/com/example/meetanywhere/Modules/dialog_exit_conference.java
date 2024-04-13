package com.example.meetanywhere.Modules;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.example.meetanywhere.R;

public class dialog_exit_conference {
        private static String screenName = "[ACTIVITY]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
        private static String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
        private static String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
        private static String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    // activity 에서 dialog 열 때 호출하는 매서드
    public static void show(Context context,
                            Callee_ExitForAll param_calleeCancel,   // 모두에 대해 회의 종료 버튼 클릭 시 실행할 콜백 함수
                            Callee_Exit param_calleeConfirm  // 회의 나가기 버튼 클릭 시 실행할 콜백 함수
    ) {
        dialogInstance dialog = new dialogInstance(context, param_calleeCancel, param_calleeConfirm);
        dialog.show();
    }

    // 확인 버튼 dialog 생성하는 class
    static class dialogInstance extends Dialog {
        private View buttonLayout;
        private TextView exitForAllBtn; // 모두에 대해 회의 종료 버튼
        private TextView exitBtn;   // 회의 나가기 버튼

        private int screenWidth;

        // 콜백 함수 사용을 위한 변수 정의
        private Caller_ExitForAll callerSelectExitForAll;
        private Callee_ExitForAll calleeSelectExitForAll;

        private Caller_Exit callerSelectExit;
        private Callee_Exit calleeSelectExit;

        public dialogInstance(@NonNull Context context, Callee_ExitForAll param_calleeCancel, Callee_Exit param_calleeConfirm) {
            super(context);
            this.screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            this.callerSelectExitForAll = new Caller_ExitForAll();
            this.calleeSelectExitForAll = param_calleeCancel;

            this.callerSelectExit = new Caller_Exit();
            this.calleeSelectExit = param_calleeConfirm;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_exit_conference);

            buttonLayout = findViewById(R.id.dialogExitConference_exitBtnLayout);
            ViewGroup.LayoutParams layoutParams = buttonLayout.getLayoutParams();
            layoutParams.width = this.screenWidth - 100;
            buttonLayout.setLayoutParams(layoutParams);

            exitForAllBtn = findViewById(R.id.dialogExitConference_exitForAllBtn);
            exitBtn = findViewById(R.id.dialogExitConference_exitBtn);

            // 모두에 대해 회의 종료 버튼 선택 시 콜백 함수 실행
            exitForAllBtn.setOnClickListener(view -> {
                callerSelectExitForAll.register(calleeSelectExitForAll);
                dismiss();
            });

            // 회의 나가기 버튼 선택 시 콜백 함수 실행
            exitBtn.setOnClickListener(view -> {
                callerSelectExit.register(calleeSelectExit);
                dismiss();
            });

        }
    }

    // 콜백 함수 전달을 위한 interface(모두에 대해 회의 종료 버튼 선택)
    interface Callback_ExitForAll{
        void call();
    }

    // 콜백 함수 전달을 위한 interface(회의 나가기 버튼 선택)
    interface Callback_Exit {
        void call();
    }

    // 호출을 하는 주체(모두에 대해 회의 종료 버튼 선택)
    static class Caller_ExitForAll{
        public void register(Callback_ExitForAll callback) {
            callback.call();
        }
    }

    // 호출을 하는 주체(회의 나가기 버튼 선택)
    static class Caller_Exit{
        public void register(Callback_Exit callback) {
            callback.call();
        }
    }

    // 요청 후 콜백 함수를 실행시키는 주체(모두에 대해 회의 종료 버튼 선택)
    public static class Callee_ExitForAll implements Callback_ExitForAll {
        public void call() {}
    }

    // 요청 후 콜백 함수를 실행시키는 주체(회의 나가기 버튼 선택)
    public static class Callee_Exit implements Callback_Exit {
        public void call() {}
    }
}
