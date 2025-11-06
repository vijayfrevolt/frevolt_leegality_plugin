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
  String _platformVersion = 'Unknown';
  String _signingResult = 'Not started';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await FrevoltLeegalityPlugin.getPlatformVersion() ?? 'Unknown platform version';
    } catch (e) {
      platformVersion = 'Failed to get platform version: $e';
    }

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> startSigning() async {
    try {
      final result = await FrevoltLeegalityPlugin.startLeegalitySigning(
        url: 'https://app1.leegality.com/sign/your-document-id-here', // Replace with actual URL
        enableZoom: true,
        timer: 5,
      );
      
      setState(() {
        _signingResult = result;
      });
      
      // Show result in dialog
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text('Signing Result'),
            content: Text(result),
            actions: [
              TextButton(
                child: Text('OK'),
                onPressed: () => Navigator.of(context).pop(),
              ),
            ],
          );
        },
      );
    } catch (e) {
      setState(() {
        _signingResult = 'Error: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Leegality Plugin Example'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('Running on: $_platformVersion\n'),
              SizedBox(height: 20),
              ElevatedButton(
                onPressed: startSigning,
                child: Text('Start Leegality Signing'),
              ),
              SizedBox(height: 20),
              Text('Last result: $_signingResult'),
            ],
          ),
        ),
      ),
    );
  }
}