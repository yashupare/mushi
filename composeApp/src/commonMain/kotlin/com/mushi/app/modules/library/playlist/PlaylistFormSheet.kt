package com.mushi.app.modules.library.playlist

import com.mushi.app.core.ui.component.MushiButton
import com.mushi.app.core.ui.component.AdaptiveDialogBottomSheet
import androidx.compose.foundation.clickable
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumDialog
import org.spotube.millennium.composables.MillenniumModalBottomSheet
import org.spotube.millennium.composables.MillenniumSwitch
import org.spotube.millennium.composables.MillenniumTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.SheetDetent
import com.composeunstyled.Text
import com.composeunstyled.rememberModalBottomSheetState
import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.millennium.adaptive.WindowBreakpoints
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.stringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.library_playlist_form_change_image
import com.mushi.app.generated.resources.library_playlist_form_collaborative
import com.mushi.app.generated.resources.library_playlist_form_collaborative_description
import com.mushi.app.generated.resources.library_playlist_form_cover_content_description
import com.mushi.app.generated.resources.library_playlist_form_cover_image
import com.mushi.app.generated.resources.library_playlist_form_create
import com.mushi.app.generated.resources.library_playlist_form_create_title
import com.mushi.app.generated.resources.library_playlist_form_description_label
import com.mushi.app.generated.resources.library_playlist_form_description_placeholder
import com.mushi.app.generated.resources.library_playlist_form_edit_title
import com.mushi.app.generated.resources.library_playlist_form_name_label
import com.mushi.app.generated.resources.library_playlist_form_name_placeholder
import com.mushi.app.generated.resources.library_playlist_form_no_image
import com.mushi.app.generated.resources.library_playlist_form_pick_image
import com.mushi.app.generated.resources.library_playlist_form_public
import com.mushi.app.generated.resources.library_playlist_form_public_description
import com.mushi.app.generated.resources.common_cancel
import com.mushi.app.generated.resources.common_save

data class PlaylistFormData(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val isCollaborating: Boolean = false,
    val imageBase64: String = "",
    val imagePreviewUrl: String? = null
)

@Composable
fun PlaylistFormSheet(
    visible: Boolean,
    formState: PlaylistFormUiState,
    onDismiss: () -> Unit,
    onSubmit: (PlaylistFormData) -> Unit,
    onImagePicked: (PlatformFile) -> Unit,
    modifier: Modifier = Modifier,
    initialData: PlaylistFormData? = null,
    isEditing: Boolean = false
) {
    if (!visible) return

    val isLargeScreen = currentWindowWidthBreakpoint() isAtLeast WindowBreakpoints.Medium

    var name by remember(initialData) { mutableStateOf(initialData?.name ?: "") }
    var description by remember(initialData) { mutableStateOf(initialData?.description ?: "") }
    var isPublic by remember(initialData) { mutableStateOf(initialData?.isPublic ?: true) }
    var isCollaborating by remember(initialData) {
        mutableStateOf(
            initialData?.isCollaborating ?: false
        )
    }
    val imageBase64 = formState.pickedImageBase64 ?: initialData?.imageBase64.orEmpty()
    val imagePreviewUrl = if (formState.pickedImageBase64 != null) null else initialData?.imagePreviewUrl

    val nameState = rememberTextFieldState(initialData?.name ?: "")
    val descriptionState = rememberTextFieldState(initialData?.description ?: "")

    LaunchedEffect(nameState) {
        snapshotFlow { nameState.text.toString() }
            .drop(1)
            .collect { name = it }
    }
    LaunchedEffect(descriptionState) {
        snapshotFlow { descriptionState.text.toString() }
            .drop(1)
            .collect { description = it }
    }

    val imagePicker = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file ->
        if (file != null) onImagePicked(file)
    }

    val currentData = PlaylistFormData(
        name = name,
        description = description,
        isPublic = isPublic,
        isCollaborating = isCollaborating,
        imageBase64 = imageBase64,
        imagePreviewUrl = imagePreviewUrl
    )

    val handleImagePick = { imagePicker.launch() }

    val formTitle = stringResource(
        if (isEditing) Res.string.library_playlist_form_edit_title else Res.string.library_playlist_form_create_title)
    AdaptiveDialogBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        dialogTitle = formTitle,
        title = { Text(formTitle, style = AppTheme.typography.headlineSmall) },
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                MillenniumButton(
                    onClick = onDismiss,
                    enabled = !formState.isSubmitting,
                    style = MillenniumButtonStyle.Secondary) {
                    Text(stringResource(Res.string.common_cancel))
                }
                MushiButton(
                    onClick = { onSubmit(currentData) },
                    enabled = name.isNotBlank() && !formState.isProcessingImage,
                    loading = formState.isSubmitting) {
                    Text(stringResource(if (isEditing) Res.string.common_save else Res.string.library_playlist_form_create))
                }
            }
        }) {
        PlaylistFormFields(
            nameState = nameState,
            descriptionState = descriptionState,
            isPublic = isPublic,
            onIsPublicChange = { isPublic = it },
            isCollaborating = isCollaborating,
            onIsCollaboratingChange = { isCollaborating = it },
            imageBase64 = imageBase64,
            imagePreviewUrl = imagePreviewUrl,
            isProcessingImage = formState.isProcessingImage,
            isImagePickEnabled = !formState.isSubmitting,
            onPickImage = handleImagePick)
    }
}

@Composable
private fun PlaylistFormFields(
    nameState: TextFieldState,
    descriptionState: TextFieldState,
    isPublic: Boolean,
    onIsPublicChange: (Boolean) -> Unit,
    isCollaborating: Boolean,
    onIsCollaboratingChange: (Boolean) -> Unit,
    imageBase64: String,
    imagePreviewUrl: String?,
    isProcessingImage: Boolean,
    isImagePickEnabled: Boolean,
    onPickImage: () -> Unit
) {
    // AdaptiveDialogBottomSheet already scrolls its content; a second scroll here nests
    // scrollables and crashes with infinite height constraints.
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MillenniumTextField(
            state = nameState,
            label = stringResource(Res.string.library_playlist_form_name_label),
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(Res.string.library_playlist_form_name_placeholder)
        )

        MillenniumTextField(
            state = descriptionState,
            label = stringResource(Res.string.library_playlist_form_description_label),
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(Res.string.library_playlist_form_description_placeholder)
        )

        val haptics = com.mushi.app.core.ui.rememberHaptics()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { haptics.tick(); onIsPublicChange(!isPublic) }
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.library_playlist_form_public), style = AppTheme.typography.bodyLarge)
                Text(
                    stringResource(Res.string.library_playlist_form_public_description),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }
            MillenniumSwitch(checked = isPublic, onCheckedChange = { haptics.tick(); onIsPublicChange(it) })
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { haptics.tick(); onIsCollaboratingChange(!isCollaborating) }
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.library_playlist_form_collaborative), style = AppTheme.typography.bodyLarge)
                Text(
                    stringResource(Res.string.library_playlist_form_collaborative_description),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }
            MillenniumSwitch(checked = isCollaborating, onCheckedChange = { haptics.tick(); onIsCollaboratingChange(it) })
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(Res.string.library_playlist_form_cover_image), style = AppTheme.typography.bodyLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        imagePreviewUrl != null -> AsyncImage(
                            model = imagePreviewUrl,
                            contentDescription = stringResource(Res.string.library_playlist_form_cover_content_description),
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Crop
                        )

                        imageBase64.isNotBlank() -> {
                            Text(
                                "✓",
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.primary
                            )
                        }

                        else -> {
                            Text(
                                stringResource(Res.string.library_playlist_form_no_image),
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
                MushiButton(
                    onClick = onPickImage,
                    enabled = isImagePickEnabled,
                    loading = isProcessingImage) {
                    Text(stringResource(if (imageBase64.isNotBlank() || imagePreviewUrl != null) Res.string.library_playlist_form_change_image else Res.string.library_playlist_form_pick_image))
                }
            }
        }
    }
}
