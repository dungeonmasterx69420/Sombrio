import 'package:flutter/material.dart';

import '../theme_spec.dart';

// Sombrio: Moonfin's default theme is System S, the design system behind sombra.wtf, as sombrAMP draws it
// (music/sombrAMP/lib/sombra/system_s.dart): ink on black, hard 1px edges and square corners, and one phosphor,
// amber, for whatever is live. System S has no red: problems are drawn in ink. The id stays Moonfin's, so saved
// settings and Moonfin's code that looks for its default theme keep working. Titles are set in Pixelify Sans by
// lib/sombrio/system_s_theme.dart.
const _desk = Color(0xFF0A0A0A);
const _pane = Color(0xFF0E0E0E);
const _well = Color(0xFF070707);
const _ink = Color(0xFFEDEDED);
const _muted = Color(0xFFA3A3A3);
const _faint = Color(0xFF6A6A6A);
const _idle = Color(0xFF4A4A4A);
const _inset = Color(0xFF2A2A2A);
const _hover = Color(0xFF1B1B1B);
const _phosphor = Color(0xFFFFB000);

const moonfinThemeSpec = ThemeSpec(
  id: 'moonfin',
  displayName: 'System S',
  fontFamily: 'packages/finamp/IBMPlexMono',
  squareCorners: true,
  semantic: ThemeSemanticTokens(
    statusAvailable: _phosphor,
    statusRequested: _muted,
    statusPending: _muted,
    statusDownloading: _phosphor,
    statusError: _ink,
    mediaTypeBadgeMovie: _hover,
    mediaTypeBadgeShow: _hover,
  ),
  colors: ThemeColorTokens(
    background: _desk,
    onBackground: _ink,
    surface: _pane,
    onSurface: _ink,
    surfaceVariant: _hover,
    scrim: Color(0x8C000000),
    accent: _phosphor,
    onAccent: _desk,
    // Buttons are panes edged in ink, and invert when they have the focus
    buttonNormal: _hover,
    buttonFocused: _ink,
    buttonDisabled: _pane,
    buttonActive: _inset,
    onButtonNormal: _ink,
    onButtonFocused: _desk,
    onButtonDisabled: _faint,
    inputBackground: _well,
    inputFocused: _hover,
    inputBorder: _muted,
    inputBorderFocused: _phosphor,
    rangeTrack: _inset,
    rangeProgress: _phosphor,
    rangeThumb: _phosphor,
    seekbarBuffered: _idle,
    badgeBackground: _phosphor,
    onBadge: _desk,
    badgeUnplayed: _phosphor,
    badgeWatched: _phosphor,
    recordingActive: _phosphor,
    recordingScheduled: _muted,
    error: _ink,
    card: _pane,
  ),
  borders: ThemeBorderTokens(
    cardBorder: BorderSide(color: _idle),
    chipBorder: BorderSide(color: _ink),
    focusBorder: BorderSide(color: _phosphor, width: 2),
    cardRadius: BorderRadius.zero,
    chipRadius: BorderRadius.zero,
    chipBackground: _pane,
    navBorder: BorderSide(color: _ink),
    focusGlow: [],
  ),
);
