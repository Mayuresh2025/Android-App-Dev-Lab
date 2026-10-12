package com.voyageur.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Trip.class},
        version = 1,
        exportSchema = false
)
public abstract class VoyageurDatabase extends RoomDatabase {

    public abstract TripDao tripDao();

    private static volatile VoyageurDatabase INSTANCE;

    public static VoyageurDatabase getInstance(Context context) {

        if (INSTANCE == null) {
            synchronized (VoyageurDatabase.class) {

                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    VoyageurDatabase.class,
                                    "voyageur_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}