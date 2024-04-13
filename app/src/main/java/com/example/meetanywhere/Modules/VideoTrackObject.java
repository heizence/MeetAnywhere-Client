package com.example.meetanywhere.Modules;

import android.util.Log;

import org.webrtc.AudioTrack;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VideoTrackObject {
    private String screenName = "[VideoTrackObject]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    public Map<String, VideoTrack> videoTrackData;
    public Map<String, VideoTrack> screenShareVideoTrackData;

    public VideoTrackObject() {
        videoTrackData = new HashMap<>();
        screenShareVideoTrackData = new HashMap<>();
    }

    public void put(String id, VideoTrack track) {
        videoTrackData.put(id, track);
    }

    public VideoTrack get(String id) {
        return videoTrackData.get(id);
    }

    public void put_ScreenShareTrack(String id, VideoTrack track) {
        screenShareVideoTrackData.put(id, track);
    }

    public VideoTrack get_ScreenShareTrack(String id) {
        return screenShareVideoTrackData.get(id);
    }

    // data 갯수 구하기. 회의 참석자 수 구할 때 쓸 수 있음.
    public int getSize() {
        return videoTrackData.size();
    }

    public void remove(String id) {
        Log.d(tag_check, "remove videoTrack : " + id);
        VideoTrack trackToRemove = videoTrackData.get(id);

        if (trackToRemove != null) {
            Log.d(tag_check, "videoTrackToRemove : " + trackToRemove);
            videoTrackData.remove(id);
        }
    }

    public void remove_ScreenShareTrack(String id) {
        Log.d(tag_check, "removeScreenShareTrack : " + id);
        VideoTrack trackToRemove = screenShareVideoTrackData.get(id);

        if (trackToRemove != null) {
            Log.d(tag_check, "videoTrackToRemove : " + trackToRemove);
            screenShareVideoTrackData.remove(id);
        }
    }

    public void removeAll() {
        Log.d(tag_check, "removeAll video tracks");
        Iterator<Map.Entry<String, VideoTrack>> iterator = videoTrackData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, VideoTrack> entry = iterator.next();
            String socketId = entry.getKey();
            VideoTrack trackToRemove = entry.getValue();

            Log.d(tag_check, "remove videoTrack : " + socketId);
            Log.d(tag_check, "videoTrackToRemove : " + trackToRemove);

            if (trackToRemove != null) {
                trackToRemove.setEnabled(false);
            }
            iterator.remove();
        }
    }

    public void checkVideoTracks() {
        Log.d(tag_check, "checkVideoTracks");
        Log.d(tag_check, "videoTrackData : " + this.videoTrackData);
        Log.d(tag_check, "screenShareVideoTrackData : " + this.screenShareVideoTrackData);
    }
}