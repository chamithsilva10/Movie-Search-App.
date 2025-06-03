package com.example.mobilw2ndcw.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data Access Object for Movie entities */
@Dao
interface MovieDao {
    /** Insert a movie, replace if exists */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    /** Insert multiple movies, replace if exist */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    /** Get all movies sorted by title */
    @Query("SELECT * FROM movies ORDER BY title ASC")
    fun getAllMovies(): Flow<List<Movie>>

    /** Find movies by actor name (case insensitive) */
    @Query("SELECT * FROM movies WHERE actors LIKE '%' || :actor || '%' COLLATE NOCASE")
    fun searchMoviesByActor(actor: String): Flow<List<Movie>>

    /** Get movie by IMDB ID */
    @Query("SELECT * FROM movies WHERE imdbID = :id")
    suspend fun getMovieById(id: String): Movie?
    
    /** Find first movie matching title */
    @Query("SELECT * FROM movies WHERE title LIKE '%' || :title || '%' LIMIT 1")
    suspend fun findMovieByTitle(title: String): Movie?
    
    /** Search movies by title */
    @Query("SELECT * FROM movies WHERE title LIKE '%' || :title || '%'")
    fun searchMoviesByTitle(title: String): Flow<List<Movie>>
} 