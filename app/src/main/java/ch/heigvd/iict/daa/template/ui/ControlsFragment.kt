package ch.heigvd.iict.daa.template.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.heigvd.iict.daa.template.R
import ch.heigvd.iict.daa.template.databinding.FragmentControlsBinding
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModel
import java.text.NumberFormat

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Fragment de contrôle, affiché uniquement sur tablette en paysage : il présente le nombre de
 * notes et deux boutons (génération d'une note aléatoire, suppression de toutes les notes) qui
 * remplacent les entrées correspondantes du menu de l'activité.
 */
class ControlsFragment : Fragment(R.layout.fragment_controls) {

    /** ViewModel partagé avec l'activité et l'autre fragment */
    private val viewModel: NotesViewModel by activityViewModels { NotesViewModel.Factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentControlsBinding.bind(view)

        // compteur mis à jour automatiquement à chaque modification de la base
        viewModel.countNotes.observe(viewLifecycleOwner) { count ->
            binding.notesCountText.text = NumberFormat.getIntegerInstance().format(count)
        }

        binding.generateButton.setOnClickListener { viewModel.generateANote() }
        binding.deleteAllButton.setOnClickListener { viewModel.deleteAllNote() }
    }
}
