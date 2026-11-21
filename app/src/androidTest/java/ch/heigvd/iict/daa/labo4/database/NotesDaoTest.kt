package ch.heigvd.iict.daa.labo4.database

import androidx.lifecycle.asFlow
import androidx.room3.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.heigvd.iict.daa.labo4.models.Note
import ch.heigvd.iict.daa.labo4.models.Schedule
import ch.heigvd.iict.daa.labo4.models.State
import ch.heigvd.iict.daa.labo4.models.Type
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Tests instrumentés du DAO sur une base Room en mémoire (exécutés sur un appareil / émulateur).
 */
@RunWith(AndroidJUnit4::class)
class NotesDaoTest {

    private lateinit var database: NotesDatabase
    private lateinit var dao: NotesDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NotesDatabase::class.java).build()
        dao = database.notesDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun note(title: String) =
        Note(null, State.IN_PROGRESS, title, "text", Calendar.getInstance(), Type.WORK)

    @Test
    fun insertWithAndWithoutScheduleIsReadBackWithRelation() = runBlocking {
        val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }
        dao.insertNoteWithSchedule(note("withSchedule"), Schedule(null, -1L, date))
        dao.insertNoteWithSchedule(note("withoutSchedule"), null)

        val notes = dao.getAllNotes().asFlow().first()
        assertEquals(2, notes.size)

        val scheduled = notes.single { it.note.title == "withSchedule" }
        assertNotNull(scheduled.schedule)
        // ownerId renseigné avec l'identifiant généré de la note
        assertEquals(scheduled.note.noteId, scheduled.schedule!!.ownerId)
        // le convertisseur de dates conserve la valeur à la milliseconde près
        assertEquals(date.timeInMillis, scheduled.schedule!!.date.timeInMillis)

        assertNull(notes.single { it.note.title == "withoutSchedule" }.schedule)
        assertEquals(2, dao.getNotesCount().asFlow().first())
    }

    @Test
    fun deleteAllRemovesNotesAndSchedules() = runBlocking {
        repeat(5) {
            dao.insertNoteWithSchedule(note("n$it"), Schedule(null, -1L, Calendar.getInstance()))
        }
        assertEquals(5, dao.getNotesCount().asFlow().first())

        dao.deleteAll()
        assertEquals(0, dao.getNotesCount().asFlow().first())
        assertEquals(0, dao.getAllNotes().asFlow().first().size)

        // aucune échéance orpheline : une nouvelle note sans échéance n'en récupère pas
        dao.insertNoteWithSchedule(note("after"), null)
        assertNull(dao.getAllNotes().asFlow().first().single().schedule)
    }
}
