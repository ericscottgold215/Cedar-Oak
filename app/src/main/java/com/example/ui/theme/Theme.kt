package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LoungeDarkColorScheme = darkColorScheme(
    primary = BurnishedGold,
    onPrimary = DeepAmberInk,
    primaryContainer = AmberContainer,
    onPrimaryContainer = WarmGoldTint,
    secondary = ToastedCaramel,
    onSecondary = CaramelInk,
    secondaryContainer = CaramelContainer,
    onSecondaryContainer = CaramelTint,
    tertiary = BotanicalSage,
    onTertiary = SageInk,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = SageTint,
    background = EspressoBackground,
    onBackground = WarmIvory,
    surface = SpanishCedarSurface,
    onSurface = WarmIvory,
    surfaceVariant = CedarContainer,
    onSurfaceVariant = AgedAsh,
    surfaceContainerLowest = EspressoBackground,
    surfaceContainerLow = CedarContainerLow,
    surfaceContainer = CedarContainer,
    surfaceContainerHigh = CedarContainerHigh,
    surfaceContainerHighest = CedarContainerHighest,
    outline = HumidorOutline,
    outlineVariant = HumidorOutlineVariant,
    error = ErrorCrimson,
    onError = Color(0xFF2C0904),
)

private val ParchmentLightColorScheme = lightColorScheme(
    primary = MahoganyGold,
    onPrimary = Color.White,
    primaryContainer = MahoganyContainer,
    onPrimaryContainer = MahoganyContainerInk,
    secondary = RoastChestnut,
    onSecondary = Color.White,
    secondaryContainer = ChestnutContainer,
    onSecondaryContainer = ChestnutContainerInk,
    tertiary = DeepSage,
    onTertiary = Color.White,
    tertiaryContainer = LightSageContainer,
    onTertiaryContainer = LightSageInk,
    background = ParchmentBackground,
    onBackground = DarkRoastInk,
    surface = ParchmentSurface,
    onSurface = DarkRoastInk,
    surfaceVariant = ParchmentContainer,
    onSurfaceVariant = CharcoalAsh,
    surfaceContainerLowest = Color(0xFFFFFCF7),
    surfaceContainerLow = ParchmentContainerLow,
    surfaceContainer = ParchmentContainer,
    surfaceContainerHigh = ParchmentContainerHigh,
    surfaceContainerHighest = ParchmentContainerHighest,
    outline = ParchmentOutline,
    outlineVariant = ParchmentOutlineVariant,
    error = ErrorCrimsonLight,
    onError = Color.White,
)

private val CedarShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun CedarAndOakTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) LoungeDarkColorScheme else ParchmentLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = CedarShapes,
        content = content,
    )
}
