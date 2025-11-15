package com.example.igdb.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.igdb.ui.activities.GameCard
import com.example.igdb.ui.theme.IGDBTheme
import com.example.igdb.viewmodel.GameViewModel
import com.example.igdb.viewmodel.PreviewGameViewModel

@Composable
fun FavouritesPage(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    onGameClicked: (Int) -> Unit = {}
) {
    val favoriteGames by gameViewModel.favoriteGames
    val isLoading by gameViewModel.isLoading

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        when {
            isLoading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            favoriteGames.isEmpty() -> {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "No Favourites",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No favourites yet, start making them", style = MaterialTheme.typography.bodyLarge)
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favoriteGames) { game ->
                        GameCard(game = game, onGameClicked = onGameClicked)
                    }
                }
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showSystemUi = true)
@Composable
fun FavouritesPreview() {
    IGDBTheme {
        FavouritesPage(gameViewModel = PreviewGameViewModel())
    }
}
