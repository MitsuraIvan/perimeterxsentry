package com.sentry.myapplication

import android.util.Log
import com.perimeterx.mobile_sdk.PerimeterX
import com.perimeterx.mobile_sdk.PerimeterXDelegate
import com.perimeterx.mobile_sdk.main.PXPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PerimeterXImpl(val application: TestApplication) : PerimeterXDelegate {

    var vid: String = ""
        private set

    val headers: Map<String, String>
        get() {
            val headers = PerimeterX.INSTANCE.headersForURLRequest(null)
            Log.d("perimiterx", "PerimeterX headers: $headers")
            return headers
        }

    init {
        Log.d("perimiterx", "PerimeterX : ${PerimeterX.INSTANCE.sdkVersion()}")
        startPerimeterX()
        val policy = PXPolicy()
        policy.requestsInterceptedAutomaticallyEnabled = false
        PerimeterX.INSTANCE.setPolicy(policy, null)
    }

    override fun perimeterxRequestBlockedHandler(p0: String) {
        Log.d("perimiterx", "perimeterxRequestBlockedHandler $p0")
    }

    override fun perimeterxChallengeSolvedHandler(p0: String) {
        Log.d("perimiterx", "perimeterxChallengeSolvedHandler $p0")
    }

    override fun perimeterxChallengeCancelledHandler(p0: String) {
        Log.d("perimiterx", "perimeterxChallengeCancelledHandler $p0")
    }

    fun managePerimeterLoginError(response: String): Boolean {
        return PerimeterX.INSTANCE.handleResponse(null, response, 403)
    }

    private fun startPerimeterX() {
//        val appId = if (BuildConfig.DEBUG) "PXX5W4fvaY" else "PXp8oF1R5L"
        val appId = "PXp8oF1R5L"
        val enabledDoctor = false
        PerimeterX.INSTANCE.start(application, appId, this, enabledDoctor) { success ->
            if (success) {
                PerimeterX.INSTANCE.vid(appId)?.let { vid ->
                    this.vid = vid
                    Log.d("perimiterx", "PerimeterX is ready (vid = ${vid})")
                }
            } else {
                Log.d("perimiterx", "PerimeterX is not initialized, restarting in 3 sec")
                CoroutineScope(Dispatchers.IO).launch {
                    delay(3000)
                    startPerimeterX()
                }
            }
        }
    }
}