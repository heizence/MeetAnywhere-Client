package com.example.meetanywhere.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetanywhere.Activities.conference_room;
import com.example.meetanywhere.Modules.dialog_conf_password_input;
import com.example.meetanywhere.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Adapter_each_host_candidate extends RecyclerView.Adapter<Adapter_each_host_candidate.ViewHolder> {
    private String screenName = "[Adapter_each_host_candidate]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context;
    private AdapterCallback adapterCallback;
    private AdapterDeleteCallback adapterDeleteCallback;
    List<Part_conf_each_participant> newHostList = new ArrayList<>();

    // sharedPreference 를 다룰 필요가 있을 때 사용
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private ImageView selectedItemCheckImgView; // 선택된 호스트 view 의 check imageView
    private int selectedItemIndex;

    // adapter 생성
    public Adapter_each_host_candidate(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferenceMain), context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
    }

    // adapter -> activity 간 데이터 전달을 위한 callback 설정
    public void setAdapterCallback(AdapterCallback callback) {
        this.adapterCallback = callback;
    }

    public void setAdapterDeleteCallback(AdapterDeleteCallback deleteCallback) {
        this.adapterDeleteCallback = deleteCallback;
    }

    // 데이터 갱신하여 리랜더링 하기
    public void reRenderData(List<Part_conf_each_participant> list) {
        Log.d(tag_check, "reRenderData");
        // 참가자 리스트로 업데이트 하는 경우
        if (list != null) {
            Log.d(tag_check, "init list");
            newHostList = new ArrayList<>(list.subList(1, list.size()));    // 맨 첫번째 호스트 데이터는 제외
            Log.d(tag_check, "create new list : " + newHostList);
            Log.d(tag_check, "list size : " + newHostList.size());
        }
        // 호스트 재할당 시 호스트 데이터를 없애주는 경우
        else {
            Log.d(tag_check, "reassign host.");
            Iterator<Part_conf_each_participant> iterator = newHostList.iterator();
            int position = -1;
            while (iterator.hasNext()) {
                Part_conf_each_participant participantObj = iterator.next();
                if (participantObj.getIsHost()) {
                    position = newHostList.indexOf(participantObj);
                    iterator.remove(); // Removes the current element
                    Log.d(tag_check, "remove host");
                }
            }
            if (position != -1) {
                Log.d(tag_check, "list size : " + newHostList.size());
                this.notifyItemRemoved(position);
            }
        }
        this.notifyDataSetChanged();
    }

    // 데이터 삭제
    public void deleteItem(String participantId) {
        Log.d(tag_check, "deleteItem : " + participantId);
        Iterator<Part_conf_each_participant> iterator = newHostList.iterator();
        int position = -1;
        while (iterator.hasNext()) {
            Part_conf_each_participant participantObj = iterator.next();
            if (participantObj.getId().equals(participantId)) {
                position = newHostList.indexOf(participantObj);
                iterator.remove(); // Removes the current element
                adapterDeleteCallback.onItemDelete();
                Log.d(tag_check, "item deleted");
                Log.d(tag_check, "list size : " + newHostList.size());
            }
        }
        if (position != -1) {
            this.notifyItemRemoved(position);
        }
    }

    // 회의 종료 취소 시 선택했던 호스트 선택 해제
    public void unselectHost() {
        Log.d(tag_check, "unselectHost");
        if (selectedItemCheckImgView != null) {
            selectedItemCheckImgView.setVisibility(View.INVISIBLE);
            selectedItemCheckImgView = null;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View rootLayout;
        private final ImageView profileImgView;
        private final TextView nameTxtView;
        private final ImageView checkImgView;

        public ViewHolder(View view) {
            super(view);
            rootLayout = view.findViewById(R.id.part_each_host_candidate_layout);
            profileImgView = view.findViewById(R.id.part_each_host_candidate_ProfileImg);
            nameTxtView = view.findViewById(R.id.part_each_host_candidate_name);
            checkImgView = view.findViewById(R.id.part_each_host_candidate_checkImg);
            checkImgView.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.part_each_host_candidate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Log.d(tag_check, "onBindViewHolder");
        //List<Part_conf_each_participant> data = Adapter_conf_each_participants.data;
        String senderProfileImage = newHostList.get(position).getProfileImg();

        // 프로필 사진이 있는 경우에만 표시해 주기
        if (senderProfileImage != null && senderProfileImage.length() != 0) {
            byte[] senderProfile_decodedString = Base64.decode(senderProfileImage, Base64.DEFAULT);
            Bitmap senderProfile_decodedBitmap = BitmapFactory.decodeByteArray(senderProfile_decodedString, 0, senderProfile_decodedString.length);
            viewHolder.profileImgView.setImageBitmap(senderProfile_decodedBitmap);
        }

        viewHolder.nameTxtView.setText(newHostList.get(position).getName());
        //int adapterPosition = viewHolder.getAdapterPosition();

        viewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(tag_check, "host layout onclick");
                int adapterPosition = viewHolder.getAdapterPosition();
                Log.d(tag_check, "adapterPosition : " + adapterPosition);
                selectedItemIndex = adapterPosition;
                Log.d(tag_check, "selectedItemIndex : " + selectedItemIndex);
                if (selectedItemCheckImgView != null) {
                    selectedItemCheckImgView.setVisibility(View.INVISIBLE);
                }
                selectedItemCheckImgView = viewHolder.checkImgView;
                Log.d(tag_check, "check new checkImgView : " + viewHolder.checkImgView);
                selectedItemCheckImgView.setVisibility(View.VISIBLE);

                String selectedHostId = newHostList.get(selectedItemIndex).getId(); // got error?
                Log.d(tag_check, "selectedHostId : " + selectedHostId);
                adapterCallback.onNewHostClicked(selectedHostId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newHostList.size();
    }

    /* ********************** adapter -> activity 간 데이터 전달을 위한 interface, callback 설정 ********************** */
    public interface AdapterCallback {
        void onNewHostClicked(String valueX);
    }

    public interface AdapterDeleteCallback {
        void onItemDelete();
    }
}
