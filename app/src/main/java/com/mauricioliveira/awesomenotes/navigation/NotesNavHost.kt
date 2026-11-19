package com.mauricioliveira.awesomenotes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mauricioliveira.awesomenotes.feature.notes.navigation.NotesListDestination
import com.mauricioliveira.awesomenotes.feature.notes.navigation.navigateToEditor
import com.mauricioliveira.awesomenotes.feature.notes.navigation.noteEditorScreen
import com.mauricioliveira.awesomenotes.feature.notes.navigation.notesListScreen

@Composable
fun NotesNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NotesListDestination) {
        notesListScreen(
            onOpenNote = { noteId -> navController.navigateToEditor(noteId) },
            onNewNote = { navController.navigateToEditor() },
        )
        noteEditorScreen(onClose = navController::navigateUp)
    }
}
