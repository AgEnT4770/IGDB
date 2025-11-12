package com.example.igdb

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class Authentication(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth

    companion object {
        private const val TAG = "Authentication"
    }

    /**
     * Handles network and Firebase errors gracefully
     */
    private fun handleError(exception: Exception?, defaultMessage: String): String {
        return when (exception) {
            is UnknownHostException -> {
                Log.e(TAG, "No internet connection", exception)
                "No internet connection. Please check your network and try again."
            }
            is SocketTimeoutException -> {
                Log.e(TAG, "Connection timeout", exception)
                "Connection timeout. Please check your internet and try again."
            }
            is java.net.ConnectException -> {
                Log.e(TAG, "Connection failed", exception)
                "Unable to connect. Please check your internet connection."
            }
            else -> {
                Log.e(TAG, defaultMessage, exception)
                exception?.message ?: defaultMessage
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        onComplete: (Boolean) -> Unit
    ) {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        addUserName(firstName, lastName)
                        onComplete(true)
                    } else {
                        val errorMessage = handleError(task.exception, "Signup failed")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Signup failed")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Signup failed")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    private fun addUserName(firstName: String, lastName: String) {
        try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.e(TAG, "User ID is null")
                return
            }
            
            val user = User(userId = userId, username = "$firstName $lastName")

            Firebase
                .firestore
                .collection("Users")
                .document(userId)
                .set(user)
                .addOnSuccessListener {
                    verifyEmail()
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Failed to save user data")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to save user data")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyEmail() {
        try {
            val user = auth.currentUser
            user?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Check your inbox to verify your email", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    if (context is SignupActivity) context.finish()
                } else {
                    val errorMessage = handleError(task.exception, "Failed to send verification email")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }?.addOnFailureListener { exception ->
                val errorMessage = handleError(exception, "Failed to send verification email")
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to send verification email")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun signIn(email: String, password: String, onComplete: (Boolean) -> Unit) {
        try {
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
                        val errorMessage = handleError(task.exception, "Login failed")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Login failed")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Login failed")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    fun resetPassword(email: String, onComplete: (Boolean) -> Unit) {
        if (email.isBlank()) {
            Toast.makeText(context, "Missing Email", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                        onComplete(true)
                    } else {
                        val errorMessage = handleError(task.exception, "Failed to send email")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Failed to send email")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to send email")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    fun checkCurrentUser(activity: ComponentActivity) {
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }

    fun getCurrentUser(): com.google.firebase.auth.FirebaseUser? {
        return auth.currentUser
    }

    fun getUserInfo(onComplete: (User?) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                onComplete(null)
                return
            }

            val userId = currentUser.uid
            Firebase.firestore
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        onComplete(user)
                    } else {
                        // Create user document if it doesn't exist
                        val displayName = currentUser.displayName ?: "User"
                        val user = User(
                            userId = userId,
                            username = displayName,
                            profilePictureUrl = currentUser.photoUrl?.toString() ?: ""
                        )
                        Firebase.firestore
                            .collection("Users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener { onComplete(user) }
                            .addOnFailureListener { exception ->
                                val errorMessage = handleError(exception, "Failed to load user info")
                                Log.e(TAG, errorMessage, exception)
                                // Return null but don't show toast as this might be called frequently
                                onComplete(null)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Failed to load user info")
                    Log.e(TAG, errorMessage, exception)
                    // Return null but don't show toast as this might be called frequently
                    onComplete(null)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to load user info")
            Log.e(TAG, errorMessage, e)
            onComplete(null)
        }
    }

    fun updateDisplayName(newName: String, onComplete: (Boolean) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
                onComplete(false)
                return
            }

            val userProfile = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser.updateProfile(userProfile)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update Firestore
                        val userId = currentUser.uid
                        Firebase.firestore
                            .collection("Users")
                            .document(userId)
                            .update("username", newName)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Display name updated successfully", Toast.LENGTH_SHORT).show()
                                onComplete(true)
                            }
                            .addOnFailureListener { exception ->
                                val errorMessage = handleError(exception, "Failed to update display name in database")
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                onComplete(false)
                            }
                    } else {
                        val errorMessage = handleError(task.exception, "Failed to update display name")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Failed to update display name")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to update display name")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    fun updateEmail(newEmail: String, password: String, onComplete: (Boolean) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
                onComplete(false)
                return
            }

            // Re-authenticate user before changing email
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                currentUser.email ?: "",
                password
            )

            currentUser.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        @Suppress("DEPRECATION")
                        val updateEmailTask = currentUser.updateEmail(newEmail)
                        updateEmailTask
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Send verification email
                                    currentUser.sendEmailVerification()
                                        .addOnCompleteListener {
                                            Toast.makeText(
                                                context,
                                                "Email updated. Please verify your new email address.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onComplete(true)
                                        }
                                        .addOnFailureListener { exception ->
                                            val errorMessage = handleError(exception, "Failed to send verification email")
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                            onComplete(false)
                                        }
                                } else {
                                    val errorMessage = handleError(updateTask.exception, "Failed to update email")
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    onComplete(false)
                                }
                            }
                            .addOnFailureListener { exception ->
                                val errorMessage = handleError(exception, "Failed to update email")
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                onComplete(false)
                            }
                    } else {
                        val errorMessage = handleError(reauthTask.exception, "Authentication failed. Please check your password.")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Authentication failed. Please check your password.")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to update email")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String, onComplete: (Boolean) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
                onComplete(false)
                return
            }

            // Re-authenticate user before changing password
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                currentUser.email ?: "",
                currentPassword
            )

            currentUser.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        currentUser.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    onComplete(true)
                                } else {
                                    val errorMessage = handleError(updateTask.exception, "Failed to update password")
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    onComplete(false)
                                }
                            }
                            .addOnFailureListener { exception ->
                                val errorMessage = handleError(exception, "Failed to update password")
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                onComplete(false)
                            }
                    } else {
                        val errorMessage = handleError(reauthTask.exception, "Authentication failed. Please check your current password.")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Authentication failed. Please check your current password.")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to update password")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    fun updateProfilePicture(imageUrl: String, onComplete: (Boolean) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
                onComplete(false)
                return
            }

            val userProfile = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setPhotoUri(android.net.Uri.parse(imageUrl))
                .build()

            currentUser.updateProfile(userProfile)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update Firestore
                        val userId = currentUser.uid
                        Firebase.firestore
                            .collection("Users")
                            .document(userId)
                            .update("profilePictureUrl", imageUrl)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                                onComplete(true)
                            }
                            .addOnFailureListener { exception ->
                                val errorMessage = handleError(exception, "Failed to update profile picture in database")
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                onComplete(false)
                            }
                    } else {
                        val errorMessage = handleError(task.exception, "Failed to update profile picture")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Failed to update profile picture")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to update profile picture")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    fun logout() {
        auth.signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}
