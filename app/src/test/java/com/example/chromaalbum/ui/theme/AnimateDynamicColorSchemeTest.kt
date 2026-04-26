package com.example.chromaalbum.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class AnimateDynamicColorSchemeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun animateColorScheme_givenTargetWithDistinctRoles_whenFirstComposed_thenAllEightRolesMatchTarget() {
        val target: ColorScheme = lightColorScheme().copy(
            primary = Color(0xFF111111),
            onPrimary = Color(0xFF222222),
            primaryContainer = Color(0xFF333333),
            secondary = Color(0xFF666666),
            background = Color(0xFFEEEEEE),
            surface = Color(0xFF202020),
            onSurface = Color(0xFF303030),
            outline = Color(0xFFD0D0D0),
        )

        var result: ColorScheme? = null
        composeTestRule.setContent {
            result = animateColorScheme(target)
        }

        composeTestRule.waitForIdle()

        val scheme = checkNotNull(result)
        assertEquals(target.primary, scheme.primary)
        assertEquals(target.onPrimary, scheme.onPrimary)
        assertEquals(target.primaryContainer, scheme.primaryContainer)
        assertEquals(target.background, scheme.background)
        assertEquals(target.surface, scheme.surface)
        assertEquals(target.onSurface, scheme.onSurface)
        assertEquals(target.secondary, scheme.secondary)
        assertEquals(target.outline, scheme.outline)
    }
}
