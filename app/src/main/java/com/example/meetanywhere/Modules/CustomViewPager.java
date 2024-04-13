package com.example.meetanywhere.Modules;

import android.app.Activity;
import android.media.projection.MediaProjection;
import android.util.Log;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.meetanywhere.Activities.conference_room;
import com.example.meetanywhere.Fragments.fragment_conference_main_video;
import com.example.meetanywhere.Fragments.fragment_conference_participants_videos;
import com.example.meetanywhere.Fragments.fragment_conference_whiteboard;

import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomViewPager extends ViewPager {
    private String screenName = "[Modules]CustomViewPager:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Activity activity;
    private CustomViewPager viewPager;
    private fragment_conference_main_video mainVideoFragment;
    private fragment_conference_participants_videos participantsVideosFragment;

    public static VideoTrackObject videoTrackObj;
    public static AudioTrackObject audioTrackObj;

    public static boolean isSwipable = true; // This variable controls swiping

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Activity activity) {
        this.activity = activity;
        Log.d(tag_check, "init. activity : " + activity);

        videoTrackObj = new VideoTrackObject();
        audioTrackObj = new AudioTrackObject();

        mainVideoFragment = new fragment_conference_main_video(activity);
        participantsVideosFragment = new fragment_conference_participants_videos(activity);
    }

    // Set whether the ViewPager is swipable or not
    public static void setSwipable(boolean swipable) {
        //Log.d(tag_check, "setSwipable");
        isSwipable = swipable;
    }

    public void setCustomAdapter(FragmentManager fm) {
        this.setAdapter(new PagerAdapter(fm));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(tag_check, "onTouchEvent");
        return isSwipable && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //Log.d(tag_check, "onInterceptTouchEvent");
        return isSwipable && super.onInterceptTouchEvent(event);
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            //Log.d(tag_check, "getItem : " + i);
            if (i == 0) {
                return mainVideoFragment;
            } else {
                return participantsVideosFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.d(tag_check, "getPageTitle : " + position);
            return "OBJECT " + (position + 1);
        }
    }

    /* ********************** fragment_conference_main_video 관련 매서드 ********************** */
    // 로컬에서 본인 카메라에서 stream capture 해서 view 에 랜더링 해 주기
    public void main_captureStreamFromCamera() {
        // Update the fragment's content with the provided message
        Log.d(tag_execute, "main_createTrackFromCamera");
        mainVideoFragment.createVideoTrackFromCameraAndShowIt();
    }

    // webRTC 연결 후 본인 영상 stream 을 track 추가해 주기
    public void main_addLocalStream(MediaStream mediaStream) {
        Log.d(tag_execute, "main_addLocalStream : " + mediaStream);
        mainVideoFragment.addLocalStream(mediaStream);
    }

    // webRTC 연결 후 remote peer 영상 stream 을 track 추가해 주기
    public void main_addRemoteStream(MediaStream mediaStream, String socketId) {
        Log.d(tag_execute, "main_addRemoteStream : " + mediaStream);
        mainVideoFragment.addRemoteStream(mediaStream, socketId);
    }
    // webRTC 연결 후 본인 영상 stream 을 track 추가해 주기

    public void main_SCREENSHARE_addLocalStream(MediaStream mediaStream, boolean isScreenSharer) {
        Log.d(tag_execute, "main_addLocalStream : " + mediaStream);
        mainVideoFragment.SCREENSHARE_addLocalStream(mediaStream, isScreenSharer);
    }

    // webRTC 연결 후 특정 참가자가 화면 공유할 때 remote peer 영상 stream 을 track 추가해 주기
    public void main_SCREENSHARE_addRemoteStream(MediaStream mediaStream, String socketId, boolean isScreenSharer, String screenSharerName) {
        Log.d(tag_execute, "main_addRemoteScreenSharingStream : " + mediaStream);
        mainVideoFragment.SCREENSHARE_addRemoteStream(mediaStream, socketId, isScreenSharer, screenSharerName);
    }

    // 화면 공유하던 참가자가 화면공유 중지
    public void main_SCREENSHARE_sharer_stopSharing(boolean renderMyself) {
        Log.d(tag_execute, "main_stopScreenSharing");
        mainVideoFragment.SCREENSHARE_sharer_stopSharing(renderMyself);
    }

    // remote peer 가 화면공유 중지 시 화면공유 videoTrack 제거해 주기
    public void main_SCREENSHARE_removeScreenSharingTrack(String socketId) {
        Log.d(tag_execute, "main_SCREENSHARE_removeScreenSharingTrack");
        mainVideoFragment.SCREENSHARE_removeScreenSharingTrack(socketId, false);
    }

    // remote peer 가 퇴장 시 회의 메인 화면 내 stream 으로 다시 랜더링 해 주기
    public void main_removeLeftParticipantsTrack(String socketId) {
        Log.d(tag_execute, "main_removeLeftParticipantsTrack");
        mainVideoFragment.removeLeftParticipantsTrack(socketId);
    }

    // 참석자 비디오 on/off 처리
    public void setVideoOnOff(boolean videoStatus, String socketId) {
        Log.d(tag_execute, "main_setVideoOnOff : " + socketId);
        Log.d(tag_check, "videoStatus : " + videoStatus);
        VideoTrack targetTrack = videoTrackObj.get(socketId);
        if (targetTrack != null) {
            Log.d(tag_check, "find track : " + targetTrack);
            targetTrack.setEnabled(videoStatus);
        }
    }

    // 참석자 마이크 on/off 처리
    public void setMicOnOff(boolean micStatus, String socketId) {
        Log.d(tag_execute, "main_setMicOnOff : " + socketId);
        Log.d(tag_check, "micStatus : " + micStatus);
        AudioTrack targetTrack = audioTrackObj.get(socketId);
        if (targetTrack != null) {
            Log.d(tag_check, "find track : " + targetTrack);
            targetTrack.setEnabled(micStatus);

            participantsVideosFragment.renderMicStatus(micStatus, socketId);
        }
    }

    // 화이트보드 열기
    public void main_openWhiteBoard() {
        Log.d(tag_execute, "main_openWhiteBoard");
        mainVideoFragment.openWhiteBoard();
    }
    // 화이트보드 닫기
    public void main_closeWhiteBoard() {
        Log.d(tag_execute, "main_closeWhiteBoard");
        mainVideoFragment.closeWhiteBoard();
    }

    /* ********************** fragment_conference_participants_videos 관련 매서드 ********************** */
    // webRTC 연결 후 local, remote 영상 stream 을 track 추가해 주기
    public void participants_addStream(MediaStream mediaStream, String socketId) {
        Log.d(tag_execute, "participants_addStream : " + mediaStream);
        participantsVideosFragment.addStream(mediaStream, socketId);
    }

    // 참석자가 입장 시 surfaceView 추가
    public void participants_addSurfaceView(String socketId) {
        Log.d(tag_execute, "participants_addSurfaceView : " + socketId);
        participantsVideosFragment.addSurfaceView(socketId);
    }

    // 참석자가 퇴장 시 surfaceView 제거
    public void participants_removeSurfaceView(String socketId) {
        Log.d(tag_execute, "participants_removeSurfaceView : " + socketId);
        participantsVideosFragment.removeSurfaceView(socketId);
    }

    // 새 호스트 설정, 비디오 데이터 및 UI 재정렬
    public void participants_setNewHost() {
        Log.d(tag_execute, "participants_setNewHost");
        participantsVideosFragment.setNewHostAndSortData();
    }
}