package net.thunderbird.feature.mail.message.tags.internal

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.thunderbird.components.ui.bolt.atom.ClickableSurface
import net.thunderbird.components.ui.bolt.atom.Surface
import net.thunderbird.components.ui.bolt.atom.button.ButtonFilled
import net.thunderbird.components.ui.bolt.atom.button.ButtonIcon
import net.thunderbird.components.ui.bolt.atom.button.ButtonText
import net.thunderbird.components.ui.bolt.atom.icon.Icon
import net.thunderbird.components.ui.bolt.atom.icon.Icons
import net.thunderbird.components.ui.bolt.atom.text.TextBodyLarge
import net.thunderbird.components.ui.bolt.atom.text.TextBodyMedium
import net.thunderbird.components.ui.bolt.atom.text.TextBodySmall
import net.thunderbird.components.ui.bolt.atom.text.TextLabelLarge
import net.thunderbird.components.ui.bolt.atom.textfield.TextFieldOutlined
import net.thunderbird.components.ui.bolt.organism.AlertDialog
import net.thunderbird.components.ui.bolt.organism.TopAppBar
import net.thunderbird.components.ui.bolt.template.Scaffold
import net.thunderbird.components.ui.bolt.theme.BoltTheme
import net.thunderbird.core.preference.display.visualSettings.message.tags.MESSAGE_TAG_NAME_MAX_LENGTH
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.Editor
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.Event
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.KeywordError
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.State
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.TagItem

private const val COLORS_PER_ROW = 6
private const val LIGHT_THRESHOLD = 0.6f
private val COLOR_SWATCH_SIZE = 32.dp
private val TAG_SWATCH_SIZE = 24.dp

private val TAG_COLOR_PALETTE = listOf(
    0xFFFF0000.toInt(),
    0xFFFF9900.toInt(),
    0xFFFFC107.toInt(),
    0xFF009900.toInt(),
    0xFF00ACC1.toInt(),
    0xFF3333FF.toInt(),
    0xFF993399.toInt(),
    0xFFE91E63.toInt(),
    0xFF795548.toInt(),
    0xFF607D8B.toInt(),
    0xFF000000.toInt(),
    0xFFFFFFFF.toInt(),
)

@Composable
internal fun MessageTagsScreen(
    state: State,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(R.string.message_tags_title),
                navigationIcon = {
                    ButtonIcon(
                        onClick = { onEvent(Event.OnBackClick) },
                        imageVector = Icons.Outlined.ArrowBack,
                    )
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            LazyColumn {
                item {
                    TextBodyMedium(
                        text = stringResource(R.string.message_tags_description),
                        modifier = Modifier.padding(BoltTheme.spacings.double),
                    )
                }
                items(items = state.tags, key = { it.keyword }) { tag ->
                    TagRow(tag = tag, onClick = { onEvent(Event.OnTagClick(tag.keyword)) })
                }
                item {
                    ButtonFilled(
                        text = stringResource(R.string.message_tags_add_tag),
                        icon = Icons.Outlined.Add,
                        onClick = { onEvent(Event.OnAddTagClick) },
                        modifier = Modifier.padding(BoltTheme.spacings.double),
                    )
                }
            }
        }
    }

    state.editor?.let { editor ->
        EditorDialog(editor = editor, onEvent = onEvent)
    }
}

@Composable
private fun TagRow(
    tag: TagItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ClickableSurface(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = BoltTheme.spacings.double, vertical = BoltTheme.spacings.default),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ColorSwatch(color = tag.color, size = TAG_SWATCH_SIZE)
            Spacer(modifier = Modifier.width(BoltTheme.spacings.double))
            Column {
                TextBodyLarge(text = tag.name)
                TextBodySmall(text = tag.keyword, color = BoltTheme.colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EditorDialog(
    editor: Editor,
    onEvent: (Event) -> Unit,
) {
    AlertDialog(
        title = stringResource(
            if (editor.isNewTag) R.string.message_tags_add_tag_title else R.string.message_tags_edit_tag_title,
        ),
        confirmText = stringResource(R.string.message_tags_save),
        onConfirmClick = { onEvent(Event.OnEditorSaveClick) },
        dismissText = stringResource(R.string.message_tags_cancel),
        onDismissClick = { onEvent(Event.OnEditorDismiss) },
        onDismissRequest = { onEvent(Event.OnEditorDismiss) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(BoltTheme.spacings.default)) {
            if (editor.isNewTag) {
                TextFieldOutlined(
                    value = editor.keyword,
                    onValueChange = { onEvent(Event.OnEditorKeywordChange(it)) },
                    label = stringResource(R.string.message_tags_keyword_label),
                    hasError = editor.keywordError != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextBodySmall(
                    text = editor.keywordError?.let { stringResource(it.messageResId()) }
                        ?: stringResource(R.string.message_tags_keyword_supporting_text),
                    color = if (editor.keywordError != null) {
                        BoltTheme.colors.error
                    } else {
                        BoltTheme.colors.onSurfaceVariant
                    },
                )
            }

            TextFieldOutlined(
                value = editor.name,
                onValueChange = { onEvent(Event.OnEditorNameChange(it.take(MESSAGE_TAG_NAME_MAX_LENGTH))) },
                label = stringResource(R.string.message_tags_name_label),
                modifier = Modifier.fillMaxWidth(),
            )

            TextLabelLarge(text = stringResource(R.string.message_tags_color_label))
            ColorPalette(
                selectedColor = editor.color,
                onColorSelect = { onEvent(Event.OnEditorColorChange(it)) },
            )

            if (editor.canReset) {
                ButtonText(
                    text = stringResource(
                        if (editor.isDefaultTag) R.string.message_tags_reset else R.string.message_tags_delete,
                    ),
                    onClick = { onEvent(Event.OnEditorResetClick) },
                )
            }
        }
    }
}

@Composable
private fun ColorPalette(
    selectedColor: Int,
    onColorSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BoltTheme.spacings.default)) {
        TAG_COLOR_PALETTE.chunked(COLORS_PER_ROW).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(BoltTheme.spacings.default)) {
                rowColors.forEach { color ->
                    ColorSwatch(
                        color = color,
                        size = COLOR_SWATCH_SIZE,
                        isSelected = color == selectedColor,
                        onClick = { onColorSelect(color) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val swatchColor = Color(color)
    val outline = BoltTheme.colors.outline
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(outline)
            .padding(1.dp)
            .clip(CircleShape)
            .background(swatchColor.compositeOver(Color.White))
            .then(clickableModifier),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = stringResource(R.string.message_tags_color_selected),
                tint = if (swatchColor.isLight()) Color.Black else Color.White,
            )
        }
    }
}

private fun Color.isLight(): Boolean = (0.299f * red + 0.587f * green + 0.114f * blue) > LIGHT_THRESHOLD

@StringRes
private fun KeywordError.messageResId(): Int = when (this) {
    KeywordError.Empty -> R.string.message_tags_keyword_error_empty
    KeywordError.Invalid -> R.string.message_tags_keyword_error_invalid
    KeywordError.AlreadyExists -> R.string.message_tags_keyword_error_exists
}
