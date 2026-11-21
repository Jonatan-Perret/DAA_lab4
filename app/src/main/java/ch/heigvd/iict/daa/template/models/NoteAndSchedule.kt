package ch.heigvd.iict.daa.template.models

import androidx.room3.Embedded
import androidx.room3.Relation

data class NoteAndSchedule(
    @Embedded val note: Note,
    @Relation(
        parentColumns = ["noteId"],
        entityColumns = ["ownerId"]
    )
    val schedule: Schedule?
)
