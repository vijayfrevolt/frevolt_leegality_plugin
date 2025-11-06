// package com.example.frevolt_leegality_plugin

// import io.flutter.embedding.engine.plugins.FlutterPlugin
// import io.flutter.plugin.common.MethodCall
// import io.flutter.plugin.common.MethodChannel
// import io.flutter.plugin.common.MethodChannel.MethodCallHandler
// import io.flutter.plugin.common.MethodChannel.Result

// /** FrevoltLeegalityPlugin */
// class FrevoltLeegalityPlugin :
//     FlutterPlugin,
//     MethodCallHandler {
//     // The MethodChannel that will the communication between Flutter and native Android
//     //
//     // This local reference serves to register the plugin with the Flutter Engine and unregister it
//     // when the Flutter Engine is detached from the Activity
//     private lateinit var channel: MethodChannel

//     override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
//         channel = MethodChannel(flutterPluginBinding.binaryMessenger, "frevolt_leegality_plugin")
//         channel.setMethodCallHandler(this)
//     }

//     override fun onMethodCall(
//         call: MethodCall,
//         result: Result
//     ) {
//         if (call.method == "getPlatformVersion") {
//             result.success("Android ${android.os.Build.VERSION.RELEASE}")
//         } else {
//             result.notImplemented()
//         }
//     }

//     override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
//         channel.setMethodCallHandler(null)
//     }
// }

package com.example.frevolt_leegality_plugin

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener

class FrevoltLeegalityPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, ActivityResultListener {
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var pendingResult: Result? = null
    private val LEEGALITY_REQUEST_CODE = 9898

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "frevolt_leegality_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "startLeegalitySigning" -> {
                startLeegalitySigning(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun startLeegalitySigning(call: MethodCall, result: Result) {
        try {
            val activity = this.activity
            if (activity == null) {
                result.error("NO_ACTIVITY", "No activity available", null)
                return
            }

            pendingResult = result

            val url = call.argument<String>("url")
            val enableZoom = call.argument<Boolean>("enableZoom") ?: true
            val timer = call.argument<Int>("timer") ?: 5

            if (url.isNullOrEmpty()) {
                result.error("INVALID_URL", "Signing URL is required", null)
                return
            }

            // Check if Leegality class exists at runtime
            try {
                val leegalityClass = Class.forName("com.gspl.leegality.Leegality")
                val intent = Intent(activity, leegalityClass)
                
                var finalUrl = url
                if (timer in 0..60) {
                    finalUrl = if (url.contains("?")) {
                        "$url&timer=$timer"
                    } else {
                        "$url?timer=$timer"
                    }
                }
                
                intent.putExtra("url", finalUrl)
                intent.putExtra("zoom", enableZoom)
                
                activity.startActivityForResult(intent, LEEGALITY_REQUEST_CODE)
                
            } catch (e: ClassNotFoundException) {
                result.error(
                    "LEEGALITY_SDK_MISSING", 
                    "Leegality SDK not found. Please add leegality.aar to your app's libs folder.", 
                    "Download from: http://gitlab.leegality.com/leegality-public/android-sdk/blob/v4.11/leegality.aar"
                )
            }
            
        } catch (e: Exception) {
            result.error("START_FAILED", "Failed to start Leegality: ${e.message}", null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == LEEGALITY_REQUEST_CODE) {
            val resultMessage = when (resultCode) {
                Activity.RESULT_OK -> {
                    val message = data?.getStringExtra("message")
                    "success:${message ?: "Signing completed"}"
                }
                Activity.RESULT_CANCELED -> {
                    val error = data?.getStringExtra("error")
                    "error:${error ?: "Signing cancelled"}"
                }
                else -> "error:Unknown result"
            }
            
            pendingResult?.success(resultMessage)
            pendingResult = null
            return true
        }
        return false
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}