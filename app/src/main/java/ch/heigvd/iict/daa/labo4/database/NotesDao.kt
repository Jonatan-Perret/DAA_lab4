package ch.heigvd.iict.daa.labo4.database

import androidx.lifecycle.LiveData
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import ch.heigvd.iict.daa.labo4.models.Note
import ch.heigvd.iict.daa.labo4.models.NoteAndSchedule
import ch.heigvd.iict.daa.labo4.models.Schedule

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * DAO (Data Access Object) donnant accès aux notes et à leurs échéances.
 *
 * Les lectures retournent des [LiveData] : Room ré-exécute automatiquement la requête et
 * notifie les observateurs à chaque modification des tables concernées.
 * Les écritures sont des fonctions suspend : elles s'exécutent hors du thread UI.
 */
@Dao
interface NotesDao {

    /**
     * Insère une note.
     * @param note la note à insérer (noteId à null pour qu'il soit auto-généré)
     * @return l'identifiant généré pour la note
     */
    @Insert
    suspend fun insertNote(note: Note): Long

    /**
     * Insère une échéance.
     * @param schedule l'échéance à insérer (son ownerId doit référencer une note existante)
     * @return l'identifiant généré pour l'échéance
     */
    @Insert
    suspend fun insertSchedule(schedule: Schedule): Long

    /**
     * Insère une note et, si elle existe, son échéance associée, au sein d'une même transaction.
     * L'ownerId de l'échéance est renseigné avec l'identifiant généré pour la note.
     * @param note la note à insérer
     * @param schedule l'échéance associée à la note, ou null si la note n'en a pas
     */
    @Transaction
    suspend fun insertNoteWithSchedule(note: Note, schedule: Schedule?) {
        val noteId = insertNote(note)
        if (schedule != null) {
            schedule.ownerId = noteId
            insertSchedule(schedule)
        }
    }

    /**
     * Récupère toutes les notes avec leur éventuelle échéance (relation One-to-One facultative).
     * L'annotation @Transaction garantit la cohérence entre la requête sur Note et celle
     * effectuée par Room sur Schedule pour résoudre la relation.
     * @return une LiveData contenant la liste (observable) de toutes les notes
     */
    @Transaction
    @Query("SELECT * FROM Note")
    fun getAllNotes(): LiveData<List<NoteAndSchedule>>

    /**
     * @return une LiveData contenant le nombre de notes en base
     */
    @Query("SELECT COUNT(*) FROM Note")
    fun getNotesCount(): LiveData<Int>

    /** Supprime toutes les notes. */
    @Query("DELETE FROM Note")
    suspend fun deleteAllNotes()

    /** Supprime toutes les échéances. */
    @Query("DELETE FROM Schedule")
    suspend fun deleteAllSchedules()

    /**
     * Supprime toutes les notes et toutes les échéances au sein d'une même transaction.
     * Les entités fournies ne déclarant pas de clé étrangère (pas de suppression en cascade),
     * les échéances doivent être supprimées explicitement pour ne pas laisser d'orphelins.
     */
    @Transaction
    suspend fun deleteAll() {
        deleteAllSchedules()
        deleteAllNotes()
    }
}
