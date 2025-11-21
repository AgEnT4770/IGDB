package com.example.igdb.auth

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.igdb.ui.activities.LoginActivity
import com.example.igdb.ui.activities.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class Login(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth


    fun signIn(email: String, password: String, onComplete: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser?.isEmailVerified == true) {
                        Toast.makeText(context, "Login Successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        if (context is LoginActivity) context.finish()
                        onComplete(true)
                    } else {
                        Toast.makeText(context, "Verify your Email!", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                } else {
                    Toast.makeText(context, task.exception?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
    }

    fun resetPassword(email: String, onComplete: (Boolean) -> Unit) {
        if (email.isBlank()) {
            Toast.makeText(context, "Missing Email", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                } else {
                    Toast.makeText(context, task.exception?.message ?: "Failed to send email", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
    }

    fun checkCurrentUser(activity: ComponentActivity) {
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }
}

