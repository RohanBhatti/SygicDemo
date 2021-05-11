package com.sygic.demo3d

import android.app.Activity
import com.sygic.aura.embedded.IApiCallback
import com.sygic.sdk.api.events.ApiEvents


class SygicNaviCallback(val mActivity: Activity?) : IApiCallback {

    override fun onEvent(event: Int, data: String?) {
        if(event == ApiEvents.EVENT_APP_EXIT) {
            mActivity?.finish()
        }
    }

    override fun onServiceConnected() {
    }

    override fun onServiceDisconnected() {
    }
}