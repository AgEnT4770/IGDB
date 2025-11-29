
package com.example.igdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.igdb.ui.activities.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDetailsPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAddAndRemoveFromFavorites() {
        // Step 1: Verify user is logged in
        composeTestRule.onNodeWithTag("Profile_tab").performClick()
        try {
            composeTestRule.waitUntil(5000) { composeTestRule.onAllNodesWithText("Logout").fetchSemanticsNodes().isNotEmpty() }
            composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            throw AssertionError("Test failed: User is not logged in. Please log in to the app on the emulator and restart the test.", e)
        }

        // Step 2: Navigate to a game and handle its initial favorite state
        composeTestRule.onNodeWithTag("Home_tab").performClick()
        composeTestRule.waitUntil(30000) { composeTestRule.onAllNodesWithTag("game_card").fetchSemanticsNodes().isNotEmpty() }
        composeTestRule.onAllNodesWithTag("game_card")[0].performClick()
        composeTestRule.waitUntil(30000) { composeTestRule.onAllNodesWithTag("about_this_game_title").fetchSemanticsNodes().isNotEmpty() }

        // If the game is already a favorite, remove it first to ensure a consistent starting state
        if (composeTestRule.onAllNodes(hasContentDescription("Remove from favorites")).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithContentDescription("Remove from favorites").performClick()
            composeTestRule.waitUntil(5000) { composeTestRule.onAllNodes(hasContentDescription("Add to favorites")).fetchSemanticsNodes().isNotEmpty() }
        }

        // Step 3: Test adding to favorites
        val addToFavoritesButton = composeTestRule.onNodeWithContentDescription("Add to favorites")
        addToFavoritesButton.performClick()
        composeTestRule.waitUntil(5000) { composeTestRule.onAllNodes(hasContentDescription("Remove from favorites")).fetchSemanticsNodes().isNotEmpty() }
        composeTestRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()

        // Step 4: Test removing from favorites
        val removeFromFavoritesButton = composeTestRule.onNodeWithContentDescription("Remove from favorites")
        removeFromFavoritesButton.performClick()
        composeTestRule.waitUntil(5000) { composeTestRule.onAllNodes(hasContentDescription("Add to favorites")).fetchSemanticsNodes().isNotEmpty() }
        composeTestRule.onNodeWithContentDescription("Add to favorites").assertIsDisplayed()
    }
}
