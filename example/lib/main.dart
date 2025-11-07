import 'package:flutter/material.dart';
import 'package:frevolt_leegality_plugin/frevolt_leegality_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _status = 'Initializing...';
  String _platformVersion = 'Unknown';
  String _leegalityResult = 'Not tested';

  @override
  void initState() {
    super.initState();
    _initPlugin();
  }

  Future<void> _initPlugin() async {
    try {
      // Test 1: Basic plugin communication
      _platformVersion = await FrevoltLeegalityPlugin.getPlatformVersion() ?? 'Unknown';
      setState(() {
        _status = 'Plugin communication: ✅';
      });
    } catch (e) {
      setState(() {
        _status = 'Plugin communication: ❌ $e';
      });
    }
  }

  Future<void> _testLeegalitySDK() async {
    try {
      setState(() {
        _leegalityResult = 'Testing...';
      });

      final result = await FrevoltLeegalityPlugin.startLeegalitySigning(
        url: 'https://sandbox.leegality.com/sign/7017b64e-b75e-4606-8ba3-3d212cfd6428',
        enableZoom: true,
        timer: 10,
      );

      setState(() {
        _leegalityResult = 'Result: $result';
      });
    } catch (e) {
      setState(() {
        _leegalityResult = 'Error: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Leegality Plugin Test'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Plugin Status
              Text(
                'Plugin Status:',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
              ),
              Text(_status),
              SizedBox(height: 10),
              
              // Platform Version
              Text(
                'Platform Version:',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              Text(_platformVersion),
              SizedBox(height: 20),
              
              // Test Button
              ElevatedButton(
                onPressed: _testLeegalitySDK,
                child: Text('Test Leegality SDK'),
              ),
              SizedBox(height: 10),
              
              // Leegality Result
              Text(
                'Leegality Test Result:',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              Text(_leegalityResult),
            ],
          ),
        ),
      ),
    );
  }
}