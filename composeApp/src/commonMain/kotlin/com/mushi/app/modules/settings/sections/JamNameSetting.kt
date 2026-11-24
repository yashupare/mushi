package com.mushi.app.modules.settings.sections

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.core.jam.JamNames
import com.mushi.app.modules.settings.components.TextInputSettingCard
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxEdit
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.jam_name_description
import com.mushi.app.generated.resources.jam_name_subtitle
import com.mushi.app.generated.resources.jam_name_title
import com.mushi.app.generated.resources.jam_name_too_long

/**
 * "Your name" card for Group Jam. Shared by the Jam screen and Jam settings so both stay
 * identical, like the remote control device name card.
 *
 * [customName] is what the user saved (blank = none), [defaultName] the generated one.
 */
@Composable
internal fun JamNameSettingCard(
    customName: String,
    defaultName: String,
    onNameSaved: (String) -> Unit,
    enabled: Boolean = true
) {
    val tooLong = stringResource(Res.string.jam_name_too_long, JamNames.MAX_LENGTH)
    TextInputSettingCard(
        title = stringResource(Res.string.jam_name_title),
        subtitle = stringResource(Res.string.jam_name_subtitle, customName.ifBlank { defaultName }),
        icon = { SettingsItemIcon(com.mushi.app.core.ui.theming.MushiIcons.Edit, stringResource(Res.string.jam_name_title)) },
        value = customName,
        dialogDescription = stringResource(Res.string.jam_name_description, defaultName),
        placeholder = defaultName,
        normalize = JamNames::normalize,
        validate = { value -> if (value.length > JamNames.MAX_LENGTH) tooLong else null },
        onValueSaved = onNameSaved,
        enabled = enabled
    )
}
