package com.example.igdb

import android.content.Context
import android.text.Html
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.igdb.ui.theme.Gold
import com.example.igdb.ui.theme.IGDBTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


@Composable
fun IndeterminateCircularIndicator(loading: Boolean) {
    if (!loading) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun GamePage(
    gameId: Int,
    viewModel: GameViewModel,
    onGameClicked: (Int) -> Unit,
    onBackClicked: () -> Unit
) {
    val gameDetails by viewModel.gameDetails
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(gameId) {
        viewModel.fetchGameDetails(gameId)
        loading = true
    }

    IndeterminateCircularIndicator(loading = loading)

    gameDetails?.let { game ->
        loading = false
        Scaffold { innerPadding ->
            GameDetails(
                modifier = Modifier.padding(innerPadding),
                game = game,
                onGameClicked = onGameClicked,
                viewModel = viewModel,
                onBackClicked = onBackClicked
            )
        }
        IndeterminateCircularIndicator(loading = loading)
    }
}

@Composable
fun GameDetails(
    modifier: Modifier = Modifier,
    game: Game,
    viewModel: GameViewModel = GameViewModel(),
    onGameClicked: (Int) -> Unit,
    onBackClicked: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    LazyColumn(
        modifier = modifier
            .background(color = backgroundColor)
    ) {
        item {
            Box(
                contentAlignment = Alignment.BottomEnd,
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = game.background_image,
                        placeholder = painterResource(R.drawable.img),
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .drawWithCache {
                            val gradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    backgroundColor.copy(alpha = 0.9F)
                                ),
                                startY = size.height / 4,
                                endY = size.height
                            )
                            onDrawWithContent {
                                drawContent()
                                drawRect(gradient, blendMode = BlendMode.Multiply)
                            }
                        },
                )

                RatingText(text = game.rating.toString())

                TopButtons(
                    onBackClicked = onBackClicked,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .width(260.dp)
                ) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .padding(start = 12.dp, bottom = 8.dp)
                    )

                    Text(
                        text = game.genres?.joinToString(separator = "|") { it.name } ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        modifier = Modifier
                            .padding(start = 12.dp, bottom = 12.dp)
                    )
                }

            }
            InfoCard(
                game = game,
                onGameClicked = onGameClicked,
                viewModel = viewModel,
            )

        }
    }
}

@Composable
fun RatingText(modifier: Modifier = Modifier, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier
                .padding(end = 4.dp)
                .size(16.dp)

        )

        Text(
            text = text,
            color = Gold,
            fontSize = 16.sp,
        )

    }
}


@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = 3
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(start = 12.dp, end = 16.dp, bottom = 12.dp)
            .animateContentSize()
    ) {
        Text(
            text = text,
            maxLines = if (isExpanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
            color = color,
            fontFamily = FontFamily.Default,
            lineHeight = 16.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        )
    }

}

@Composable
fun TopButtons(modifier: Modifier = Modifier, onBackClicked: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Button(
            onClick = onBackClicked,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )

        }

        Button(
            onClick = { /*TODO*/ },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )

        }
    }
}


@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    game: Game,
    viewModel: GameViewModel,
    onGameClicked: (Int) -> Unit
) {
    val description = remember(game.description) {
        Html.fromHtml(game.description ?: "", Html.FROM_HTML_MODE_LEGACY).toString()
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )

    ) {
        Text(
            text = stringResource(R.string.about_this_game),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier
                .padding(start = 12.dp, top = 16.dp, bottom = 12.dp)
        )
        ExpandableText(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        RelatedGames(
            viewModel = viewModel,
            game = game,
            onGameClicked = onGameClicked
        )
        AddingRateManager()
        TabMenu(game = game)
    }
}

@Composable
fun TabMenu(game: Game) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.reviews),
        stringResource(R.string.minimum_specs), stringResource(R.string.recommended_specs)
    )

    Column {
        TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.padding(4.dp)) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title, fontSize = 12.sp) }
                )
            }
        }

        val pcRequirements = game.platforms?.find { it.platform.slug == "pc" }?.requirements

        when (selectedTabIndex) {
            0 -> {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(10) {
                        ReviewCard()
                    }
                }
            }

            1 -> {
                val minimumRequirements = pcRequirements?.minimum
                ExpandableText(
                    text = minimumRequirements ?: stringResource(R.string.no_minimum_requirements),
                    maxLines = 10,
                    modifier = Modifier.padding(16.dp)
                )
            }

            2 -> {
                val recommendedRequirements = pcRequirements?.recommended
                ExpandableText(
                    text = recommendedRequirements
                        ?: stringResource(R.string.no_recommended_requirements),
                    maxLines = 10,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {

        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 12.dp, top = 12.dp, bottom = 4.dp)
                    .fillMaxWidth()

            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icn),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Text(
                    text = "Mohamed Ibrahim",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)

                )

                RatingText(text = "5.0")
            }
            ExpandableText(
                text = "After years of hype and anticipation, Cyberpunk 2077 delivers a visually stunning and narratively rich dive into the futuristic city of Night City. You play as V, a mercenary navigating a world of corporate corruption, cybernetic enhancements, and moral ambiguity.",
            )


        }


    }
}


@Composable
fun RelatedGames(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    game: Game,
    onGameClicked: (Int) -> Unit
) {
    val relatedGames by viewModel.relatedGames
    val filterRelatedGames = relatedGames.filter { it.id != game.id }


    LaunchedEffect(game.genres) {
        game.genres?.firstOrNull()?.slug?.let {
            viewModel.fetchRelatedGames(it)
        }
    }

    if (filterRelatedGames.isNotEmpty()) {
        Column {
            Text(
                text = stringResource(R.string.related_games),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier
                    .padding(start = 12.dp, bottom = 12.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(filterRelatedGames) { relatedGame ->
                    GameCard(
                        game = relatedGame,
                        onGameClicked = onGameClicked
                    )
                }
            }
        }
    }
}

@Composable
fun AddingRateManager(modifier: Modifier = Modifier) {
    Column() {
        Text(
            text = stringResource(R.string.did_you_played_this_game),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp)
        )
        RateItCard()
    }
}

@Composable
fun RateItCard(modifier: Modifier = Modifier, isClicked: (Boolean) -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)

                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.rate_it),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.let_the_world_know_how_you_felt),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier
                    .size(32.dp)

            )

        }


    }
}


@Preview(showSystemUi = true)
@Composable
fun GameDetailsPreview() {
    IGDBTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            GameDetails(
                game = Game(
                    1,
                    "The Witcher 3: Wild Hunt",
                    "https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg",
                    "<p>The Witcher 3: Wild Hunt is a 2015 action role-playing game developed and published by Polish developer CD Projekt Red and is based on The Witcher series of fantasy novels by Andrzej Sapkowski. The game is the sequel to the 2011 game The Witcher 2: Assassins of Kings, and the third main installment in The Witcher video game series, played in an open world with a third-person perspective.</p>",
                    platforms = listOf(
                        PlatformEntry(
                            platform = Platform(1, "PC", "pc"),
                            requirements = Requirements(
                                minimum = "Intel CPU Core i5-2500K 3.3GHz / AMD CPU Phenom II X4 940",
                                recommended = "Intel CPU Core i7 3770 3.4 GHz / AMD CPU AMD FX-8350 4 GHz"
                            )
                        )
                    ),
                    rating = 3.6,
                    genres = listOf(GameGenre("Action", "Action"), GameGenre("RPG", "RPG"))
                ),
                onGameClicked = {},
                onBackClicked = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

}


//logic
fun addRate(auth: FirebaseAuth, context: Context, userRate: Double) {
    val userId = auth.currentUser?.uid
    val user = User(id = userId, rate = userRate)

    Firebase
        .firestore
        .collection("users")
        .document(userId!!)
        .set(user)
        .addOnSuccessListener {
            Toast.makeText(context, "Rate Added", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }

}