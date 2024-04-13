package com.example.meetanywhere.Activities;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.text.Editable;
import android.text.TextWatcher;

import android.graphics.Rect;
import android.view.inputmethod.InputMethodManager;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetanywhere.Adapters.Adapter_conf_each_chat_message;
import com.example.meetanywhere.Adapters.Adapter_conf_each_participants;
import com.example.meetanywhere.Adapters.Adapter_each_host_candidate;
import com.example.meetanywhere.Adapters.Part_conf_each_participant;
import com.example.meetanywhere.Adapters.Part_each_participant_stream;
import com.example.meetanywhere.Fragments.fragment_conference_main_video;
import com.example.meetanywhere.Fragments.fragment_conference_whiteboard;
import com.example.meetanywhere.Modules.CustomViewPager;
import com.example.meetanywhere.Modules.Sha256_hash;
import com.example.meetanywhere.Modules.dialog_conf_password_input;
import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.dialog_new_chat;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.example.meetanywhere.Services.ScreenCaptureNotification;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.example.meetanywhere.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoTrack;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class conference_room extends AppCompatActivity implements conference_room2 {
    private final String screenName = "[ACTIVITY conference_room]:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용
    private String tag_socket_event = screenName + "[SOCKET EVENT]";

    private Context context = this;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private Socket mSocket;
    private Handler mHandler;
    private Intent screenSharingServiceIntent;

    private int screenWidth;
    private int screenHeight;
    private double bottomSheetAdjustConst = 0.97;    // bottomSheet 에 layout 을 표시할 때 mismatch 를 줄여주기 위한 상수

    private View conferenceRoomLayout;
    private TextView exitConferenceButton;
    private TextView exitForAllBtn;
    private TextView exitBtn;
    private View conferenceRoomInfoBtn;
    private View headerView;
    private View bottomTabView;
    private View ExitButtonsView;
    private View AssignNewHostButtonsView;

    private View conferenceRoomMicOnOffBtn;
    private View conferenceRoomVideoOnOffBtn;
    private ImageView conferenceRoomMicOnOffBtnImg;
    private ImageView conferenceRoomVideoOnOffBtnImg;
    private TextView conferenceRoomMicOnOffBtnTxt;
    private TextView conferenceRoomVideoOnOffBtnTxt;
    private View conferenceRoomParticipantsBtn;
    private View conferenceRoomChatMessageBtn;
    private ImageView conferenceRoomShareBtnImg;
    private TextView conferenceRoomShareBtnTxt;

    private View bottomSheetParticipantsView;
    private View bottomSheetChatMessagesView;
    private RecyclerView participantsRecyclerView;
    private RecyclerView hostCandidatesRecyclerView;
    private RecyclerView chatMessageRecyclerView;
    private Adapter_conf_each_participants participantsAdapter;
    private Adapter_each_host_candidate hostCandidateAdapter;
    private Adapter_conf_each_chat_message chatMessageAdapter;
    private TextInputEditText chatInput;
    private ImageView sendChatBtn;

    // 화면 공유 옵션 dialog 내부 view
    private View sharingOptionDialog;
    private TextView optionScreen;
    private TextView optionWhiteBoard;
    private TextView optionCancel;

    private MediaProjectionManager mediaProjectionManager;
    private CustomViewPager viewPager;

    // BottomSheetBehaviors
    private BottomSheetBehavior<View> bottomSheetBehavior_info;  // 회의 정보 bottomSheetBehavior
    private BottomSheetBehavior<View> bottomSheetBehavior_participants;  // 참가자 목록 bottomSheetBehavior
    private BottomSheetBehavior<View> bottomSheetBehavior_chat_message;  // 채팅 목록 bottomSheetBehavior

    // 회의 정보 bottomSheet 내부 view
    private TextView confNameView;
    private TextView confIdView;
    private TextView hostNameView;
    private TextView passwordView;
    private TextView inviteLinkView;
    private TextView chatRedDot;

    private TextView bottomSheetParticipants_headerTxt; // 참가자 숫자 표시하는 TextView
    private int chatModalOpenStat = 0;    // 채팅 bottomSheet 열린 상태 여부. 0 : HIDDEN(닫힘), 1 : HALF_EXPANDED(반만 열림), 2 : EXPANDED(완전히 다 열림)

    // 회의실 정보 관련 변수
    private String localVar_hostName;
    private String localVar_conferenceId;
    private String localVar_conferencePassword;
    private String localVar_conferenceInviteLink;
    private int localVar_numberOfUnreadChat = 0;    // 읽지 않은 채팅 수(최대 99 개)
    private int localVar_NumberOfParticipants;
    private boolean localVideoRendered = false; // 자기 자신의 비디오 stream 이 이미 표시되었는가 여부
    private String newHostId;   // 기존 호스트 퇴장 시 새로 선택된 호스트의 id

    // 유저 관련 변수
    public static String mySocketId;
    private String profileImg;
    private String userName;
    public static boolean isHost;
    private boolean isVideoPermitted;   // 비디오(카메라) 권한 허용 상태
    private boolean isMicPermitted; // 마이크 권한 허용 상태
    private boolean isVideoOn = true; // 비디오 on/off 상태
    private boolean isMicOn = true; // 마이크 on/off 상태
    public static boolean isSharingOn = false;    // 현재 화면, 컨텐츠 공유 상태
    public boolean isSharingBoard = false;
    private boolean isChatNotificationOn = true;  // 채팅 알람 on/off 상태
    private String chatMessage = "";    // has to be an empty string for check. do not change it to null or undefined!

    // for WebRTC connection
    private PeerConnectionList peerConnectionList;
    public static EglBase rootEglBase;
    public static PeerConnectionFactory factory;
    private String mediaStreamLabel = "ARDAMS";
    private String screenSharingPcID;   // 화면공유 peerConnection ID

    protected void onCreate(Bundle savedInstanceState) {
        // 각 변수들 선언 및 값 할당 순서 함부로 바꾸지 말 것!
        super.onCreate(savedInstanceState);
        Log.d(tag_check, "onCreate");
        setContentView(R.layout.screen_conference_room);

        sharedPref = getSharedPreferences(getString(R.string.sharedPreferenceMain), MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        // 새 호스트 후보 adapter 등록(선언 위치 바꾸지 말 것!)
        hostCandidateAdapter = new Adapter_each_host_candidate(context);
        // 새 호스트 선택 시 실행할 callback 등록
        hostCandidateAdapter.setAdapterCallback(new Adapter_each_host_candidate.AdapterCallback() {
            @Override
            public void onNewHostClicked(String param_newHostId) {
                Log.d(tag_check, "onNewHostClicked. host Id : " + param_newHostId);
                newHostId = param_newHostId;
            }
        });

        isHost = getIntent().getBooleanExtra(getString(R.string.isHost), false);
        profileImg = sharedPref.getString(getString(R.string.store_U_ProfileImg), "");

        // 아래쪽 조건문이랑 합치지 말 것.
        if (!isHost) {
            localVar_conferenceId = getIntent().getStringExtra(getString(R.string.conferenceId));
            userName = getIntent().getStringExtra(getString(R.string.userName));
            Log.d(tag_check, "userName from intent : " + userName);
        } else {
            userName = sharedPref.getString(getString(R.string.store_U_Name), "");
            localVar_hostName = userName;
        }

        mHandler = new Handler();
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        conferenceRoomLayout = findViewById(R.id.conferenceRoomLayout);

        // 비디오, 마이크 버튼 전체 부분(텍스트 포함)
        conferenceRoomMicOnOffBtn = findViewById(R.id.conferenceRoomMicLayout);
        conferenceRoomVideoOnOffBtn = findViewById(R.id.conferenceRoomVideoLayout);

        // 비디오, 마이크 버튼 이미지 부분
        conferenceRoomMicOnOffBtnImg = findViewById(R.id.conferenceRoomMicBtn);
        conferenceRoomVideoOnOffBtnImg = findViewById(R.id.conferenceRoomVideoBtn);

        // 비디오, 마이크 버튼 텍스트 부분
        conferenceRoomMicOnOffBtnTxt = findViewById(R.id.conferenceRoomMicBtnTxt);
        conferenceRoomVideoOnOffBtnTxt = findViewById(R.id.conferenceRoomVideoBtnTxt);

        chatRedDot = findViewById(R.id.conferenceRoomChatRedDot);

        Log.d(tag_check, "isHost : " + isHost);
        Log.d(tag_check, "localVar_conferenceId : " + localVar_conferenceId);

        // 회의 시작 전 카메라 및 마이크 권한 허용 여부 체크
        checkCameraPermission();

        // webRTC 관련 요소 초기 설정
        webRTCInit();

        viewPagerInit();

        // 회의 종료 버튼 설정
        setExitConferenceBtns();

        // 회의 정보 bottomSheet 설정
        setConferenceInfoBottomSheet();

        // 참가자 목록 bottomSheet 설정
        setParticipantsBottomSheet();

        // 채팅 목록 bottomSheet 설정
        setChatMessagesBottomSheet();

        // 윗쪽 조건문이랑 합치지 말 것.
        if (isHost) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //viewPager.main_setVideoOn();
                    setSocket();
                }
            }, 300);
        } else {
            // 회의 암호 입력하기
            //checkConferencePassword();

            // 원활한 시연을 위해 회의 암호 입력 생략
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //viewPager.main_setVideoOn();
                    setSocket();
                }
            }, 300);
        }

        // socket 과 연결 후 비디오, 마이크 on/off 버튼 설정해 주기
        conferenceRoomVideoOnOffBtn.setOnClickListener(view -> {
            setVideoOnOff();
        });

        conferenceRoomMicOnOffBtn.setOnClickListener(view -> {
            setMicOnOff();
        });

        // 공유 버튼 이벤트 등록
        setShareBtn();

        IntentFilter filter = new IntentFilter(fragment_conference_main_video.SCREEN_SHARED);
        filter.addAction(fragment_conference_main_video.SCREEN_TOUCHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        Log.d(tag_check, "LifeCycle onDestroy");
        super.onDestroy();
        //peerConnectionList = null;
        isSharingOn = false;

        if (screenSharingServiceIntent != null) {
            stopService(screenSharingServiceIntent);
        }
        try {
            if (isHost) {
                participantsAdapter.deleteAllItem();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // webRTC 관련 요소 초기 설정
    public void webRTCInit() {
        Log.d(tag_check, "webRTCInit");
        rootEglBase = EglBase.create();
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(getApplicationContext())
                .setEnableInternalTracer(true)
                .createInitializationOptions());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        PeerConnectionFactory.builder()
                .setOptions(options)
                .createPeerConnectionFactory();

        VideoEncoderFactory encoderFactory;
        VideoDecoderFactory decoderFactory;
        encoderFactory = new SoftwareVideoEncoderFactory();
        decoderFactory = new SoftwareVideoDecoderFactory();

        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        Log.d(tag_check, "peerConnectionFactory : " + factory);
        peerConnectionList = new PeerConnectionList();   // 여러 개의 peerConnection 객체를 관리할 저장 공간 생성.
        Log.d(tag_check, "check peerConnectionList : " + peerConnectionList);
    }

    public void viewPagerInit() {
        Log.d(tag_check, "viewPagerInit");
        // view pager 설정
        viewPager = findViewById(R.id.conferenceRoomPager);
        viewPager.init(this);
        viewPager.setSwipable(true);
        viewPager.setCustomAdapter(getSupportFragmentManager());
    }

    // 참석자의 경우 회의 암호 입력하기
    public void checkConferencePassword() {
        Log.d(tag_check, "checkConferencePassword");
        // 회의 암호 입력 취소 시 실행할 callback
        class Callee_Cancel extends dialog_conf_password_input.Callee_Cancel {
            public void call() {
                Log.d(tag_check, "Callback Cancel.");
                finish();
            }
        }

        // 회의 암호를 올바르게 입력했을 때 실행할 callback
        class Callee_Confirm extends dialog_conf_password_input.Callee_Confirm {
            public void call() {
                Log.d(tag_check, "Callback Confirm");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //viewPager.main_setVideoOn();
                        setSocket();
                    }
                }, 300);
            }
        }
        dialog_conf_password_input.show(context, localVar_conferenceId, new Callee_Cancel(), new Callee_Confirm());
    }

    // 회의 시작 전 카메라 권한 허용 여부 체크
    public void checkCameraPermission() {
        Log.d(tag_execute, "checkCameraPermission");
        int PERMISSION_REQUEST_CODE = 100;

        // 카메라 권한 허용 상태
        boolean cameraPermissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        Log.d(tag_check, "cameraPermissionStatus : " + cameraPermissionStatus);

        if (cameraPermissionStatus) {
            Log.d(tag_check, "Camera permission has already granted!");
            isVideoPermitted = true;
            checkMicPermission();
        } else {
            // Permission not granted
            // Request for camera permissions
            Log.d(tag_check, "Camera permission not granted. request permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }

    // 회의 시작 전 마이크 권한 허용 여부 체크
    public void checkMicPermission() {
        Log.d(tag_execute, "checkMicPermission");
        int PERMISSION_REQUEST_CODE = 101;

        // 오디오 권한 허용 상태(true 이면 허용이 되지 않은 상태)
        boolean micPermissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        Log.d(tag_check, "micPermissionStatus : " + micPermissionStatus);

        if (micPermissionStatus) {
            Log.d(tag_check, "Mic permission has already granted!");
            isMicPermitted = true;
        } else {
            // Permission not granted
            // Request for camera permissions
            Log.d(tag_check, "Mic permission not granted. request permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(tag_execute, "onRequestPermissionResult. requestCode : " + requestCode);
        /*
        grantResults values
        0 : while using this app
        -1 : not allow
        */
        if (requestCode == 100) {
            Log.d(tag_check, "Camera permission. grantResult[0] : " + grantResults[0]);
            //Log.d(tag_check, "grantResult[1] : " + grantResults[1]);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(tag_check, "Camera permission now granted!");
                //cameraViewInit();
                isVideoPermitted = true;
            } else {
                Log.d(tag_check, "Camera permission denied");
                isVideoPermitted = false;
                // Permissions are denied, show an error message or disable the functionality
            }
            checkMicPermission();
        } else if (requestCode == 101) {
            Log.d(tag_check, "Mic permission. grantResult[0] : " + grantResults[0]);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(tag_check, "Mic permission now granted!");
                //cameraViewInit();
                isMicPermitted = true;
            } else {
                Log.d(tag_check, "Mic permission denied");
                isMicPermitted = false;
                // Permissions are denied, show an error message or disable the functionality
            }
        }
    }

    // 회의 종료 버튼 설정
    public void setExitConferenceBtns() {
        Log.d(tag_check, "setExitConferenceBtns");
        ExitButtonsView = findViewById(R.id.conferenceRoom_exit_btns);
        // 호스트 : 모두에 대해 회의 종료 버튼, 참석자 : 회의 나가기 버튼
        exitForAllBtn = ExitButtonsView.findViewById(R.id.dialogExitConference_exitForAllBtn);
        // 호스트 : 회의 나가기 버튼, 참석자의 경우 표시해 주지 않음
        exitBtn = ExitButtonsView.findViewById(R.id.dialogExitConference_exitBtn);
        // 회의 화면 상단 우측에 있는 종료 버튼
        exitConferenceButton = findViewById(R.id.conferenceRoomExitBtn);

        // 호스트일 경우 버튼 동작 처리
        if (isHost) {
            Log.d(tag_check, "isHost true");

            exitConferenceButton.setText("종료");
            exitForAllBtn.setText("모두에 대해 회의 종료");
            exitForAllBtn.setOnClickListener(v -> {
                Log.d(tag_check, "exitForAll");
                ExitButtonsView.setVisibility(View.INVISIBLE);
                endConference();
            });
            Log.d(tag_check, "check exitBtn visibility(0 is visible) : " + exitBtn.getVisibility());
            exitBtn.setText("회의 나가기");
            exitBtn.setVisibility(View.VISIBLE);
            Log.d(tag_check, "check exitBtn visibility(0 is visible) : " + exitBtn.getVisibility());
            exitBtn.setOnClickListener(v -> {
                Log.d(tag_check, "exit");
                if (localVar_NumberOfParticipants < 2) {
                    Log.d(tag_check, "only 1 participant. exit for all");
                    ExitButtonsView.setVisibility(View.INVISIBLE);
                    endConference();
                } else {
                    Log.d(tag_check, "more than 1 participant. assign new host");
                    ExitButtonsView.setVisibility(View.INVISIBLE);
                    AssignNewHostButtonsView.setVisibility(View.VISIBLE);
                }
            });
            exitConferenceButton.setOnClickListener(v -> {
                // 종료 상태에서 버튼 클릭했을 때
                if (ExitButtonsView.getVisibility() == View.INVISIBLE && AssignNewHostButtonsView.getVisibility() == View.INVISIBLE) {
                    Log.d(tag_check, "Status : ExitButtonsView INVISIBLE");
                    ExitButtonsView.setVisibility(View.VISIBLE);
                    exitConferenceButton.setText("취소");
                    exitConferenceButton.setTextColor(Color.BLACK);
                    exitConferenceButton.setBackgroundResource(R.drawable.style_conf_exit_btn_transparent);
                }
                // 취소 상태에서 버튼 클릭했을 때
                else {
                    ExitButtonsView.setVisibility(View.INVISIBLE);
                    AssignNewHostButtonsView.setVisibility(View.INVISIBLE);
                    exitConferenceButton.setText("종료");
                    exitConferenceButton.setTextColor(Color.WHITE);
                    exitConferenceButton.setBackgroundResource(R.drawable.style_conference_exit_for_all_btn);

                    // 이미 지정한 새 호스트가 있을 경우 선택 취소 처리
                    hostCandidateAdapter.unselectHost();
                    newHostId = "";
                }
            });

            /* ********************** 새 호스트 설정, 저장 후 나가기 버튼 설정 ********************** */
            AssignNewHostButtonsView = findViewById(R.id.conferenceRoom_assign_new_host);
            TextView assignAndExitBtn = AssignNewHostButtonsView.findViewById(R.id.dialogAssignNewHost_exitBtn);

            // hostCandidates recyclerView, adapter 설정기
            hostCandidatesRecyclerView = AssignNewHostButtonsView.findViewById(R.id.dialog_new_host_list_recyclerView);
            // 참석자 퇴장 시(데이터 삭제 시) 실행할 callback 등록
            hostCandidateAdapter.setAdapterDeleteCallback(new Adapter_each_host_candidate.AdapterDeleteCallback() {
                @Override
                public void onItemDelete() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(tag_check, "hostCandidateAdapter onItemDelete");
                            int recyclerViewHeight = hostCandidatesRecyclerView.getHeight(); // Get the height
                            Log.d(tag_check, "recyclerViewHeight : " + recyclerViewHeight);
                            ViewGroup.LayoutParams recyclerViewLayoutParams = (ViewGroup.LayoutParams) hostCandidatesRecyclerView.getLayoutParams();
                            recyclerViewLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            hostCandidatesRecyclerView.setLayoutParams(recyclerViewLayoutParams);
                            Log.d(tag_check, "recyclerViewLayoutParams set");
                        }
                    });
                }
            });
            Log.d(tag_check, "hostCandidatesRecyclerView : " + hostCandidatesRecyclerView);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    hostCandidatesRecyclerView.setHasFixedSize(true);
                    hostCandidatesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    Log.d(tag_check, "set hostCandidateAdapter : " + hostCandidateAdapter);
                    hostCandidatesRecyclerView.setAdapter(hostCandidateAdapter);
                }
            });

            // 저장 후 나가기 버튼
            assignAndExitBtn.setOnClickListener(v -> {
                Log.d(tag_check, "assignAndExitBtn");

                if (!newHostId.equals("")) {
                    Log.d(tag_check, "has new Host Id : " + newHostId);
                    AssignNewHostButtonsView.setVisibility(View.INVISIBLE);
                    JSONObject data = new JSONObject();
                    try {
                        data.put(getString(R.string.newHostSocketId), newHostId);
                        data.put(getString(R.string.hostSocketId), mySocketId);  // for test
                        mSocket.emit(getString(R.string.host_left), data);
                        mSocket.disconnect();
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(tag_check, "no new host selected!");
                }
            });
        }
        // 참석자일 경우 버튼 동작 처리
        else {
            Log.d(tag_check, "isHost false");
            exitForAllBtn.setText("회의 나가기");
            exitBtn.setVisibility(View.INVISIBLE);
            exitForAllBtn.setOnClickListener(v -> {
                Log.d(tag_check, "exit");
                ExitButtonsView.setVisibility(View.INVISIBLE);
                mSocket.emit(getString(R.string.participant_left));
                mSocket.disconnect();
                finish();
            });

            exitConferenceButton.setText("나가기");
            exitConferenceButton.setOnClickListener(v -> {
                if (ExitButtonsView.getVisibility() == View.INVISIBLE) {
                    Log.d(tag_check, "Status : ExitButtonsView INVISIBLE");
                    ExitButtonsView.setVisibility(View.VISIBLE);
                    exitConferenceButton.setText("취소");
                    exitConferenceButton.setTextColor(Color.BLACK);
                    exitConferenceButton.setBackgroundResource(R.drawable.style_conf_exit_btn_transparent);
                } else {
                    ExitButtonsView.setVisibility(View.INVISIBLE);
                    exitConferenceButton.setText("나가기");
                    exitConferenceButton.setTextColor(Color.WHITE);
                    exitConferenceButton.setBackgroundResource(R.drawable.style_conference_exit_for_all_btn);
                }
            });
        }

        /* ********************** 버튼 layout margin 설정 ********************** */
        CoordinatorLayout.LayoutParams exitForAllBtnLayoutParams = (CoordinatorLayout.LayoutParams) ExitButtonsView.getLayoutParams();

        headerView = findViewById(R.id.conferenceRoomHeader);
        headerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int headerViewHeight = headerView.getHeight(); // Get the height
                exitForAllBtnLayoutParams.setMargins(0, headerViewHeight, 0, 0);
                ExitButtonsView.setLayoutParams(exitForAllBtnLayoutParams);

                if (isHost) {
                    CoordinatorLayout.LayoutParams AssignNewHostButtonsLayoutParams = (CoordinatorLayout.LayoutParams) AssignNewHostButtonsView.getLayoutParams();
                    AssignNewHostButtonsLayoutParams.setMargins(0, headerViewHeight, 0, 0);
                    AssignNewHostButtonsView.setLayoutParams(AssignNewHostButtonsLayoutParams);

                    ViewGroup.LayoutParams exitBtnLayoutParams = (ViewGroup.LayoutParams) exitBtn.getLayoutParams();
                    exitBtnLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    exitBtn.setLayoutParams(exitBtnLayoutParams);
                } else {
                    ViewGroup.LayoutParams dummyBtnLayoutParams = (ViewGroup.LayoutParams) exitBtn.getLayoutParams();
                    dummyBtnLayoutParams.height = 0;
                    exitBtn.setLayoutParams(dummyBtnLayoutParams);
                }
                headerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    // 회의 정보 bottomSheet 설정
    public void setConferenceInfoBottomSheet() {
        Log.d(tag_check, "setConferenceInfoBottomSheet");
        View infoBottomSheet = findViewById(R.id.conferenceRoom_info_BottomSheet);

        confNameView = infoBottomSheet.findViewById(R.id.bottom_sheet_conf_info_bottom_sheet_dialog_confName);
        confIdView = infoBottomSheet.findViewById(R.id.bottom_sheet_conf_info_bottom_sheet_dialog_confId);
        hostNameView = infoBottomSheet.findViewById(R.id.bottom_sheet_conf_info_bottom_sheet_dialog_hostName);
        passwordView = infoBottomSheet.findViewById(R.id.bottom_sheet_conf_info_bottom_sheet_dialog_confPassword);
        //inviteLinkView = infoBottomSheet.findViewById(R.id.bottom_sheet_conf_info_bottom_sheet_dialog_inviteLink);

        bottomSheetBehavior_info = BottomSheetBehavior.from(infoBottomSheet);
        bottomSheetBehavior_info.setDraggable(true);
        bottomSheetBehavior_info.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior_info.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                //Log.d(tag_check, "onStateChanged.");
                //Log.d(tag_check, "check hostName : " + localVar_hostName);
                confNameView.setText(localVar_hostName + "님의 회의");
                String confId = localVar_conferenceId.substring(0, 3) + " " + localVar_conferenceId.substring(3, 7) + " " + localVar_conferenceId.substring(7);
                confIdView.setText(confId);
                hostNameView.setText(localVar_hostName);
                passwordView.setText(localVar_conferencePassword);
                //inviteLinkView.setText("");
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Prevent expanding beyond a certain point (e.g., halfway)
                if (slideOffset > 0.5f) {
                    // Adjust the state to COLLAPSED
                    bottomSheetBehavior_info.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }
            }
        });

        conferenceRoomInfoBtn = findViewById(R.id.conferenceRoomInfoBtn);
        conferenceRoomInfoBtn.setOnClickListener(v -> {
            bottomSheetBehavior_info.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        });
    }

    // 참가자 목록 bottomSheet 설정
    public void setParticipantsBottomSheet() {
        View bottomSheetParticipantsLayout = findViewById(R.id.conferenceRoom_participants_BottomSheet);   // include layout
        bottomSheetParticipantsView = bottomSheetParticipantsLayout.findViewById(R.id.bottom_sheet_dialog_conf_participants_layout);   // bottom_sheet_dialog_conf_participants_layout xml file
        ImageView bottomSheetParticipants_closeBtn = bottomSheetParticipantsLayout.findViewById(R.id.bottom_sheet_dialog_conf_participants_close_btn);
        bottomSheetParticipants_headerTxt = bottomSheetParticipantsLayout.findViewById(R.id.bottom_sheet_dialog_conf_participants_participants_num);

        // participant recyclerView, adapter 설정
        participantsRecyclerView = bottomSheetParticipantsView.findViewById(R.id.conf_participants_recyclerView);
        participantsRecyclerView.setHasFixedSize(true);
        participantsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        participantsRecyclerView.setOnTouchListener((v, event) -> {
            Log.d(tag_check, "bottomSheetChatMessagesView ontouch!");
            if (this.getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                bottomSheetChatMessagesView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                chatInput.clearFocus();
            }
            return false;
        });

        participantsAdapter = new Adapter_conf_each_participants(context);
        participantsRecyclerView.setAdapter(participantsAdapter);

        bottomSheetBehavior_participants = BottomSheetBehavior.from(bottomSheetParticipantsLayout);
        bottomSheetBehavior_participants.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior_participants.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            ViewGroup.LayoutParams closeBtnLayoutParams = bottomSheetParticipants_closeBtn.getLayoutParams();

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                adjustBottomSheetHeight(bottomSheetParticipantsView, newState);
                bottomSheetParticipants_headerTxt.setText("참가자(" + localVar_NumberOfParticipants + ")");
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetParticipants_closeBtn.setVisibility(View.VISIBLE);
                    bottomSheetParticipants_headerTxt.setGravity(Gravity.CENTER);
                    bottomSheetParticipants_closeBtn.setEnabled(true);
                    closeBtnLayoutParams.width = 70;
                    closeBtnLayoutParams.height = 70;
                    bottomSheetBehavior_participants.setDraggable(false);
                } else {
                    bottomSheetParticipants_closeBtn.setVisibility(View.INVISIBLE);
                    bottomSheetParticipants_headerTxt.setGravity(Gravity.LEFT);
                    bottomSheetParticipants_closeBtn.setEnabled(false);
                    closeBtnLayoutParams.width = 0;
                    closeBtnLayoutParams.height = 0;
                    bottomSheetBehavior_participants.setDraggable(true);
                }
                bottomSheetParticipants_closeBtn.setLayoutParams(closeBtnLayoutParams);
                bottomSheetParticipants_closeBtn.setOnClickListener(v -> {
                    bottomSheetBehavior_participants.setState(BottomSheetBehavior.STATE_HIDDEN);
                });
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        conferenceRoomParticipantsBtn = findViewById(R.id.conferenceRoomParticipantsBtn);
        conferenceRoomParticipantsBtn.setOnClickListener(v -> {
            bottomSheetBehavior_participants.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        });
    }

    // 채팅 목록 bottomSheet 설정
    public void setChatMessagesBottomSheet() {
        View bottomSheetChatMessagesLayout = findViewById(R.id.conferenceRoom_chat_messages_BottomSheet);   // include layout
        bottomSheetChatMessagesView = bottomSheetChatMessagesLayout.findViewById(R.id.bottom_sheet_dialog_conf_chat_messages_layout);   // bottom_sheet_dialog_conf_chat_messages_layout xml file

        // chatMessage recyclerView, adapter 설정
        chatMessageRecyclerView = bottomSheetChatMessagesLayout.findViewById(R.id.conf_chat_messages_recyclerView);
        chatMessageRecyclerView.setHasFixedSize(true);
        chatMessageRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        chatMessageRecyclerView.setOnTouchListener((v, event) -> {
            Log.d(tag_check, "bottomSheetChatMessagesView onTouch!");
            if (this.getCurrentFocus() != null) {
                Log.d(tag_check, "getCurrentFocus");
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                bottomSheetChatMessagesView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                Log.d(tag_check, "check layoutparams match parent : " + ViewGroup.LayoutParams.MATCH_PARENT);
                chatInput.clearFocus();
                Log.d(tag_check, "focus cleared");
            }
            return false;
        });

        chatMessageAdapter = new Adapter_conf_each_chat_message(context);
        chatMessageRecyclerView.setAdapter(chatMessageAdapter);

        // keyboard 표시될 때 recyclerView 높이 조절
        bottomSheetChatMessagesView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            //Log.d(tag_check, "onGlobalLayout");
            // Determine the visibility of the keyboard
            Rect r = new Rect();
            bottomSheetChatMessagesView.getWindowVisibleDisplayFrame(r);

            int keypadHeight = screenHeight - r.bottom;
            //Log.d(tag_check, "check keypadHeight : " + keypadHeight);

            // 채팅 bottomSheet 이 완전히 열려 있을 때만 실행
            if (chatModalOpenStat == 2) {
                // Adjust the RecyclerView's height
                if (keypadHeight > screenHeight * 0.15) {
                    Log.d(tag_check, "reduce view's height");
                    bottomSheetChatMessagesView.getLayoutParams().height = screenHeight - keypadHeight;
                    Log.d(tag_check, "check height : " + (screenHeight - keypadHeight));
                } else {
                    Log.d(tag_check, "expand view's height");
                    bottomSheetChatMessagesView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    Log.d(tag_check, "check height : " + ViewGroup.LayoutParams.MATCH_PARENT);
                    chatInput.clearFocus();
                    Log.d(tag_check, "focus cleared");
                }
            }
        });

        chatInput = bottomSheetChatMessagesLayout.findViewById(R.id.bottom_sheet_dialog_conf_chat_messages_input);
        chatInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d(tag_check, "chatInput OnFocusChangeListener!");
                bottomSheetBehavior_chat_message.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        chatInput.addTextChangedListener(chatMessageTextWatcher);   // onCreate 내부에 설정해 주기

        sendChatBtn = bottomSheetChatMessagesLayout.findViewById(R.id.bottom_sheet_dialog_conf_chat_messages_sendBtn);
        sendChatBtn.setOnClickListener(v -> {
            sendChatMessage();
        });

        ImageView bottomSheetChatMessages_closeBtn = bottomSheetChatMessagesLayout.findViewById(R.id.bottom_sheet_dialog_conf_chat_messages_close_btn);
        ImageView bottomSheetChatMessages_alarmBtn = bottomSheetChatMessagesLayout.findViewById(R.id.bottom_sheet_dialog_conf_chat_messages_alarm_btn);
        TextView bottomSheetChatMessages_headerTxt = bottomSheetChatMessagesLayout.findViewById(R.id.bottom_sheet_dialog_conf_chat_message_header_txt);

        bottomSheetBehavior_chat_message = BottomSheetBehavior.from(bottomSheetChatMessagesLayout);
        bottomSheetBehavior_chat_message.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior_chat_message.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            ViewGroup.LayoutParams closeBtnLayoutParams = bottomSheetChatMessages_closeBtn.getLayoutParams();
            ViewGroup.LayoutParams alarmBtnLayoutParams = bottomSheetChatMessages_alarmBtn.getLayoutParams();

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                adjustBottomSheetHeight(bottomSheetChatMessagesView, newState);

                chatRedDot.setVisibility(View.INVISIBLE);
                localVar_numberOfUnreadChat = 0;

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    chatModalOpenStat = 2;
                    bottomSheetChatMessages_closeBtn.setVisibility(View.VISIBLE);
                    bottomSheetChatMessages_alarmBtn.setVisibility(View.VISIBLE);
                    bottomSheetChatMessages_headerTxt.setGravity(Gravity.CENTER);
                    bottomSheetBehavior_chat_message.setDraggable(false);

                    closeBtnLayoutParams.width = 70;
                    closeBtnLayoutParams.height = 70;
                    alarmBtnLayoutParams.width = 70;
                    alarmBtnLayoutParams.height = 70;
                } else if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    chatModalOpenStat = 1;
                } else {
                    chatModalOpenStat = 0;
                    bottomSheetChatMessages_closeBtn.setVisibility(View.INVISIBLE);
                    bottomSheetChatMessages_alarmBtn.setVisibility(View.INVISIBLE);
                    bottomSheetChatMessages_headerTxt.setGravity(Gravity.LEFT);
                    closeBtnLayoutParams.width = 0;
                    closeBtnLayoutParams.height = 0;
                    alarmBtnLayoutParams.width = 0;
                    alarmBtnLayoutParams.height = 0;
                    chatInput.clearFocus();
                    bottomSheetBehavior_chat_message.setDraggable(true);
                }

                bottomSheetChatMessages_closeBtn.setLayoutParams(closeBtnLayoutParams);
                bottomSheetChatMessages_alarmBtn.setLayoutParams(alarmBtnLayoutParams);
                chatMessageRecyclerView.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        // 채팅 알람 설정 버튼 이벤트 등록
        bottomSheetChatMessages_alarmBtn.setOnClickListener(v -> {
            isChatNotificationOn = !isChatNotificationOn;

            if (isChatNotificationOn) {
                bottomSheetChatMessages_alarmBtn.setImageResource(R.drawable.ic_notification_on);
            } else {
                bottomSheetChatMessages_alarmBtn.setImageResource(R.drawable.ic_notification_off);
            }
            Log.d(tag_check, "isChatNotificationOn : " + isChatNotificationOn);
        });

        conferenceRoomChatMessageBtn = findViewById(R.id.conferenceRoomMessageBtn);
        conferenceRoomChatMessageBtn.setOnClickListener(v -> {
            chatMessageRecyclerView.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
            bottomSheetBehavior_chat_message.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        });
        bottomSheetChatMessages_closeBtn.setOnClickListener(v -> {
            bottomSheetBehavior_chat_message.setState(BottomSheetBehavior.STATE_HIDDEN);
            chatMessage = "";
        });
    }

    // bottomSheet Layout 높이 조절하기
    public void adjustBottomSheetHeight(View targetBottomSheetView, int newState) {
        // Handle state changes
        //Log.d(tag_check, "onStateChanged. newState : " + newState);
        ViewGroup.LayoutParams layoutParams = targetBottomSheetView.getLayoutParams();

        // Handle state changes
                /* Behavior state
                1.STATE_DRAGGING : 드래깅되고 있는 상태
                2.STATE_SETTLING : 드래그/스와이프 직후 고정된 상태
                3.STATE_EXPANDED : 완전히 펼쳐진 상태
                4.STATE_COLLAPSED : 접혀있는 상태
                5.STATE_HIDDEN : 아래로 숨겨진 상태 (보이지 않음)
                6.STATE_HALF_EXPANDED : 절반으로 펼쳐진 상태
                */
        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            Log.d(tag_check, "STATE_EXPANDED");
            layoutParams.height = (int) (screenHeight * bottomSheetAdjustConst);
            targetBottomSheetView.setLayoutParams(layoutParams);
        } else if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            Log.d(tag_check, "STATE_HALF_EXPANDED");
            layoutParams.height = (int) (screenHeight * bottomSheetAdjustConst * 0.5);
            targetBottomSheetView.setLayoutParams(layoutParams);
        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            Log.d(tag_check, "STATE_COLLAPSED");
            layoutParams.height = (int) (screenHeight * bottomSheetAdjustConst * 0.66);
            targetBottomSheetView.setLayoutParams(layoutParams);
        } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            Log.d(tag_check, "STATE_HIDDEN");
            layoutParams.height = (int) (screenHeight * bottomSheetAdjustConst * 0.5);
            targetBottomSheetView.setLayoutParams(layoutParams);
        }
    }

    // PeerConnection 객체 생성
    public PeerConnection createMyPeerConnection(String peerConnectionID, String peerSocketId) {
        Log.d(tag_execute, "createMyPeerConnection. peerConnectionID : " + peerConnectionID);
        //Log.d(tag_check, "check PeerConnection : " + peerConnection);
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();

        String URL_STUN = "stun:stun.l.google.com:19305";
        iceServers.add(new PeerConnection.IceServer(URL_STUN));

        String URL_coTURN = "turn:3.37.128.14:3478";
        String coTURN_username = "doheon";
        String coTURN_password = "_G7B+AH5";

        PeerConnection.IceServer turnServer = new PeerConnection.IceServer(URL_coTURN, coTURN_username, coTURN_password);
        Log.d(tag_check, "turnServer : " + turnServer);
        iceServers.add(new PeerConnection.IceServer(URL_coTURN, coTURN_username, coTURN_password));
        Log.d(tag_check, "iceServers : " + iceServers);

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        Log.d(tag_check, "rtcConfig : " + rtcConfig);
        Log.d(tag_check, "pcConstraints : " + pcConstraints);

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(tag_event, "peerConnection.onIceCandidate. iceCandidate : " + iceCandidate);
                JSONObject iceCandidateObj = new JSONObject();

                try {
                    iceCandidateObj.put(getString(R.string.type), getString(R.string.candidate));
                    iceCandidateObj.put(getString(R.string.peerConnectionID), peerConnectionID);
                    iceCandidateObj.put(getString(R.string.from), mSocket.id());
                    iceCandidateObj.put("from(name)", userName);    // for check
                    iceCandidateObj.put(getString(R.string.to), peerSocketId);
                    iceCandidateObj.put(getString(R.string.id), iceCandidate.sdpMid);
                    iceCandidateObj.put(getString(R.string.label), iceCandidate.sdpMLineIndex);
                    iceCandidateObj.put(getString(R.string.sdp), iceCandidate.sdp);

                    mSocket.emit(getString(R.string.iceCandidate), iceCandidateObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(tag_event, "peerConnection.onRenegotiationNeeded");
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                Log.d(tag_event, "peerConnection.onAddTrack");
                Log.d(tag_check, "mediaStreams : " + mediaStreams);
                Log.d(tag_check, "check videoTracks : " + mediaStreams[0].videoTracks);
                Log.d(tag_check, "check preservedTracks : " + mediaStreams[0].preservedVideoTracks);
            }

            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(tag_event, "peerConnection.onSignalingChange. signalingState : " + signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(tag_event, "peerConnection.onIceConnectionChange. iceConnectionState : " + iceConnectionState);

                // Does not work on android emulators yet.
                if (iceConnectionState.toString().equals("CONNECTED")) {
                    try {
                        JSONObject data = new JSONObject();
                        data.put(getString(R.string.from), mSocket.id());
                        data.put(getString(R.string.to), peerSocketId);
                        data.put(getString(R.string.is_connected), true);
                        mSocket.emit(getString(R.string.connected_to_peer), data);
                        Log.d(tag_check, "send data : " + data);
                        // for check
                        mHandler.post(() -> {
                            //Toast.makeText(context, peerSocketId + "와 연결되었습니다!", Toast.LENGTH_LONG).show();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(tag_event, "peerConnection.onIceConnectionReceivingChange. boolean : " + b);
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(tag_event, "peerConnection.onIceGatheringChange. iceGatheringState : " + iceGatheringState);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(tag_event, "peerConnection.onIceCandidatesRemoved. iceCandidates : " + iceCandidates);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(tag_event, "peerConnection.onAddStream. mediaStream : " + mediaStream);

                int videoTrackSize = mediaStream.videoTracks.size();
                int audioTrackSize = mediaStream.audioTracks.size();
                Log.d(tag_check, "check videoTrackSize : " + videoTrackSize);
                Log.d(tag_check, "check audioTrackSize : " + audioTrackSize);

                Log.d(tag_check, "peerSocketId : " + peerSocketId);
                Log.d(tag_check, "videoTracks : " + mediaStream.videoTracks);
                Log.d(tag_check, "audioTracks : " + mediaStream.audioTracks);

                viewPager.main_addRemoteStream(mediaStream, peerSocketId);
                viewPager.participants_addStream(mediaStream, peerSocketId);
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(tag_event, "peerConnection.onRemoveStream. mediaStream : " + mediaStream);
                VideoTrack myViewVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(false);
                myViewVideoTrack.setEnabled(false);
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(tag_event, "peerConnection.onDataChannel. dataChannel : " + dataChannel);
            }
        };

        PeerConnection peerConnection = factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
        Log.d(tag_check, "createMyPeerConnection finished!");

        MediaStream mediaStream = factory.createLocalMediaStream(mediaStreamLabel);
        Log.d(tag_check, "localVideoRendered : " + localVideoRendered);

        viewPager.main_addLocalStream(mediaStream);

        if (!localVideoRendered) {
            viewPager.participants_addStream(mediaStream, mSocket.id());
            localVideoRendered = true;
        }

        Log.d(tag_check, "check videoTracks : " + mediaStream.videoTracks);
        Log.d(tag_check, "check preservedTracks : " + mediaStream.preservedVideoTracks);

        peerConnection.addStream(mediaStream);
        Log.d(tag_check, "peerConnection.addStream. mediaStream : " + mediaStream);

        peerConnectionList.add(peerConnectionID, peerSocketId, peerConnection);
        Log.d(tag_check, "check peerConnnection : " + peerConnectionList.get(peerConnectionID));
        Log.d(tag_check, "check peerConnectionList : " + peerConnectionList);
        return peerConnection;
    }

    // 화면공유 시 필요한 PeerConnection 객체 새로 생성
    public PeerConnection recreatePeerConnection(String peerConnectionID, String peerSocketId, boolean isScreenSharer) {
        Log.d(tag_execute, "recreateMyPeerConnection. peerConnectionID : " + peerConnectionID);
        //Log.d(tag_check, "check PeerConnection : " + peerConnection);
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();

        String URL_STUN = "stun:stun.l.google.com:19305";
        iceServers.add(new PeerConnection.IceServer(URL_STUN));

        String URL_coTURN = "turn:3.37.128.14:3478";
        String coTURN_username = "doheon";
        String coTURN_password = "_G7B+AH5";

        PeerConnection.IceServer turnServer = new PeerConnection.IceServer(URL_coTURN, coTURN_username, coTURN_password);
        Log.d(tag_check, "turnServer : " + turnServer);
        iceServers.add(new PeerConnection.IceServer(URL_coTURN, coTURN_username, coTURN_password));
        Log.d(tag_check, "iceServers : " + iceServers);

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        Log.d(tag_check, "rtcConfig : " + rtcConfig);
        Log.d(tag_check, "pcConstraints : " + pcConstraints);

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(tag_event, "peerConnection.onIceCandidate. iceCandidate : " + iceCandidate);
                JSONObject iceCandidateObj = new JSONObject();

                try {
                    iceCandidateObj.put(getString(R.string.type), getString(R.string.candidate));
                    iceCandidateObj.put(getString(R.string.peerConnectionID), peerConnectionID);
                    iceCandidateObj.put(getString(R.string.from), mSocket.id());
                    iceCandidateObj.put("from(name)", userName);    // for check
                    iceCandidateObj.put(getString(R.string.to), peerSocketId);
                    iceCandidateObj.put(getString(R.string.id), iceCandidate.sdpMid);
                    iceCandidateObj.put(getString(R.string.label), iceCandidate.sdpMLineIndex);
                    iceCandidateObj.put(getString(R.string.sdp), iceCandidate.sdp);

                    mSocket.emit(getString(R.string.iceCandidate), iceCandidateObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(tag_event, "peerConnection.onRenegotiationNeeded");
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                Log.d(tag_event, "peerConnection.onAddTrack");
                Log.d(tag_check, "mediaStreams : " + mediaStreams);
                Log.d(tag_check, "check videoTracks : " + mediaStreams[0].videoTracks);
                Log.d(tag_check, "check preservedTracks : " + mediaStreams[0].preservedVideoTracks);
            }

            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(tag_event, "peerConnection.onSignalingChange. signalingState : " + signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(tag_event, "peerConnection.onIceConnectionChange. iceConnectionState : " + iceConnectionState);
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(tag_event, "peerConnection.onIceConnectionReceivingChange. boolean : " + b);
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(tag_event, "peerConnection.onIceGatheringChange. iceGatheringState : " + iceGatheringState);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(tag_event, "peerConnection.onIceCandidatesRemoved. iceCandidates : " + iceCandidates);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(tag_event, "peerConnection.onAddStream. mediaStream : " + mediaStream);

                int videoTrackSize = mediaStream.videoTracks.size();
                int audioTrackSize = mediaStream.audioTracks.size();
                Log.d(tag_check, "check videoTrackSize : " + videoTrackSize);
                Log.d(tag_check, "check audioTrackSize : " + audioTrackSize);

                Log.d(tag_check, "peerSocketId : " + peerSocketId);
                Log.d(tag_check, "videoTracks : " + mediaStream.videoTracks);
                Log.d(tag_check, "audioTracks : " + mediaStream.audioTracks);

                Log.d(tag_check, "isScreenSharer : " + isScreenSharer);
                String sharerName = participantsAdapter.getParticipant(peerSocketId).getName();
                Log.d(tag_check, "screenSharerName : " + sharerName);

                if (isScreenSharer) {
                    sharerName = "";
                } else {
                    sharerName = participantsAdapter.getParticipant(peerSocketId).getName();
                }
                viewPager.main_SCREENSHARE_addRemoteStream(mediaStream, peerSocketId, isScreenSharer, sharerName);
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(tag_event, "peerConnection.onRemoveStream. mediaStream : " + mediaStream);
                VideoTrack myViewVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(false);
                myViewVideoTrack.setEnabled(false);
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(tag_event, "peerConnection.onDataChannel. dataChannel : " + dataChannel);
            }
        };

        PeerConnection peerConnection = factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
        Log.d(tag_check, "createMyPeerConnection finished!");

        MediaStream mediaStream = factory.createLocalMediaStream(mediaStreamLabel);
        Log.d(tag_check, "localVideoRendered : " + localVideoRendered);


        Log.d(tag_check, "isAddingScreenShareStream true");
        viewPager.main_SCREENSHARE_addLocalStream(mediaStream, isScreenSharer);
        Log.d(tag_check, "addLocalStream is done. check mediaStream videoTracks : " + mediaStream.videoTracks);

        Log.d(tag_check, "check videoTracks : " + mediaStream.videoTracks);
        Log.d(tag_check, "check preservedTracks : " + mediaStream.preservedVideoTracks);

        peerConnection.addStream(mediaStream);
        Log.d(tag_check, "peerConnection.addStream. mediaStream : " + mediaStream);

        peerConnectionList.add(peerConnectionID, peerSocketId, peerConnection);
        Log.d(tag_check, "check peerConnectionList : " + peerConnectionList);

        return peerConnection;
    }

    // 여러 개의 PeerConnection 을 저장하는 리스트 생성 class
    public class PeerConnectionList {
        private Map<String, PeerConnection> list;
        private List<PeerConnectionIdObj> pcIdList;  // key 값으로 사용되는 peerConnectionID 를 저장하는 list

        // 리스트 생성
        public PeerConnectionList() {
            list = new HashMap<>();
            pcIdList = new ArrayList<>();
        }

        // 새로운 참석자가 접속했을 때 peerConnection 생성하여 추가. key 값은 별도의 peerConnectionID 로 설정.
        public void add(String peerConnectionID, String peerSocketId, PeerConnection peerConnection) {
            // Add the PeerConnection to the container
            list.put(peerConnectionID, peerConnection);
            pcIdList.add(new PeerConnectionIdObj(peerConnectionID, peerSocketId));
        }

        // 리스트에 있는 peerConnection 불러오기
        public PeerConnection get(String key) {
            return list.get(key);
        }

        // 리스트에 있는 peerConnection 삭제하기
        public void remove(String key) {
            PeerConnection peerConnection = list.remove(key);
            Iterator<PeerConnectionIdObj> iterator = pcIdList.iterator();

            while (iterator.hasNext()) {
                PeerConnectionIdObj eachObj = iterator.next();
                if (eachObj.mainId.equals(key)) {
                    iterator.remove(); // Removes the current element
                }
            }

            if (peerConnection != null) {
                // Clean up and close the PeerConnection if needed
                peerConnection.close();
            }
        }
    }

    // PeerConnection 의 고유 ID, remote peer socket id 를 저장하기 위한 객체
    public class PeerConnectionIdObj {
        public String mainId;
        public String remotePeerSocketId;

        public PeerConnectionIdObj(String param_mainId, String param_remotePeerSocketId) {
            this.mainId = param_mainId;
            this.remotePeerSocketId = param_remotePeerSocketId;
        }
    }

    // SdpObserver 에서 꼭 필요한 기능만 남긴 새로운 SdpObserver
    public class SimpleSdpObserver implements SdpObserver {

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String s) {
        }

        @Override
        public void onSetFailure(String s) {
        }
    }

    public void addNewStreamAndRenegotiate() {
        Log.d(tag_execute, "addNewStreamAndRenegotiate");
        Log.d(tag_check, "check peerConnectionList : " + peerConnectionList);
        //Log.d(tag_check, "peerConnection pcIdList : " + peerConnectionList.pcIdList);

        int listSize = peerConnectionList.pcIdList.size();
        Log.d(tag_check, "listSize : " + listSize);

        for (int i = 0; i < listSize; i++) {
            Log.d(tag_check, "peerConnection index : " + i);

            PeerConnectionIdObj peerConnectionIdObj = peerConnectionList.pcIdList.get(i);
            String remotePeerSocketId = peerConnectionIdObj.remotePeerSocketId;
            String oldPeerConnectionID = peerConnectionIdObj.mainId;
            String newPeerConnectionID = UUID.randomUUID().toString();
            screenSharingPcID = newPeerConnectionID;
            Log.d(tag_check, "oldPeerConnectionID : " + oldPeerConnectionID);
            Log.d(tag_check, "remotePeerSocketId : " + remotePeerSocketId);

            //PeerConnection eachPeerConnection = peerConnectionList.get(peerConnectionID);

            PeerConnection eachPeerConnection = recreatePeerConnection(newPeerConnectionID, remotePeerSocketId, true);
            Log.d(tag_check, "eachPeerConnection : " + eachPeerConnection);

            MediaConstraints sdpMediaConstraints = new MediaConstraints();
            sdpMediaConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
            sdpMediaConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
            Log.d(tag_check, "sdpMediaConstraints : " + sdpMediaConstraints);

            eachPeerConnection.createOffer(new SimpleSdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    Log.d(tag_event, "peerConnection offer onCreateSuccess");
                    eachPeerConnection.setLocalDescription(new SimpleSdpObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.d(tag_check, "offer setLocalDescription onSetSuccess");
                            JSONObject offer = new JSONObject();
                            try {
                                offer.put(getString(R.string.type), getString(R.string.offer));
                                offer.put(getString(R.string.from), mSocket.id());
                                offer.put(getString(R.string.to), remotePeerSocketId);
                                offer.put(getString(R.string.oldPeerConnectionID), oldPeerConnectionID);
                                offer.put(getString(R.string.peerConnectionID), newPeerConnectionID);
                                offer.put(getString(R.string.sdp), sessionDescription.description);

                                mSocket.emit(getString(R.string.offer_screenShare), offer);
                                Log.d(tag_execute, "offer_screenShare event emitted");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, sessionDescription);
                }
            }, sdpMediaConstraints);
            Log.d(tag_check, "addStream done");

            //peerConnectionList.remove(oldPeerConnectionID);
            Log.d(tag_check, "remove old peerConnection. ID : " + oldPeerConnectionID);
        }
    }

    // 회의 정보(회의 ID, 암호) DB 에 저장
    public void createConferenceData() {
        Log.d(tag_execute, "createConferenceData");
        String hashedConferenceId = Sha256_hash.hexString(localVar_conferenceId);
        String hashedConferencePassword = Sha256_hash.hexString(localVar_conferencePassword);

        // 요청 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                Log.d(tag_check, "createConferenceData success. data : " + responseObj.data);
            }
        }

        // 요청 실패 시 실행할 callback
        class Callee_failed extends httpRequestAPIs.Callee_failed {
            public void call(int statusCode) {
                Log.d(tag_check, "createConferenceData failed. statusCode : " + statusCode);
                dialog_confirm.show(context, "에러가 발생하였습니다.", null);

            }
        }
        httpRequestAPIs.createConferenceData(hashedConferenceId, hashedConferencePassword, new Callee_success(), new Callee_failed());
    }

    // 모두에 대해 회의 종료하기
    public void endConference() {
        Log.d(tag_check, "endConference");

        // 회의 종료 이벤트 emit 후 소켓 연결 종료
        mSocket.emit(getString(R.string.end_conference));
        mSocket.disconnect();

        // 회의 데이터 삭제하기
        Log.d(tag_execute, "deleteConferenceData");
        String hashedConferenceId = Sha256_hash.hexString(localVar_conferenceId);

        // DB 에서 방송 데이터 삭제 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                Log.d(tag_check, "deleteConferenceData success!");
            }
        }

        // 요청 실패 시 실행할 callback
        class Callee_failed extends httpRequestAPIs.Callee_failed {
            public void call(int statusCode) {
                Log.d(tag_check, "deleteConferenceData failed. statusCode : " + statusCode);
                dialog_confirm.show(context, "에러가 발생하였습니다.", null);
            }
        }
        httpRequestAPIs.deleteConferenceData(hashedConferenceId, new Callee_success(), new Callee_failed());

        finish();
    }

    // 비디오 on/off 설정
    public void setVideoOnOff() {
        Log.d(tag_check, "setVideoOnOff");
        try {
            JSONObject dataObj = new JSONObject();
            dataObj.put(getString(R.string.senderSocketId), mySocketId);
            dataObj.put(getString(R.string.video_status), !isVideoOn);
            Log.d(tag_check, "dataObj : " + dataObj);

            if (isVideoOn) {
                Log.d(tag_check, "video was on");
                conferenceRoomVideoOnOffBtnImg.setBackgroundResource(R.drawable.ic_video_off);
                conferenceRoomVideoOnOffBtnTxt.setText("비디오 시작");
                Log.d(tag_check, "set video off");
            } else {
                Log.d(tag_check, "video was off");
                conferenceRoomVideoOnOffBtnImg.setBackgroundResource(R.drawable.ic_video_on);
                conferenceRoomVideoOnOffBtnTxt.setText("비디오 중지");
                Log.d(tag_check, "set video on");
            }
            mSocket.emit(getString(R.string.switch_video_status), dataObj);
            isVideoOn = !isVideoOn;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 마이크 on/off 설정
    public void setMicOnOff() {
        Log.d(tag_check, "setMicOnOff");
        try {
            JSONObject dataObj = new JSONObject();
            dataObj.put(getString(R.string.senderSocketId), mySocketId);
            dataObj.put(getString(R.string.mic_status), !isMicOn);
            Log.d(tag_check, "dataObj : " + dataObj);

            if (isMicOn) {
                Log.d(tag_check, "mic was on");
                conferenceRoomMicOnOffBtnImg.setImageResource(R.drawable.ic_mic_off);
                conferenceRoomMicOnOffBtnTxt.setText("음소거 해제");
                Log.d(tag_check, "set mic off");
            } else {
                Log.d(tag_check, "mic was off");
                conferenceRoomMicOnOffBtnImg.setImageResource(R.drawable.ic_mic_on);
                conferenceRoomMicOnOffBtnTxt.setText("음소거");
                Log.d(tag_check, "set mic on");
            }
            mSocket.emit(getString(R.string.switch_mic_status), dataObj);
            isMicOn = !isMicOn;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 화면 공유하기 옵션 설정
    public void setShareBtn() {
        Log.d(tag_check, "setShareBtn");

        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        conferenceRoomShareBtnImg = findViewById(R.id.conferenceRoomShareBtn);  // 공유 버튼 img
        conferenceRoomShareBtnTxt = findViewById(R.id.conferenceRoomShareBtnTxt);   // 공유 버튼 caption text

        sharingOptionDialog = findViewById(R.id.conferenceRoom_share_screen_options);   // 공유 버튼 클릭 시 나타나는 dialog
        sharingOptionDialog.setVisibility(View.INVISIBLE);

        bottomTabView = findViewById(R.id.conferenceRoomBottomTab);
        bottomTabView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int bottomTabViewHeight = bottomTabView.getHeight(); // Get the height

                CoordinatorLayout.LayoutParams optionDialogLayoutParams = (CoordinatorLayout.LayoutParams) sharingOptionDialog.getLayoutParams();
                optionDialogLayoutParams.setMargins(0, 0, 0, bottomTabViewHeight);
                optionDialogLayoutParams.gravity = Gravity.BOTTOM;
                sharingOptionDialog.setLayoutParams(optionDialogLayoutParams);
                sharingOptionDialog.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // dialog 내 공유 옵션 항목
        optionScreen = sharingOptionDialog.findViewById(R.id.dialogSharingOptions_screen);
        optionWhiteBoard = sharingOptionDialog.findViewById(R.id.dialogSharingOptions_whiteBoard);
        optionCancel = sharingOptionDialog.findViewById(R.id.dialogSharingOptions_cancel);

        // 공유하기 버튼 이벤트 등록
        conferenceRoomShareBtnImg.setOnClickListener(v -> {
            Log.d(tag_check, "conferenceRoomShareBtnImg onclick");
            Log.d(tag_check, "isSharingOn : " + isSharingOn);
            if (!isSharingOn) {
                Log.d(tag_check, "open sharing option Modal");
                sharingOptionDialog.setVisibility(View.VISIBLE);    // 모달 열기
                conferenceRoomShareBtnImg.setClickable(false);
                // 모달 열고 옵션 선택, stream 교환 후 webRTC 재연결 -> 완료 시 버튼 이미지, 텍스트 변경하기
            } else {
                /*
                Log.d(tag_check, "stop sharing");
                // 화면 공유 중지하기 -> stream 교환 후 webRTC 재연결 -> 완료 시 버튼 이미지, 텍스트 변경하기
                conferenceRoomShareBtnImg.setImageResource(R.drawable.ic_share);
                conferenceRoomShareBtnTxt.setText("공유");
                conferenceRoomShareBtnImg.setClickable(true);
                isSharingOn = false;
                 */
            }
        });

        // 각 옵션 클릭 이벤트 등록
        optionScreen.setOnClickListener(view -> {
            Log.d(tag_check, "option screen clicked");
            shareOption_screen();
        });
        optionWhiteBoard.setOnClickListener(view -> {
            Log.d(tag_check, "option whiteboard clicked");
            shareOption_whiteBoard();
        });
        optionCancel.setOnClickListener(view -> {
            Log.d(tag_check, "option cancel clicked");
            sharingOptionDialog.setVisibility(View.INVISIBLE);
            conferenceRoomShareBtnImg.setClickable(true);
        });
    }

    // 화면 공유하기 옵션 각 항목 공통설정 부분
    public void setSharingOptionDialog_common() {
        sharingOptionDialog.setVisibility(View.INVISIBLE);
        conferenceRoomShareBtnImg.setImageResource(R.drawable.ic_stop_share);
        conferenceRoomShareBtnTxt.setText("공유 중지");
        isSharingOn = true;
        conferenceRoomShareBtnImg.setClickable(true);
    }

    // 공유 옵션 : 화면 공유하기
    public void shareOption_screen() {
        Log.d(tag_check, "shareOption_screen");
        Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
        Log.d(tag_check, "check permissionIntent : " + permissionIntent);
        startActivityForResult(permissionIntent, 1);
    }

    // 공유 옵션 : 화이트보드 공유하기
    public void shareOption_whiteBoard() {
        Log.d(tag_check, "shareOption_whiteBoard");
        Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
        Log.d(tag_check, "check permissionIntent : " + permissionIntent);
        startActivityForResult(permissionIntent, 1);
        isSharingBoard = true;
    }

    // 화면 공유 권한 요청 결과 처리하기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Log.d(tag_event, "shareOption onActivityResult");
        Log.d(tag_check, "requestCode : " + 1 + ", resultCode : " + resultCode);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.d(tag_check, "RESULT_OK. permission granted");

                Intent serviceIntent = new Intent(context, ScreenCaptureNotification.class);
                serviceIntent.putExtra("resultCode", resultCode);
                serviceIntent.putExtra("resultData", resultData);
                ContextCompat.startForegroundService(context, serviceIntent);
                setSharingOptionDialog_common();    // 버튼 텍스트, 이미지, isSharing 값 변경

                // 화면공유 중지 버튼 설정
                conferenceRoomShareBtnImg.setOnClickListener(view -> {
                    Log.d(tag_execute, "stop sharing screen");
                    screenSharingServiceIntent = new Intent(this, ScreenCaptureNotification.class);

                    try {
                        JSONObject data = new JSONObject();
                        data.put(getString(R.string.participantSocketId), mySocketId);
                        mSocket.emit(getString(R.string.stopScreenSharing), data);
                        boolean renderMyself = localVar_NumberOfParticipants == 1;  // 참가자가 자신 혼자인지 아닌지 판별.
                        Log.d(tag_check, "renderMyself : " + renderMyself);
                        viewPager.main_SCREENSHARE_sharer_stopSharing(renderMyself);

                        if (isSharingBoard) {
                            viewPager.main_closeWhiteBoard();
                            isSharingBoard = false;
                        }

                        stopService(screenSharingServiceIntent);
                        peerConnectionList.remove(screenSharingPcID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 화면 공유 중지하기 -> stream 교환 후 webRTC 재연결 -> 완료 시 버튼 이미지, 텍스트 변경하기
                    conferenceRoomShareBtnImg.setImageResource(R.drawable.ic_share);
                    conferenceRoomShareBtnTxt.setText("공유");
                    conferenceRoomShareBtnImg.setClickable(true);
                    isSharingOn = false;

                    // 화면 공유 중지되었을 때 다시 공유 옵션 모달 열기 처리 해 주기
                    conferenceRoomShareBtnImg.setOnClickListener(v -> {
                        Log.d(tag_check, "open sharing option Modal");
                        sharingOptionDialog.setVisibility(View.VISIBLE);    // 모달 열기
                        conferenceRoomShareBtnImg.setClickable(false);
                    });
                });
            } else {
                Log.d(tag_check, "permission denied");
            }
        }
    }

    // 채팅 메시지 보내기
    public void sendChatMessage() {
        Log.d(tag_execute, "sendChatMessage");
        if (chatMessage.length() != 0) {
            try {
                Log.d(tag_check, "sendChat start");
                JSONObject chatMessageObj = new JSONObject();
                chatMessageObj.put(getString(R.string.senderName), userName);
                chatMessageObj.put(getString(R.string.senderProfileImg), profileImg);
                chatMessageObj.put(getString(R.string.chatMessageContents), chatMessage);
                Log.d(tag_check, "#message_chatMessageObj : " + chatMessageObj);
                mSocket.emit(getString(R.string.new_chat_message), chatMessageObj);
                Log.d(tag_check, "sendChat finished");
            } catch (Exception e) {
                e.printStackTrace();
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    chatMessage = "";
                    chatInput.setText("");
                }
            });
        }
    }

    // 소켓 초기 설정
    public void setSocket() {
        Log.d(tag_execute, "setSocket");
        try {
            // socket 생성 시 적용해 줄 option
            IO.Options mOptions = new IO.Options();

            mOptions.query = "userName=" + userName;
            mOptions.query += "&isHost=" + isHost;
            mOptions.query += "&conferenceId=" + localVar_conferenceId;
            Log.d(tag_check, "setSocket mOptions query : " + mOptions.query);

            String signalingServer_URL = httpRequestAPIs.webRTC_Signaling_Server_URL;
            Log.d(tag_check, "check signalingServer_URL : " + signalingServer_URL);

            mSocket = IO.socket(signalingServer_URL, mOptions);
            Log.d(tag_check, "mSocket : " + mSocket);

            /****************************** emit 이벤트 리스너 등록 *********************************/

            // 연결 시
            mSocket.on(getString(R.string.connect), args -> {
                Log.d(tag_socket_event, "connect");
                Log.d(tag_check, "my socket id : " + mSocket.id());
                try {
                    JSONObject data = new JSONObject();
                    data.put(getString(R.string.profileImg), profileImg);
                    data.put(getString(R.string.isMicOn), isMicOn);
                    data.put(getString(R.string.isVideoOn), isVideoOn);

                    mySocketId = mSocket.id();
                    Log.d(tag_check, "mySocketId : " + mySocketId);

                    viewPager.main_captureStreamFromCamera();    // 연결과 동시에 camera 로 영상 capture 해서 보여주기
                    if (isHost) {
                        Log.d(tag_check, "data : " + data);
                        mSocket.emit(getString(R.string.create_new_conference), data);
                        Log.d(tag_execute, "create new conference emitted");
                    } else {
                        data.put(getString(R.string.participantSocketId), mSocket.id());
                        Log.d(tag_check, "data : " + data);
                        mSocket.emit(getString(R.string.request_join), data);
                        Log.d(tag_execute, "request join emitted");
                    }
                    chatMessageAdapter.setUserSocketId(mSocket.id());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 호스트가 새 회의 생성 완료
            mSocket.on(getString(R.string.conference_created), args -> {
                Log.d(tag_socket_event, "conference created");
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "data : " + data);
                    localVar_conferenceId = data.getString(getString(R.string.conferenceId));
                    Log.d(tag_check, "localVar_conferenceId : " + localVar_conferenceId);
                    localVar_NumberOfParticipants = 1;
                    localVar_conferencePassword = data.getString(getString(R.string.conferencePassword));
                    Log.d(tag_check, "localVar_conferencePassword : " + localVar_conferencePassword);

                    // 참가자 목록에 호스트 정보 추가
                    Part_conf_each_participant participantObj = new Part_conf_each_participant(
                            mSocket.id(),
                            userName,
                            profileImg,
                            true,
                            true,
                            true
                    );
                    Log.d(tag_check, "participantObj : " + participantObj);
                    participantsAdapter.add(participantObj);

                    createConferenceData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 새로운 참석자 입장 요청
            mSocket.on(getString(R.string.request_join), args -> {
                Log.d(tag_socket_event, "request join");

                try {
                    JSONObject data = (JSONObject) args[0];
                    String hostSocketId = data.getString(getString(R.string.hostSocketId));
                    String participantSocketId = data.getString(getString(R.string.participantSocketId));

                    Log.d(tag_check, "check JSONObject data : " + data);
                    Log.d(tag_check, "check mSocket id : " + mSocket.id());

                    // 호스트일 경우, 참가자가 자기 자신이 아닐 경우(자신 외에 다른 참가자가 입장 시)
                    if (isHost
                            || !mSocket.id().equals(participantSocketId)
                    ) {
                        Log.d(tag_check, "host or participant is not myself");
                        Log.d(tag_check, "hostSocketId : " + hostSocketId);
                        Log.d(tag_check, "participantSocketId : " + participantSocketId);

                        String peerConnectionID = UUID.randomUUID().toString();
                        Log.d(tag_check, "peerConnectionID : " + peerConnectionID);

                        // 호스트, 신규 사용자일 경우에만 peerConnection 객체 생성해 주기
                        // peerConnection offer 생성을 위한 constraints
                        MediaConstraints sdpMediaConstraints = new MediaConstraints();
                        sdpMediaConstraints.mandatory.add(
                                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                        sdpMediaConstraints.mandatory.add(
                                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                        Log.d(tag_check, "sdpMediaConstraints : " + sdpMediaConstraints);

                        createMyPeerConnection(peerConnectionID, participantSocketId);

                        // peerConnection offer 생성 후 전송
                        PeerConnection peerConnection = peerConnectionList.get(peerConnectionID);
                        Log.d(tag_check, "check peerConnection from list : " + peerConnection);

                        Thread thread = new Thread(() -> {
                            try {
                                peerConnection.createOffer(new SimpleSdpObserver() {
                                    @Override
                                    public void onCreateSuccess(SessionDescription sessionDescription) {
                                        Log.d(tag_event, "peerConnection offer onCreateSuccess");
                                        peerConnection.setLocalDescription(new SimpleSdpObserver() {
                                            @Override
                                            public void onSetSuccess() {
                                                Log.d(tag_check, "offer setLocalDescription onSetSuccess");
                                                JSONObject offer = new JSONObject();
                                                try {
                                                    offer.put(getString(R.string.type), getString(R.string.offer));
                                                    offer.put(getString(R.string.from), mSocket.id());
                                                    //offer.put("from(name)", userName);  // for check
                                                    offer.put(getString(R.string.to), participantSocketId);
                                                    offer.put(getString(R.string.peerConnectionID), peerConnectionID);
                                                    offer.put(getString(R.string.sdp), sessionDescription.description);
                                                    mSocket.emit(getString(R.string.offer), offer);
                                                    Log.d(tag_execute, "offer event emited");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, sessionDescription);
                                    }
                                }, sdpMediaConstraints);
                                // do something
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        thread.start();
                    }
                } catch (Exception e) {
                    Log.d(tag_check, "error : " + e);
                    e.printStackTrace();
                }
            });

            // offer 수신
            mSocket.on(getString(R.string.offer), args -> {
                Log.d(tag_socket_event, "offer");

                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);

                    String remotePeerSocketId = data.getString(getString(R.string.from)); // offer 보낸 사람 socket ID
                    String localPeerSocketId = data.getString(getString(R.string.to)); // offer 받는 사람 socket ID
                    String peerConnectionID = data.getString(getString(R.string.peerConnectionID));

                    createMyPeerConnection(peerConnectionID, remotePeerSocketId);
                    PeerConnection peerConnection = peerConnectionList.get(peerConnectionID);
                    Log.d(tag_check, "check peerConnectionList : " + peerConnectionList);
                    peerConnection.setRemoteDescription(new SimpleSdpObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.d(tag_check, "offer setRemoteDescription onSetSuccess");
                        }
                    }, new SessionDescription(SessionDescription.Type.OFFER, data.getString(getString(R.string.sdp))));

                    peerConnection.createAnswer(new SimpleSdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {
                            peerConnection.setLocalDescription(new SimpleSdpObserver() {
                                @Override
                                public void onSetSuccess() {
                                    Log.d(tag_check, "answer setLocalDescription onSetSuccess");
                                    Log.d(tag_check, "answer receiver : " + remotePeerSocketId);
                                    JSONObject answer = new JSONObject();
                                    try {
                                        answer.put(getString(R.string.type), getString(R.string.answer));
                                        answer.put(getString(R.string.from), localPeerSocketId.equals("") ? mySocketId : localPeerSocketId);
                                        answer.put("from(name)", userName); // for check
                                        answer.put(getString(R.string.to), remotePeerSocketId);
                                        answer.put(getString(R.string.peerConnectionID), peerConnectionID);
                                        answer.put(getString(R.string.sdp), sessionDescription.description);
                                        mSocket.emit(getString(R.string.answer), answer);
                                        Log.d(tag_execute, "answer emitted");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, sessionDescription);
                        }
                    }, new MediaConstraints());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // answer 수신
            mSocket.on(getString(R.string.answer), args -> {
                Log.d(tag_socket_event, "answer");
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);
                    String peerConnectionID = data.getString(getString(R.string.peerConnectionID));

                    PeerConnection peerConnection = peerConnectionList.get(peerConnectionID);
                    peerConnection.setRemoteDescription(new SimpleSdpObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.d(tag_check, "answer setRemoteDescription onSetSuccess");
                        }
                    }, new SessionDescription(SessionDescription.Type.ANSWER, data.getString(getString(R.string.sdp))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // offer_screenShare 수신
            mSocket.on(getString(R.string.offer_screenShare), args -> {
                Log.d(tag_socket_event, "offer_screenShare");

                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);

                    String remotePeerSocketId = data.getString(getString(R.string.from)); // offer 보낸 사람 socket ID
                    String localPeerSocketId = data.getString(getString(R.string.to)); // offer 받는 사람 socket ID
                    String oldPeerConnectionID = data.getString(getString(R.string.oldPeerConnectionID));
                    String newPeerConnectionID = data.getString(getString(R.string.peerConnectionID));

                    screenSharingPcID = newPeerConnectionID;

                    PeerConnection peerConnection = recreatePeerConnection(newPeerConnectionID, remotePeerSocketId, false);
                    Log.d(tag_check, "peerConnection : " + peerConnection);

                    peerConnection.setRemoteDescription(new SimpleSdpObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.d(tag_check, "offer setRemoteDescription onSetSuccess");
                        }
                    }, new SessionDescription(SessionDescription.Type.OFFER, data.getString(getString(R.string.sdp))));

                    peerConnection.createAnswer(new SimpleSdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {
                            Log.d(tag_check, "answer receiver : " + remotePeerSocketId);
                            peerConnection.setLocalDescription(new SimpleSdpObserver() {
                                @Override
                                public void onSetSuccess() {
                                    Log.d(tag_check, "answer setLocalDescription onSetSuccess");
                                    Log.d(tag_check, "answer receiver : " + remotePeerSocketId);
                                    JSONObject answer = new JSONObject();
                                    try {
                                        answer.put(getString(R.string.type), getString(R.string.answer));
                                        answer.put(getString(R.string.from), localPeerSocketId.equals("") ? mySocketId : localPeerSocketId);
                                        answer.put(getString(R.string.to), remotePeerSocketId);
                                        answer.put(getString(R.string.peerConnectionID), newPeerConnectionID);
                                        answer.put(getString(R.string.sdp), sessionDescription.description);
                                        mSocket.emit(getString(R.string.answer_screenShare), answer);
                                        Log.d(tag_execute, "answer_screenShare emitted");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, sessionDescription);
                        }
                    }, new MediaConstraints());

                    //peerConnectionList.remove(oldPeerConnectionID);
                    Log.d(tag_check, "remove oldPeerConnection. ID : " + oldPeerConnectionID);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // answer_screenShare 수신
            mSocket.on(getString(R.string.answer_screenShare), args -> {
                Log.d(tag_socket_event, "answer_screenShare");
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);
                    String peerConnectionID = data.getString(getString(R.string.peerConnectionID));

                    PeerConnection peerConnection = peerConnectionList.get(peerConnectionID);
                    Log.d(tag_check, "peerConnection : " + peerConnection);

                    peerConnection.setRemoteDescription(new SimpleSdpObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.d(tag_check, "answer setRemoteDescription onSetSuccess");
                        }
                    }, new SessionDescription(SessionDescription.Type.ANSWER, data.getString(getString(R.string.sdp))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 호스트, 참석자가 iceCandidate 수신
            mSocket.on(getString(R.string.iceCandidate), args -> {
                Log.d(tag_socket_event, "iceCandidate");
                try {
                    JSONObject data = (JSONObject) args[0];
                    String peerConnectionID = data.getString(getString(R.string.peerConnectionID));
                    String id = data.getString(getString(R.string.id));
                    int label = data.getInt(getString(R.string.label));
                    String sdp = data.getString(getString(R.string.sdp));
                    String from = data.getString("from(name)");    // for check

                    Log.d(tag_check, "candidate sender name : " + from);

                    PeerConnection peerConnection = peerConnectionList.get(peerConnectionID);
                    Log.d(tag_check, "peerConnectionID : " + peerConnectionID);
                    if (peerConnection != null) {
                        Log.d(tag_check, "has peerConnection");
                        IceCandidate candidate = new IceCandidate(id, label, sdp);
                        Log.d(tag_check, "check candidate : " + candidate);
                        peerConnection.addIceCandidate(candidate);
                        Log.d(tag_execute, "peerConnection.addIceCandidate done.");
                    } else {
                        Log.d(tag_check, "peerConnection is null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 새로운 참석자 입장 처리
            mSocket.on(getString(R.string.participant_joined), args -> {
                Log.d(tag_socket_event, "participant joined");
                try {
                    // 참가자 목록에 호스트 정보 추가
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);
                    String conferencePassword = data.getString(getString(R.string.conferencePassword));
                    String hostName = data.getString(getString(R.string.hostName));
                    String newParticipantSocketId = data.getString(getString(R.string.newParticipantSocketId));
                    String participantsList = data.getString("participantsList");
                    Log.d(tag_check, "conferencePassword : " + conferencePassword);
                    Log.d(tag_check, "participantsList : " + participantsList);
                    Log.d(tag_check, "newParticipantSocketId : " + newParticipantSocketId);

                    Gson gson = new Gson();
                    String jsonStr = participantsList.replaceAll(":", "="); // Replace ":" with "=" to make it valid JSON
                    Type listType = new TypeToken<List<Part_conf_each_participant>>() {
                    }.getType();
                    List<Part_conf_each_participant> newParticipantsList = gson.fromJson(jsonStr, listType);
                    Log.d(tag_check, "converted newParticipantsList : " + newParticipantsList);

                    mHandler.post(() -> {
                        participantsAdapter.reRenderData(newParticipantsList);
                        hostCandidateAdapter.reRenderData(newParticipantsList);
                        viewPager.participants_addSurfaceView(newParticipantSocketId);
                    });

                    localVar_conferencePassword = conferencePassword;
                    localVar_NumberOfParticipants = newParticipantsList.size();
                    localVar_hostName = hostName;

                } catch (Exception e) {
                    Log.d(tag_check, "error : " + e);
                    e.printStackTrace();
                }
            });

            // 참석자 퇴장
            mSocket.on(getString(R.string.participant_left), args -> {
                Log.d(tag_socket_event, "participant left");
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);
                    String peerConnectionID = data.getString(getString(R.string.peerConnectionID));
                    String participantSocketId = data.getString(getString(R.string.participantSocketId));

                    peerConnectionList.remove(peerConnectionID);  // 해당 참석자와 연결되어 있었던 peerConnection 제거
                    participantsAdapter.deleteItem(participantSocketId);
                    localVar_NumberOfParticipants -= 1;

                    //viewPager.main_editMainViewVideoTrack();    // 참석자가 나가면 회의 메인 화면 내 카메라 비디오로 랜더링 해 주기
                    viewPager.main_removeLeftParticipantsTrack(participantSocketId);
                    viewPager.participants_removeSurfaceView(participantSocketId);  // 참석자들 화면에서 나간 참석자 비디오 view 제거하기

                    if (isHost) {
                        hostCandidateAdapter.deleteItem(participantSocketId);
                    }
                } catch (Exception e) {
                    Log.d(tag_check, "error! : " + e);
                    e.printStackTrace();
                }
            });

            // 호스트가 전체 회의 종료(participants only)
            mSocket.on(getString(R.string.end_conference), args -> {
                Log.d(tag_socket_event, "end conference");
                try {
                    mSocket.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
                mHandler.post(() -> Toast.makeText(context, "회의가 종료되었습니다!", Toast.LENGTH_SHORT).show());
            });

            // 호스트가 회의 나감(participants only)
            mSocket.on(getString(R.string.host_left), args -> {
                Log.d(tag_socket_event, "host left");
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);
                    String oldHostSocketId = data.getString(getString(R.string.hostSocketId));
                    String newHostSocketId = data.getString(getString(R.string.newHostSocketId));
                    String newHostName = data.getString(getString(R.string.newHostName));
                    String peerConnectionID = data.getString(getString(R.string.peerConnectionID));

                    peerConnectionList.remove(peerConnectionID);  // 이전 호스트와 연결되어 있었던 peerConnection 제거
                    localVar_hostName = newHostName;
                    localVar_NumberOfParticipants -= 1;

                    viewPager.main_removeLeftParticipantsTrack(oldHostSocketId);
                    viewPager.participants_removeSurfaceView(oldHostSocketId);

                    // 새로운 호스트 할당하기
                    if (mySocketId.equals(newHostSocketId)) {
                        Log.d(tag_check, "I'm the new host!");
                        isHost = true;
                        setExitConferenceBtns();
                    }

                    participantsAdapter.deleteItem(oldHostSocketId);
                    participantsAdapter.assignNewHost(newHostSocketId);
                    hostCandidateAdapter.reRenderData(null);
                    mHandler.postDelayed(() -> viewPager.participants_setNewHost(), 300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 채팅 수신
            mSocket.on(getString(R.string.new_chat_message), args -> {
                Log.d(tag_socket_event, "new chat message");
                mHandler.post(() -> {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        Log.d(tag_check, "check JSONObject data : " + data);

                        String senderSocketId = data.getString(getString(R.string.senderSocketId));
                        //Log.d(tag_check, "senderSocketId : " + senderSocketId);
                        String senderName = data.getString(getString(R.string.senderName));
                        String senderProfileImg = data.getString(getString(R.string.senderProfileImg));
                        String chatMessageContents = data.getString(getString(R.string.chatMessageContents));

                        // chat message 추가
                        chatMessageAdapter.add(senderSocketId, senderName, senderProfileImg, chatMessageContents);
                        chatMessageRecyclerView.scrollToPosition(chatMessageAdapter.getItemCount() - 1);

                        // 채팅 알람 on 상태이고 채팅 bottomSheet 이 닫혀 있을 때 채팅 알림 dialog 보여주기
                        if (isChatNotificationOn && chatModalOpenStat == 0) {
                            dialog_new_chat.show(context, senderName, senderProfileImg, chatMessageContents);
                            Log.d(tag_check, "numberOfUnreadChat : " + localVar_numberOfUnreadChat);
                            if (localVar_numberOfUnreadChat < 10) {
                                Log.d(tag_check, "setText start");
                                localVar_numberOfUnreadChat += 1;
                                chatRedDot.setText(String.valueOf(localVar_numberOfUnreadChat));
                                Log.d(tag_check, "setText done");
                            } else {
                                Log.d(tag_check, "setText start");
                                chatRedDot.setText("+" + localVar_numberOfUnreadChat);
                                Log.d(tag_check, "setText done");
                            }
                            chatRedDot.setVisibility(View.VISIBLE);
                        }
                        //dialog_new_chat.show(context, senderName, senderProfileImg, chatMessageContents); // for test
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });

            // 참석자 비디오 on/off 이벤트 수신
            mSocket.on(getString(R.string.switch_video_status), args -> {
                Log.d(tag_socket_event, "switch video status");
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);
                    String senderSocketId = data.getString(getString(R.string.senderSocketId));
                    boolean videoStatus = data.getBoolean(getString(R.string.video_status));
                    Log.d(tag_check, "senderSocketId : " + senderSocketId);
                    Log.d(tag_check, "videoStatus : " + videoStatus);
                    viewPager.setVideoOnOff(videoStatus, senderSocketId);
                    participantsAdapter.editVideoStatus(senderSocketId, videoStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 참석자 마이크 on/off 이벤트 수신
            mSocket.on(getString(R.string.switch_mic_status), args -> {
                Log.d(tag_socket_event, "switch mic status");
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);
                    String senderSocketId = data.getString(getString(R.string.senderSocketId));
                    boolean micStatus = data.getBoolean(getString(R.string.mic_status));
                    Log.d(tag_check, "senderSocketId : " + senderSocketId);
                    Log.d(tag_check, "micStatus : " + micStatus);
                    viewPager.setMicOnOff(micStatus, senderSocketId);
                    participantsAdapter.editMicStatus(senderSocketId, micStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 화면 공유하던 참석자가 화면 공유 중단
            mSocket.on(getString(R.string.stopScreenSharing), args -> {
                Log.d(tag_socket_event, "stopScreenSharing");
                try {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(tag_check, "check JSONObject data : " + data);
                    String participantSocketId = data.getString(getString(R.string.participantSocketId));
                    Log.d(tag_check, "participantSocketId : " + participantSocketId);

                    viewPager.main_SCREENSHARE_removeScreenSharingTrack(participantSocketId);

                    peerConnectionList.remove(screenSharingPcID);
                    Log.d(tag_check, "peerConnection remove");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 소켓 연결하기
            mSocket.connect();
        } catch (
                URISyntaxException e) {
            Log.d(tag_check, "Error! " + e);
            e.printStackTrace();
        }
    }

    public final TextWatcher chatMessageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            chatMessage = s.toString();
            Log.d(tag_check, "chatMessage : " + chatMessage);
            if (!chatMessage.equals("")) {
                Log.d(tag_check, "has text");
                sendChatBtn.setAlpha(0.9F);
                sendChatBtn.setClickable(true);
            } else {
                Log.d(tag_check, "no text");
                sendChatBtn.setAlpha(0.4F);
                sendChatBtn.setClickable(false);
            }
        }
    };

    // fragment 로부터 알람을 받을 Broadcast Receiver
    public BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(fragment_conference_main_video.SCREEN_SHARED)) {
                Log.d(tag_check, "notificationReceiver. action: SCREEN_SHARED");
                if (isSharingBoard) {
                    viewPager.main_openWhiteBoard();
                }
                // 미리 화면공유가 되지 않은 경우에 peerConnection 에 mediaStream 추가해 주기
                if (localVar_NumberOfParticipants > 1) {
                    Log.d(tag_check, "start renegotiate. check peerConnectionList : " + peerConnectionList);
                    addNewStreamAndRenegotiate();
                }
            }
            else if (intent.getAction() != null && intent.getAction().equals(fragment_conference_main_video.SCREEN_TOUCHED)) {
                Log.d(tag_check, "notificationReceiver2. action : SCREEN_TOUCHED");

                int headerViewVisibility = headerView.getVisibility();
                Log.d(tag_check, "headerViewVisibility : " + headerViewVisibility);
                int bottomViewVisibility = bottomTabView.getVisibility();
                Log.d(tag_check, "bottomViewVisibility : " + bottomViewVisibility);

                if (headerViewVisibility == View.VISIBLE && bottomViewVisibility == View.VISIBLE) {
                    Log.d(tag_check, "header and bottom are visible.");
                    headerView.setVisibility(View.INVISIBLE);
                    bottomTabView.setVisibility(View.INVISIBLE);
                } else {
                    Log.d(tag_check, "header and bottom are invisible.");
                    headerView.setVisibility(View.VISIBLE);
                    bottomTabView.setVisibility(View.VISIBLE);
                }
                Log.d(tag_check, "visibility convert done.");
                Log.d(tag_check, "headerViewVisibility : " + headerViewVisibility);
                Log.d(tag_check, "bottomViewVisibility : " + bottomViewVisibility);
            }
        }
    };

    @Override
    public void onBackPressed() {
        // Do nothing to disable the back button
    }
}

