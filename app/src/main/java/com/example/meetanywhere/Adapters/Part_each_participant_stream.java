package com.example.meetanywhere.Adapters;

import org.webrtc.MediaStream;

public class Part_each_participant_stream {
    private String id;  // 고유 id 값
    private MediaStream stream;

    public Part_each_participant_stream(String id, MediaStream stream) {
        this.id = id;
        this.stream = stream;
    }

    // 각 데이터 불러오기
    public String getId() {
        return id;
    }

    public MediaStream getStream() {
        return stream;
    }
}