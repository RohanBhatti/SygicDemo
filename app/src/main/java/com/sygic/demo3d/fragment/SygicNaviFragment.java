package com.sygic.demo3d.fragment;

import com.sygic.aura.embedded.SygicFragmentSupportV4;
import com.sygic.demo3d.SygicNaviCallback;


public class SygicNaviFragment extends SygicFragmentSupportV4 {

    @Override
    public void onResume() {
        startNavi();
        setCallback(new SygicNaviCallback(getActivity()));
        super.onResume();
    }
}
