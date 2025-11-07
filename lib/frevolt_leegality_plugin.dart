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

  static Future<String> checkLeegalityAvailability() async {
    try {
      final String result = await _channel.invokeMethod('checkLeegalityAvailability');
      return result;
    } on PlatformException catch (e) {
      return 'error: ${e.message}';
    }
  }

  /// Starts the Leegality signing process
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