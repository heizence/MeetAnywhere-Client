package com.example.meetanywhere.Adapters;
import android.util.Log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.meetanywhere.R;

import java.util.ArrayList;
import java.util.List;

public class Adapter_conf_each_chat_message extends RecyclerView.Adapter<Adapter_conf_each_chat_message.ViewHolder> {
        private String screenName = "[ACTIVITY]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
        private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
        private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
        private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    public Context context;
    public String userSocketId; // 채팅내역에서 내가 쓴 채팅을 식별하기 위한 socket id.
    private FragmentManager fragmentManager;    // fragmentManager 가 필요가 있을 때만 사용.
    private ActivityResultLauncher<Intent> startForResult;  // activityResultLauncher 가 필요할 때만 사용

    // sharedPreference 를 다룰 필요가 있을 때 사용
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    public int selectedItemIndex;

    // 데이터 ArrayList
    private List<Part_conf_each_chat_message> data = new ArrayList<>();

    // adapter 생성
    public Adapter_conf_each_chat_message(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferenceMain), context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
    }

    // 채팅내역에서 내가 쓴 채팅을 식별하기 위한 socket id 등록
    public void setUserSocketId(String userSocketId) {
        this.userSocketId = userSocketId;
    }

    // activityResultLauncher 등록. activityResultLauncher 가 필요할 때만 사용
    public void setActivityResultLauncher(ActivityResultLauncher<Intent> startForResult) {
        this.startForResult = startForResult;
    }

    // 데이터 추가
    public void add(String id, String name, String profileImg, String chatMessage) {
        Part_conf_each_chat_message chatMessageObj = new Part_conf_each_chat_message(id, name, profileImg, chatMessage);
        data.add(chatMessageObj);
        this.notifyItemInserted(data.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImgView;
        private final TextView nameTxtView;
        private final TextView chatMessageTxtView;

        public ViewHolder(View view) {
            super(view);
            profileImgView = view.findViewById(R.id.part_conf_each_chat_message_ProfileImg);
            nameTxtView = view.findViewById(R.id.part_conf_each_chat_message_username);
            chatMessageTxtView = view.findViewById(R.id.part_conf_each_chat_message_txt);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.part_each_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String senderProfileImage = data.get(position).getProfileImg();

        Log.d(tag_check, "#message_onBindViewHolder sender name : " + data.get(position).getName());
        Log.d(tag_check, "#message_onBindViewHolder sender message : " + data.get(position).getChatMessage());
        Log.d(tag_check, "#message_onBindViewHolder sender profileImg : " + data.get(position).getProfileImg());
        Log.d(tag_check, "#message_onBindViewHolder sender profileImg2 : " + senderProfileImage);
        // 프로필 사진이 있는 경우에만 표시해 주기

        if (senderProfileImage != null && senderProfileImage.length() != 0) {
            Log.d(tag_check, "image not empty or null!");
            byte[] senderProfile_decodedString = Base64.decode(senderProfileImage, Base64.DEFAULT);
            Bitmap senderProfile_decodedBitmap = BitmapFactory.decodeByteArray(senderProfile_decodedString, 0, senderProfile_decodedString.length);
            Log.d(tag_check, "senderProfileImage : " + senderProfileImage);
            Log.d(tag_check, "senderProfile_decodedString : " + senderProfile_decodedString);
            Log.d(tag_check, "senderProfile_decodedBitmap : " + senderProfile_decodedBitmap);
            viewHolder.profileImgView.setImageBitmap(senderProfile_decodedBitmap);
        }
        // 프로필 misrendering 방지
        else {
            viewHolder.profileImgView.setImageBitmap(null);
        }

        if (data.get(position).getId().equals(userSocketId)) {
            viewHolder.nameTxtView.setText("나");
        }
        else {
            viewHolder.nameTxtView.setText(data.get(position).getName());
        }

        viewHolder.chatMessageTxtView.setText(data.get(position).getChatMessage());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
