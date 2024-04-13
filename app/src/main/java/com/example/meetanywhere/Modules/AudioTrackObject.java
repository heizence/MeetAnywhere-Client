package com.example.meetanywhere.Modules;

import android.util.Log;

import org.webrtc.AudioTrack;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// AudioTrack 을 저장하고 관리하는 object 생성 class
public class AudioTrackObject {
    private String screenName = "[AudioTrackObject]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Map<String, AudioTrack> audioTrackData;

    public AudioTrackObject() {
        audioTrackData = new HashMap<>();
    }

    public void put(String socketId, AudioTrack track) {
        audioTrackData.put(socketId, track);
    }

    public AudioTrack get(String socketId) {
        return audioTrackData.get(socketId);
    }

    public void remove(String socketId) {
        Log.d(tag_check, "remove audioTrack : " + socketId);
        AudioTrack trackToRemove = audioTrackData.get(socketId);
        Log.d(tag_check, "audioTrackToRemove : " + trackToRemove);
        if (trackToRemove != null) {
            audioTrackData.remove(socketId);
        }
    }

    public void removeAll() {
        Log.d(tag_check, "removeAll audio tracks");
        Iterator<Map.Entry<String, AudioTrack>> iterator = audioTrackData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AudioTrack> entry = iterator.next();
            String socketId = entry.getKey();
            AudioTrack trackToRemove = entry.getValue();

            Log.d(tag_check, "remove audioTrack : " + socketId);
            Log.d(tag_check, "audioTrackToRemove : " + trackToRemove);

            if (trackToRemove != null) {
                trackToRemove.setEnabled(false);
            }
            iterator.remove();
        }
    }
}