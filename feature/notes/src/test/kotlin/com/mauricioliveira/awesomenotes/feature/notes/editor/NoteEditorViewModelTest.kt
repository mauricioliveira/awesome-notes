package com.mauricioliveira.awesomenotes.feature.notes.editor

import androidx.lifecycle.SavedStateHandle
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.testing.FakeNoteRepository
import com.mauricioliveira.awesomenotes.core.testing.MainDispatcherRule
import com.mauricioliveira.awesomenotes.core.testing.aNote
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val note = aNote(id = "n1", title = "Groceries", content = "Milk")
    private val repository = FakeNoteRepository(note)
    private val savedStateHandle = SavedStateHandle()

    private fun editor(noteId: String? = "n1") = NoteEditorViewModel(noteId, savedStateHandle, repository)

    /** The current state, which the test expects to be the editing screen. */
    private val NoteEditorViewModel.editing: NoteEditorState.Editing
        get() = assertIs<NoteEditorState.Editing>(state.value)

    private fun NoteEditorViewModel.type(title: String? = null, content: String? = null) {
        title?.let { onIntent(NoteEditorIntent.TitleChanged(it)) }
        content?.let { onIntent(NoteEditorIntent.ContentChanged(it)) }
    }

    // ---- existing note

    @Test
    fun `opens the cached note`() = runTest {
        val editing = editor().editing

        assertEquals("Groceries", editing.title)
        assertEquals(note.updatedAt, editing.updatedAt)
        assertFalse(editing.hasChanges)
        assertFalse(editing.canSave)
    }

    @Test
    fun `a note that is not cached shows not found`() = runTest {
        assertEquals(NoteEditorState.NotFound, editor(noteId = "missing").state.value)
    }

    @Test
    fun `saving an edit updates the note and closes the editor`() = runTest {
        val editor = editor()
        editor.type(content = "Milk, eggs")

        editor.onIntent(NoteEditorIntent.Save)

        assertEquals(NoteEditorState.Closed, editor.state.value)
        assertEquals("Milk, eggs", repository.notes.value.single().content)
    }

    @Test
    fun `a failed save keeps the text, and retry saves it`() = runTest {
        repository.error = AppError.NoConnection
        val editor = editor()
        editor.type(content = "Milk, eggs")

        editor.onIntent(NoteEditorIntent.Save)

        assertEquals(EditorFailure(EditorAction.Save, AppError.NoConnection), editor.editing.failed)
        assertEquals("Milk, eggs", editor.editing.content)

        repository.error = null
        editor.onIntent(NoteEditorIntent.Retry)

        assertEquals(NoteEditorState.Closed, editor.state.value)
        assertEquals("Milk, eggs", repository.notes.value.single().content)
    }

    @Test
    fun `saving a note deleted on another device shows not found`() = runTest {
        repository.error = AppError.NotFound
        val editor = editor()
        editor.type(content = "Milk, eggs")

        editor.onIntent(NoteEditorIntent.Save)

        assertEquals(NoteEditorState.NotFound, editor.state.value)
    }

    @Test
    fun `shows saving and ignores a second save while it runs`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val editor = editor()
        editor.type(content = "Milk, eggs")

        editor.onIntent(NoteEditorIntent.Save)
        editor.onIntent(NoteEditorIntent.Save)

        assertEquals(EditorAction.Save, editor.editing.inProgress)
        assertFalse(editor.editing.canSave)
        gate.complete(Unit)
        assertEquals(NoteEditorState.Closed, editor.state.value)
    }

    @Test
    fun `delete asks first, then deletes and closes`() = runTest {
        val editor = editor()

        editor.onIntent(NoteEditorIntent.Delete)
        assertEquals(EditorDialog.Delete, editor.editing.dialog)

        editor.onIntent(NoteEditorIntent.ConfirmDelete)

        assertEquals(NoteEditorState.Closed, editor.state.value)
        assertTrue(repository.notes.value.isEmpty())
    }

    @Test
    fun `a failed delete keeps the note and offers a retry`() = runTest {
        repository.error = AppError.NoConnection
        val editor = editor()
        editor.onIntent(NoteEditorIntent.Delete)

        editor.onIntent(NoteEditorIntent.ConfirmDelete)

        assertEquals(EditorFailure(EditorAction.Delete, AppError.NoConnection), editor.editing.failed)
        assertNull(editor.editing.dialog)
        assertEquals(1, repository.notes.value.size)
    }

    @Test
    fun `back without changes closes, back with changes asks first`() = runTest {
        val untouched = editor()
        untouched.onIntent(NoteEditorIntent.Back)
        assertEquals(NoteEditorState.Closed, untouched.state.value)

        val edited = NoteEditorViewModel("n1", SavedStateHandle(), repository)
        edited.type(title = "Groceries for Saturday")
        edited.onIntent(NoteEditorIntent.Back)
        assertEquals(EditorDialog.Discard, edited.editing.dialog)

        edited.onIntent(NoteEditorIntent.ConfirmDiscard)
        assertEquals(NoteEditorState.Closed, edited.state.value)
        assertEquals("Groceries", repository.notes.value.single().title)
    }

    @Test
    fun `after process death the typed text comes back over the cached one`() = runTest {
        editor().type(content = "Milk, eggs, bread")

        // A new ViewModel with the same SavedStateHandle is what Android gives back after process death.
        val restored = editor().editing

        assertEquals("Milk, eggs, bread", restored.content)
        assertEquals("Milk", restored.savedContent)
        assertTrue(restored.hasChanges)
    }

    // ---- new note

    @Test
    fun `a new note cannot be saved while empty, and needs a title`() = runTest {
        val editor = editor(noteId = null)
        assertFalse(editor.editing.canSave)

        editor.type(content = "Call the dentist")
        editor.onIntent(NoteEditorIntent.Save)

        assertTrue(editor.editing.titleMissing)
        assertTrue(repository.createdIds.isEmpty())

        editor.type(title = "Dentist")
        assertFalse(editor.editing.titleMissing)
    }

    @Test
    fun `a new note keeps its id across a failed save and process death`() = runTest {
        repository.error = AppError.NoConnection
        val first = editor(noteId = null)
        first.type(title = "Dentist", content = "Monday afternoon")
        first.onIntent(NoteEditorIntent.Save)

        repository.error = null
        val restored = editor(noteId = null)
        restored.onIntent(NoteEditorIntent.Save)

        assertEquals(2, repository.createdIds.size)
        assertEquals(1, repository.createdIds.distinct().size)
        assertEquals(NoteEditorState.Closed, restored.state.value)
        assertEquals("Dentist", repository.notes.value.single { it.id == repository.createdIds.first() }.title)
    }

    @Test
    fun `text past the server limits is not accepted`() = runTest {
        val editor = editor(noteId = null)

        editor.type(title = "a".repeat(300))

        assertEquals(255, editor.editing.title.length)
    }
}
