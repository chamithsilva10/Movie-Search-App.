package com.example.mobilw2ndcw.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilw2ndcw.data.Movie
import com.example.mobilw2ndcw.repository.MovieRepository
import com.example.mobilw2ndcw.util.OmdbApiUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for movie data and UI state management */
class MovieViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    // UI state tracking
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Search results
    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults: StateFlow<List<Movie>> = _searchResults.asStateFlow()

    /** Reset UI state */
    fun resetUiState() {
        _uiState.value = UiState.Initial
    }

    /** Clear search results */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _uiState.value = UiState.Initial
    }

    /** Add movies to database */
    fun addMoviesToDb(movies: List<Movie>) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                println("ViewModel: Starting to add ${movies.size} movies to database")
                
                repository.insertMovies(movies)
                println("ViewModel: Movies added successfully")
                
                _uiState.value = UiState.Success("${movies.size} movies added to database")
            } catch (e: Exception) {
                println("ViewModel Error: Failed to add movies - ${e.message}")
                e.printStackTrace()
                _uiState.value = UiState.Error("Failed to add movies: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /** Search movie by title (API then local DB) */
    fun searchMovieByTitle(title: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                println("Searching for movie with title: $title")
                
                // First try to get the movie from the API
                try {
                    // Get movie data from OMDB API
                    val result = OmdbApiUtil.getMovieByTitle(title)
                    result.fold(
                        onSuccess = { movie ->
                            // Save to database for future searches and display
                            repository.insertMovies(listOf(movie))
                            _searchResults.value = listOf(movie)
                            _uiState.value = UiState.Success("Movie found: ${movie.title}")
                        },
                        onFailure = { error ->
                            // Try the repository as fallback
                            println("API search failed, trying repository: ${error.message}")
                            try {
                                val movie = repository.searchMovieByTitle(title)
                                _searchResults.value = listOf(movie)
                                _uiState.value = UiState.Success("Movie found")
                            } catch (e: Exception) {
                                _uiState.value = UiState.Error("Movie not found. Please try a different title or use Web Search.")
                            }
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Failed to fetch movie: ${e.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                println("Error searching for movie: ${e.message}")
                e.printStackTrace()
                _uiState.value = UiState.Error("Failed to fetch movie: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /** Search movies by title in local DB */
    fun searchMoviesByTitle(title: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                // Use try-catch to safely handle empty results without exception
                try {
                    // Collect the Flow to get actual List<Movie>
                    repository.searchMoviesByTitle(title).collect { movies ->
                        if (movies.isNotEmpty()) {
                            _searchResults.value = movies
                            _uiState.value = UiState.Success("Found ${movies.size} movies")
                        } else {
                            _uiState.value = UiState.Error("No movies found with title \"$title\"")
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Search failed: ${e.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("No movies found with that title")
            }
        }
    }

    /** Search movies by actor in local DB */
    fun searchMoviesByActor(actor: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                // Make sure search term is not empty
                if (actor.isBlank()) {
                    _uiState.value = UiState.Error("Please enter an actor name")
                    return@launch
                }
                
                repository.searchMoviesByActor(actor).collect { movies ->
                    _searchResults.value = movies
                    _uiState.value = if (movies.isEmpty()) {
                        UiState.Error("No movies found with actor \"$actor\"")
                    } else {
                        UiState.Success("Found ${movies.size} movies with actor \"$actor\"")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error("Search failed: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    /** Find and save movie to database from API */
    fun saveMovieToDb(title: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                println("Saving movie with title: $title to database")
                
                try {
                    // Get movie data from OMDB API
                    val result = OmdbApiUtil.getMovieByTitle(title)
                    result.fold(
                        onSuccess = { movie ->
                            // Save to database
                            repository.insertMovies(listOf(movie))
                            _searchResults.value = listOf(movie)
                            _uiState.value = UiState.Success("Movie \"${movie.title}\" saved to database")
                        },
                        onFailure = { error ->
                            _uiState.value = UiState.Error("No movie found with title \"$title\": ${error.message}")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Failed to save movie: ${e.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                println("Error saving movie: ${e.message}")
                e.printStackTrace()
                _uiState.value = UiState.Error("Failed to save movie: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /** Search directly from OMDB API (not local DB) */
    fun extendedMovieSearch(searchText: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                // Make sure the search term is not empty
                if (searchText.isBlank()) {
                    _uiState.value = UiState.Error("Please enter a search term")
                    return@launch
                }
                
                // Make the API call to search for movies
                val result = OmdbApiUtil.searchMovies(searchText)
                result.fold(
                    onSuccess = { movies ->
                        if (movies.isNotEmpty()) {
                            // Display the search results without saving to the database
                            _searchResults.value = movies
                            _uiState.value = UiState.Success("Found ${movies.size} movies for \"$searchText\"")
                        } else {
                            _uiState.value = UiState.Error("No movies found containing \"$searchText\"")
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error("Search failed: ${error.message ?: "Unknown error"}")
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error("Search failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /** UI state representation */
    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
} 