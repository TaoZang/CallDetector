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
                    (context.applicationContext as MyApplication).handleIncoming(number)
                }
            } else {
                (context.applicationContext as MyApplication).handleIdle()
            }
        }
    }
}