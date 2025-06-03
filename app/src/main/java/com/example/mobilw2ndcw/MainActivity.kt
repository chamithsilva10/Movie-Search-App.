package com.example.mobilw2ndcw

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.mobilw2ndcw.data.Movie
import com.example.mobilw2ndcw.data.MovieDatabase
import com.example.mobilw2ndcw.ui.SimpleMovieList
import com.example.mobilw2ndcw.ui.theme.Mobilw2ndCwTheme
import com.example.mobilw2ndcw.util.PredefinedMovies
import com.example.mobilw2ndcw.viewmodel.MovieViewModel
import com.example.mobilw2ndcw.viewmodel.MovieViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Main activity for the OMDB movie application */
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MovieViewModel
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show splash screen until initialization is complete
        installSplashScreen().setKeepOnScreenCondition { !isReady }
        super.onCreate(savedInstanceState)

        // Initialize database and viewModel in background
        CoroutineScope(Dispatchers.IO).launch {
            val database = MovieDatabase.getDatabase(this@MainActivity)
            val factory = MovieViewModelFactory(database.movieDao())
            viewModel = ViewModelProvider(this@MainActivity, factory)[MovieViewModel::class.java]

            // Set up UI on main thread once initialization is complete
            withContext(Dispatchers.Main) {
                isReady = true
                setContent {
                    Mobilw2ndCwTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

/** Main UI screen with navigation and content display */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MovieViewModel) {
    // Dialog visibility states
    var showSearchDialog by remember { mutableStateOf(false) }
    var showActorSearchDialog by remember { mutableStateOf(false) }
    var showExtendedSearchDialog by remember { mutableStateOf(false) }
    var showAddMoviesDialog by remember { mutableStateOf(false) }

    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is MovieViewModel.UiState.Success -> {
                showAddMoviesDialog = false
                snackbarHostState.showSnackbar(
                    message = (uiState as MovieViewModel.UiState.Success).message,
                    duration = SnackbarDuration.Short
                )
            }
            is MovieViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (uiState as MovieViewModel.UiState.Error).message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar with navigation
            TopAppBar(
                title = { Text("Movie") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    if (searchResults.isNotEmpty() || showSearchDialog || showActorSearchDialog ||
                        showExtendedSearchDialog || showAddMoviesDialog) {
                        IconButton(onClick = {
                            viewModel.clearSearchResults()
                            showSearchDialog = false
                            showActorSearchDialog = false
                            showExtendedSearchDialog = false
                            showAddMoviesDialog = false
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to main menu")
                        }
                    }
                }
            )

            // Content area based on current state
            when {
                // Loading indicator
                uiState is MovieViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                // Error display
                uiState is MovieViewModel.UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = (uiState as MovieViewModel.UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { viewModel.resetUiState() }) {
                                Text("Back to Main Menu")
                            }
                        }
                    }
                }
                // Search results display
                searchResults.isNotEmpty() -> {
                    SimpleMovieList(movies = searchResults)
                }
                // Main menu with action cards
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ActionCard(
                                title = "Add Movies",
                                icon = Icons.Default.Add,
                                description = "Add predefined movies to database",
                                onClick = { showAddMoviesDialog = true }
                            )
                        }
                        item {
                            ActionCard(
                                title = "Search Movies",
                                icon = Icons.Default.Search,
                                description = "Search your local database",
                                onClick = { showSearchDialog = true }
                            )
                        }
                        item {
                            ActionCard(
                                title = "Find Actors",
                                icon = Icons.Default.Person,
                                description = "Search by actor name",
                                onClick = { showActorSearchDialog = true }
                            )
                        }
                        item {
                            ActionCard(
                                title = "Web Search",
                                icon = Icons.Default.Search,
                                description = "Search OMDB for movies with detailed information",
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                onClick = { showExtendedSearchDialog = true }
                            )
                        }
                    }
                }
            }
        }

        // Snackbar for displaying messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    // Dialog for adding predefined movies
    if (showAddMoviesDialog) {
        AddMoviesDialog(
            onDismiss = { showAddMoviesDialog = false },
            onAddMovies = {
                scope.launch {
                    viewModel.addMoviesToDb(PredefinedMovies.getMovies())
                }
            },
            isLoading = uiState is MovieViewModel.UiState.Loading
        )
    }

    // Dialog for searching movies by title
    if (showSearchDialog) {
        MovieSearchDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { query -> viewModel.searchMovieByTitle(query) },
            onSave = { query -> viewModel.saveMovieToDb(query) },
            viewModel = viewModel,
            isLoading = uiState is MovieViewModel.UiState.Loading
        )
    }

    // Dialog for searching movies by actor
    if (showActorSearchDialog) {
        ModernSearchDialog(
            title = "Find Actors",
            subtitle = "Enter part of an actor's name to find all movies featuring them",
            placeholder = "e.g. 'cruise' will find Tom Cruise movies",
            onSearch = { viewModel.searchMoviesByActor(it) },
            onDismiss = { showActorSearchDialog = false },
            isLoading = uiState is MovieViewModel.UiState.Loading
        )
    }

    // Dialog for extended web search
    if (showExtendedSearchDialog) {
        ModernSearchDialog(
            title = "Web Search",
            subtitle = "Type part of a movie title to find movies from OMDB. Results will show detailed information including title, year, rating, director, actors and plot.",
            placeholder = "e.g. 'mat' will find Matrix, Matilda...",
            onSearch = { viewModel.extendedMovieSearch(it) },
            onDismiss = { showExtendedSearchDialog = false },
            isLoading = uiState is MovieViewModel.UiState.Loading
        )
    }
}

/** Card for main menu actions with icon and description */
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    description: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Dialog for adding predefined movies to the database */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoviesDialog(
    onDismiss: () -> Unit,
    onAddMovies: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Movies to Database") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add predefined classic movies to your database?")
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Adding movies...")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddMovies,
                enabled = !isLoading
            ) {
                Text("Add Movies")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

/** Dialog for searching movies with search and save options */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieSearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    onSave: (String) -> Unit,
    viewModel: MovieViewModel,
    isLoading: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    "Search for Movies",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search input field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Enter movie title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onSearch(searchQuery) },
                        enabled = searchQuery.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("Retrieve Movie")
                    }

                    Button(
                        onClick = { onSave(searchQuery) },
                        enabled = searchQuery.isNotBlank() && !isLoading && searchResults.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Save to Database")
                    }
                }

                // Status indicators
                if (isLoading) {
                    LoadingIndicator()
                }

                if (uiState is MovieViewModel.UiState.Error) {
                    ErrorMessage((uiState as MovieViewModel.UiState.Error).message)
                }

                // Movie details if found
                if (searchResults.isNotEmpty()) {
                    MovieDetails(searchResults.first())
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

/** Generic search dialog with customizable title and placeholder */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchDialog(
    title: String,
    subtitle: String? = null,
    placeholder: String,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                // Header with back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                // Optional subtitle
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(placeholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                // Loading indicator
                if (isLoading) {
                    LoadingIndicator()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSearch(searchQuery) },
                enabled = searchQuery.isNotBlank() && !isLoading
            ) {
                Text("Search")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

/** Loading indicator with text */
@Composable
fun LoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Searching...", style = MaterialTheme.typography.bodyMedium)
    }
}

/** Error message display with icon */
@Composable
fun ErrorMessage(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

/** Displays detailed movie information */
@Composable
fun MovieDetails(movie: Movie) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Title: ${movie.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Year: ${movie.year}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Rated: ${movie.rated}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Released: ${movie.released}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Runtime: ${movie.runtime}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Genre: ${movie.genre}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Director: ${movie.director}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Writer: ${movie.writer}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Actors: ${movie.actors}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // Plot summary text
            Text(text = "Plot: ${movie.plot}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}