package com.example.meetanywhere.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import org.webrtc.MediaStream;
import org.webrtc.VideoTrack;

import com.example.meetanywhere.Adapters.Adapter_each_participant_video;
import com.example.meetanywhere.Adapters.Part_each_participant_stream;
import com.example.meetanywhere.R;


public class fragment_conference_participants_videos extends Fragment {
    private String screenName = "[FRAGMENT fragment_conference_participants_videos]:";
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context;
    private Activity activity;
    private Handler mHandler;
    private View fragmentView;
    private LinearLayout videoGridView;

    private Adapter_each_participant_video eachVideoAdapter;
    private RecyclerView eachVideoRecyclerView;

    public fragment_conference_participants_videos(
            Activity activity
    ) {
        this.activity = activity;
        this.context = activity.getBaseContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(tag_execute, "LifeCycle onCreate");
        super.onCreate(savedInstanceState);
        context = getContext();
        mHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(tag_execute, "LifeCycle onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_conference_participants_videos, container, false);
        videoGridView = fragmentView.findViewById(R.id.f_conference_participants_video_grid);

        videoGridView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(tag_check, "videoOnPreDraw");

                Rect r = new Rect();
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                videoGridView.getWindowVisibleDisplayFrame(r);

                int keypadHeight = screenHeight - r.bottom;
                Log.d(tag_check, "check keypadHeight : " + keypadHeight);
                int videoGridViewHeight = videoGridView.getHeight();

                if (keypadHeight <= 0) {
                    Log.d(tag_check, "keypadHeight is 0");
                    videoGridView.getViewTreeObserver().removeOnPreDrawListener(this); // Remove the listener to avoid multiple calls

                    Log.d(tag_check, "videoGridViewHeight : " + videoGridViewHeight);
                    eachVideoAdapter = new Adapter_each_participant_video(context, videoGridViewHeight);
                    Log.d(tag_check, "eachVideoAdapter : " + eachVideoAdapter);

                    eachVideoRecyclerView = fragmentView.findViewById(R.id.each_participant_video_recyclerView);
                    Log.d(tag_check, "eachVideoRecyclerView : " + eachVideoRecyclerView);

                    eachVideoRecyclerView.setAdapter(eachVideoAdapter);
                    eachVideoRecyclerView.setHasFixedSize(true);
                    eachVideoRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                }
                return true;
            }
        });

        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag_check, "LifeCycle onDestroy");
        removeAllSurfaceView();
    }

    // 참석자가 퇴장 시 surfaceView 제거
    public void removeSurfaceView(String socketId) {
        Log.d(tag_check, "removeSurfaceView : " + socketId);
        eachVideoAdapter.deleteItem(socketId);
    }

    // 본인이 퇴장 시 모든 surfaceView 제거
    public void removeAllSurfaceView() {
        Log.d(tag_check, "removeAllSurfaceView");
        eachVideoAdapter.deleteAllItem();
    }

    public void addStream(MediaStream mediaStream, String socketId) {
        Log.d(tag_check, "addStream");
        Log.d(tag_check, "check socketId : " + socketId);
        Log.d(tag_check, "check mediaStream : " + mediaStream);

        // 화면공유 stream 이 있는지 판별하여 알맞은 track 랜더링 해 주기
        int videoTrackSize = mediaStream.videoTracks.size();
        int audioTrackSize = mediaStream.audioTracks.size();
        Log.d(tag_check, "check videoTrackSize : " + videoTrackSize);
        Log.d(tag_check, "check audioTrackSize : " + audioTrackSize);
        boolean isSharingStream = videoTrackSize != audioTrackSize;
        Log.d(tag_check, "isSharingStream : " + isSharingStream);

        if (isSharingStream) {
            mediaStream.removeTrack(mediaStream.videoTracks.get(videoTrackSize - 1));
            Log.d(tag_check, "check mediaStream videoTracks : " + mediaStream.videoTracks);
            if (mediaStream.videoTracks.size() == 0) {
                return;
            }
        }
        Log.d(tag_check, "add stream obj");
        // mediaStream 데이터 추가
        Part_each_participant_stream obj = new Part_each_participant_stream(socketId, mediaStream);
        eachVideoAdapter.addStreamData(obj);
    }

    // 참석자가 입장 시 surfaceView 추가
    public void addSurfaceView(String socketId) {
        Log.d(tag_check, "addSurfaceView : " + socketId);
        eachVideoAdapter.addSurfaceView(socketId);
    }

    // 화면에 표시되는 참석자 마이크 상태 변경 시 새로 랜더링 해 주기
    public void renderMicStatus(boolean micStatus, String socketId) {
        Log.d(tag_check, "renderMicStatus");
        eachVideoAdapter.renderMicStatus(micStatus, socketId);
    }

    // 새 호스트 할당 후 데이터 재정렬하기
    public void setNewHostAndSortData() {
        Log.d(tag_check, "setNewHostAndSortData");
        eachVideoAdapter.sortData(true);    // 여기서 문제 발생
        //eachVideoAdapter.moveData();
    }
}
