import 'package:flutter/material.dart';

class AppTheme {
  static const _primary = Color(0xFFFF6B2B);
  static const _surface = Color(0xFF232A36);
  static const _background = Color(0xFF171C24);
  static const _outline = Color(0xFF394150);
  static const _fontFamily = 'NotoSansKR';

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
      textTheme: _buildTextTheme(base.textTheme, scheme),
      chipTheme: base.chipTheme.copyWith(
        backgroundColor: const Color(0xFF2A3240),
        disabledColor: const Color(0xFF2A3240).withValues(alpha: 0.52),
        selectedColor: _primary.withValues(alpha: 0.22),
        secondarySelectedColor: _primary.withValues(alpha: 0.22),
        checkmarkColor: Colors.white,
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        labelStyle: const TextStyle(
          color: Color(0xFFE7ECF5),
          fontSize: 13,
          fontWeight: FontWeight.w600,
        ),
        secondaryLabelStyle: const TextStyle(
          color: Colors.white,
          fontSize: 13,
          fontWeight: FontWeight.w700,
        ),
        side: const BorderSide(color: _outline),
        shape: const StadiumBorder(),
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
      textTheme: _buildTextTheme(base.textTheme, scheme),
    );
  }

  static TextTheme _buildTextTheme(TextTheme base, ColorScheme scheme) {
    final applied = base.apply(
      fontFamily: _fontFamily,
      bodyColor: scheme.onSurface,
      displayColor: scheme.onSurface,
    );
    final strong = scheme.onSurface;
    final muted = scheme.onSurface.withValues(alpha: 0.78);
    final subtle = scheme.onSurface.withValues(alpha: 0.64);

    return applied.copyWith(
      bodySmall: applied.bodySmall?.copyWith(
        fontSize: 13,
        height: 1.4,
        color: muted,
      ),
      bodyMedium: applied.bodyMedium?.copyWith(
        fontSize: 15,
        height: 1.5,
        color: strong,
      ),
      bodyLarge: applied.bodyLarge?.copyWith(
        fontSize: 16,
        height: 1.5,
        color: strong,
      ),
      labelSmall: applied.labelSmall?.copyWith(
        fontSize: 12,
        height: 1.3,
        color: subtle,
      ),
      labelMedium: applied.labelMedium?.copyWith(
        fontSize: 13,
        height: 1.35,
        color: muted,
      ),
      titleSmall: applied.titleSmall?.copyWith(
        fontSize: 14,
        height: 1.35,
        color: strong,
      ),
      titleMedium: applied.titleMedium?.copyWith(
        fontSize: 16,
        height: 1.35,
        color: strong,
      ),
      titleLarge: applied.titleLarge?.copyWith(
        fontSize: 20,
        height: 1.25,
        color: strong,
      ),
      headlineSmall: applied.headlineSmall?.copyWith(
        fontSize: 24,
        height: 1.2,
        color: strong,
      ),
      headlineLarge: applied.headlineLarge?.copyWith(
        fontSize: 32,
        height: 1.15,
        color: strong,
      ),
    );
  }
}
