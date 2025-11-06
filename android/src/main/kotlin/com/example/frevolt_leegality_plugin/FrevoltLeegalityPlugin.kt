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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private var broadcastReceiver: BroadcastReceiver? = null
    
    // Use a unique request code
    private companion object {
        const val LEEGALITY_REQUEST_CODE = 9898
        const val CHANNEL_NAME = "frevolt_leegality_plugin"
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME)
        channel.setMethodCallHandler(this)
        Log.d("LeegalityPlugin", "Plugin attached to engine")
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

            // Store the result for later use
            pendingResult = result

            // Get parameters
            val url = call.argument<String>("url")
            val enableZoom = call.argument<Boolean>("enableZoom") ?: true
            val timer = call.argument<Int>("timer") ?: 5

            if (url.isNullOrEmpty()) {
                result.error("INVALID_URL", "Signing URL is required", null)
                return
            }

            Log.d("LeegalityPlugin", "Starting Leegality with URL: $url")

            // Register broadcast receiver for Leegality events
            registerBroadcastReceiver(activity)

            // Start Leegality activity
            val intent = Intent(activity, Class.forName("com.gspl.leegality.Leegality"))
            
            // Build final URL with timer parameter if needed
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
            Log.e("LeegalityPlugin", "Leegality class not found", e)
            result.error("CLASS_NOT_FOUND", "Leegality SDK not found: ${e.message}", null)
        } catch (e: Exception) {
            Log.e("LeegalityPlugin", "Error starting Leegality", e)
            result.error("START_FAILED", "Failed to start Leegality: ${e.message}", null)
        }
    }

    private fun registerBroadcastReceiver(context: Context) {
        // Unregister previous receiver if exists
        unregisterBroadcastReceiver()
        
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val data = intent?.getStringExtra("data")
                Log.d("LeegalityPlugin", "Received broadcast: $data")
                
                // You can send this back to Flutter if needed
                // For now, we'll just log it
            }
        }
        
        val filter = IntentFilter("com.gspl.leegality.events")
        context.registerReceiver(broadcastReceiver, filter)
        Log.d("LeegalityPlugin", "Broadcast receiver registered")
    }

    private fun unregisterBroadcastReceiver() {
        try {
            broadcastReceiver?.let { receiver ->
                activity?.unregisterReceiver(receiver)
                broadcastReceiver = null
                Log.d("LeegalityPlugin", "Broadcast receiver unregistered")
            }
        } catch (e: Exception) {
            Log.e("LeegalityPlugin", "Error unregistering broadcast receiver", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == LEEGALITY_REQUEST_CODE) {
            Log.d("LeegalityPlugin", "Received activity result: $resultCode")
            
            val resultMessage = when (resultCode) {
                Activity.RESULT_OK -> {
                    val message = data?.getStringExtra("message")
                    if (!message.isNullOrEmpty()) {
                        "success:$message"
                    } else {
                        "success:Signing completed successfully"
                    }
                }
                Activity.RESULT_CANCELED -> {
                    val error = data?.getStringExtra("error")
                    if (!error.isNullOrEmpty()) {
                        "error:$error"
                    } else {
                        "error:Signing was cancelled"
                    }
                }
                else -> {
                    "error:Unknown result"
                }
            }
            
            // Send result back to Flutter
            pendingResult?.success(resultMessage)
            pendingResult = null
            
            // Clean up
            unregisterBroadcastReceiver()
            
            return true
        }
        return false
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        unregisterBroadcastReceiver()
        Log.d("LeegalityPlugin", "Plugin detached from engine")
    }

    // ActivityAware methods
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
        Log.d("LeegalityPlugin", "Plugin attached to activity")
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        Log.d("LeegalityPlugin", "Plugin detached from activity for config changes")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
        Log.d("LeegalityPlugin", "Plugin reattached to activity for config changes")
    }

    override fun onDetachedFromActivity() {
        activity = null
        unregisterBroadcastReceiver()
        Log.d("LeegalityPlugin", "Plugin detached from activity")
    }
}