package ch.heigvd.iict.daa.labo4

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ch.heigvd.iict.daa.labo4.models.SortOrder
import ch.heigvd.iict.daa.labo4.viewmodel.NotesViewModel

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Activité principale. Elle héberge le(s) fragment(s) défini(s) dans son layout :
 * - smartphone (et tablette en portrait) : la liste des notes uniquement ;
 * - tablette en paysage (layout-sw600dp-land) : le fragment de contrôle et la liste.
 *
 * Elle gère le menu : tri des notes et, sur smartphone uniquement (menu-sw600dp-land ne les
 * contient pas), génération d'une note aléatoire et suppression de toutes les notes.
 */
class MainActivity : AppCompatActivity() {

    /** ViewModel partagé avec les fragments (même instance, liée à l'activité) */
    private val viewModel: NotesViewModel by viewModels { NotesViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // depuis android 15 (sdk 35), le mode edge2edge doit être activé
        enableEdgeToEdge()

        // on spécifie le layout à afficher
        setContentView(R.layout.activity_main)

        // comme edge2edge est activé, l'application doit garder un espace suffisant pour la barre système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // le menu affiche l'ordre de tri actif : on le redessine quand celui-ci change
        viewModel.sortOrder.observe(this) { invalidateOptionsMenu() }
    }

    /**
     * Crée le menu de l'activité (la ressource choisie dépend de la configuration de l'appareil).
     * @return true pour que le menu soit affiché
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Coche, dans le sous-menu de tri, l'option correspondant à l'ordre de tri courant.
     * @return true pour que le menu soit affiché
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        when (viewModel.sortOrder.value) {
            SortOrder.BY_CREATION_DATE -> menu.findItem(R.id.menu_sort_creation_date)?.isChecked = true
            SortOrder.BY_SCHEDULE_DATE -> menu.findItem(R.id.menu_sort_schedule_date)?.isChecked = true
            else -> Unit
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Traite la sélection d'une entrée du menu en déléguant l'action au ViewModel.
     * @param item l'entrée sélectionnée
     * @return true si la sélection a été traitée
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_creation_date -> viewModel.sortBy(SortOrder.BY_CREATION_DATE)
            R.id.menu_sort_schedule_date -> viewModel.sortBy(SortOrder.BY_SCHEDULE_DATE)
            R.id.menu_generate -> viewModel.generateANote()
            R.id.menu_delete_all -> viewModel.deleteAllNote()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
