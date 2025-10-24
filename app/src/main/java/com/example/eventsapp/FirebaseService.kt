package com.example.eventsapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object FirebaseService {

    // --- AUTH ---
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // --- FIRESTORE ---

    val firestore: FirebaseFirestore by lazy {
        val instance = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        instance.firestoreSettings = settings
        instance
    }

    // --- REALTIME DATABASE ---
    val rtdb: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance("https://eventapp-a58af-default-rtdb.firebaseio.com/")
    }
}
