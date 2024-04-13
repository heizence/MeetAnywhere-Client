package com.example.meetanywhere.Fragments;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.meetanywhere.R;

public class fragment_book extends Fragment {
    private String screenName = "[FRAGMENT fragment_book]:";
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context = getContext();

    private View fragmentView;

    /* 부모 fragment 또는 부모 activity 에서 parameter 로 전달받는 변수들
    newInstance 실행 시 값 설정됨.
    String, int, boolean 외 어떤 변수형도 선언해서 전달 가능(arrayList, object 등)
    */
    private String param1;
    private int param2;
    private boolean param3;

    /* 부모 fragment 또는 부모 activity 에서 파라미터를 받아오기 위한 static factory method 생성.
    파라미터를 전달받을 일이 없다면 사용하지 않아도 됨.
    */
    public static fragment_book newInstance(String param1, int param2, boolean param3) {
        fragment_book fragment = new fragment_book();
        //fragment.setData();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(tag_execute, "LifeCycle onCreate");
        super.onCreate(savedInstanceState);
        Log.d(tag_check, "check param1 : " + param1);
        Log.d(tag_check, "check param2 :: " + param2);
        Log.d(tag_check, "check param3 : " + param3);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(tag_execute, "LifeCycle onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_book, container, false);
        // do something
        return fragmentView;
    }

    /* fragment 에 필요한 데이터 설정. newInstance 매서드 내에서만 실행하기.
    부모 fragment 또는 부모 activity 에서 파라미터를 받아서 local 변수에 값 할당해 주기.
    파라미터를 전달받을 일이 없다면 사용하지 않아도 됨.
    */
    public void setData(String param1, int param2, boolean param3){
        Log.d(tag_execute, "setData");
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }
}
