package cu.edu.inca.abonosverdes.di

import android.content.Context
import androidx.room.Room
import cu.edu.inca.abonosverdes.data.local.AppDatabase
import cu.edu.inca.abonosverdes.data.local.daos.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .enableMultiInstanceInvalidation()
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    @Provides
    fun provideAbonoOrganicoDao(db: AppDatabase): AbonoOrganicoDao = db.abonoOrganicoDao()

    @Provides
    fun provideCultivosDao(db: AppDatabase): CultivosDao = db.cultivosDao()

    @Provides
    fun provideFertAbOrgDao(db: AppDatabase): FertAbOrgDao = db.fertAbOrgDao()

    @Provides
    fun provideSuelosDao(db: AppDatabase): SuelosDao = db.suelosDao()

    @Provides
    fun provideDatabaseVersionDao(db: AppDatabase): DatabaseVersionDao = db.databaseVersionDao()
}
