package ch.heigvd.iict.daa.labo4.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.daa.labo4.R
import ch.heigvd.iict.daa.labo4.databinding.FragmentNotesListBinding
import ch.heigvd.iict.daa.labo4.viewmodel.NotesViewModel

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Fragment affichant la liste des notes dans une RecyclerView.
 *
 * Il partage le ViewModel de l'activité hôte (activityViewModels) et observe :
 * - la liste des notes, transmise à l'adapter à chaque modification de la base ;
 * - l'ordre de tri, transmis à l'adapter qui se charge de réordonner la liste.
 */
class NotesListFragment : Fragment(R.layout.fragment_notes_list) {

    /** ViewModel partagé avec l'activité et l'autre fragment */
    private val viewModel: NotesViewModel by activityViewModels { NotesViewModel.Factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNotesListBinding.bind(view)
        val recyclerView = binding.notesRecyclerView

        val adapter = NotesAdapter()
        recyclerView.adapter = adapter

        // Lorsqu'une note est insérée en tête de liste (p. ex. tri par date de création) alors que
        // l'utilisateur est déjà en haut, la RecyclerView garde sinon l'ancien premier élément
        // comme ancre et la nouvelle note apparaîtrait hors de l'écran, au-dessus.
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0 && layoutManager.findFirstCompletelyVisibleItemPosition() <= 0) {
                    recyclerView.scrollToPosition(0)
                }
            }
        })

        // viewLifecycleOwner : les observateurs sont retirés à la destruction de la vue du fragment
        viewModel.allNotes.observe(viewLifecycleOwner) { notes ->
            adapter.setNotes(notes)
            binding.notesEmptyText.isVisible = notes.isEmpty()
        }

        viewModel.sortOrder.observe(viewLifecycleOwner) { sortOrder ->
            // après un changement de tri, on revient en haut de la liste
            adapter.setSortOrder(sortOrder) { recyclerView.scrollToPosition(0) }
        }
    }
}
