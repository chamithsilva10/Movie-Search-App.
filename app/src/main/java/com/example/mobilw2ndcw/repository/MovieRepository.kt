package com.example.mobilw2ndcw.repository

import com.example.mobilw2ndcw.api.DirectOmdbApiClient
import com.example.mobilw2ndcw.data.Movie
import com.example.mobilw2ndcw.data.MovieDao
import com.example.mobilw2ndcw.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MovieRepository(
    private val movieDao: MovieDao,
    private val apiKey: String = ""
) {
    private val apiClient = DirectOmdbApiClient()

    fun getAllMovies(): Flow<List<Movie>> = movieDao.getAllMovies()

    /**
     * Searches for movies by actor name with the following features:
     * - Case-insensitive: Will find results regardless of case (e.g., "cruise" finds "Tom Cruise")
     * - Substring match: Will find partial matches (e.g., "ruis" finds "Tom Cruise")
     * - Returns all matches: Will return all movies containing actors that match the search term
     *
     * @param actor The actor name or part of the name to search for
     * @return Flow of all movies featuring actors whose names contain the search term
     */
    fun searchMoviesByActor(actor: String): Flow<List<Movie>> = 
        movieDao.searchMoviesByActor(actor)

    /**
     * Finds a movie by its exact title in the local database
     * 
     * @param title The exact title to search for
     * @return The movie if found, or null if not found
     */
    suspend fun findMovieByTitle(title: String): Movie? {
        return movieDao.findMovieByTitle(title)
    }

    /**
     * Searches for movies with titles containing the given string in the local database
     * 
     * @param title Part of a title to search for
     * @return Flow of movies with matching titles
     */
    fun searchMoviesByTitle(title: String): Flow<List<Movie>> {
        return movieDao.searchMoviesByTitle(title)
    }

    suspend fun searchMovieByTitle(title: String): Movie {
        try {
            val movieJson = apiClient.retrieveMovie(title)
            val movie = apiClient.parseJSONToMovie(movieJson)
            
            if (movie == null || movie.title.isNullOrBlank() || movie.imdbID.isNullOrBlank()) {
                throw IllegalStateException("Movie not found")
            }
            
            val sanitizedMovie = movie.copy(
                title = movie.title ?: "",
                year = movie.year ?: "",
                rated = movie.rated ?: "",
                runtime = movie.runtime ?: ""
            )
            movieDao.insertMovie(sanitizedMovie)
            return sanitizedMovie
        } catch (e: Exception) {
            throw IllegalStateException("Failed to fetch movie: ${e.message}")
        }
    }

    suspend fun searchMoviesByTitleFromApi(title: String): List<Movie> {
        try {
            val movieJson = apiClient.searchMovies(title)
            val movies = apiClient.parseJSONToMovieList(movieJson)
            
            if (movies.isNotEmpty()) {
                movieDao.insertMovies(movies)
            }
            
            return movies
        } catch (e: Exception) {
            throw IllegalStateException("Failed to search movies: ${e.message}")
        }
    }

    suspend fun insertMovies(movies: List<Movie>) {
        try {
            println("Attempting to insert ${movies.size} movies into the database")
            
            // Get existing movies to check for duplicates
            val existingMovies = try {
                movieDao.getAllMovies().first()
            } catch (e: Exception) {
                println("Error getting existing movies: ${e.message}")
                emptyList()
            }
            
            val existingIds = existingMovies.map { it.imdbID }.toSet()
            println("Found ${existingIds.size} existing movies in database")
            
            // Filter out movies that already exist and ensure all required fields are valid
            val newMovies = movies.filter { movie -> 
                val isValid = !movie.title.isNullOrBlank() && !movie.imdbID.isNullOrBlank()
                val isNew = !existingIds.contains(movie.imdbID)
                
                if (!isValid) println("Skipping invalid movie: ${movie.title} (ID: ${movie.imdbID})")
                if (!isNew) println("Skipping existing movie: ${movie.title} (ID: ${movie.imdbID})")
                
                isValid && isNew
            }.map { movie ->
                // Ensure all fields have valid values even if empty
                movie.copy(
                    title = movie.title,
                    year = movie.year,
                    rated = movie.rated,
                    released = movie.released,
                    runtime = movie.runtime,
                    genre = movie.genre,
                    director = movie.director,
                    writer = movie.writer,
                    actors = movie.actors,
                    plot = movie.plot
                )
            }
            
            println("Will insert ${newMovies.size} new movies")
            
            if (newMovies.isNotEmpty()) {
                // Insert each movie individually to better handle errors
                for (movie in newMovies) {
                    try {
                        movieDao.insertMovie(movie)
                        println("Successfully inserted: ${movie.title}")
                    } catch (e: Exception) {
                        println("Failed to insert movie ${movie.title}: ${e.message}")
                    }
                }
                println("Movie insertion completed")
            } else {
                println("No new movies to insert")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalStateException("Failed to insert movies: ${e.message}")
        }
    }

    /**
     * Clears all movies from the database
     */
    suspend fun clearAllMovies() {
        try {
            println("Clearing all movies functionality is not implemented")
            // Database clearing is disabled in this version
            // For a production app, you would implement an alternative approach:
            // 1. For a complete clear, recreate the database
            // 2. For selective clearing, implement individual delete queries
        } catch (e: Exception) {
            println("Error with clear database operation: ${e.message}")
            throw e
        }
    }
} 