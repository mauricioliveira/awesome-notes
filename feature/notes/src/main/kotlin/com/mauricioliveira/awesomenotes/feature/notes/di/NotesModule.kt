package com.mauricioliveira.awesomenotes.feature.notes.di

import com.mauricioliveira.awesomenotes.feature.notes.editor.NoteEditorViewModel
import com.mauricioliveira.awesomenotes.feature.notes.list.NotesListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notesModule = module {
    viewModel { NotesListViewModel(noteRepository = get(), authRepository = get()) }
    viewModel { params ->
        NoteEditorViewModel(
            noteId = params.getOrNull(),
            savedStateHandle = get(),
            noteRepository = get(),
        )
    }
}
