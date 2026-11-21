package ch.heigvd.iict.daa.template.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.daa.template.R
import ch.heigvd.iict.daa.template.databinding.ListItemNoteBinding
import ch.heigvd.iict.daa.template.databinding.ListItemNoteScheduleBinding
import ch.heigvd.iict.daa.template.models.Note
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import ch.heigvd.iict.daa.template.models.Schedule
import ch.heigvd.iict.daa.template.models.SortOrder
import ch.heigvd.iict.daa.template.models.State
import ch.heigvd.iict.daa.template.models.Type
import com.google.android.material.color.MaterialColors
import java.text.DateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Adapter de la RecyclerView affichant les notes.
 *
 * - Deux types de vues (et donc deux layouts) sont utilisés : un pour les notes "simples",
 *   un pour les notes associées à une échéance (Schedule).
 * - L'adapter implémente le tri des notes : il conserve la liste reçue de la base (non triée)
 *   ainsi que l'ordre de tri courant, et affiche la liste triée correspondante.
 * - Il hérite de [ListAdapter] : les différences entre l'ancienne et la nouvelle liste sont
 *   calculées par DiffUtil (sur un thread d'arrière-plan), ce qui permet de ne rafraîchir que
 *   les éléments modifiés (avec animations) au lieu de redessiner toute la liste.
 */
class NotesAdapter : ListAdapter<NoteAndSchedule, NotesAdapter.NoteViewHolder>(NotesDiffCallback) {

    /** Dernière liste reçue de la base de données, dans son ordre d'origine */
    private var unsortedNotes: List<NoteAndSchedule> = emptyList()

    /**
     * Ordre de tri courant. Il vaut null tant que l'ordre enregistré n'a pas encore été lu
     * (lecture asynchrone) : on n'affiche alors rien, afin d'éviter que la liste ne s'affiche
     * brièvement non triée avant d'être réordonnée.
     */
    private var sortOrder: SortOrder? = null

    /**
     * Met à jour les notes à afficher (en conservant l'ordre de tri courant).
     * @param notes la liste des notes issue de la base de données
     */
    fun setNotes(notes: List<NoteAndSchedule>) {
        unsortedNotes = notes
        refresh()
    }

    /**
     * Change l'ordre de tri des notes affichées.
     * @param newSortOrder le nouvel ordre de tri
     * @param onSortChanged appelé une fois la liste réordonnée affichée, uniquement si l'ordre de
     *                      tri a effectivement été modifié (et non lors de son chargement initial)
     */
    fun setSortOrder(newSortOrder: SortOrder, onSortChanged: () -> Unit = {}) {
        if (newSortOrder == sortOrder) return
        val isChange = sortOrder != null
        sortOrder = newSortOrder
        refresh(if (isChange) onSortChanged else null)
    }

    /**
     * Soumet la liste triée selon l'ordre courant (si celui-ci est connu).
     * @param onCommitted callback appelé lorsque la nouvelle liste est affichée
     */
    private fun refresh(onCommitted: (() -> Unit)? = null) {
        val order = sortOrder ?: return
        submitList(sortNotes(unsortedNotes, order), onCommitted?.let { Runnable(it) })
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).schedule == null) VIEW_TYPE_SIMPLE else VIEW_TYPE_SCHEDULED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SCHEDULED ->
                ScheduledNoteViewHolder(ListItemNoteScheduleBinding.inflate(inflater, parent, false))
            else ->
                SimpleNoteViewHolder(ListItemNoteBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** ViewHolder de base, commun aux deux types de notes. */
    abstract class NoteViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Affiche une note dans l'item.
         * @param item la note (et son éventuelle échéance) à afficher
         */
        abstract fun bind(item: NoteAndSchedule)

        /**
         * Remplit les champs communs aux deux layouts : icône du type (teintée selon l'état),
         * titre, texte, état et date de création.
         */
        protected fun bindNote(
            note: Note,
            typeIcon: ImageView,
            title: TextView,
            text: TextView,
            info: TextView
        ) {
            val context = itemView.context

            typeIcon.setImageResource(note.type.iconRes())
            typeIcon.contentDescription = context.getString(note.type.labelRes())
            val stateColor = ContextCompat.getColor(
                context,
                if (note.state == State.DONE) R.color.note_done else R.color.note_in_progress
            )
            ImageViewCompat.setImageTintList(typeIcon, ColorStateList.valueOf(stateColor))

            title.text = note.title
            text.text = note.text
            info.text = context.getString(
                R.string.note_info,
                context.getString(note.state.labelRes()),
                DATE_TIME_FORMAT.format(note.creationDate.time)
            )
        }
    }

    /** ViewHolder d'une note "simple" (sans échéance). */
    private class SimpleNoteViewHolder(private val binding: ListItemNoteBinding) :
        NoteViewHolder(binding.root) {

        override fun bind(item: NoteAndSchedule) {
            bindNote(item.note, binding.noteTypeIcon, binding.noteTitle, binding.noteText, binding.noteInfo)
        }
    }

    /** ViewHolder d'une note associée à une échéance. */
    private class ScheduledNoteViewHolder(private val binding: ListItemNoteScheduleBinding) :
        NoteViewHolder(binding.root) {

        override fun bind(item: NoteAndSchedule) {
            bindNote(item.note, binding.noteTypeIcon, binding.noteTitle, binding.noteText, binding.noteInfo)
            // getItemViewType garantit qu'un ScheduledNoteViewHolder reçoit toujours une échéance
            bindSchedule(item.note, requireNotNull(item.schedule))
        }

        /**
         * Affiche l'échéance : date prévue et délai restant. Une note non terminée dont la date
         * prévue est passée est signalée "En retard" (texte et horloge en rouge).
         * @param note la note propriétaire de l'échéance
         * @param schedule l'échéance à afficher
         */
        private fun bindSchedule(note: Note, schedule: Schedule) {
            val context = itemView.context
            val resources = context.resources
            val days = daysFromToday(schedule.date)
            val isLate = days < 0 && note.state == State.IN_PROGRESS

            binding.scheduleDate.text = DATE_FORMAT.format(schedule.date.time)
            binding.scheduleRemaining.text = when {
                isLate -> context.getString(R.string.schedule_late)
                days == 0 -> context.getString(R.string.schedule_today)
                days > 0 -> resources.getQuantityString(R.plurals.schedule_in_days, days, days)
                else -> resources.getQuantityString(R.plurals.schedule_days_ago, abs(days), abs(days))
            }

            val color = if (isLate) {
                ContextCompat.getColor(context, R.color.note_late)
            } else {
                MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurfaceVariant)
            }
            binding.scheduleRemaining.setTextColor(color)
            ImageViewCompat.setImageTintList(binding.scheduleIcon, ColorStateList.valueOf(color))
        }
    }

    /**
     * Comparaison des éléments pour DiffUtil : deux éléments représentent la même note s'ils
     * ont le même identifiant ; leur contenu est identique si les data classes sont égales.
     */
    private object NotesDiffCallback : DiffUtil.ItemCallback<NoteAndSchedule>() {
        override fun areItemsTheSame(oldItem: NoteAndSchedule, newItem: NoteAndSchedule) =
            oldItem.note.noteId == newItem.note.noteId

        override fun areContentsTheSame(oldItem: NoteAndSchedule, newItem: NoteAndSchedule) =
            oldItem == newItem
    }

    companion object {
        private const val VIEW_TYPE_SIMPLE = 0
        private const val VIEW_TYPE_SCHEDULED = 1

        /** Formats de dates, dépendants de la langue de l'appareil */
        private val DATE_FORMAT: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
        private val DATE_TIME_FORMAT: DateFormat =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

        /**
         * Trie les notes selon l'ordre demandé (tri stable : à égalité, l'ordre d'origine est gardé).
         * @param notes les notes à trier
         * @param sortOrder l'ordre de tri
         * @return une nouvelle liste triée
         */
        fun sortNotes(notes: List<NoteAndSchedule>, sortOrder: SortOrder): List<NoteAndSchedule> =
            when (sortOrder) {
                SortOrder.NONE -> notes
                // de la plus récente à la plus ancienne
                SortOrder.BY_CREATION_DATE -> notes.sortedByDescending { it.note.creationDate }
                // échéance la plus proche en premier, notes sans échéance à la fin
                SortOrder.BY_SCHEDULE_DATE -> notes.sortedWith(compareBy(nullsLast()) { it.schedule?.date })
            }

        /**
         * Calcule le nombre de jours calendaires entre aujourd'hui et une date.
         * @param date la date cible
         * @return le nombre de jours (négatif si la date est passée, 0 si c'est aujourd'hui)
         */
        fun daysFromToday(date: Calendar): Int {
            val difference = startOfDay(date).timeInMillis - startOfDay(Calendar.getInstance()).timeInMillis
            // arrondi : absorbe le décalage d'une heure lors des changements d'heure (été/hiver)
            return (difference.toDouble() / TimeUnit.DAYS.toMillis(1)).roundToLong().toInt()
        }

        /** @return une copie de la date, ramenée à minuit (début de journée) */
        private fun startOfDay(date: Calendar): Calendar =
            (date.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        /** @return l'icône associée au type de note */
        private fun Type.iconRes(): Int = when (this) {
            Type.NONE -> R.drawable.note
            Type.TODO -> R.drawable.todo
            Type.SHOPPING -> R.drawable.shopping
            Type.WORK -> R.drawable.work
            Type.FAMILY -> R.drawable.family
        }

        /** @return le libellé (accessibilité) associé au type de note */
        private fun Type.labelRes(): Int = when (this) {
            Type.NONE -> R.string.type_none
            Type.TODO -> R.string.type_todo
            Type.SHOPPING -> R.string.type_shopping
            Type.WORK -> R.string.type_work
            Type.FAMILY -> R.string.type_family
        }

        /** @return le libellé associé à l'état de la note */
        private fun State.labelRes(): Int = when (this) {
            State.IN_PROGRESS -> R.string.state_in_progress
            State.DONE -> R.string.state_done
        }
    }
}
