package com.example.meetanywhere.Adapters;

public class Part_conf_each_participant {
    // 변수명은 서버에서 보내주는 데이터의 변수명과 통일하야 함. 변경 시 서버 쪽과 반드시 같이 변경하기
    private String id;  // 고유 id 값
    private String name;   // 이름
    private String profileImg;   // 프로필 이미지 데이터(base64 string)
    private boolean isHost; // 회의 주최자 여부
    private boolean isMicOn; // 마이크 켜짐 여부
    private boolean isVideoOn; // 비디오 켜짐 여부

    public Part_conf_each_participant(String id, String name, String profileImg, boolean isHost, boolean isMicOn, boolean isVideoOn) {
        this.id = id;
        this.name = name;
        this.profileImg = profileImg;
        this.isHost = isHost;
        this.isMicOn = isMicOn;
        this.isVideoOn = isVideoOn;
    }

    // 각 데이터 불러오기
    public String getId() {return id;}
    public String getName() {return name;}
    public String getProfileImg() {return profileImg;}
    public boolean getIsHost() {return isHost;}
    public void assignHost() { this.isHost = true; }
    public boolean getIsMicOn() {return isMicOn;}
    public boolean getIsVideoOn() {return isVideoOn;}

    public void setIsMicOn(boolean status) { this.isMicOn = status; }
    public void setIsVideoOn(boolean status) { this.isVideoOn = status; }
}