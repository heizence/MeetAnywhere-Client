package com.example.meetanywhere.Adapters;

public class Part_conf_each_chat_message {
    // 변수명은 서버에서 보내주는 데이터의 변수명과 통일하야 함. 변경 시 서버 쪽과 반드시 같이 변경하기
    private String id;  // 채팅 전송자 고유 id
    private String name;   // 이름
    private String profileImg;   // 프로필 이미지 데이터(base64 string)
    private String chatMessage; // 채팅 메시지

    public Part_conf_each_chat_message(String id, String name, String profileImg, String chatMessage) {
        this.id = id;
        this.name = name;
        this.profileImg = profileImg;
        this.chatMessage = chatMessage;
    }

    // 각 데이터 불러오기
    public String getId() {return id;}
    public String getName() {return name;}
    public String getProfileImg() {return profileImg;}
    public String getChatMessage() {return chatMessage;}
}