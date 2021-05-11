package com.sygic.demo3d

import com.sygic.aura.embedded.SygicFragmentSupportV4

class SygicNaviFragment : SygicFragmentSupportV4() {

    override fun onResume() {
        startNavi()
        setCallback(SygicNaviCallback(activity))
        super.onResume()
    }

}