# sombrAMP runs on the Flutter engine, which finds its plugins and its own classes by reflection and through JNI.
# These are the rules the Flutter Gradle plugin gives Flutter apps.
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.GeneratedPluginRegistrant { *; }
-dontwarn io.flutter.embedding.**
-dontwarn io.flutter.plugin.**
# R8 sometimes strips plugin classes (https://github.com/flutter/flutter/issues/154580)
-if class * implements io.flutter.embedding.engine.plugins.FlutterPlugin
-keep,allowshrinking,allowobfuscation class <1>
# Flutter's deferred components use Play Core, which Sombrio doesn't ship.
-dontwarn com.google.android.play.core.**
