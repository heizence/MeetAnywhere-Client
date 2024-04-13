package com.example.meetanywhere.Fragments;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;

import org.webrtc.CapturerObserver;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.meetanywhere.Activities.conference_room;
import com.example.meetanywhere.Modules.CustomViewPager;
import com.example.meetanywhere.R;
import com.example.meetanywhere.Services.ScreenCaptureNotification;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

public class fragment_conference_main_video extends Fragment {
    private String screenName = "[FRAGMENT fragment_conference_main_video]:";
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context;
    private Activity activity;
    private Handler mHandler;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private View fragmentView;

    private View rootLayout;
    private SurfaceViewRenderer mainView; // 회의 메인 화면 view
    private org.webrtc.SurfaceViewRenderer myView;   // 내 화면 view
    private View myViewWrapper;
    private VideoTrack mainViewVideoTrack;
    private SurfaceTextureHelper surfaceTextureHelper;
    private SurfaceTextureHelper surfaceTextureHelper_forShare;
    private TextView screenShareOnTxt;
    private TextView screenSharerNameTxt;

    private MediaProjectionManager mediaProjectionManager;
    private MediaConstraints audioConstraints;
    private VideoCapturer videoCapturer;    // camera 캡쳐를 위한 capturer
    private VideoCapturer screenCapturer;   // 화면 캡쳐를 위한 capturer

    private VideoSource videoSource;
    private int videoResolutionWidth = 500;
    private int videoResolutionHeight = 500;
    private float videoRatio;   // Calculated by screenHeight / screenWidth
    private int FPS = 30;
    private String VIDEO_TRACK_ID = "ARDAMSv0";
    private String currentVideoStreamId;    // 현재 화면에 표시되고 있는 비디오 track id(유저 socketId)
    private boolean isRemotePeerSharingScreen = false;

    public static String SCREEN_SHARED = "SCREEN_SHARED";   // 화면공유 완료 시 conference_room 에 보내줄 broadcast 메시지
    public static String SCREEN_TOUCHED = "SCREEN_TOUCHED";   // 화면터치 시 conference_room 에 보내줄 broadcast 메시지

    public fragment_conference_main_video(
            Activity activity
    ) {
        this.activity = activity;
        this.context = activity.getBaseContext();
    }

    // 화면 공유 시 service 에서 권한 허용 완료 후 진행할 작업
    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context_, Intent intent) {
            Log.d(tag_event, "ScreenCapture onReceive");
            Log.d(tag_check, "currentVideoStreamId : " + currentVideoStreamId);

            //VideoTrack mainViewVideoTrack = CustomViewPager.videoTrackObj.get(currentVideoStreamId);
            Log.d(tag_check, "mainViewVideoTrack : " + mainViewVideoTrack);
            if (mainViewVideoTrack != null) {
                Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);
                Log.d(tag_check, "check mainViewVideoTrack : " + mainViewVideoTrack);
                mainViewVideoTrack.removeSink(mainView);
                Log.d(tag_check, "remove current video");
            }

            if (intent.getAction() != null &&
                    intent.getAction().equals(ScreenCaptureNotification.ACTION_SCREEN_CAPTURE_PERMITTED)) {
                // Handle the received data
                Log.d(tag_event, "ACTION_SCREEN_CAPTURE_PERMITTED");

                DisplayMetrics metrics = activity.getResources().getDisplayMetrics();

                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;

                mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                Log.d(tag_check, "create mediaProjectionManager : " + mediaProjectionManager);

                // Get the result code and data from the intent
                int resultCode = intent.getIntExtra("resultCode", -1);
                Intent resultData = intent.getParcelableExtra("resultData");
                String type = intent.getStringExtra("type");
                Log.d(tag_check, "resultCode : " + resultCode);
                Log.d(tag_check, "resultData : " + resultData);
                Log.d(tag_check, "check type : " + type);

                if (screenCapturer == null) {
                    Log.d(tag_check, "screenCapturer is null");
                    screenCapturer = new ScreenCapturerAndroid(
                            resultData, new MediaProjection.Callback() {
                        @Override
                        public void onStop() {
                            Log.d(tag_event, "onStop");
                        }
                    });
                    Log.d(tag_check, "screenCapturer : " + screenCapturer);
                    videoSource = conference_room.factory.createVideoSource(screenCapturer.isScreencast());
                    Log.d(tag_execute, "create videoSource : " + videoSource);
                    screenCapturer.initialize(surfaceTextureHelper_forShare, activity.getApplicationContext(), videoSource.getCapturerObserver());
                    Log.d(tag_check, "screenCapturer initialize done");
                }
                screenCapturer.startCapture(screenWidth, screenHeight, 30);
                mainView.setMirror(false);
                Log.d(tag_check, "screenCapturer startCapture");

                // 화면공유 VideoTrack 생성
                VideoTrack newVideoTrack = conference_room.factory.createVideoTrack("SCREEN_SHARE", videoSource);
                Log.d(tag_check, "newVideoTrack : " + newVideoTrack);
                String videoTrackId = conference_room.mySocketId;
                CustomViewPager.videoTrackObj.put_ScreenShareTrack(videoTrackId, newVideoTrack);
                Log.d(tag_check, "check videoTrack : " + CustomViewPager.videoTrackObj.get_ScreenShareTrack(videoTrackId));

                currentVideoStreamId = videoTrackId;
                newVideoTrack.addSink(mainView);
                mainViewVideoTrack = newVideoTrack;

                // 화면 공유중 텍스트 표시해 주기
                screenShareOnTxt.setVisibility(View.VISIBLE);
                mainView.setBackgroundColor(context.getResources().getColor(R.color.black));

                // conference_room 에 화면공유 완료 메시지 보내기
                Intent sendMessageIntent = new Intent(SCREEN_SHARED);
                Log.d(tag_check, "sendMessageIntent : " + sendMessageIntent);

                if (getActivity() != null) {
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(sendMessageIntent);
                } else {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(sendMessageIntent);
                }
                Log.d(tag_check, "message sent");
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag_execute, "LifeCycle onCreate");
        IntentFilter intentFilter = new IntentFilter(ScreenCaptureNotification.ACTION_SCREEN_CAPTURE_PERMITTED);
        activity.registerReceiver(dataReceiver, intentFilter);
        fragmentManager = getActivity().getSupportFragmentManager();
        mHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(tag_execute, "LifeCycle onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_conference_main_video, container, false);
        rootLayout = fragmentView.findViewById(R.id.f_conference_main_video_layout);
        mainView = fragmentView.findViewById(R.id.conferenceRoomMainView);
        myView = fragmentView.findViewById(R.id.conferenceRoomMyView);
        myViewWrapper = fragmentView.findViewById(R.id.conferenceRoomMyViewWrapper);
        screenShareOnTxt = fragmentView.findViewById(R.id.screenShareOnTxt);
        screenSharerNameTxt = fragmentView.findViewById(R.id.screenSharerNameTxt);
        initializeSurfaceViews();

        mainView.setOnClickListener(v->{
            Log.d(tag_event, "mainView onClick");
            Intent dataIntent = new Intent(SCREEN_TOUCHED);
            dataIntent.putExtra("resultCode", 1);
            LocalBroadcastManager.getInstance(context).sendBroadcast(dataIntent);
            Log.d(tag_check, "sendBroadCast");
        });
        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag_execute, "LifeCycle onDestroy");
        try {
            if (videoCapturer != null) {
                videoCapturer.stopCapture();
            }
            if (screenCapturer != null) {
                screenCapturer.stopCapture();
            }
            CustomViewPager.videoTrackObj.removeAll();
            CustomViewPager.audioTrackObj.removeAll();

            mainView.release();
            myView.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 영상 화면 표시할 surfaceView 설정
    public void initializeSurfaceViews() {
        Log.d(tag_execute, "initializeSurfaceViews");

        // 메인 화면 설정
        mainView.init(conference_room.rootEglBase.getEglBaseContext(), new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
                Log.d(tag_event, "onFirstFrameRendered");
            }

            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
            }
        });
        mainView.setEnableHardwareScaler(true);
        mainView.setMirror(false);

        // 내 비디오 화면 설정
        myView.init(conference_room.rootEglBase.getEglBaseContext(), null);
        myView.setEnableHardwareScaler(true);
        myView.setMirror(false);

        surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), conference_room.rootEglBase.getEglBaseContext());
        surfaceTextureHelper_forShare = SurfaceTextureHelper.create(Thread.currentThread().getName(), conference_room.rootEglBase.getEglBaseContext());
    }

    // 기기 카메라에서 영상 데이터 불러와서 표시해 주기
    public void createVideoTrackFromCameraAndShowIt() {
        Log.d(tag_execute, "createVideoTrackFromCameraAndShowIt");

        audioConstraints = new MediaConstraints();
        Log.d(tag_check, "context : " + context);
        Boolean useCamera2 = Camera2Enumerator.isSupported(context);
        Log.d(tag_check, "useCamera2 : " + useCamera2);
        if (useCamera2) {
            Log.d(tag_check, "useCamera2 supported");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(context));
        } else {
            Log.d(tag_check, "useCamera2 not supported");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }

        Log.d(tag_check, "create videoCapturer : " + videoCapturer);
        VideoSource videoSource = conference_room.factory.createVideoSource(videoCapturer.isScreencast());
        Log.d(tag_check, "videoSource : " + videoSource);

        videoCapturer.initialize(surfaceTextureHelper, activity.getApplicationContext(), videoSource.getCapturerObserver());
        Log.d(tag_check, "videoCapturer initialize done.");

        if (videoSource != null) {
            Log.d(tag_check, "has videoSource");
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Log.d(tag_check, "activity : " + activity);
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            videoRatio = screenHeight / screenWidth;
            Log.d(tag_check, "videoRatio : " + videoRatio);
            Log.d(tag_check, "videoCapturer : " + videoCapturer);
            videoCapturer.startCapture(videoResolutionWidth, videoResolutionHeight, FPS);

            VideoTrack localVideoTrack = conference_room.factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            localVideoTrack.setEnabled(true);
            Log.d(tag_check, "localVideoTrack : " + localVideoTrack);

            if (conference_room.isHost) {
                Log.d(tag_check, "mainView : " + mainView);
                localVideoTrack.addSink(mainView);

                Log.d(tag_check, "mainView addSink");
            }

            localVideoTrack.addSink(myView);
            Log.d(tag_check, "myView addSink");

            Log.d(tag_check, "check mySocketId : " + conference_room.mySocketId);
            CustomViewPager.videoTrackObj.put(conference_room.mySocketId, localVideoTrack);
            Log.d(tag_check, "check videoTrackObj : " + CustomViewPager.videoTrackObj.get(conference_room.mySocketId));
            currentVideoStreamId = conference_room.mySocketId;
            mainViewVideoTrack = localVideoTrack;   // for test
            Log.d(tag_check, "currentVideoStreamId : " + currentVideoStreamId);

            //create an AudioSource instance
            AudioSource audioSource = conference_room.factory.createAudioSource(audioConstraints);
            AudioTrack localAudioTrack = conference_room.factory.createAudioTrack("101", audioSource);
            Log.d(tag_check, "audioSource : " + audioSource);
            Log.d(tag_check, "localAudioTrack : " + localAudioTrack);
            CustomViewPager.audioTrackObj.put(conference_room.mySocketId, localAudioTrack);
            Log.d(tag_check, "check audioTrackObj : " + CustomViewPager.audioTrackObj.get(conference_room.mySocketId));
            Log.d(tag_check, "video and audio set!");
        } else {
            Log.d(tag_check, "no videoSource");
        }
    }

    // CameraCapturer 객체 생성
    public VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        Log.d(tag_execute, "createCameraCapturer");
        final String[] deviceNames = enumerator.getDeviceNames();

        VideoCapturer videoCapturer;

        for (String deviceName : deviceNames) {
            // 전면 카메라 이용
            Log.d(tag_check, "use front camera");
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d(tag_check, "front camera exist!");
                videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        Log.d(tag_check, "camera doesn't exist or other problem occurred!");
        return null;
    }

    // local camera stream 추가하기
    public void addLocalStream(MediaStream mediaStream) {
        Log.d(tag_execute, "addLocalStream : " + mediaStream);
        AudioTrack localAudioTrack = CustomViewPager.audioTrackObj.get(conference_room.mySocketId);
        Log.d(tag_check, "localAudioTrack : " + localAudioTrack);

        Log.d(tag_check, "add camera track");
        VideoTrack localCameraVideoTrack = CustomViewPager.videoTrackObj.get(conference_room.mySocketId);
        Log.d(tag_check, "localCameraVideoTrack : " + localCameraVideoTrack);
        mediaStream.addTrack(localCameraVideoTrack);
        mediaStream.addTrack(localAudioTrack);    // Disabled for silence. Remove caption when needed for test

        if (conference_room.isSharingOn) {
            Log.d(tag_check, "isSharingOn true. add screenSharing track");
            VideoTrack localScreenShareVideoTrack = CustomViewPager.videoTrackObj.get_ScreenShareTrack(conference_room.mySocketId);
            Log.d(tag_check, "localScreenShareVideoTrack : " + localScreenShareVideoTrack);
            mediaStream.addPreservedTrack(localScreenShareVideoTrack);
        }

        Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);
    }

    // remote peer camera stream 추가하기
    public void addRemoteStream(MediaStream mediaStream, String socketId) {
        Log.d(tag_execute, "addRemoteStream : " + mediaStream);
        Log.d(tag_check, "socketId : " + socketId);

        if (mediaStream.videoTracks.size() != 0) {
            //VideoTrack mainViewVideoTrack;
            Log.d(tag_check, "isSharingOn : " + conference_room.isSharingOn);
            if (conference_room.isSharingOn) {
                Log.d(tag_check, "has sharing stream");
                if (currentVideoStreamId.equals(conference_room.mySocketId)) {
                    Log.d(tag_check, "currentVideoStreamId is my socketId");
                    //mainViewVideoTrack = CustomViewPager.videoTrackObj.get_ScreenShareTrack(currentVideoStreamId);
                } else {
                    Log.d(tag_check, "currentVideoStreamId is remote peer's socketId");
                    //mainViewVideoTrack = CustomViewPager.videoTrackObj.get(currentVideoStreamId);
                }

            } else {
                //mainViewVideoTrack = CustomViewPager.videoTrackObj.get(currentVideoStreamId);
            }

            Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);
            Log.d(tag_check, "check mainViewVideoTrack : " + mainViewVideoTrack);
            mainViewVideoTrack.removeSink(mainView);
            Log.d(tag_check, "mainViewVideoTrack removeSink");

            // 화면공유 stream 이 있는지 판별하여 알맞은 track 랜더링 해 주기
            int videoTrackSize = mediaStream.videoTracks.size();
            int audioTrackSize = mediaStream.audioTracks.size();

            VideoTrack newVideoTrack;
            currentVideoStreamId = socketId;

            if (videoTrackSize != audioTrackSize) {
                Log.d(tag_check, "screenSharing stream");
                newVideoTrack = mediaStream.videoTracks.get(videoTrackSize - 1);
                VideoTrack cameraTrack = mediaStream.videoTracks.get(0);

                CustomViewPager.videoTrackObj.put_ScreenShareTrack(socketId, newVideoTrack);
                CustomViewPager.videoTrackObj.put(socketId, cameraTrack);
            } else {
                Log.d(tag_check, "camera stream");
                newVideoTrack = mediaStream.videoTracks.get(0);
                CustomViewPager.videoTrackObj.put(socketId, newVideoTrack);
            }

            Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);
            newVideoTrack.addSink(mainView);
            mainViewVideoTrack = newVideoTrack; // for test
            Log.d(tag_check, "newVideoTrack addSink");
        }
        if (mediaStream.audioTracks.size() != 0) {
            AudioTrack newAudioTrack = mediaStream.audioTracks.get(0);
            newAudioTrack.setEnabled(true);
            Log.d(tag_check, "check newAudioTrack : " + newAudioTrack);
            CustomViewPager.audioTrackObj.put(socketId, newAudioTrack);
        }
    }

    // 퇴장한 참석자의 비디오 track 제거해 주기
    public void removeLeftParticipantsTrack(String socketId) {
        Log.d(tag_execute, "removeLeftParticipantsTrack : " + socketId);
        Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);
        //VideoTrack mainViewVideoTrack = CustomViewPager.videoTrackObj.get(socketId);
        VideoTrack screenSharingVideoTrack = CustomViewPager.videoTrackObj.get_ScreenShareTrack(socketId);
        Log.d(tag_check, "check mainViewVideoTrack : " + mainViewVideoTrack);

        // 퇴장한 참석자의 비디오가 현재 표시되고 있었던 비디오의 경우
        if (currentVideoStreamId.equals(socketId)) {
            if (mainViewVideoTrack != null) {
                mainViewVideoTrack.removeSink(mainView);
                Log.d(tag_check, "mainViewVideoTrack removeSink");

                VideoTrack newVideoTrack = CustomViewPager.videoTrackObj.get(conference_room.mySocketId);
                Log.d(tag_check, "newVideoTrack : " + newVideoTrack);
                newVideoTrack.addSink(mainView);
                mainViewVideoTrack = newVideoTrack; // for test
                Log.d(tag_check, "newVideoTrack addSink");
                currentVideoStreamId = conference_room.mySocketId;
                Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);

                // 다른 참가자가 화면 공유 중이었을 경우 화면공유 track 삭제
                Log.d(tag_check, "screenSharingVideoTrack : " + screenSharingVideoTrack);
                if (screenSharingVideoTrack != null) {
                    SCREENSHARE_removeScreenSharingTrack(socketId, true);
                }
            }
        }

        CustomViewPager.videoTrackObj.remove(socketId);
        CustomViewPager.audioTrackObj.remove(socketId);
    }

    /* ********************** 연결 후 화면공유 시 사용할 매서드 ********************** */

    // 회의 도중 화면 공유 시 화면 공유 mediaStream 만 추가하기
    public void SCREENSHARE_addLocalStream(MediaStream mediaStream, boolean isScreenSharer) {
        Log.d(tag_execute, "SCREENSHARE_addLocalStream : " + mediaStream);

        if (isScreenSharer) {
            Log.d(tag_check, "isSharingOn true. add screenSharing track");
            VideoTrack localScreenShareVideoTrack = CustomViewPager.videoTrackObj.get_ScreenShareTrack(conference_room.mySocketId);
            Log.d(tag_check, "localScreenShareVideoTrack : " + localScreenShareVideoTrack);
            mediaStream.addTrack(localScreenShareVideoTrack);
        } else {
            VideoTrack localVideoTrack = CustomViewPager.videoTrackObj.get(conference_room.mySocketId);
            Log.d(tag_check, "localVideoTrack : " + localVideoTrack);
            mediaStream.addTrack(localVideoTrack);
        }

        if (currentVideoStreamId == null) {
            Log.d(tag_check, "currentVideoStreamId is null");
            currentVideoStreamId = conference_room.mySocketId;
            Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);
        }
    }

    // 회의 도중 특정 참가자가 화면 공유를 할 때 사용
    public void SCREENSHARE_addRemoteStream(MediaStream mediaStream, String socketId, boolean isScreenSharer, String screenSharerName) {
        Log.d(tag_execute, "SCREENSHARE_addRemoteStream : " + mediaStream);
        Log.d(tag_check, "screenSharerName : " + screenSharerName);
        Log.d(tag_check, "socketId : " + socketId);

        //VideoTrack mainViewVideoTrack;
        if (isScreenSharer) {
            // 화면 공유자의 경우 remote peer 로부터 받아온 videoTrack 은 사용하지 않음.
            Log.d(tag_check, "isScreenSharer true");
            //mainViewVideoTrack = CustomViewPager.videoTrackObj.get_ScreenShareTrack(currentVideoStreamId);
            Log.d(tag_check, "check mainViewVideoTrack : " + mainViewVideoTrack);
            if (mainViewVideoTrack == null) {
                //mainViewVideoTrack = CustomViewPager.videoTrackObj.get(currentVideoStreamId);
                //Log.d(tag_check, "check another mainViewVideoTrack : " + mainViewVideoTrack);
            }
            mainViewVideoTrack.removeSink(mainView);

            VideoTrack newVideoTrack = CustomViewPager.videoTrackObj.get(socketId);
            Log.d(tag_check, "check newVideoTrack : " + newVideoTrack);
            newVideoTrack.addSink(mainView);
            mainViewVideoTrack = newVideoTrack; // for test
            currentVideoStreamId = socketId;
        } else {
            Log.d(tag_check, "isScreenSharer false");
            Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);
            //mainViewVideoTrack = CustomViewPager.videoTrackObj.get(currentVideoStreamId);
            mainViewVideoTrack.removeSink(mainView);

            Log.d(tag_check, "check videoTracks : " + mediaStream.videoTracks);
            VideoTrack newVideoTrack = mediaStream.videoTracks.get(0);
            Log.d(tag_check, "check newVideoTrack : " + newVideoTrack);
            CustomViewPager.videoTrackObj.put_ScreenShareTrack(socketId, newVideoTrack);
            newVideoTrack.addSink(mainView);
            mainViewVideoTrack = newVideoTrack; // for test
            currentVideoStreamId = socketId;
            Log.d(tag_check, "newVideoTrack addSink");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 화면 비율 조절
                    RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT;   // stream 의 모든 영역 보이게 표시. 기기 크기에 맞게 사이즈 자동 조절하기
                    mainView.setScalingType(scalingType);
                    Log.d(tag_check, "mainView setScalingType. type : " + scalingType);

                    if (!screenSharerName.equals("")) {
                        Log.d(tag_check, "screenSharerName : " + screenSharerName);
                        screenSharerNameTxt.setText(screenSharerName + "의 화면");
                        screenSharerNameTxt.setVisibility(View.VISIBLE);
                        //myViewWrapper.setVisibility(View.INVISIBLE);
                    }
                }
            }, 500);
        }
    }

    // 화면 공유하던 참가자가 화면공유 중지
    public void SCREENSHARE_sharer_stopSharing(boolean renderMyself) {
        Log.d(tag_execute, "SCREENSHARE_sharer_stopSharing");
        //VideoTrack mainViewVideoTrack = CustomViewPager.videoTrackObj.get(currentVideoStreamId);
        Log.d(tag_check, "mainViewVideoTrack : " + mainViewVideoTrack);
        Log.d(tag_check, "currentVideoStreamId : " + currentVideoStreamId);
        CustomViewPager.videoTrackObj.remove_ScreenShareTrack(currentVideoStreamId);
        mainViewVideoTrack.removeSink(mainView);
        Log.d(tag_check, "mainView removeRenderer");

        // 참가자가 자신 혼자이면 내 videoTrack 을, 다른 참가자가 같이 있으면 상대방 videoTrack 을 랜더링 해 주기
        VideoTrack newVideoTrack;
        if (renderMyself) {
            newVideoTrack = CustomViewPager.videoTrackObj.get(conference_room.mySocketId);
        } else {
            newVideoTrack = CustomViewPager.videoTrackObj.get(currentVideoStreamId);
        }

        mainViewVideoTrack = newVideoTrack; // for test
        currentVideoStreamId = conference_room.mySocketId;
        Log.d(tag_check, "check mySocketId : " + conference_room.mySocketId);
        Log.d(tag_check, "newVideoTrack : " + newVideoTrack);
        newVideoTrack.addSink(mainView);
        Log.d(tag_check, "check currentVideoStreamId : " + currentVideoStreamId);

        mainView.setBackgroundColor(getResources().getColor(R.color.transparent));

        Thread thread = new Thread(() -> {
            try {
                screenCapturer.stopCapture();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // 화면 비율 조절
                RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_BALANCED;   // stream 의 모든 영역 보이게 표시. 기기 크기에 맞게 사이즈 자동 조절하기
                mainView.setScalingType(scalingType);
                screenShareOnTxt.setVisibility(View.INVISIBLE);
                Log.d(tag_check, "mainView setScalingType. type : " + scalingType);
            }
        });
    }

    // 다른 참가자의 화면 공유 track 제거하기
    public void SCREENSHARE_removeScreenSharingTrack(String socketId, boolean isParticipantLeft) {
        Log.d(tag_execute, "removeScreenSharingTrack");
        Log.d(tag_check, "isParticipantLeft : " + isParticipantLeft);
        Log.d(tag_check, "socketId : " + socketId);
        Log.d(tag_check, "currentVideoStreamId : " + currentVideoStreamId);

        if (currentVideoStreamId.equals(socketId)) {
            //VideoTrack mainViewVideoTrack = CustomViewPager.videoTrackObj.get_ScreenShareTrack(currentVideoStreamId);
            Log.d(tag_check, "mainViewVideoTrack : " + mainViewVideoTrack);
            mainViewVideoTrack.removeSink(mainView);
            Log.d(tag_check, "mainViewVideoTrack removeSink");

            VideoTrack newVideoTrack;
            if (isParticipantLeft) {
                int videoTrackSize = CustomViewPager.videoTrackObj.getSize();
                //newVideoTrack = CustomViewPager.videoTrackObj.get(conference_room.mySocketId);
                newVideoTrack = CustomViewPager.videoTrackObj.videoTrackData.get(videoTrackSize - 1);   // 참가자가 퇴장 시 가장 마지막에 들어온 사람의 track 불러오기. 혼자 남았을 경우 내 video track 불러오기
                currentVideoStreamId = conference_room.mySocketId;
                Log.d(tag_check, "isParticipantLeft true. newVideoTrack : " + newVideoTrack);
            } else {
                newVideoTrack = CustomViewPager.videoTrackObj.get(socketId);
                currentVideoStreamId = socketId;
                Log.d(tag_check, "isParticipantLeft false. newVideoTrack : " + newVideoTrack);
            }
            newVideoTrack.addSink(mainView);
            mainViewVideoTrack = newVideoTrack; // for test
            Log.d(tag_check, "newVideoTrack addSink");
        }
        CustomViewPager.videoTrackObj.remove_ScreenShareTrack(socketId);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // 화면 비율 조절
                RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_BALANCED;   // stream 의 모든 영역 보이게 표시. 기기 크기에 맞게 사이즈 자동 조절하기
                mainView.setScalingType(scalingType);
                Log.d(tag_check, "mainView setScalingType. type : " + scalingType);
                //myViewWrapper.setVisibility(View.VISIBLE);
                screenSharerNameTxt.setVisibility(View.INVISIBLE);
            }
        });
    }

    /* ********************** 화이트보드 열고 닫기 ********************** */
    public void openWhiteBoard() {
        Log.d(tag_check, "openWhiteBoard");
        CustomViewPager.setSwipable(false);
        Fragment fragment = new fragment_conference_whiteboard(activity);
        Log.d(tag_check, "check fragment : " + fragment);

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.f_conference_main_video_layout, fragment);
        fragmentTransaction.commit();
    }

    public void closeWhiteBoard() {
        Log.d(tag_check, "closeWhiteBoard");
        CustomViewPager.setSwipable(true);
        Fragment fragment = fragmentManager.findFragmentById(R.id.f_conference_main_video_layout);
        Log.d(tag_check, "check fragment : " + fragment);
        if (fragment != null) {
            Log.d(tag_check, "fragment is not null");
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }
    }
}
