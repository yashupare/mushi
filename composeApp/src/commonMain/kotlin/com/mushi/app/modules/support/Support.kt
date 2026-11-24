package com.mushi.app.modules.support

import org.spotube.millennium.composables.MillenniumDialogHeader
import org.spotube.millennium.composables.MillenniumDialogTitle
import com.mushi.app.core.ui.component.AppDialogClose
import org.spotube.millennium.composables.MillenniumDialogBody
import org.spotube.millennium.composables.MillenniumDialogDescription
import org.spotube.millennium.composables.MillenniumDialogFooter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.openUrlInBrowser
import com.mushi.app.resources.iconsax.BrandBuyMeACoffee
import com.mushi.app.resources.iconsax.BrandOpencollective
import com.mushi.app.resources.iconsax.BrandPatreon
import com.mushi.app.resources.iconsax.Iconsax
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumDialog
import org.spotube.millennium.composables.MillenniumIcon
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.support_dialog_dismiss
import com.mushi.app.generated.resources.support_dialog_message
import com.mushi.app.generated.resources.support_dialog_reminder_hint
import com.mushi.app.generated.resources.support_dialog_title
import com.mushi.app.generated.resources.welcome_donate_bmac_subtitle
import com.mushi.app.generated.resources.welcome_donate_bmac_title
import com.mushi.app.generated.resources.welcome_donate_opencollective_subtitle
import com.mushi.app.generated.resources.welcome_donate_opencollective_title
import com.mushi.app.generated.resources.welcome_donate_patreon_subtitle
import com.mushi.app.generated.resources.welcome_donate_patreon_title

/** A single donation destination offered across the app. */
data class DonationOption(
    val id: String,
    val title: StringResource,
    val subtitle: StringResource,
    val url: String,
    val icon: ImageVector)

/** The donation destinations shown in onboarding, settings and the support prompt. */
/** Ways to support the app. Empty until Mushi has its own; the upstream author's links were removed. */
val donationOptions: List<DonationOption> = emptyList()

/** Minimum time between two automatic support reminders. */
const val SupportReminderIntervalMillis: Long = 7L * 24 * 60 * 60 * 1000

/**
 * Opens the support prompt from anywhere inside the app shell. Hosted at the app root so the
 * sidebar, home top bar and the periodic reminder all drive the same dialog.
 */
val LocalSupportPrompt = staticCompositionLocalOf { {} }

/** Opens a donation destination in the user's browser. */
fun openDonation(option: DonationOption) {
    runCatching { openUrlInBrowser(option.url) }
}

/** A dialog that lets the user pick a way to support the project. */
@Composable
fun SupportDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    isReminder: Boolean = false) {
    if (!visible) return
    MillenniumDialog(visible = true, onDismissRequest = onDismissRequest) {
        MillenniumDialogHeader {
            MillenniumDialogTitle(stringResource(Res.string.support_dialog_title))
            AppDialogClose(onDismissRequest)
        }
        MillenniumDialogBody {
            MillenniumDialogDescription(stringResource(Res.string.support_dialog_message))
            if (isReminder) {
                Text(
                    stringResource(Res.string.support_dialog_reminder_hint),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant)
            }
            donationOptions.forEach { option ->
                MillenniumButton(
                    onClick = {
                        openDonation(option)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = MillenniumButtonStyle.Secondary) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MillenniumIcon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (option.id == "patreon") {
                                AppTheme.colors.onSurface
                            } else {
                                Color.Unspecified
                            })
                        Text(stringResource(option.title))
                    }
                }
            }
        }
        MillenniumDialogFooter {
            MillenniumButton(
                onClick = onDismissRequest,
                style = MillenniumButtonStyle.Quiet) {
                Text(stringResource(Res.string.support_dialog_dismiss))
            }
        }
    }
}
