
// import 'frevolt_leegality_plugin_platform_interface.dart';

// class FrevoltLeegalityPlugin {
//   Future<String?> getPlatformVersion() {
//     return FrevoltLeegalityPluginPlatform.instance.getPlatformVersion();
//   }
// }


import 'dart:async';
import 'package:flutter/services.dart';

class FrevoltLeegalityPlugin {
  static const MethodChannel _channel =
      MethodChannel('frevolt_leegality_plugin');

  /// Gets the platform version
  static Future<String?> getPlatformVersion() async {
    try {
      final String? version = await _channel.invokeMethod('getPlatformVersion');
      return version;
    } on PlatformException catch (e) {
      print("Failed to get platform version: '${e.message}'");
      return null;
    }
  }

  /// Starts the Leegality signing process
  /// 
  /// [url] - The signing URL from Leegality (required)
  /// [enableZoom] - Whether to enable zoom feature (default: true)
  /// [timer] - Duration for success screen in seconds (0-60, default: 5)
  /// 
  /// Returns a Future that completes with a string result:
  /// - "success:message" when signing is successful
  /// - "error:message" when there's an error
  static Future<String> startLeegalitySigning({
    required String url,
    bool enableZoom = true,
    int timer = 5,
  }) async {
    try {
      final String result = await _channel.invokeMethod('startLeegalitySigning', {
        'url': url,
        'enableZoom': enableZoom,
        'timer': timer,
      });
      return result;
    } on PlatformException catch (e) {
      return 'error: ${e.message}';
    } catch (e) {
      return 'error: $e';
    }
  }
}