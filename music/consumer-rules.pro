# sombrAMP runs on the Flutter engine, which finds its plugins and its own classes by reflection and through JNI.
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.GeneratedPluginRegistrant { *; }
-dontwarn io.flutter.embedding.**
# Flutter's deferred components use Play Core, which Sombrio doesn't ship.
-dontwarn com.google.android.play.core.**
