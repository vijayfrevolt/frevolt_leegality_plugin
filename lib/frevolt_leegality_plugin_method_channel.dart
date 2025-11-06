import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'frevolt_leegality_plugin_platform_interface.dart';

/// An implementation of [FrevoltLeegalityPluginPlatform] that uses method channels.
class MethodChannelFrevoltLeegalityPlugin extends FrevoltLeegalityPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('frevolt_leegality_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
