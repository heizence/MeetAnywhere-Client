package com.example.meetanywhere.Adapters;
import android.os.Handler;

import android.util.Log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetanywhere.Activities.conference_room;
import com.example.meetanywhere.R;

import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Adapter_each_participant_video extends RecyclerView.Adapter<Adapter_each_participant_video.ViewHolder> {
    private String screenName = "[Adapter_each_participant_video]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    public Context context;
    private int videoGridHeight;   // videoView height 설정 시 사용

    // sharedPreference 를 다룰 필요가 있을 때 사용
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

    public int selectedItemIndex;
    private List<Part_each_participant_stream> streamData = new ArrayList<>();
    private MicStatusViewObj micStatusViewObj = new MicStatusViewObj();

    private ArrayList<VideoViewObj> videoViewList = new ArrayList<>();

    // adapter 생성
    public Adapter_each_participant_video(Context context, int videoGridHeight) {
        Log.d(tag_check, "Adapter_each_participant_video");
        this.context = context;
        this.videoGridHeight = videoGridHeight;
        sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferenceMain), context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
    }

    // webRTC 연결 시 전달받는 mediaStream 데이터 추가하기
    public void addStreamData(Part_each_participant_stream object) {
        Log.d(tag_check, "addStreamData : " + object);
        streamData.add(object);
    }

    // 비디오 view 추가
    public void addSurfaceView(String socketId) {
        Log.d(tag_check, "add : " + socketId);
        Log.d(tag_check, "isFirstRender true");
        sortData(false);
        Log.d(tag_check, "streamData size : " + streamData.size());
    }

    // 랜더링 전 데이터 정렬하기 : 호스트는 가장 앞으로, 자기 자신 데이터는 가장 끝으로 index 이동
    public void sortData(boolean hasNewHost) {
        Log.d(tag_check, "sortData");
        int endIndex = streamData.size() - 1;
        Log.d(tag_check, "check endIndex : " + endIndex);

        for (int i = 0; i <= endIndex; i++) {
            Log.d(tag_check, "index : " + i);
            String streamDataId = streamData.get(i).getId();
            Log.d(tag_check, "check streamDataId : " + streamDataId);
            Part_conf_each_participant userData = Adapter_conf_each_participants.getParticipant(streamDataId);
            Log.d(tag_check, "check userData : " + userData);
            boolean isHost = userData.getIsHost();
            Log.d(tag_check, "isHost : " + isHost);
            boolean isMyself = conference_room.mySocketId.equals(streamDataId);
            Log.d(tag_check, "isMyself : " + isMyself);

            if (isHost && i != 0) {
                Log.d(tag_check, "isHost : swap data");
                Collections.swap(streamData, i, 0);
                Log.d(tag_check, "swap complete!");
                break;
            }
        }

        if (hasNewHost) {
            Log.d(tag_check, "hasNewhost. fire notifyDataSetChanged event");
            this.notifyDataSetChanged();
        } else {
            Log.d(tag_check, "add new list. fire notifyItemInserted event");
            this.notifyItemInserted(streamData.size());
        }
    }

    // 참석자 마이크 상태 변경 시 새로 랜더링 해 주기
    public void renderMicStatus(boolean micStatus, String participantId) {
        if (micStatus) {
            micStatusViewObj.get(participantId).setImageResource(R.drawable.ic_mic_on);
        } else {
            micStatusViewObj.get(participantId).setImageResource(R.drawable.ic_mic_off);
        }
    }

    // 데이터 삭제
    public void deleteItem(String participantId) {
        Log.d(tag_check, "deleteItem : " + participantId);
        // Use an Iterator to find and remove the element
        Iterator<Part_each_participant_stream> iterator = streamData.iterator();
        int position = -1;
        while (iterator.hasNext()) {
            Part_each_participant_stream participantObj = iterator.next();
            if (participantObj.getId().equals(participantId)) {
                position = streamData.indexOf(participantObj);
                Log.d(tag_check, "check position : " + position);
                iterator.remove(); // Removes the current element
            }
        }
        if (position != -1) {
            this.notifyItemRemoved(position);
        }
    }

    // 퇴장 시 모든 데이터 삭제
    public void deleteAllItem() {
        Log.d(tag_check, "deleteAllItem");
        int size = streamData.size();
        Log.d(tag_check, "data size : " + size);

        for (int i = 0; i < size; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                try {
                    Log.d(tag_check, "index : " + finalI);
                    Part_each_participant_stream participantObj = streamData.get(finalI);
                    Log.d(tag_check, "participantObj : " + participantObj);
                    String id = participantObj.getId();
                    micStatusViewObj.remove(id);

                    View view = videoViewList.get(finalI).view;
                    if (view != null) {
                        videoViewList.get(finalI).view.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }

        Handler mHandler=new Handler();
        mHandler.postDelayed(new Runnable(){
            @Override
            public void run(){
                streamData.clear();
                //videoViewList.clear();
            }
        }, 1500);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final SurfaceViewRenderer videoView;
        private final TextView nameTxtView;
        private final ImageView micStatusImgView;

        public ViewHolder(View view) {
            super(view);
            Log.d(tag_check, "ViewHolder : " + view);
            videoView = view.findViewById(R.id.PEPV_videoView);
            nameTxtView = view.findViewById(R.id.PEPV_nameTextView);
            micStatusImgView = view.findViewById(R.id.PEPV_micStatus);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(tag_check, "onCreateViewHolder : " + parent);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.part_each_participant_video, parent, false);

        // 높이 화면의 절반 크기로 조절
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = videoGridHeight / 2;
        view.setLayoutParams(layoutParams);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Log.d(tag_check, "onBindViewHolder. position : " + position);
        String streamDataId = streamData.get(position).getId();
        Log.d(tag_check, "check streamDataId : " + streamDataId);
        Part_conf_each_participant userData = Adapter_conf_each_participants.getParticipant(streamDataId);
        Log.d(tag_check, "check userData : " + userData);
        String userName = userData.getName();
        Log.d(tag_check, "userName : " + userName);
        boolean isHost = userData.getIsHost();
        Log.d(tag_check, "isHost : " + isHost);
        boolean isMyself = conference_room.mySocketId.equals(streamDataId);
        Log.d(tag_check, "isMyself : " + isMyself);
        String suffix = "";

        if (isHost && isMyself) {
            suffix = "(호스트, 나)";
        } else if (isHost) {
            suffix = "(호스트)";
        } else if (isMyself) {
            suffix = "(나)";
        }

        viewHolder.nameTxtView.setText(userName + suffix);

        // 마이크 아이콘 랜더링
        boolean isMicOn = userData.getIsMicOn();
        if (isMicOn) viewHolder.micStatusImgView.setImageResource(R.drawable.ic_mic_on);
        else viewHolder.micStatusImgView.setImageResource(R.drawable.ic_mic_off);
        micStatusViewObj.put(streamDataId, viewHolder.micStatusImgView);

        // videoStream 랜더링 해 주기
        MediaStream mediaStream = streamData.get(position).getStream();
        if (mediaStream.videoTracks.size() != 0) {

            Log.d(tag_check, "check viewToRender : " + viewHolder.videoView);
            VideoViewObj videoViewFound = findVideoView(viewHolder.videoView);
            Log.d(tag_check, "check videoViewFound : " + videoViewFound);

            if (videoViewFound == null) {
                Log.d(tag_check, "no videoView. init start");
                viewHolder.videoView.init(conference_room.rootEglBase.getEglBaseContext(), null);   // got error!
                viewHolder.videoView.setEnableHardwareScaler(true);
                //viewHolder.videoView.setMirror(true);

            }
            Log.d(tag_check, "rendering start");
            VideoTrack newVideoTrack = mediaStream.videoTracks.get(0);
            Log.d(tag_check, "check newVideoTrack : " + newVideoTrack);
            newVideoTrack.addSink(viewHolder.videoView);
            Log.d(tag_check, "newVideoTrack addSink");
            videoViewList.add(new VideoViewObj(viewHolder.videoView, newVideoTrack));
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder viewHolder) {
        super.onViewDetachedFromWindow(viewHolder);
        Log.d(tag_check, "onViewDetachedFromWindow");
        Log.d(tag_check, "check videoView : " + viewHolder.videoView);

        VideoViewObj videoViewObj = findVideoView(viewHolder.videoView);
        VideoTrack track = videoViewObj.videoTrack;
        track.removeSink(viewHolder.videoView);
        Log.d(tag_check, "newVideoTrack removeSink");
        viewHolder.videoView.release();

        int size = videoViewList.size();
        Log.d(tag_check, "videoViewList size : " + size);

        for (int i = 0; i < size; i++) {
            if (videoViewList.get(i).view == viewHolder.videoView) {
                Log.d(tag_check, "found view. remove");
                videoViewList.remove(i);
                break;
            }
        }
        Log.d(tag_check, "view not found!");
    }

    @Override
    public int getItemCount() {
        return streamData.size();
    }

    class VideoViewObj {
        public org.webrtc.SurfaceViewRenderer view;
        public VideoTrack videoTrack;

        public VideoViewObj(org.webrtc.SurfaceViewRenderer _view, VideoTrack _videoTrack) {
            this.view = _view;
            this.videoTrack = _videoTrack;
        }
    }

    class MicStatusViewObj {
        private Map<String, ImageView> list;

        public MicStatusViewObj() {
            list = new HashMap<>();
        }

        public void put(String id, ImageView view) {
            list.put(id, view);
        }

        public ImageView get(String id) {
            return list.get(id);
        }

        public void remove(String id) {
            ImageView viewToRemove = list.get(id);
            if (viewToRemove != null) {
                list.remove(id);
            }
        }
    }

    // videoView 찾기.
    // 0 : 없음.
    // 1 : videoView 있음. 초기화 완료된 상태
    public VideoViewObj findVideoView(org.webrtc.SurfaceViewRenderer view) {
        Log.d(tag_check, "findVideoViewIsRendered. view : " + view);
        int size = videoViewList.size();
        Log.d(tag_check, "videoViewList size : " + size);

        for (int i = 0; i < size; i++) {
            VideoViewObj viewObj = videoViewList.get(i);
            if (viewObj.view == view) {
                Log.d(tag_check, "found view.");
                return viewObj;
            }
        }
        Log.d(tag_check, "view not found!");
        return null;
    }
}
