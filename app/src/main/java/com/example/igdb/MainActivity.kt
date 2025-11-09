package com.example.igdb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
//import com.example.igdb.UI.GameCard
//import com.example.igdb.viewmodel.GameViewModel
//import com.example.igdb.UI.GameCard
import com.example.igdb.discover.DiscoverPage
import com.example.igdb.ui.screens.GamePage
import com.example.igdb.ui.components.gamecard.GameCard
import com.example.igdb.ui.screens.ProfilePage
import com.example.igdb.ui.theme.IGDPTheme
import com.example.igdb.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IGDPTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController = navController)
        }
        composable(
            route = "gameDetails/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId")
            if (gameId != null) {
                GamePage(gameId = gameId, viewModel = viewModel(), onGameClicked = { gameId ->
                    navController.navigate("gameDetails/$gameId")
                })
            }
        }
    }
}


data class Genre(val name: String, val slug: String)

internal val genres = listOf(
    Genre("Trending", "-popularity"),
    Genre("Action", "action"),
    Genre("Adventure", "adventure"),
    Genre("RPG", "role-playing-games-rpg"),
    Genre("Strategy", "strategy"),
    Genre("Indie", "indie"),
    Genre("Shooter", "shooter"),
    Genre("Casual", "casual"),
    Genre("Simulation", "simulation"),
    Genre("Puzzle", "puzzle"),
    Genre("Arcade", "arcade"),
    Genre("Platformer", "platformer"),
    Genre("Massively Multiplayer", "massively-multiplayer"),
    Genre("Racing", "racing"),
    Genre("Sports", "sports"),
    Genre("Fighting", "fighting"),
    Genre("Family", "family"),
    Genre("Board Games", "board-games"),
    Genre("Card", "card"),
    Genre("Educational", "educational")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController, gameViewModel: GameViewModel = viewModel()) { // Hoisted ViewModel
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navItems = listOf("Home", "Search", "Discover", "Profile")
    val navIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Search,
        Icons.Filled.Explore,
        Icons.Filled.Person
    )
    val pagerState = rememberPagerState(pageCount = { navItems.size })
    val coroutineScope = rememberCoroutineScope()
    var userScrollEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(pagerState.currentPage) {
        userScrollEnabled = true
    }


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.gamingbook),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = navItems[pagerState.currentPage],
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },

                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
                NavigationBar(modifier = Modifier.clip(RoundedCornerShape(24.dp))) {
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(navIcons[index], contentDescription = item) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = userScrollEnabled
        ) { page ->
            when (page) {
                0 -> ScrollContent(
                    innerPadding = innerPadding,
                    gameViewModel = gameViewModel,
                    onUserInteractingWithLazyRow = { isInteracting ->
                        userScrollEnabled = !isInteracting
                    },
                    onShowMoreClicked = { categoryName ->
                        val genre = genres.find { it.name == categoryName }
                        if (genre != null) {
                            gameViewModel.setInitialGenreForDiscover(genre)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2) // Switch to Discover page
                            }
                        }
                    },
                    onGameClicked = { gameId ->
                        navController.navigate("gameDetails/$gameId")
                    }
                )
                1 -> SearchPage(
                    modifier = Modifier.padding(innerPadding),
                    gameViewModel = gameViewModel,
                    onGameClicked = {
                        navController.navigate("gameDetails/$it")
                    }
                )
                2 -> DiscoverPage(
                    modifier = Modifier.padding(innerPadding),
                    gameViewModel = gameViewModel,
                    onGameClicked = {
                        navController.navigate("gameDetails/$it")
                    }
                )
                3 -> ProfilePage(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun ScrollContent(
    innerPadding: PaddingValues,
    gameViewModel: GameViewModel,
    onUserInteractingWithLazyRow: (Boolean) -> Unit,
    onShowMoreClicked: (String) -> Unit,
    onGameClicked: (Int) -> Unit
) {
    LaunchedEffect(Unit) {
        gameViewModel.fetchGames()
    }

    val gamesByCategory = gameViewModel.games.value

    LazyColumn(
        modifier = Modifier.padding(innerPadding)
    ) {
        gamesByCategory.forEach { (category, games) ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Show more ->",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onShowMoreClicked(category) }
                    )
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onUserInteractingWithLazyRow(true)
                                try {
                                    awaitRelease()
                                } finally {
                                    onUserInteractingWithLazyRow(false)
                                }
                            }
                        )
                    }
                ) {
                    items(games) { game ->
                        GameCard(game, onGameClicked = onGameClicked)
                    }
                }
            }
        }
    }
}



@Preview(showSystemUi = true)
@Composable
fun GreetingPreview() {
    IGDPTheme {
        AppNavigation()
    }
}
