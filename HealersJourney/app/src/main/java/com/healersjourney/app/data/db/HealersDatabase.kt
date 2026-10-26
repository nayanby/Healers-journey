package com.healersjourney.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProfileEntity::class,
        ActionEntity::class,
        LogEntity::class,
        StudyHoursEntity::class,
        DateFlagEntity::class,
        WeeklyXpEntity::class,
        ActivityEntity::class,
        CustomAchievementEntity::class,
        MilestoneEntity::class,
        CareEntity::class,
        TodoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HealersDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        const val NAME = "healers.db"

        fun create(context: Context): HealersDatabase =
            Room.databaseBuilder(context.applicationContext, HealersDatabase::class.java, NAME)
                .addMigrations(object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE profile ADD COLUMN gameExtrasJson TEXT NOT NULL DEFAULT '{}'")
                    }
                })
                .build()
    }
}
