package ch.heigvd.iict.daa.template.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import ch.heigvd.iict.daa.template.models.SortOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Repository persistant le choix de tri de la liste des notes (question 6.1).
 *
 * Le choix est stocké avec Jetpack DataStore (Preferences) : il survit donc à la fermeture
 * de l'application, contrairement à un état conservé dans le ViewModel ou dans le Bundle
 * de sauvegarde d'instance. DataStore est asynchrone (coroutines / Flow) et n'effectue
 * aucune entrée-sortie sur le thread UI.
 *
 * @param dataStore le DataStore de préférences de l'application
 * @param applicationScope scope de coroutines lié à la durée de vie de l'application
 */
class SortPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val applicationScope: CoroutineScope
) {

    /**
     * Ordre de tri enregistré, [SortOrder.NONE] si aucun choix n'a encore été fait
     * (ou si la valeur stockée est invalide).
     */
    val sortOrder: Flow<SortOrder> = dataStore.data
        .catch { exception ->
            // en cas d'erreur de lecture du fichier, on repart des valeurs par défaut
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val storedName = preferences[SORT_ORDER_KEY]
            SortOrder.entries.firstOrNull { it.name == storedName } ?: SortOrder.NONE
        }
        .distinctUntilChanged()

    /**
     * Enregistre de manière persistante l'ordre de tri choisi.
     * @param sortOrder l'ordre de tri à enregistrer
     */
    fun setSortOrder(sortOrder: SortOrder) {
        applicationScope.launch {
            dataStore.edit { preferences -> preferences[SORT_ORDER_KEY] = sortOrder.name }
        }
    }

    companion object {
        /** Clé sous laquelle l'ordre de tri est stocké (nom de la valeur de l'énumération) */
        private val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
    }
}
