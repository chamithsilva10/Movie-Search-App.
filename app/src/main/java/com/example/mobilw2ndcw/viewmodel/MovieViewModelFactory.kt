package com.example.mobilw2ndcw.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobilw2ndcw.data.MovieDao
import com.example.mobilw2ndcw.repository.MovieRepository
import com.example.mobilw2ndcw.util.Constants

/**
 * Factory for creating a MovieViewModel with proper dependencies
 */
class MovieViewModelFactory(
    private val movieDao: MovieDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            val apiKey = Constants.OMDb_API_KEY
            val repository = MovieRepository(movieDao, apiKey)
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 