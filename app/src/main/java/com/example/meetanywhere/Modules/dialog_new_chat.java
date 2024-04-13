package com.example.meetanywhere.Modules;

import android.view.ViewGroup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.meetanywhere.R;

public class dialog_new_chat {
    // activity 에서 dialog 열 때 호출하는 매서드
    public static void show(Context context,
                            String senderName, String senderProfileImg, String message
    ) {
        dialogInstance dialog = new dialogInstance(context, senderName, senderProfileImg, message);
        dialog.setCanceledOnTouchOutside(true);

        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent); // Set background to transparent
        window.setDimAmount(0); // outside background transparency

        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setGravity(Gravity.BOTTOM);


        dialog.show();
    }

    // 확인 버튼 dialog 생성하는 class
    static class dialogInstance extends Dialog {
        private String senderName;
        private String senderProfileImg;
        private String message;

        private View layoutView;
        private ImageView profileImgView;
        private TextView nameView;
        private TextView messageView;

        public dialogInstance(@NonNull Context context, String senderName, String senderProfileImg, String message) {
            super(context);
            this.senderName = senderName;
            this.senderProfileImg = senderProfileImg;
            this.message = message;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_new_chat);

            layoutView = findViewById(R.id.dialog_new_chat_layout);
            profileImgView = findViewById(R.id.dialog_new_chat_ProfileImg);
            nameView = findViewById(R.id.dialog_new_chat_username);
            messageView = findViewById(R.id.dialog_new_chat_txt);

            if (senderProfileImg != null) {
                byte[] decodedString = Base64.decode(senderProfileImg, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profileImgView.setImageBitmap(decodedBitmap);
            }

            nameView.setText(senderName);
            messageView.setText(message);

            // Add fade-in and fade-out animations
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setDuration(0); // 500 milliseconds
            fadeIn.setFillAfter(true);

            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setStartOffset(3000); // Start fading out after 2 seconds
            fadeOut.setDuration(300);
            fadeOut.setFillAfter(true);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Animation start (you can perform actions here if needed)
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Animation end - dismiss the dialog
                    dismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Animation repeat (if the animation is set to repeat)
                }
            });

            layoutView.startAnimation(fadeOut); // Apply fade-out animation
        }
    }
}