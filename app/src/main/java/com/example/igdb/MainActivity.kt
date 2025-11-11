package com.example.igdb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.example.igdb.ui.theme.Gold
import com.example.igdb.ui.theme.IGDBTheme
import com.example.igdb.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IGDBTheme {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LineIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
) {
    val totalWidth = 160.dp
    if (pagerState.pageCount == 0) return
    val thumbWidth = totalWidth / pagerState.pageCount
    val indicatorOffset = (pagerState.currentPage + pagerState.currentPageOffsetFraction) * thumbWidth

    Box(
        modifier = modifier
            .width(totalWidth)
            .height(4.dp)
            .background(inactiveColor, RoundedCornerShape(2.dp))
            .clip(RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .width(thumbWidth)
                .height(4.dp)
                .offset(x = indicatorOffset)
                .background(activeColor)
        )
    }
}

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
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.app_icn),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                navItems[pagerState.currentPage],
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },

                    scrollBehavior = scrollBehavior,
                )
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 1.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = pagerState.currentPage == index
                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = item, modifier = Modifier.size(28.dp)) },
                        label = { Text(item) },
                        selected = isSelected,
                        alwaysShowLabel = true,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 8.dp),
            userScrollEnabled = userScrollEnabled
        ) { page ->
            when (page) {
                0 -> ScrollContent(
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
                    modifier = Modifier,
                    gameViewModel = gameViewModel,
                    onGameClicked = {
                        navController.navigate("gameDetails/$it")
                    }
                )
                2 -> DiscoverPage(
                    modifier = Modifier,
                    gameViewModel = gameViewModel,
                    onGameClicked = {
                        navController.navigate("gameDetails/$it")
                    }
                )
                3 -> ProfilePage(modifier = Modifier)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrollContent(
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
        modifier = Modifier
    ) {
        item {
            val trendingGames = gamesByCategory["Trending"] ?: emptyList()
            if (trendingGames.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { trendingGames.size })

                LaunchedEffect(pagerState) {
                    while (true) {
                        delay(3000) // Adjust the delay as needed
                        if (pagerState.pageCount > 0) {
                            pagerState.animateScrollToPage((pagerState.currentPage + 1) % pagerState.pageCount)
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 40.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) { page ->
                        val game = trendingGames[page]
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

                        TrendingGameCard(
                            game = game,
                            onGameClicked = onGameClicked,
                            modifier = Modifier.graphicsLayer {
                                val scale = lerp(1f, 0.85f, pageOffset.coerceIn(0f, 1f))
                                scaleX = scale
                                scaleY = scale

                                alpha = lerp(1f, 0.5f, pageOffset.coerceIn(0f, 1f))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val currentGame = trendingGames.getOrNull(pagerState.currentPage)
                    if (currentGame != null) {
                        Text(
                            text = currentGame.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentGame.genres?.joinToString { it.name } ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LineIndicator(pagerState = pagerState)
                    }
                }

            }
        }

        gamesByCategory.forEach { (category, games) ->
            if (category != "Trending") {
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
                            text = "Show more",
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
}


@Composable
fun TrendingGameCard(game: Game, onGameClicked: (Int) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onGameClicked(game.id) },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = game.background_image,
                    placeholder = painterResource(id = R.drawable.gamingbook)
                ),
                contentDescription = game.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 16.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    game.rating?.let {
                        Text(
                            text = String.format("%.1f", it),
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Star, // Star icon
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun GameCard(game: Game, onGameClicked: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .clickable { onGameClicked(game.id) }
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(
                    model = game.background_image,
                    placeholder = painterResource(id = R.drawable.gamingbook) // Placeholder image
                ),
                contentDescription = game.name,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            game.rating?.let {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = String.format("%.1f", it),
                        color = White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Text(
            text = game.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun GreetingPreview() {
    IGDBTheme {
        AppNavigation()
    }
}
