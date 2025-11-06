import 'package:flutter_test/flutter_test.dart';
import 'package:frevolt_leegality_plugin/frevolt_leegality_plugin.dart';
import 'package:frevolt_leegality_plugin/frevolt_leegality_plugin_platform_interface.dart';
import 'package:frevolt_leegality_plugin/frevolt_leegality_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFrevoltLeegalityPluginPlatform
    with MockPlatformInterfaceMixin
    implements FrevoltLeegalityPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FrevoltLeegalityPluginPlatform initialPlatform = FrevoltLeegalityPluginPlatform.instance;

  test('$MethodChannelFrevoltLeegalityPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFrevoltLeegalityPlugin>());
  });

  test('getPlatformVersion', () async {
    final version = await FrevoltLeegalityPlugin.getPlatformVersion();
    MockFrevoltLeegalityPluginPlatform fakePlatform = MockFrevoltLeegalityPluginPlatform();
    FrevoltLeegalityPluginPlatform.instance = fakePlatform;

    expect(version, '42');
  });
}
