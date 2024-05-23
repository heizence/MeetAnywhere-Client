package com.example.meetanywhere.Fragments;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.meetanywhere.Activities.delete_account;
import com.example.meetanywhere.Activities.update_name;
import com.example.meetanywhere.Activities.update_password;
import com.example.meetanywhere.Modules.dialog_confirm;
import com.example.meetanywhere.Modules.dialog_edit_profile_img;
import com.example.meetanywhere.Modules.httpRequestAPIs;
import com.example.meetanywhere.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class fragment_mypage extends Fragment {
    private String screenName = "[FRAGMENT fragment_mypage]:";
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = getContext();

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

    private View fragmentView;
    private ImageView profileImgView;
    private TextView emailView;
    private TextView nameView;

    private LinearLayout editProfileMenu;
    private LinearLayout editAccountMenu;
    private LinearLayout editNameMenu;
    private LinearLayout updatePasswordMenu;

    /* 부모 fragment 또는 부모 activity 에서 파라미터를 받아오기 위한 static factory method 생성.
    파라미터를 전달받을 일이 없다면 사용하지 않아도 됨.
    */
    public static fragment_mypage newInstance(String param1, int param2, boolean param3) {
        fragment_mypage fragment = new fragment_mypage();
        //fragment.setData();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(tag_execute, "onCreate");
        super.onCreate(savedInstanceState);
        context = getContext();
        sharedPref = context.getSharedPreferences(getString(R.string.sharedPreferenceMain), context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(tag_check, "onStart");
        String email = sharedPref.getString(getString(R.string.store_U_Email), "");
        String name = sharedPref.getString(getString(R.string.store_U_Name), "");
        String profileImg = sharedPref.getString(getString(R.string.store_U_ProfileImg), "");

        emailView.setText(email);
        nameView.setText(name);

        // 프로필 이미지 표시
        if (profileImg != null && !profileImg.equals("")) {
            byte[] decodedString = Base64.decode(profileImg, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            profileImgView.setImageBitmap(decodedBitmap);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(tag_execute, "onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_mypage, container, false);

        profileImgView = fragmentView.findViewById(R.id.fragmentMyPageProfileImg);
        emailView = fragmentView.findViewById(R.id.fragmentMyPageAccountTxt);
        nameView = fragmentView.findViewById(R.id.fragmentMyPageNameTxt);

        editProfileMenu = fragmentView.findViewById(R.id.fragmentMyPageEditProfileMenu);
        editAccountMenu = fragmentView.findViewById(R.id.fragmentMyPageEditAccountMenu);
        editNameMenu = fragmentView.findViewById(R.id.fragmentMyPageEditNameMenu);
        updatePasswordMenu = fragmentView.findViewById(R.id.fragmentMyPageUpdatePasswordMenu);

        editProfileMenu.setOnClickListener(view -> {
            openImageSelectDialog();
        });
        editAccountMenu.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), delete_account.class);
            startActivity(intent);
        });
        editNameMenu.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), update_name.class);
            startActivity(intent);

        });
        updatePasswordMenu.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), update_password.class);
            startActivity(intent);
        });

        return fragmentView;
    }

    // 프로필 사진 변경 dialog 열기
    public void openImageSelectDialog() {
        // 앨범에서 사진 선택 시 실행할 콜백 함수
        class Callee_Select_Album extends dialog_edit_profile_img.Callee_Select_Album {
            public void call() {
                selectAlbum();
            }
        }
        // 카메라로 사진 촬영 시 실행할 콜백 함수
        class Callee_Select_Camera extends dialog_edit_profile_img.Callee_Select_Camera {
            public void call() {
                selectCamera();
            }
        }
        dialog_edit_profile_img.show(getContext(), new Callee_Select_Album(), new Callee_Select_Camera());
    }

    // 앨범에서 사진 선택 시 진행할 과정
    public void selectAlbum() {
        /*
        Android 10 이상을 실행하는 기기에서는 저장소 관련 권한이 없어도 MediaStore.Downloads 컬렉션 내 파일을 포함하여 앱 소유 미디어 파일에 액세스하고 미디어 파일을 수정할 수 있습니다.
        예를 들어 카메라 앱을 개발하고 있다면 미디어 저장소에 쓰고 있는 이미지를 앱이 소유하고 있으므로 저장소 관련 권한을 요청할 필요가 없습니다.
        */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    // 카메라로 사진 촬영 선택 시 진행할 과정
    public void selectCamera() {
        Boolean isPermitted = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (isPermitted) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 1);
        } else {
            String isCameraPermissionAsked = sharedPref.getString(getString(R.string.store_is_camera_permission_asked), null);

            if (isCameraPermissionAsked == null) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 1);
                sharedPrefEditor.putString(getString(R.string.store_is_camera_permission_asked), "true");
                sharedPrefEditor.apply();
            } else {
                dialog_confirm.show(context, "카메라 기능 권한이 허용되어 있지 않습니다. 설정에서 권한을 허용해 주세요.", null);
            }
        }
    }

    // 프로필 사진 변경 요청
    public void editProfileImgRequest(String imageFileString) {
        // 요청 성공 시 실행할 callback
        class Callee_success extends httpRequestAPIs.Callee_success {
            public void call(httpRequestAPIs.ResponseObject responseObj) {
                String imageBase64String = responseObj.data.replace("\n", "");
                // 프로필 이미지 저장
                sharedPrefEditor.putString(getString(R.string.store_U_ProfileImg), imageBase64String);
                sharedPrefEditor.apply();
            }
        }

        // 요청 실패 시 실행할 callback
        class Callee_failed extends httpRequestAPIs.Callee_failed {
            public void call(int statusCode) {
                dialog_confirm.show(context, "요청 중 에러가 발생하였습니다. 잠시 후 다시 시도해 주세요.", null);
            }
        }

        String studentId = sharedPref.getString(context.getString(R.string.store_U_Id), "");
        httpRequestAPIs.editProfileImg(studentId, imageFileString, new Callee_success(), new Callee_failed());
    }

    // 이미지 선택 후 처리 작업
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int RESULT_OK = -1;
        if (resultCode == RESULT_OK) {
            Bitmap imageBitmap = null;

            if (requestCode == 0) {
                // 앨범에서 이미지 불러오기 완료 시
                Uri selectedImage = data.getData();
                profileImgView.setImageURI(selectedImage);

                // Bitmap 생성
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(selectedImage);
                    imageBitmap = BitmapFactory.decodeStream(inputStream);

                    //imageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), selectedImage));
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else if (requestCode == 1) {
                // 카메라로 사진 촬영 완료 시
                imageBitmap = (Bitmap) data.getParcelableExtra("data");
                profileImgView.setImageBitmap(imageBitmap);
            }

            // 서버로 전송해 주기 위한 이미지 처리 과정

            // new code
            // Flip the captured image horizontally
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            Bitmap resizedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
            //

            /*
            // old code. do not delete yet!
            // Resize the Bitmap to your desired dimensions
            int desiredWidth = 100; // Set the desired width
            int desiredHeight = 100; // Set the desired height
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, desiredWidth, desiredHeight, true);
            */

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);

            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(tag_check, "generated base64 img : " + base64Image);
            editProfileImgRequest(base64Image); // 프로필 이미지 변경 요청 보내기
        }
    }


    /* fragment 에 필요한 데이터 설정. newInstance 매서드 내에서만 실행하기.
    부모 fragment 또는 부모 activity 에서 파라미터를 받아서 local 변수에 값 할당해 주기.
    파라미터를 전달받을 일이 없다면 사용하지 않아도 됨.
    */
    public void setData(String param1, int param2, boolean param3) {
        Log.d(tag_execute, "setData");
    }
}
