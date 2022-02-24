package art.zangtao.numberdetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class PhoneReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == "android.intent.action.PHONE_STATE") {
            val state = intent.getStringExtra("state")
            if (TelephonyManager.EXTRA_STATE_RINGING == state) {
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)?.let { number ->
                    FloatingPresenter.getInstance(context).present(number)
                }
            } else {
                FloatingPresenter.getInstance(context).dismiss()
            }
        }
    }
}