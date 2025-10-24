package com.example.eventsapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// 1. (FIXED) This import must match your package name
import com.example.eventsapp.R
// 2. (FIXED) These are the correct imports for Firebase Database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

class QuestionsAdapter(
    private val currentEventId: String
) : RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {

    private var questionsList = listOf<Question>()

    // --- ViewHolder Class ---
    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)
        val voteCountText: TextView = itemView.findViewById(R.id.voteCountText)
        val upvoteButton: ImageButton = itemView.findViewById(R.id.upvoteButton)
    }

    // --- Adapter Overrides ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun getItemCount(): Int = questionsList.size

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questionsList[position]

        holder.questionText.text = question.text
        holder.voteCountText.text = "🔥 ${question.voteCount}"

        holder.upvoteButton.setOnClickListener {
            onUpvoteClicked(question.id)
        }
    }

    // --- Custom Functions ---
    fun updateQuestions(newQuestions: List<Question>) {
        val oldQuestionMap = questionsList.associateBy { it.id }

        questionsList = newQuestions.map { newQuestion ->
            oldQuestionMap[newQuestion.id]?.let { oldQuestion ->
                newQuestion.voteCount = oldQuestion.voteCount
            }
            newQuestion
        }
        sortAndNotify()
    }

    fun updateVotes(voteCounts: Map<String, Int>) {
        var didUpdate = false
        questionsList.forEach { question ->
            val newCount = voteCounts[question.id]
            if (newCount != null && question.voteCount != newCount) {
                question.voteCount = newCount
                didUpdate = true
            }
        }

        if (didUpdate) {
            sortAndNotify()
        }
    }

    private fun sortAndNotify() {
        questionsList = questionsList.sortedByDescending { it.voteCount }
        notifyDataSetChanged()
    }

    private fun onUpvoteClicked(questionId: String) {
        val rtdb = FirebaseService.rtdb
        val voteRef = rtdb.reference
            .child("question_votes")
            .child(currentEventId)
            .child(questionId)

        // 3. (FIXED) This is the correct Transaction.Handler from Firebase
        voteRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var count = currentData.getValue(Int::class.java)
                if (count == null) {
                    count = 0
                }
                currentData.value = count + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("QuestionsAdapter", "Vote transaction failed: ${error.message}")
                }
            }
        })

        Log.d("QuestionsAdapter", "Upvote clicked for $questionId")
    }
}