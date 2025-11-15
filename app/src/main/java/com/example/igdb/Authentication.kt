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
            is com.google.firebase.firestore.FirebaseFirestoreException -> {
                if (exception.message?.contains("offline") == true || exception.message?.contains("UNAVAILABLE") == true) {
                    Log.e(TAG, "Firestore offline", exception)
                    "You're offline. Please check your internet connection."
                } else {
                    Log.e(TAG, "Firestore error: $defaultMessage", exception)
                    exception.message ?: defaultMessage
                }
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

            val userId = currentUser.uid
            val oldName = currentUser.displayName ?: ""

            val userProfile = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser.updateProfile(userProfile)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Firebase.firestore
                            .collection("Users")
                            .document(userId)
                            .update("username", newName)
                            .addOnSuccessListener {
                                updateReviewsWithNewName(userId, newName) {
                                    Toast.makeText(context, "Display name updated successfully", Toast.LENGTH_SHORT).show()
                                    onComplete(true)
                                }
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

    private fun updateReviewsWithNewName(userId: String, newName: String, onComplete: () -> Unit) {
        try {
            Firebase.firestore
                .collectionGroup("game_reviews")
                .whereEqualTo("reviewerId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Log.d(TAG, "No reviews found for user $userId")
                        onComplete()
                        return@addOnSuccessListener
                    }

                    val reviewDocs = querySnapshot.documents.map { it.reference }
                    Log.d(TAG, "Found ${reviewDocs.size} reviews to update for user $userId")

                    if (reviewDocs.isEmpty()) {
                        onComplete()
                        return@addOnSuccessListener
                    }

                    updateReviewsInBatches(reviewDocs, newName, "reviewerName") { success ->
                        if (success) {
                            Log.d(TAG, "Successfully updated ${reviewDocs.size} reviews with new name")
                        } else {
                            Log.e(TAG, "Failed to update some reviews")
                        }
                        onComplete()
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = exception.message ?: "Unknown error"
                    if (errorMessage.contains("index") || errorMessage.contains("Index") || errorMessage.contains("COLLECTION_GROUP")) {
                        Log.w(TAG, "Collection group index not found, falling back to iterative method", exception)
                        updateReviewsWithNewNameFallback(userId, newName, onComplete)
                    } else {
                        Log.e(TAG, "Failed to fetch reviews for user $userId", exception)
                        updateReviewsWithNewNameFallback(userId, newName, onComplete)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating reviews, trying fallback", e)
            updateReviewsWithNewNameFallback(userId, newName, onComplete)
        }
    }

    private fun updateReviewsWithNewNameFallback(userId: String, newName: String, onComplete: () -> Unit) {
        try {
            Log.d(TAG, "Starting fallback method to update reviews for user: $userId with name: $newName")
            Firebase.firestore
                .collection("Reviews")
                .get()
                .addOnSuccessListener { reviewsSnapshot ->
                    Log.d(TAG, "Found ${reviewsSnapshot.size()} game documents in Reviews collection")
                    if (reviewsSnapshot.isEmpty) {
                        Log.d(TAG, "No games with reviews found (fallback) - user may not have written any reviews yet")
                        onComplete()
                        return@addOnSuccessListener
                    }

                    val gameDocs = reviewsSnapshot.documents
                    if (gameDocs.isEmpty()) {
                        Log.d(TAG, "Game documents list is empty")
                        onComplete()
                        return@addOnSuccessListener
                    }

                    val allReviewDocs = mutableListOf<com.google.firebase.firestore.DocumentReference>()
                    val lock = Any()
                    var completedQueries = 0
                    val totalGames = gameDocs.size
                    var hasError = false

                    Log.d(TAG, "Querying ${totalGames} games for user reviews...")

                    gameDocs.forEach { gameDoc ->
                        val gameId = gameDoc.id
                        Log.d(TAG, "Querying reviews for game: $gameId")
                        gameDoc.reference
                            .collection("game_reviews")
                            .whereEqualTo("reviewerId", userId)
                            .get()
                            .addOnSuccessListener { userReviews ->
                                synchronized(lock) {
                                    Log.d(TAG, "Found ${userReviews.size()} reviews for game $gameId")
                                    userReviews.documents.forEach { reviewDoc ->
                                        allReviewDocs.add(reviewDoc.reference)
                                        Log.d(TAG, "Added review document: ${reviewDoc.id} for game $gameId")
                                    }

                                    completedQueries++
                                    Log.d(TAG, "Completed queries: $completedQueries/$totalGames, Total reviews collected: ${allReviewDocs.size}")

                                    if (completedQueries == totalGames) {
                                        if (allReviewDocs.isEmpty()) {
                                            Log.w(TAG, "No reviews found for user $userId after checking all games")
                                            onComplete()
                                            return@addOnSuccessListener
                                        }

                                        Log.d(TAG, "All queries completed. Found ${allReviewDocs.size} reviews to update. Starting batch updates...")
                                        updateReviewsInBatches(allReviewDocs, newName, "reviewerName") { success ->
                                            if (success) {
                                                Log.d(TAG, "✓ Successfully updated ${allReviewDocs.size} reviews with new name (fallback)")
                                            } else {
                                                Log.e(TAG, "✗ Failed to update some reviews (fallback)")
                                            }
                                            onComplete()
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                synchronized(lock) {
                                    Log.e(TAG, "Failed to fetch reviews for game ${gameDoc.id} (fallback)", exception)
                                    hasError = true
                                    completedQueries++
                                    Log.d(TAG, "Completed queries (with error): $completedQueries/$totalGames")

                                    if (completedQueries == totalGames) {
                                        if (allReviewDocs.isNotEmpty()) {
                                            Log.d(TAG, "Some queries failed but found ${allReviewDocs.size} reviews. Updating...")
                                            updateReviewsInBatches(allReviewDocs, newName, "reviewerName") { success ->
                                                if (success) {
                                                    Log.d(TAG, "✓ Updated ${allReviewDocs.size} reviews despite some errors")
                                                }
                                                onComplete()
                                            }
                                        } else {
                                            Log.w(TAG, "No reviews found after all queries completed (some had errors)")
                                            onComplete()
                                        }
                                    }
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to fetch Reviews collection (fallback)", exception)
                    onComplete()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fallback method", e)
            onComplete()
        }
    }

    private fun updateReviewsInBatches(
        reviewDocs: List<com.google.firebase.firestore.DocumentReference>,
        newValue: String,
        fieldName: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (reviewDocs.isEmpty()) {
            Log.w(TAG, "No review documents to update")
            onComplete(false)
            return
        }

        val batchSize = 500
        var processed = 0
        var hasError = false
        val totalReviews = reviewDocs.size

        Log.d(TAG, "Starting to update $totalReviews reviews in batches of $batchSize")

        fun processBatch(startIndex: Int) {
            if (startIndex >= reviewDocs.size) {
                Log.d(TAG, "All batches processed. Total: $processed/$totalReviews, Errors: $hasError")
                onComplete(!hasError)
                return
            }

            val batch = Firebase.firestore.batch()
            val endIndex = minOf(startIndex + batchSize, reviewDocs.size)
            val batchNumber = (startIndex / batchSize) + 1
            val totalBatches = (reviewDocs.size + batchSize - 1) / batchSize

            Log.d(TAG, "Processing batch $batchNumber/$totalBatches (reviews ${startIndex + 1}-$endIndex of $totalReviews)")

            for (i in startIndex until endIndex) {
                try {
                    batch.update(reviewDocs[i], fieldName, newValue)
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding review ${reviewDocs[i].path} to batch", e)
                }
            }

            batch.commit()
                .addOnSuccessListener {
                    processed += (endIndex - startIndex)
                    Log.d(TAG, "✓ Batch $batchNumber/$totalBatches committed successfully. Progress: $processed/$totalReviews reviews")
                    processBatch(endIndex)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "✗ Failed to commit batch $batchNumber/$totalBatches starting at index $startIndex", exception)
                    Log.e(TAG, "Error details: ${exception.message}")
                    hasError = true
                    processed += (endIndex - startIndex)
                    Log.d(TAG, "Continuing with next batch despite error. Progress: $processed/$totalReviews")
                    processBatch(endIndex)
                }
        }

        processBatch(0)
    }

    private fun updateReviewsWithNewProfilePicture(userId: String, newProfilePictureUrl: String, onComplete: () -> Unit) {
        try {
            Firebase.firestore
                .collectionGroup("game_reviews")
                .whereEqualTo("reviewerId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Log.d(TAG, "No reviews found for user $userId")
                        onComplete()
                        return@addOnSuccessListener
                    }

                    val reviewDocs = querySnapshot.documents.map { it.reference }
                    Log.d(TAG, "Found ${reviewDocs.size} reviews to update with new profile picture for user $userId")

                    if (reviewDocs.isEmpty()) {
                        onComplete()
                        return@addOnSuccessListener
                    }

                    updateReviewsInBatches(reviewDocs, newProfilePictureUrl, "profilePictureUrl") { success ->
                        if (success) {
                            Log.d(TAG, "Successfully updated ${reviewDocs.size} reviews with new profile picture")
                        } else {
                            Log.e(TAG, "Failed to update some reviews")
                        }
                        onComplete()
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = exception.message ?: "Unknown error"
                    if (errorMessage.contains("index") || errorMessage.contains("Index") || errorMessage.contains("COLLECTION_GROUP")) {
                        Log.w(TAG, "Collection group index not found, falling back to iterative method", exception)
                        updateReviewsWithNewProfilePictureFallback(userId, newProfilePictureUrl, onComplete)
                    } else {
                        Log.e(TAG, "Failed to fetch reviews for user $userId", exception)
                        updateReviewsWithNewProfilePictureFallback(userId, newProfilePictureUrl, onComplete)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating reviews, trying fallback", e)
            updateReviewsWithNewProfilePictureFallback(userId, newProfilePictureUrl, onComplete)
        }
    }

    private fun updateReviewsWithNewProfilePictureFallback(userId: String, newProfilePictureUrl: String, onComplete: () -> Unit) {
        try {
            Firebase.firestore
                .collection("Reviews")
                .get()
                .addOnSuccessListener { reviewsSnapshot ->
                    if (reviewsSnapshot.isEmpty) {
                        Log.d(TAG, "No games with reviews found (fallback)")
                        onComplete()
                        return@addOnSuccessListener
                    }

                    val gameDocs = reviewsSnapshot.documents
                    if (gameDocs.isEmpty()) {
                        onComplete()
                        return@addOnSuccessListener
                    }

                    val allReviewDocs = mutableListOf<com.google.firebase.firestore.DocumentReference>()
                    var completedQueries = 0
                    val totalGames = gameDocs.size

                    gameDocs.forEach { gameDoc ->
                        gameDoc.reference
                            .collection("game_reviews")
                            .whereEqualTo("reviewerId", userId)
                            .get()
                            .addOnSuccessListener { userReviews ->
                                userReviews.documents.forEach { reviewDoc ->
                                    allReviewDocs.add(reviewDoc.reference)
                                }

                                completedQueries++
                                if (completedQueries == totalGames) {
                                    if (allReviewDocs.isEmpty()) {
                                        Log.d(TAG, "No reviews found for user $userId (fallback)")
                                        onComplete()
                                        return@addOnSuccessListener
                                    }

                                    Log.d(TAG, "Found ${allReviewDocs.size} reviews to update with new profile picture (fallback method)")
                                    updateReviewsInBatches(allReviewDocs, newProfilePictureUrl, "profilePictureUrl") { success ->
                                        if (success) {
                                            Log.d(TAG, "Successfully updated ${allReviewDocs.size} reviews with new profile picture (fallback)")
                                        } else {
                                            Log.e(TAG, "Failed to update some reviews (fallback)")
                                        }
                                        onComplete()
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Failed to fetch reviews for game ${gameDoc.id} (fallback)", exception)
                                completedQueries++
                                if (completedQueries == totalGames) {
                                    if (allReviewDocs.isNotEmpty()) {
                                        updateReviewsInBatches(allReviewDocs, newProfilePictureUrl, "profilePictureUrl") { success ->
                                            onComplete()
                                        }
                                    } else {
                                        onComplete()
                                    }
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to fetch reviews collection (fallback)", exception)
                    onComplete()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fallback method", e)
            onComplete()
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
                                updateReviewsWithNewProfilePicture(userId, imageUrl) {
                                    Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                                    onComplete(true)
                                }
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
        if (context is ComponentActivity) {
            (context as ComponentActivity).finishAffinity()
        }
    }
}
