package com.example.mobilw2ndcw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.mobilw2ndcw.data.MovieDatabase
import com.example.mobilw2ndcw.ui.MovieList
import com.example.mobilw2ndcw.ui.theme.Mobilw2ndCwTheme
import com.example.mobilw2ndcw.viewmodel.MovieViewModel
import com.example.mobilw2ndcw.viewmodel.MovieViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieActivity : ComponentActivity() {
    private lateinit var viewModel: MovieViewModel
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and ViewModel on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            val database = MovieDatabase.getDatabase(this@MovieActivity)
            val movieDao = database.movieDao()
            
            // Use the updated MovieViewModelFactory that creates the repository internally
            viewModel = MovieViewModelFactory(movieDao).create(MovieViewModel::class.java)

            // Switch back to main thread for UI updates
            withContext(Dispatchers.Main) {
                isReady = true
                setContent {
                    Mobilw2ndCwTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            // Get the current state from the ViewModel
                            val searchResults by viewModel.searchResults.collectAsState()
                            
                            // Display the movie list using the MovieList composable
                            MovieList(movies = searchResults)
                        }
                    }
                }
            }
        }
    }
} 