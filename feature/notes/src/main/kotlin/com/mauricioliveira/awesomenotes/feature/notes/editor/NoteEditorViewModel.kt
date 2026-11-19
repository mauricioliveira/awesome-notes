package com.mauricioliveira.awesomenotes.feature.notes.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.model.asAppError
import com.mauricioliveira.awesomenotes.core.domain.repository.NoteRepository
import com.mauricioliveira.awesomenotes.core.domain.validation.NoteRules
import com.mauricioliveira.awesomenotes.feature.notes.editor.NoteEditorState.Closed
import com.mauricioliveira.awesomenotes.feature.notes.editor.NoteEditorState.Editing
import com.mauricioliveira.awesomenotes.feature.notes.editor.NoteEditorState.Loading
import com.mauricioliveira.awesomenotes.feature.notes.editor.NoteEditorState.NotFound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Edits an existing note ([noteId] set) or writes a new one ([noteId] null).
 *
 * [SavedStateHandle] keeps only what cannot be read again after process death:
 * the text being typed, and the id of a new note.
 */
class NoteEditorViewModel(
    noteId: String?,
    private val savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val isNew = noteId == null

    /** A new note gets its id once and keeps it, so retrying a create never makes a duplicate. */
    private val id: String = noteId
        ?: savedStateHandle[KEY_NEW_NOTE_ID]
        ?: UUID.randomUUID().toString().also { savedStateHandle[KEY_NEW_NOTE_ID] = it }

    private val _state = MutableStateFlow<NoteEditorState>(Loading)
    val state: StateFlow<NoteEditorState> = _state.asStateFlow()

    init {
        viewModelScope.launch { _state.value = open() }
    }

    fun onIntent(intent: NoteEditorIntent) {
        when (intent) {
            is NoteEditorIntent.TitleChanged -> {
                val title = intent.value.take(NoteRules.TITLE_MAX_LENGTH)
                savedStateHandle[KEY_TITLE] = title
                updateEditing { it.copy(title = title, titleMissing = false) }
            }
            is NoteEditorIntent.ContentChanged -> {
                val content = intent.value.take(NoteRules.CONTENT_MAX_LENGTH)
                savedStateHandle[KEY_CONTENT] = content
                updateEditing { it.copy(content = content) }
            }
            NoteEditorIntent.Save -> save()
            NoteEditorIntent.Delete -> updateEditing { if (it.canDelete) it.copy(dialog = EditorDialog.Delete) else it }
            NoteEditorIntent.ConfirmDelete -> delete()
            NoteEditorIntent.Back -> _state.update {
                if (it is Editing && it.hasChanges) it.copy(dialog = EditorDialog.Discard) else Closed
            }
            NoteEditorIntent.ConfirmDiscard -> _state.value = Closed
            NoteEditorIntent.DismissDialog -> updateEditing { it.copy(dialog = null) }
            NoteEditorIntent.Retry -> when ((_state.value as? Editing)?.failed?.action) {
                EditorAction.Save -> save()
                EditorAction.Delete -> delete()
                null -> Unit
            }
        }
    }

    /**
     * The starting screen: the cached note (or an empty one for a new note), with any text typed
     * before process death on top of it.
     */
    private suspend fun open(): NoteEditorState {
        val note = if (isNew) null else noteRepository.observeNote(id).first() ?: return NotFound
        val savedTitle = note?.title.orEmpty()
        val savedContent = note?.content.orEmpty()
        return Editing(
            isNew = isNew,
            title = savedStateHandle[KEY_TITLE] ?: savedTitle,
            content = savedStateHandle[KEY_CONTENT] ?: savedContent,
            savedTitle = savedTitle,
            savedContent = savedContent,
            updatedAt = note?.updatedAt,
        )
    }

    private fun save() {
        val editing = _state.value as? Editing ?: return
        if (!editing.canSave) return
        if (!NoteRules.isValidTitle(editing.title)) {
            _state.value = editing.copy(titleMissing = true)
            return
        }
        _state.value = editing.copy(inProgress = EditorAction.Save, failed = null)

        viewModelScope.launch {
            val title = editing.title.trim()
            // A new note stays new until a create succeeds. Retrying create with the same id is safe:
            // if the first request did reach the server, it answers 200 with that note.
            val result = if (isNew) {
                noteRepository.create(id, title, editing.content)
            } else {
                noteRepository.update(id, title, editing.content)
            }
            finish(EditorAction.Save, result)
        }
    }

    private fun delete() {
        val editing = _state.value as? Editing ?: return
        if (!editing.canDelete) return
        _state.value = editing.copy(inProgress = EditorAction.Delete, failed = null, dialog = null)

        viewModelScope.launch {
            finish(EditorAction.Delete, noteRepository.delete(id))
        }
    }

    /** Success closes the editor. A failure keeps the text and offers a retry. */
    private fun finish(action: EditorAction, result: Result<*>) {
        val error = result.exceptionOrNull()?.asAppError()
        _state.update { current ->
            when {
                error == null -> Closed
                // Deleted on another device while it was open here.
                error == AppError.NotFound -> NotFound
                current is Editing -> current.copy(inProgress = null, failed = EditorFailure(action, error))
                else -> current
            }
        }
    }

    /** Changes the state only while editing. Intents that arrive on another screen are ignored. */
    private inline fun updateEditing(transform: (Editing) -> NoteEditorState) {
        _state.update { if (it is Editing) transform(it) else it }
    }

    private companion object {
        const val KEY_NEW_NOTE_ID = "newNoteId"
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
    }
}
