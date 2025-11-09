package com.example.igdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igdb.ui.viewmodel.GameViewModel
import com.example.igdb.ui.components.gamecard.GameCard
import com.example.igdb.ui.theme.IGDPTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel(),
    onGameClicked: (Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by gameViewModel.searchResults
    val isLoading by gameViewModel.isLoading

    LaunchedEffect(searchQuery) {
        gameViewModel.searchGames(searchQuery)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            placeholder = { Text("Search games...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFC107))
                }
            }
            searchQuery.isNotBlank() && searchResults.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No games found.")
                }
            }
            searchQuery.isNotBlank() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { game ->
                        GameCard(game = game, onGameClicked = onGameClicked)
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Type to search for games 🔎")
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun SearchPreview() {
    IGDPTheme {
        SearchPage()
    }
}
