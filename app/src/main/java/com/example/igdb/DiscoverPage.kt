package com.example.igdb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DiscoverPage(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel(),
    onGameClicked: (Int) -> Unit
) {
    val initialGenre = gameViewModel.initialDiscoverGenre.value
    var selectedGenre by remember { mutableStateOf(initialGenre ?: genres.first { it.name == "Trending" }) }
    
    val allGamesMap = gameViewModel.games.value
    val currentGames = allGamesMap[selectedGenre.name] ?: emptyList()

    LaunchedEffect(selectedGenre) {
        gameViewModel.fetchGamesByGenre(selectedGenre.name, selectedGenre.slug)
    }

    // Consume the initial genre after it has been used
    LaunchedEffect(initialGenre) {
        if (initialGenre != null) {
            gameViewModel.consumeInitialGenre()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(8.dp)
    ) {
        GenreChips(
            genres = genres,
            selectedGenre = selectedGenre,
            onGenreSelected = { genre ->
                selectedGenre = genre
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            gameViewModel.isLoading.value -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFC107))
                }
            }

            currentGames.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No games found in ${selectedGenre.name}",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }

            else -> {
                GameGrid(currentGames, onGameClicked)
            }
        }
    }
}

@Composable
fun GenreChips(
    genres: List<Genre>,
    selectedGenre: Genre?,
    onGenreSelected: (Genre) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(genres.size) { index ->
            val genre = genres[index]
            val isSelected = genre == selectedGenre

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Color(0xFFFFC107) else Color.DarkGray)
                    .clickable { onGenreSelected(genre) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = genre.name,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GameGrid(games: List<Game>,onGameClicked: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(games) { game ->
            GameCard(game,onGameClicked = onGameClicked)
        }
    }
}
