package ch.heigvd.iict.daa.labo4

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import ch.heigvd.iict.daa.labo4.database.NotesDatabase
import ch.heigvd.iict.daa.labo4.repository.NotesRepository
import ch.heigvd.iict.daa.labo4.repository.SortPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Instance unique du DataStore de préférences de l'application (fichier "settings").
 * La délégation preferencesDataStore garantit qu'une seule instance existe par processus.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Application surchargée afin de créer et de conserver les instances uniques des repositories
 * (et donc de la base de données) pour toute la durée de vie de l'application.
 * Les instances sont créées paresseusement (by lazy), à leur premier accès.
 */
class NotesApp : Application() {

    /**
     * Scope de coroutines lié à la durée de vie de l'application, utilisé pour les écritures.
     * Le SupervisorJob évite qu'un échec d'une coroutine n'annule toutes les autres.
     */
    private val applicationScope = CoroutineScope(SupervisorJob())

    /** Repository des notes (base de données Room) */
    val notesRepository by lazy {
        NotesRepository(
            NotesDatabase.getDatabase(this, applicationScope).notesDao(),
            applicationScope
        )
    }

    /** Repository du choix de tri persistant (DataStore) */
    val sortPreferencesRepository by lazy {
        SortPreferencesRepository(settingsDataStore, applicationScope)
    }
}
