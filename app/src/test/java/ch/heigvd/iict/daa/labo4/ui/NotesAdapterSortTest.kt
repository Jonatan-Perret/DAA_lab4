package ch.heigvd.iict.daa.labo4.ui

import ch.heigvd.iict.daa.labo4.models.Note
import ch.heigvd.iict.daa.labo4.models.NoteAndSchedule
import ch.heigvd.iict.daa.labo4.models.Schedule
import ch.heigvd.iict.daa.labo4.models.SortOrder
import ch.heigvd.iict.daa.labo4.models.State
import ch.heigvd.iict.daa.labo4.models.Type
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Tests unitaires (JVM) de la logique de tri et du calcul des délais de [NotesAdapter].
 */
class NotesAdapterSortTest {

    /** @return un Calendar décalé de [days] jours (et [minutes] minutes) par rapport à maintenant */
    private fun inDays(days: Int, minutes: Int = 0): Calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, days)
        add(Calendar.MINUTE, minutes)
    }

    /** Crée une note d'identifiant [id], créée à [created], avec une échéance optionnelle. */
    private fun item(id: Long, created: Calendar, scheduled: Calendar?) = NoteAndSchedule(
        Note(id, State.IN_PROGRESS, "title$id", "text$id", created, Type.TODO),
        scheduled?.let { Schedule(id, id, it) }
    )

    private val notes = listOf(
        item(1, created = inDays(-3), scheduled = inDays(10)),
        item(2, created = inDays(-1), scheduled = null),
        item(3, created = inDays(-5), scheduled = inDays(-2)),
        item(4, created = inDays(-2), scheduled = null),
        item(5, created = inDays(-4), scheduled = inDays(1)),
    )

    private fun List<NoteAndSchedule>.ids() = map { it.note.noteId }

    @Test
    fun noSortKeepsDatabaseOrder() {
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), NotesAdapter.sortNotes(notes, SortOrder.NONE).ids())
    }

    @Test
    fun sortByCreationDateIsNewestFirst() {
        assertEquals(
            listOf(2L, 4L, 1L, 5L, 3L),
            NotesAdapter.sortNotes(notes, SortOrder.BY_CREATION_DATE).ids()
        )
    }

    @Test
    fun sortByScheduleDateIsEarliestFirstWithUnscheduledLast() {
        // les notes sans échéance (2 et 4) restent à la fin, dans leur ordre d'origine
        assertEquals(
            listOf(3L, 5L, 1L, 2L, 4L),
            NotesAdapter.sortNotes(notes, SortOrder.BY_SCHEDULE_DATE).ids()
        )
    }

    @Test
    fun sortingDoesNotModifyTheSourceList() {
        NotesAdapter.sortNotes(notes, SortOrder.BY_CREATION_DATE)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), notes.ids())
    }

    @Test
    fun daysFromTodayCountsCalendarDays() {
        assertEquals(0, NotesAdapter.daysFromToday(Calendar.getInstance()))
        assertEquals(1, NotesAdapter.daysFromToday(inDays(1)))
        assertEquals(-1, NotesAdapter.daysFromToday(inDays(-1)))
        assertEquals(45, NotesAdapter.daysFromToday(inDays(45)))
        // l'heure dans la journée n'a pas d'influence
        val tomorrowMorning = inDays(1).apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 1) }
        assertEquals(1, NotesAdapter.daysFromToday(tomorrowMorning))
    }
}
