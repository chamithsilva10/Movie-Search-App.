package com.example.mobilw2ndcw.util

import com.example.mobilw2ndcw.data.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object MovieDataLoader {
    suspend fun loadMoviesFromUrl(url: String): List<Movie> = withContext(Dispatchers.IO) {
        val text = URL(url).readText()
        parseMovies(text)
    }

    private fun parseMovies(text: String): List<Movie> {
        return text.lines()
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split("|")
                Movie(
                    imdbID = parts[0],
                    title = parts[1],
                    year = parts[2],
                    rated = parts[3],
                    released = parts[4],
                    runtime = parts[5],
                    genre = parts[6],
                    director = parts[7],
                    writer = parts[8],
                    actors = parts[9],
                    plot = parts[10]
                )
            }
    }
} 