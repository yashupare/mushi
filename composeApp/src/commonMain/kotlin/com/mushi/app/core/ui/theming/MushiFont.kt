package com.mushi.app.core.ui.theming

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.plus_jakarta_sans_bold
import com.mushi.app.generated.resources.plus_jakarta_sans_extrabold
import com.mushi.app.generated.resources.plus_jakarta_sans_medium
import com.mushi.app.generated.resources.plus_jakarta_sans_regular
import com.mushi.app.generated.resources.plus_jakarta_sans_semibold
import org.jetbrains.compose.resources.Font

/**
 * Mushi's typeface: Plus Jakarta Sans (SIL Open Font License 1.1; the licence ships in
 * `composeResources/files/licenses/plus_jakarta_sans_OFL.txt`). Scripts it doesn't cover, such as
 * Devanagari lyrics, fall back to the system font automatically.
 */
@Composable
fun rememberMushiFontFamily(): FontFamily = FontFamily(
    Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(Res.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(Res.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)
