import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

import 'package:frevolt_leegality_plugin/frevolt_leegality_plugin.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('getPlatformVersion test', (WidgetTester tester) async {
    final String? version = await FrevoltLeegalityPlugin.getPlatformVersion();
    expect(version?.isNotEmpty, true);
  });
}
