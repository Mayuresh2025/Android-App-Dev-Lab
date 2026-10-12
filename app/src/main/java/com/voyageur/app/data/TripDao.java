package com.voyageur.app.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TripDao {

    @Insert
    long insertTrip(Trip trip);

    @Query("SELECT * FROM trips ORDER BY id DESC")
    List<Trip> getAllTrips();

    @Update
    void updateTrip(Trip trip);

    @Delete
    void deleteTrip(Trip trip);

    @Query("DELETE FROM trips")
    void deleteAllTrips();
}