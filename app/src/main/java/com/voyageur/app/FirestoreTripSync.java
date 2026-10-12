package com.voyageur.app;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.voyageur.app.data.AppDatabase;
import com.voyageur.app.data.Trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirestoreTripSync {

    private static final String TAG = "FirestoreTripSync";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final AppDatabase database;
    private final ExecutorService executor;

    public FirestoreTripSync(Context context) {

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        database = AppDatabase.getInstance(
                context.getApplicationContext()
        );

        executor = Executors.newSingleThreadExecutor();
    }

    // ============================================================
    // UPLOAD ONE TRIP TO FIRESTORE
    // ============================================================

    public void uploadTrip(Trip trip) {

        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "No logged-in user. Trip not uploaded.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> tripData = new HashMap<>();

        tripData.put("id", trip.getId());
        tripData.put("destination", trip.getDestination());
        tripData.put("startDate", trip.getStartDate());
        tripData.put("endDate", trip.getEndDate());
        tripData.put("budget", trip.getBudget());
        tripData.put("notes", trip.getNotes());

        firestore
                .collection("users")
                .document(uid)
                .collection("trips")
                .document(String.valueOf(trip.getId()))
                .set(tripData)
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Trip uploaded successfully")
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Trip upload failed", e)
                );
    }

    // ============================================================
    // DELETE ONE TRIP FROM FIRESTORE
    // ============================================================

    public void deleteTrip(Trip trip) {

        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "No logged-in user. Trip not deleted from Firestore.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        firestore
                .collection("users")
                .document(uid)
                .collection("trips")
                .document(String.valueOf(trip.getId()))
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Trip deleted from Firestore")
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Firestore delete failed", e)
                );
    }

    // ============================================================
    // DOWNLOAD FIRESTORE TRIPS INTO ROOM
    // ============================================================

    public void syncFromFirestore(Runnable onComplete) {

        if (auth.getCurrentUser() == null) {

            if (onComplete != null) {
                onComplete.run();
            }

            return;
        }

        String uid = auth.getCurrentUser().getUid();

        firestore
                .collection("users")
                .document(uid)
                .collection("trips")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    executor.execute(() -> {

                        for (QueryDocumentSnapshot document :
                                querySnapshot) {

                            try {

                                String destination =
                                        document.getString("destination");

                                String startDate =
                                        document.getString("startDate");

                                String endDate =
                                        document.getString("endDate");

                                Double budgetValue =
                                        document.getDouble("budget");

                                String notes =
                                        document.getString("notes");

                                Long idValue =
                                        document.getLong("id");

                                double budget =
                                        budgetValue != null
                                                ? budgetValue
                                                : 0;

                                Trip trip = new Trip(
                                        destination,
                                        startDate,
                                        endDate,
                                        budget,
                                        notes
                                );

                                if (idValue != null) {
                                    trip.setId(idValue.intValue());
                                }

                                database.tripDao()
                                        .insertTrip(trip);

                            } catch (Exception e) {

                                Log.e(
                                        TAG,
                                        "Error importing trip",
                                        e
                                );
                            }
                        }

                        if (onComplete != null) {
                            runOnUiThreadSafe(onComplete);
                        }
                    });
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Firestore sync failed",
                            e
                    );

                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
    }

    // ============================================================
    // UPLOAD ALL ROOM TRIPS TO FIRESTORE
    // ============================================================

    public void uploadAllTrips() {

        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "No logged-in user.");
            return;
        }

        executor.execute(() -> {

            List<Trip> trips =
                    database.tripDao().getAllTrips();

            for (Trip trip : trips) {
                uploadTrip(trip);
            }
        });
    }

    // ============================================================
    // SAFE CALLBACK
    // ============================================================

    private void runOnUiThreadSafe(Runnable runnable) {

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(runnable);
    }

    // ============================================================
    // SHUTDOWN
    // ============================================================

    public void shutdown() {
        executor.shutdown();
    }
}