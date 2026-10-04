package net.thunderbird.feature.mail.message.tags.internal

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import net.thunderbird.core.ui.contract.mvi.UnidirectionalViewModel

internal interface MessageTagsContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    @Stable
    data class State(
        val tags: ImmutableList<TagItem> = persistentListOf(),
        val editor: Editor? = null,
    )

    /**
     * A tag as displayed in the list.
     *
     * @param isDefaultTag `true` for the tags every user has by default, `false` for tags added by the user.
     * @param isCustomized `true` if the user changed the name or color of a default tag.
     */
    data class TagItem(
        val keyword: String,
        val name: String,
        val color: Int,
        val isDefaultTag: Boolean,
        val isCustomized: Boolean,
    )

    /**
     * State of the dialog used to edit an existing tag or to add a new one.
     *
     * @param isNewTag `true` when adding a new tag. The keyword can only be changed for new tags.
     * @param canReset `true` if there is something to reset (default tags) or delete (custom tags).
     */
    data class Editor(
        val isNewTag: Boolean,
        val keyword: String,
        val name: String,
        val color: Int,
        val isDefaultTag: Boolean,
        val canReset: Boolean,
        val keywordError: KeywordError? = null,
    )

    enum class KeywordError {
        Empty,
        Invalid,
        AlreadyExists,
    }

    sealed interface Event {
        data class OnTagClick(val keyword: String) : Event
        data object OnAddTagClick : Event
        data class OnEditorKeywordChange(val keyword: String) : Event
        data class OnEditorNameChange(val name: String) : Event
        data class OnEditorColorChange(val color: Int) : Event
        data object OnEditorSaveClick : Event
        data object OnEditorResetClick : Event
        data object OnEditorDismiss : Event
        data object OnBackClick : Event
    }

    sealed interface Effect {
        data object NavigateBack : Effect
    }
}
