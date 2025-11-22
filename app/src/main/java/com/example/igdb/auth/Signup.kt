package com.example.igdb.auth

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.igdb.ui.activities.LoginActivity
import com.example.igdb.ui.activities.SignupActivity
import com.google.firebase.Firebase
import com.example.igdb.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class Signup(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore


    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        onComplete: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addUserName(firstName, lastName)
                    onComplete(true)
                } else {
                    Toast.makeText(context, task.exception?.message ?: "Signup failed", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
    }

    private fun addUserName(firstName: String, lastName: String) {
        val userId = auth.currentUser?.uid ?: return
        val user = User(userId = userId, username = "$firstName $lastName")

        db.collection("Users").document(userId)
            .set(user)
            .addOnSuccessListener {
                verifyEmail()
            }
            .addOnFailureListener {
                Toast.makeText(context, it.message ?: "Error adding user name", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verifyEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Check your inbox to verify your email", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, LoginActivity::class.java))
                if (context is SignupActivity) context.finish()
            }
        }
    }
}