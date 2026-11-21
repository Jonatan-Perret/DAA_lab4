package ch.heigvd.iict.daa.labo4.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.heigvd.iict.daa.labo4.NotesApp
import ch.heigvd.iict.daa.labo4.models.NoteAndSchedule
import ch.heigvd.iict.daa.labo4.models.SortOrder
import ch.heigvd.iict.daa.labo4.repository.NotesRepository
import ch.heigvd.iict.daa.labo4.repository.SortPreferencesRepository

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * ViewModel partagé entre la MainActivity et ses deux Fragments (liste et contrôles).
 * Il expose les données sous forme de LiveData et les actions possibles sur celles-ci.
 *
 * Conformément à l'architecture MVVM, il ne contient aucune référence vers des éléments du
 * cycle de vie Android (Activité, Fragment, Vue, Application, Context) : ses dépendances
 * (les repositories) lui sont injectées par le constructeur, via [Factory].
 *
 * @param notesRepository repository des notes
 * @param sortPreferencesRepository repository du choix de tri persistant
 */
class NotesViewModel(
    private val notesRepository: NotesRepository,
    private val sortPreferencesRepository: SortPreferencesRepository
) : ViewModel() {

    /** Toutes les notes (avec leur éventuelle échéance) */
    val allNotes: LiveData<List<NoteAndSchedule>> = notesRepository.allNotes

    /** Nombre de notes en base */
    val countNotes: LiveData<Int> = notesRepository.countNotes

    /** Ordre de tri courant (persistant, restauré au démarrage de l'application) */
    val sortOrder: LiveData<SortOrder> = sortPreferencesRepository.sortOrder.asLiveData()

    /** Crée une note aléatoire et l'insère dans la base de données. */
    fun generateANote() {
        notesRepository.generateANote()
    }

    /** Supprime toutes les notes de la base de données. */
    fun deleteAllNote() {
        notesRepository.deleteAllNotes()
    }

    /**
     * Change (et enregistre) l'ordre de tri de la liste des notes.
     * @param sortOrder le nouvel ordre de tri
     */
    fun sortBy(sortOrder: SortOrder) {
        sortPreferencesRepository.setSortOrder(sortOrder)
    }

    companion object {
        /**
         * Factory permettant d'instancier le ViewModel avec ses dépendances.
         * L'Application n'est utilisée ici que pour récupérer les repositories : elle n'est pas
         * transmise au ViewModel.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotesApp
                NotesViewModel(app.notesRepository, app.sortPreferencesRepository)
            }
        }
    }
}
