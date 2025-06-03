package com.example.mobilw2ndcw.util

import com.example.mobilw2ndcw.data.Movie
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Utility class for making direct HTTP requests to the OMDb API without using Retrofit.
 * This can be used as an alternative approach or for cases where direct URL access is needed.
 */
object OmdbApiUtil {
    private val gson = Gson()
    
    /**
     * Fetches movie details by title directly using HttpURLConnection
     */
    suspend fun getMovieByTitle(title: String): Result<Movie> = withContext(Dispatchers.IO) {
        try {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val url = Constants.getOmdbUrlWithTitle(encodedTitle)
            
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            
            val response = if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = InputStreamReader(connection.inputStream)
                val movieResponse = gson.fromJson(reader, MovieResponse::class.java)
                if (movieResponse.Response == "True") {
                    Result.success(movieResponse.toMovie())
                } else {
                    Result.failure(Exception(movieResponse.Error ?: "Movie not found"))
                }
            } else {
                Result.failure(Exception("HTTP Error: ${connection.responseCode}"))
            }
            
            connection.disconnect()
            response
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Searches for movies by title directly using HttpURLConnection
     */
    suspend fun searchMovies(searchTerm: String): Result<List<Movie>> = withContext(Dispatchers.IO) {
        try {
            val encodedSearch = URLEncoder.encode(searchTerm, "UTF-8")
            val url = Constants.getOmdbSearchUrl(encodedSearch)
            
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            
            val response = if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = InputStreamReader(connection.inputStream)
                val searchResponse = gson.fromJson(reader, SearchResponse::class.java)
                if (searchResponse.Response == "True") {
                    val movies = searchResponse.Search?.map { result ->
                        // We only get basic info from search, so we need to fetch full details
                        val detailsUrl = Constants.getOmdbUrlWithId(result.imdbID)
                        val detailsConnection = URL(detailsUrl).openConnection() as HttpURLConnection
                        val detailsReader = InputStreamReader(detailsConnection.inputStream)
                        val movieResponse = gson.fromJson(detailsReader, MovieResponse::class.java)
                        movieResponse.toMovie()
                    } ?: emptyList()
                    Result.success(movies)
                } else {
                    Result.failure(Exception(searchResponse.Error ?: "No movies found"))
                }
            } else {
                Result.failure(Exception("HTTP Error: ${connection.responseCode}"))
            }
            
            connection.disconnect()
            response
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper data classes for parsing JSON responses
    data class MovieResponse(
        @SerializedName("imdbID") val imdbID: String,
        @SerializedName("Title") val Title: String,
        @SerializedName("Year") val Year: String,
        @SerializedName("Rated") val Rated: String,
        @SerializedName("Released") val Released: String,
        @SerializedName("Runtime") val Runtime: String,
        @SerializedName("Genre") val Genre: String,
        @SerializedName("Director") val Director: String,
        @SerializedName("Writer") val Writer: String,
        @SerializedName("Actors") val Actors: String,
        @SerializedName("Plot") val Plot: String,
        @SerializedName("Response") val Response: String,
        @SerializedName("Error") val Error: String?
    ) {
        fun toMovie() = Movie(
            imdbID = imdbID,
            title = Title,
            year = Year,
            rated = Rated,
            released = Released,
            runtime = Runtime,
            genre = Genre,
            director = Director,
            writer = Writer,
            actors = Actors,
            plot = Plot
        )
    }
    
    data class SearchResponse(
        @SerializedName("Search") val Search: List<SearchResult>?,
        @SerializedName("totalResults") val totalResults: String?,
        @SerializedName("Response") val Response: String,
        @SerializedName("Error") val Error: String?
    )
    
    data class SearchResult(
        @SerializedName("Title") val Title: String,
        @SerializedName("Year") val Year: String,
        @SerializedName("imdbID") val imdbID: String,
        @SerializedName("Type") val Type: String,
        @SerializedName("Poster") val Poster: String
    )
}