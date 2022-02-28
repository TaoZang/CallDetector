package art.zangtao.numberdetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class PhoneReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (number != null) {
                if (TelephonyManager.EXTRA_STATE_RINGING == intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                    (context.applicationContext as MyApplication).handleIncoming(number)
                } else {
                    (context.applicationContext as MyApplication).handleIdle()
                }
            }
        }
    }
}