import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'frevolt_leegality_plugin_method_channel.dart';

abstract class FrevoltLeegalityPluginPlatform extends PlatformInterface {
  /// Constructs a FrevoltLeegalityPluginPlatform.
  FrevoltLeegalityPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static FrevoltLeegalityPluginPlatform _instance = MethodChannelFrevoltLeegalityPlugin();

  /// The default instance of [FrevoltLeegalityPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelFrevoltLeegalityPlugin].
  static FrevoltLeegalityPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FrevoltLeegalityPluginPlatform] when
  /// they register themselves.
  static set instance(FrevoltLeegalityPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
