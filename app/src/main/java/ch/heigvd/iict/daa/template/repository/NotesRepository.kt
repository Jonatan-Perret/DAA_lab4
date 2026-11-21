package ch.heigvd.iict.daa.template.repository

import androidx.lifecycle.LiveData
import ch.heigvd.iict.daa.template.database.NotesDao
import ch.heigvd.iict.daa.template.models.Note
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Repository des notes : point d'accès unique aux données pour le reste de l'application.
 * Il masque la source des données (ici la base Room) au ViewModel.
 *
 * Les écritures sont lancées dans [applicationScope], un scope lié à la durée de vie de
 * l'application et non à celle d'un écran : une écriture démarrée n'est ainsi jamais annulée
 * si l'utilisateur quitte l'écran pendant son exécution.
 *
 * @param notesDao le DAO d'accès aux notes
 * @param applicationScope scope de coroutines lié à la durée de vie de l'application
 */
class NotesRepository(
    private val notesDao: NotesDao,
    private val applicationScope: CoroutineScope
) {

    /** Toutes les notes avec leur éventuelle échéance, mises à jour automatiquement */
    val allNotes: LiveData<List<NoteAndSchedule>> = notesDao.getAllNotes()

    /** Nombre de notes en base, mis à jour automatiquement */
    val countNotes: LiveData<Int> = notesDao.getNotesCount()

    /**
     * Génère une note aléatoire (avec ou sans échéance, aléatoirement) et l'insère en base.
     */
    fun generateANote() {
        applicationScope.launch {
            notesDao.insertNoteWithSchedule(Note.generateRandomNote(), Note.generateRandomSchedule())
        }
    }

    /**
     * Supprime toutes les notes (et leurs échéances) de la base.
     */
    fun deleteAllNotes() {
        applicationScope.launch {
            notesDao.deleteAll()
        }
    }
}
