package ch.heigvd.iict.daa.template.models

/**
 * Auteurs : François de Courville, Jonatan Perret
 *
 * Ordres de tri possibles pour la liste des notes.
 *
 * - [NONE]             : ordre naturel de la base de données (ordre d'insertion)
 * - [BY_CREATION_DATE] : de la note la plus récente à la plus ancienne
 * - [BY_SCHEDULE_DATE] : de l'échéance la plus proche à la plus lointaine,
 *                        les notes sans échéance sont placées à la fin
 */
enum class SortOrder {
    NONE, BY_CREATION_DATE, BY_SCHEDULE_DATE
}
