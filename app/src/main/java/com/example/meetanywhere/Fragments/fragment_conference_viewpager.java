package com.example.meetanywhere.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.meetanywhere.R;

public class fragment_conference_viewpager extends Fragment {
    private String screenName = "[FRAGMENT fragment_conference_viewpager]:";
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context;
    private View fragmentView;
    private ViewPager viewPager;
    PagerAdapter pagerAdapter;

    private fragment_conference_main_video mainVideoFragment;
    private fragment_conference_participants_videos participantsVideosFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(tag_execute, "LifeCycle onCreate");
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(tag_execute, "LifeCycle onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_conference_viewpager, container, false);
        viewPager = fragmentView.findViewById(R.id.f_conference_pager);
        pagerAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        return fragmentView;
    }



    public class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(tag_check, "getItem : " + i);

            if (i == 0) {
                //mainVideoFragment = new fragment_conference_main_video();
                return mainVideoFragment;
            } else {
                //participantsVideosFragment = new fragment_conference_participants_videos();
                return participantsVideosFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.d(tag_check, "getPageTitle : " + position);
            return "OBJECT " + (position + 1);
        }
    }


    public void updateContent(String message) {
        // Update the fragment's content with the provided message
        Log.d(tag_check, "updateContent : " + message);
    }
}
