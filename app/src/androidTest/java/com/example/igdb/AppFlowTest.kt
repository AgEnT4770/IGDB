
package com.example.igdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.igdb.ui.activities.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testHomeScreenIsDisplayed() {
        composeTestRule.onNodeWithTag("top_app_bar_title").assertIsDisplayed()
    }

    @Test
    fun testNavigateToDiscoverScreen() {
        composeTestRule.onNodeWithTag("Discover_tab").performClick()
        composeTestRule.onNodeWithTag("top_app_bar_title").assertIsDisplayed()
    }

    @Test
    fun testNavigateToFavouritesScreen() {
        composeTestRule.onNodeWithTag("Favourites_tab").performClick()
        composeTestRule.onNodeWithTag("top_app_bar_title").assertIsDisplayed()
    }

    @Test
    fun testNavigateToPersonalScreen() {
        composeTestRule.onNodeWithTag("Profile_tab").performClick()
        composeTestRule.onNodeWithTag("top_app_bar_title").assertIsDisplayed()
    }

    @Test
    fun testNavigateToGameDetails() {
        composeTestRule.waitUntil(30000) {
            composeTestRule.onAllNodesWithTag("game_card").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("game_card")[0].performClick()
        composeTestRule.waitUntil(30000) {
            composeTestRule.onAllNodesWithTag("about_this_game_title").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("about_this_game_title").assertIsDisplayed()
    }
}
