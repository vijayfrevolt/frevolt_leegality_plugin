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
        Log.d("LeegalityPlugin", "✅ Plugin attached to engine")
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        Log.d("LeegalityPlugin", "📞 Method called: ${call.method}")
        
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "checkLeegalityAvailability" -> {  // ✅ ADD THIS METHOD
                checkLeegalityAvailability(result)
            }
            "startLeegalitySigning" -> {
                startLeegalitySigning(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    // ✅ ADD THIS NEW METHOD
    private fun checkLeegalityAvailability(result: Result) {
        try {
            Log.d("LeegalityPlugin", "🔍 Checking for Leegality SDK...")
            
            // Try to load the Leegality class
            val leegalityClass = Class.forName("com.gspl.leegality.Leegality")
            Log.d("LeegalityPlugin", "✅ Leegality SDK found!")
            result.success("available")
            
        } catch (e: ClassNotFoundException) {
            Log.e("LeegalityPlugin", "❌ Leegality SDK not found", e)
            result.success("missing")
        } catch (e: Exception) {
            Log.e("LeegalityPlugin", "❌ Error checking Leegality: ${e.message}", e)
            result.error("CHECK_FAILED", "Failed to check Leegality availability: ${e.message}", null)
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

            Log.d("LeegalityPlugin", "🚀 Starting Leegality signing with URL: $url")

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
                
                Log.d("LeegalityPlugin", "📤 Starting Leegality activity...")
                activity.startActivityForResult(intent, LEEGALITY_REQUEST_CODE)
                
            } catch (e: ClassNotFoundException) {
                Log.e("LeegalityPlugin", "❌ Leegality SDK not found during signing", e)
                result.error(
                    "LEEGALITY_SDK_MISSING", 
                    "Leegality SDK not found. Please add leegality.aar to your app's libs folder.", 
                    "Download from: http://gitlab.leegality.com/leegality-public/android-sdk/blob/v4.11/leegality.aar"
                )
            }
            
        } catch (e: Exception) {
            Log.e("LeegalityPlugin", "❌ Failed to start Leegality: ${e.message}", e)
            result.error("START_FAILED", "Failed to start Leegality: ${e.message}", null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.d("LeegalityPlugin", "📥 Activity result: requestCode=$requestCode, resultCode=$resultCode")
        
        if (requestCode == LEEGALITY_REQUEST_CODE) {
            val resultMessage = when (resultCode) {
                Activity.RESULT_OK -> {
                    val message = data?.getStringExtra("message")
                    Log.d("LeegalityPlugin", "✅ Signing completed: $message")
                    "success:${message ?: "Signing completed"}"
                }
                Activity.RESULT_CANCELED -> {
                    val error = data?.getStringExtra("error")
                    Log.d("LeegalityPlugin", "❌ Signing cancelled: $error")
                    "error:${error ?: "Signing cancelled"}"
                }
                else -> {
                    Log.d("LeegalityPlugin", "❓ Unknown result: $resultCode")
                    "error:Unknown result"
                }
            }
            
            pendingResult?.success(resultMessage)
            pendingResult = null
            return true
        }
        return false
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        Log.d("LeegalityPlugin", "🔌 Plugin detached from engine")
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
        Log.d("LeegalityPlugin", "📱 Plugin attached to activity")
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        Log.d("LeegalityPlugin", "🔄 Plugin detached for config changes")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
        Log.d("LeegalityPlugin", "🔄 Plugin reattached for config changes")
    }

    override fun onDetachedFromActivity() {
        activity = null
        Log.d("LeegalityPlugin", "🔌 Plugin detached from activity")
    }
}