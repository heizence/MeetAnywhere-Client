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
import java.util.Iterator;
import java.util.List;

public class Adapter_conf_each_participants extends RecyclerView.Adapter<Adapter_conf_each_participants.ViewHolder> {
    private String screenName = "[Adapter_conf_each_participants]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용
    public Context context;

    // sharedPreference 를 다룰 필요가 있을 때 사용
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

    public int selectedItemIndex;

    // 데이터 ArrayList
    public static List<Part_conf_each_participant> data = new ArrayList<>();

    // adapter 생성
    public Adapter_conf_each_participants(Context context) {
        Log.d(tag_execute, "create adapter");
        this.context = context;
        sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferenceMain), context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        Log.d(tag_check, "check list size : " + data.size());
    }


    // 데이터 추가
    public void add(Part_conf_each_participant object) {
        Log.d(tag_execute, "add");
        data.add(object);
        this.notifyItemInserted(data.size());
    }

    // 호스트 변경
    public void assignNewHost(String participantId) {
        Log.d(tag_execute, "assignNewHost : " + participantId);
        Iterator<Part_conf_each_participant> iterator = data.iterator();
        int position = -1;
        while (iterator.hasNext()) {
            Part_conf_each_participant participantObj = iterator.next();
            if (participantObj.getId().equals(participantId)) {
                Log.d(tag_check, "found participant : " + participantObj);
                participantObj.assignHost();
                Log.d(tag_check, "check isHost after change : " + participantObj.getIsHost());
            }
        }
        if (position != -1) {
            this.notifyItemChanged(position);
        }
    }

    // 마이크 상태 수정
    public void editMicStatus(String participantId, boolean micStatus) {
        Log.d(tag_execute, "editMicStatus : " + participantId);
        Log.d(tag_check, "micStatus : " + micStatus);
        // Use an Iterator to find and edit the element
        Iterator<Part_conf_each_participant> iterator = data.iterator();
        int position = -1;
        while (iterator.hasNext()) {
            Part_conf_each_participant participantObj = iterator.next();
            if (participantObj.getId().equals(participantId)) {
                position = data.indexOf(participantObj);
                Log.d(tag_check, "found item. position : " + position);
                data.get(position).setIsMicOn(micStatus);
            }
        }
        if (position != -1) {
            this.notifyItemChanged(position);
        }
    }

    // 비디오 상태 수정
    public void editVideoStatus(String participantId, boolean videoStatus) {
        Log.d(tag_execute, "editVideoStatus : " + participantId);
        Log.d(tag_check, "videoStatus : " + videoStatus);
        // Use an Iterator to find and edit the element
        Iterator<Part_conf_each_participant> iterator = data.iterator();
        int position = -1;
        while (iterator.hasNext()) {
            Part_conf_each_participant participantObj = iterator.next();
            if (participantObj.getId().equals(participantId)) {
                position = data.indexOf(participantObj);
                Log.d(tag_check, "found item. position : " + position);
                data.get(position).setIsVideoOn(videoStatus);
            }
        }
        if (position != -1) {
            Adapter_conf_each_participants.this.notifyItemChanged(position);
        }
    }

    // 데이터 삭제
    public void deleteItem(String participantId) {
        Log.d(tag_execute, "deleteItem : " + participantId);
        // Use an Iterator to find and remove the element
        Iterator<Part_conf_each_participant> iterator = data.iterator();
        int position = -1;
        while (iterator.hasNext()) {
            Part_conf_each_participant participantObj = iterator.next();
            if (participantObj.getId().equals(participantId)) {
                position = data.indexOf(participantObj);
                iterator.remove(); // Removes the current element
            }
        }
        if (position != -1) {
            Adapter_conf_each_participants.this.notifyItemRemoved(position);
        }
    }

    // 호스트가 회의 종료 시 모든 데이터 삭제
    public void deleteAllItem() {
        Log.d(tag_check, "deleteAllItem");
        data.clear();
        this.notifyDataSetChanged();
    }

    // 데이터 리랜더링
    public void reRenderData(List<Part_conf_each_participant> newData) {
        Log.d(tag_execute, "reRenderData : " + newData);
        data.clear();
        Log.d(tag_check, "check data : " + this.data);
        Log.d(tag_check, "check data size : " + this.data.size());
        data.addAll(newData);
        Log.d(tag_check, "check data after add all: " + this.data);
        Log.d(tag_check, "check data size after add all : " + this.data.size());
        this.notifyDataSetChanged();
    }

    public static Part_conf_each_participant getParticipant(String participantId) {
        String screenName = "[Adapter_conf_each_participants]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
        String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용

        Log.d(tag_check, "getParticipant : " + participantId);
        Iterator<Part_conf_each_participant> iterator = data.iterator();
        int position;
        while (iterator.hasNext()) {
            Part_conf_each_participant participantObj = iterator.next();
            if (participantObj.getId().equals(participantId)) {
                position = data.indexOf(participantObj);
                Log.d(tag_check, "found obj : " + data.get(position));
                return data.get(position);
            }
        }
        Log.d(tag_check, "no data found!");
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImgView;
        private final TextView nameTxtView;
        private final ImageView micStatusImgView;
        private final ImageView videoStatusImgView;

        public ViewHolder(View view) {
            super(view);
            profileImgView = view.findViewById(R.id.part_conf_each_participants_ProfileImg);
            nameTxtView = view.findViewById(R.id.part_conf_each_participants_name);
            micStatusImgView = view.findViewById(R.id.part_conf_each_participants_mic_btn);
            videoStatusImgView = view.findViewById(R.id.part_conf_each_participants_video_btn);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.part_conf_each_participants, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Log.d(tag_event, "onBindViewHolder check data : " + data.get(position));
        String senderProfileImage = data.get(position).getProfileImg();

        // 프로필 사진이 있는 경우에만 표시해 주기
        if (senderProfileImage != null && senderProfileImage.length() != 0) {
            byte[] senderProfile_decodedString = Base64.decode(senderProfileImage, Base64.DEFAULT);
            Bitmap senderProfile_decodedBitmap = BitmapFactory.decodeByteArray(senderProfile_decodedString, 0, senderProfile_decodedString.length);
            viewHolder.profileImgView.setImageBitmap(senderProfile_decodedBitmap);
        }

        boolean isHost = data.get(position).getIsHost();
        String suffix = "";
        if (isHost) {
            suffix = "(호스트)";
        }
        viewHolder.nameTxtView.setText(data.get(position).getName() + suffix);

        // 마이크 아이콘 랜더링
        boolean isMicOn = data.get(position).getIsMicOn();
        if (isMicOn) viewHolder.micStatusImgView.setImageResource(R.drawable.ic_mic_on);
        else viewHolder.micStatusImgView.setImageResource(R.drawable.ic_mic_off);

        // 비디오 아이콘 랜더링
        boolean isVideoOn = data.get(position).getIsVideoOn();
        if (isVideoOn) viewHolder.videoStatusImgView.setImageResource(R.drawable.ic_video_on);
        else viewHolder.videoStatusImgView.setImageResource(R.drawable.ic_video_off);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        Log.d(tag_event, "onDetachedFromRecyclerView");
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }
}
