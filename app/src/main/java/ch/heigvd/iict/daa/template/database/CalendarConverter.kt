package ch.heigvd.iict.daa.template.database

import androidx.room3.ColumnTypeConverter
import java.util.Calendar

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Convertisseurs de types utilisés par Room pour stocker les dates.
 *
 * SQLite ne connaît pas le type [Calendar] : on le stocke donc sous forme d'un timestamp
 * (nombre de millisecondes depuis l'epoch, type INTEGER en base).
 * Les énumérations (State, Type) sont, elles, gérées nativement par Room (stockées par leur nom).
 *
 * Remarque : dans Room 3, les annotations TypeConverter(s) de Room 2 ont été renommées
 * ColumnTypeConverter(s).
 */
class CalendarConverter {

    /**
     * Convertit un timestamp lu en base en [Calendar].
     * @param timestamp nombre de millisecondes depuis l'epoch
     * @return le [Calendar] correspondant
     */
    @ColumnTypeConverter
    fun toCalendar(timestamp: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = timestamp }

    /**
     * Convertit un [Calendar] en timestamp afin de le stocker en base.
     * @param calendar la date à stocker
     * @return le nombre de millisecondes depuis l'epoch
     */
    @ColumnTypeConverter
    fun fromCalendar(calendar: Calendar): Long = calendar.timeInMillis
}
