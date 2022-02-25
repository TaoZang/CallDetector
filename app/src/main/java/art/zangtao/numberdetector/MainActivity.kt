package art.zangtao.numberdetector

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker

class MainActivity : AppCompatActivity() {
    private val permissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG)
    private val textView by lazy { findViewById<TextView>(R.id.switch_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        if (checkPermission()) {
            textView.isEnabled = true
            textView.setText(R.string.open_permission)
            textView.setOnClickListener {
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
        } else if (!Settings.canDrawOverlays(this)) {
            textView.isEnabled = true
            textView.setText(R.string.go_to_settings)
            textView.setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        } else {
            textView.isEnabled = false
            textView.setText(R.string.authorized)
        }
    }

    private fun checkPermission(): Boolean {
        return permissions.any { PermissionChecker.checkSelfPermission(this, it) != PermissionChecker.PERMISSION_GRANTED }
    }
}