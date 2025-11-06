import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frevolt_leegality_plugin/frevolt_leegality_plugin_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelFrevoltLeegalityPlugin platform = MethodChannelFrevoltLeegalityPlugin();
  const MethodChannel channel = MethodChannel('frevolt_leegality_plugin');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
