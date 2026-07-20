package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepNavy = Color(0xFF0B1A30)
val DarkBlueAccent = Color(0xFF1E2D4A)
val GoldAccent = Color(0xFFD4AF37)
val LightGold = Color(0xFFFFF8E7)
val EmeraldGreen = Color(0xFF2E7D32)
val LightGreen = Color(0xFFE8F5E9)
val SoftCrimson = Color(0xFFC62828)
val LightRed = Color(0xFFFFEBEE)
val LightGrayBg = Color(0xFFF5F7FA)

// Adaptive Color Helpers for Backward-Compatible Dynamic Colors
@Composable
fun getAdaptiveDeepNavy(): Color = if (isSystemInDarkTheme()) Color(0xFFECEFF1) else DeepNavy

@Composable
fun getAdaptiveCardBg(): Color = if (isSystemInDarkTheme()) DarkBlueAccent else Color.White

@Composable
fun getAdaptiveText(): Color = if (isSystemInDarkTheme()) Color.White else DeepNavy

@Composable
fun getAdaptiveLightGold(): Color = if (isSystemInDarkTheme()) Color(0xFF372F15) else LightGold

@Composable
fun getAdaptiveLightGreen(): Color = if (isSystemInDarkTheme()) Color(0xFF1B3821) else LightGreen

@Composable
fun getAdaptiveLightRed(): Color = if (isSystemInDarkTheme()) Color(0xFF451E23) else LightRed

@Composable
fun getAdaptiveTextSecondary(): Color = if (isSystemInDarkTheme()) Color.LightGray else Color.Gray

@Composable
fun getAdaptiveBorder(): Color = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent, // Vibrant gold stands out in dark mode
    secondary = GoldAccent,
    background = Color(0xFF0F172A), // Modern slate-dark background
    surface = Color(0xFF1E293B), // Modern slate-surface
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0xFF334155),
    onPrimaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    secondary = GoldAccent,
    background = LightGrayBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = DeepNavy,
    onSurface = DeepNavy,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = DeepNavy
)

@Composable
fun BlueFoxLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
