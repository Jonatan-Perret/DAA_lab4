package ch.heigvd.iict.daa.labo4.database

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.livedata.LiveDataDaoReturnTypeConverter
import androidx.sqlite.SQLiteConnection
import ch.heigvd.iict.daa.labo4.models.Note
import ch.heigvd.iict.daa.labo4.models.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Base de données Room de l'application, contenant les tables Note et Schedule.
 *
 * - @ColumnTypeConverters  : enregistre le convertisseur pour les dates (Calendar <-> Long)
 * - @DaoReturnTypeConverters : dans Room 3, le support des LiveData comme type de retour des
 *   DAO n'est plus implicite, il doit être activé explicitement (librairie room3-livedata)
 *
 * Une seule instance de la base est créée pour toute l'application (singleton).
 */
@Database(entities = [Note::class, Schedule::class], version = 1, exportSchema = false)
@ColumnTypeConverters(CalendarConverter::class)
@DaoReturnTypeConverters(LiveDataDaoReturnTypeConverter::class)
abstract class NotesDatabase : RoomDatabase() {

    /** @return le DAO permettant d'accéder aux notes */
    abstract fun notesDao(): NotesDao

    companion object {
        private const val DATABASE_NAME = "notes.db"

        /** Nombre de notes aléatoires générées à la création de la base */
        private const val POPULATE_NOTES_COUNT = 10

        @Volatile
        private var INSTANCE: NotesDatabase? = null

        /**
         * Retourne l'instance unique de la base de données, en la créant au besoin.
         * @param context contexte (l'Application est utilisée pour éviter toute fuite mémoire)
         * @param scope scope de coroutines (lié à l'application) utilisé pour peupler la base
         * @return l'instance unique de [NotesDatabase]
         */
        fun getDatabase(context: Context, scope: CoroutineScope): NotesDatabase {
            // double-checked locking : évite de créer deux instances depuis deux threads
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(PopulateCallback(scope))
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Callback peuplant la base avec des données de test lors de sa toute première création.
         * @param scope scope de coroutines dans lequel l'insertion est lancée
         */
        private class PopulateCallback(private val scope: CoroutineScope) : Callback() {
            override suspend fun onCreate(connection: SQLiteConnection) {
                super.onCreate(connection)
                // onCreate est appelé pendant l'ouverture de la base : on ne peut pas utiliser le
                // DAO directement ici (la base n'est pas encore prête), on lance donc l'insertion
                // dans une coroutine séparée qui s'exécutera une fois l'ouverture terminée.
                scope.launch {
                    INSTANCE?.let { populate(it.notesDao()) }
                }
            }
        }

        /**
         * Insère [POPULATE_NOTES_COUNT] notes aléatoires (avec ou sans échéance).
         * Les dates de création sont décalées aléatoirement dans le passé afin que le tri
         * par date de création soit visible avec les données de test.
         * @param notesDao le DAO à utiliser pour l'insertion
         */
        private suspend fun populate(notesDao: NotesDao) {
            repeat(POPULATE_NOTES_COUNT) {
                val note = Note.generateRandomNote().apply {
                    creationDate.add(Calendar.MINUTE, -Random.nextInt(0, 7 * 24 * 60))
                }
                notesDao.insertNoteWithSchedule(note, Note.generateRandomSchedule())
            }
        }
    }
}
