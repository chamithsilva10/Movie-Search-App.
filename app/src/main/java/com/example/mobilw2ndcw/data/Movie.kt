package com.example.mobilw2ndcw.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Movie entity for local database */
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey
    val imdbID: String,     // IMDB ID
    val title: String,      // Movie title
    val year: String,       // Release year
    val rated: String,      // Age rating
    val released: String,   // Release date
    val runtime: String,    // Duration
    val genre: String,      // Genres
    val director: String,   // Director
    val writer: String,     // Writer
    val actors: String,     // Cast
    val plot: String        // Plot summary
) 