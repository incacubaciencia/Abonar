package cu.edu.inca.abonosverdes.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "database_version")
data class DatabaseVersion(
    @PrimaryKey val id: Int = 1,
    val currentVersion: Int
)
