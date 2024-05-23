package com.example.meetanywhere.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meetanywhere.R;
import com.example.meetanywhere.Fragments.fragment_conference;
import com.example.meetanywhere.Fragments.fragment_book;
import com.example.meetanywhere.Fragments.fragment_mypage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class signedin_main extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_signedin_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigation);    // nav bar 등록
        getSupportFragmentManager().beginTransaction().add(R.id.mainFragmentView, new fragment_conference()).commit();    // 메인 화면에서 첫 번째 fragment(fragment_main.xml)가 표시되도록 설정.

        // 메뉴 각 항목 선택했을 때 Fragment 전환
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {

                case R.id.item_fragment_conference:
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentView, new fragment_conference()).commit();
                    break;
                case R.id.item_fragment_mypage:
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentView, new fragment_mypage()).commit();
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    /*
    fragment 화면 간 뒤로 가기 기능.
    android 기기 backpress 버튼 터치 시 back stack 에서 쌓인 fragment 화면으로 이동해 주기
    */
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    // 로그아웃 메뉴 클릭
    public void onClickLogout(View view) {
        Context context = signedin_main.this;
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.sharedPreferenceMain), Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString(getString(R.string.store_app_token), "");
        sharedPrefEditor.putString(getString(R.string.store_U_Id), "");
        sharedPrefEditor.putString(getString(R.string.store_U_Email), "");
        sharedPrefEditor.putString(getString(R.string.store_U_Name), "");
        sharedPrefEditor.putString(getString(R.string.store_U_ProfileImg), "");
        sharedPrefEditor.apply();

        Intent intent = new Intent(this, main_activity.class);
        startActivity(intent);
        finish();
    }
}
