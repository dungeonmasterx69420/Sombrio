import 'package:finamp/sombra/system_s.dart' show SystemSFonts;
import 'package:flutter/material.dart';

/// System S's type for Moonfin's screens, as sombrAMP sets it: titles, buttons and labels in Pixelify Sans, and text
/// and figures in IBM Plex Mono, the theme's font (packages/design/lib/src/theme/themes/moonfin_theme_spec.dart).
/// Both fonts are sombrAMP's, the music/sombrAMP package's.
TextTheme systemSTextTheme(TextTheme base) {
  TextStyle? pixel(TextStyle? style) =>
      style?.copyWith(fontFamily: SystemSFonts.pixel, fontFeatures: SystemSFonts.pixelFeatures);
  return base.copyWith(
    displayLarge: pixel(base.displayLarge),
    displayMedium: pixel(base.displayMedium),
    displaySmall: pixel(base.displaySmall),
    headlineLarge: pixel(base.headlineLarge),
    headlineMedium: pixel(base.headlineMedium),
    headlineSmall: pixel(base.headlineSmall),
    titleLarge: pixel(base.titleLarge),
    titleMedium: pixel(base.titleMedium),
    titleSmall: pixel(base.titleSmall),
    labelLarge: pixel(base.labelLarge),
    labelMedium: pixel(base.labelMedium),
    labelSmall: pixel(base.labelSmall),
  );
}
