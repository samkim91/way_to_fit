import 'package:flutter/material.dart';

class AppTheme {
  static const _primary = Color(0xFFFF6B2B);
  static const _surface = Color(0xFF232A36);
  static const _background = Color(0xFF171C24);
  static const _outline = Color(0xFF394150);

  static ThemeData dark() {
    final base = ThemeData.dark(useMaterial3: true);
    const scheme = ColorScheme.dark(
      primary: _primary,
      secondary: Color(0xFF3B82F6),
      surface: _surface,
      onSurface: Color(0xFFF5F7FB),
      onPrimary: Colors.white,
      outline: _outline,
      error: Color(0xFFF87171),
    );

    return base.copyWith(
      colorScheme: scheme,
      scaffoldBackgroundColor: _background,
      textTheme: base.textTheme.apply(
        fontFamily: 'NotoSansKR',
        bodyColor: scheme.onSurface,
        displayColor: scheme.onSurface,
      ),
      chipTheme: base.chipTheme.copyWith(
        side: const BorderSide(color: _outline),
        selectedColor: _primary.withValues(alpha: 0.16),
      ),
      cardTheme: const CardThemeData(
        color: _surface,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(24)),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFF2A3240),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: const BorderSide(color: _outline),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: const BorderSide(color: _outline),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: const BorderSide(color: _primary, width: 1.4),
        ),
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        foregroundColor: Colors.white,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
      ),
    );
  }

  static ThemeData light() {
    final base = ThemeData.light(useMaterial3: true);
    final scheme = ColorScheme.fromSeed(seedColor: _primary);

    return base.copyWith(
      colorScheme: scheme,
      textTheme: base.textTheme.apply(fontFamily: 'NotoSansKR'),
    );
  }
}
