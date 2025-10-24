package com.example.eventsapp

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

class EventActivity : AppCompatActivity() {

    private val firestore = FirebaseService.firestore
    private val rtdb = FirebaseService.rtdb
    private val currentEventId = "hKXimyqywC92xbictYWs"

    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var questionEditText: EditText
    private lateinit var submitQuestionButton: ImageButton


    private lateinit var questionsAdapter: QuestionsAdapter
    private var votesListener: ValueEventListener? = null

    // Use 'by lazy' so it's initialized after 'currentEventId'
    private val votesRef by lazy {
        rtdb.reference.child("question_votes").child(currentEventId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        questionsRecyclerView = findViewById(R.id.questionsRecyclerView)
        questionEditText = findViewById(R.id.questionEditText)
        submitQuestionButton = findViewById(R.id.submitQuestionButton)

        questionsAdapter = QuestionsAdapter(currentEventId)
        questionsRecyclerView.adapter = questionsAdapter
        questionsRecyclerView.layoutManager = LinearLayoutManager(this)

        submitQuestionButton.setOnClickListener {
            val questionText = questionEditText.text.toString().trim()
            if (questionText.isNotEmpty()) {
                submitQuestion(questionText)
                questionEditText.text.clear()
            }
        }

        listenForQuestions()
        listenForVotes()
    }

    private fun submitQuestion(questionText: String) {
        val userId = FirebaseService.auth.currentUser?.uid ?: return
        Log.d("EventActivity", "Submitting question: $questionText")

        val question = hashMapOf(
            "text" to questionText,
            "authorId" to userId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("events").document(currentEventId)
            .collection("questions")
            .add(question)
            .addOnSuccessListener { documentReference ->
                Log.d("EventActivity", "Question added with ID: ${documentReference.id}")
                initializeVoteCount(documentReference.id)
            }
            .addOnFailureListener { e: Exception -> // This is the correct syntax
                Log.w("EventActivity", "Error adding question", e)
                Toast.makeText(this, "Failed to submit question", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initializeVoteCount(questionId: String) {
        rtdb.reference.child("question_votes")
            .child(currentEventId)
            .child(questionId)
            .setValue(0)
    }

    private fun listenForQuestions() {
        firestore.collection("events").document(currentEventId)
            .collection("questions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("EventActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val newQuestions = snapshots!!.documents.map { doc ->
                    Question(
                        id = doc.id,
                        text = doc.getString("text") ?: ""
                    )
                }

                questionsAdapter.updateQuestions(newQuestions)
            }
    }

    private fun listenForVotes() {
        votesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val voteCounts = mutableMapOf<String, Int>()

                for (child in snapshot.children) {
                    val questionId = child.key
                    val count = child.getValue(Int::class.java)
                    if (questionId != null && count != null) {
                        voteCounts[questionId] = count
                    }
                }
                questionsAdapter.updateVotes(voteCounts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("EventActivity", "Failed to read vote counts.", error.toException())
            }
        }
        votesRef.addValueEventListener(votesListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        votesListener?.let {
            votesRef.removeEventListener(it)
        }
    }
}