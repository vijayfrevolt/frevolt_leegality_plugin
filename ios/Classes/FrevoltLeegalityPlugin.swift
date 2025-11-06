import Flutter
import UIKit

public class FrevoltLeegalityPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "frevolt_leegality_plugin", binaryMessenger: registrar.messenger())
    let instance = FrevoltLeegalityPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "startLeegalitySigning":
      result(FlutterError(code: "UNIMPLEMENTED", 
                         message: "Leegality SDK is not available on iOS", 
                         details: nil))
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}