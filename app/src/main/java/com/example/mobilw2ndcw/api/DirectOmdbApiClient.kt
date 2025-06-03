package com.example.mobilw2ndcw.api

import com.example.mobilw2ndcw.data.Movie
import com.example.mobilw2ndcw.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Client for making direct API calls to OMDb API using HttpURLConnection
 */
class DirectOmdbApiClient {
    
    /**
     * Retrieves movie data from the OMDb API by title
     */
    suspend fun retrieveMovie(searchText: String): String {
        val keyword = searchText.replace(" ", "+")
        val urlString = "https://www.omdbapi.com/?t=${keyword}&apikey=${Constants.OMDb_API_KEY}"
        return fetchFromUrl(urlString)
    }
    
    /**
     * Searches for multiple movies from the OMDb API by search term
     * Returns up to 10 results (first page from API)
     */
    suspend fun searchMovies(searchText: String, page: Int = 1): String {
        val keyword = searchText.replace(" ", "+")
        // Make sure the search is case-insensitive by default in OMDB API
        val urlString = "https://www.omdbapi.com/?s=${keyword}&page=${page}&apikey=${Constants.OMDb_API_KEY}"
        println("Extended search URL: $urlString") // Debug log
        return fetchFromUrl(urlString)
    }
    
    /**
     * Retrieves detailed movie data by IMDb ID
     */
    suspend fun getMovieById(imdbId: String): String {
        val urlString = "https://www.omdbapi.com/?i=${imdbId}&apikey=${Constants.OMDb_API_KEY}"
        return fetchFromUrl(urlString)
    }
    
    /**
     * Generic method to fetch data from a URL
     */
    private suspend fun fetchFromUrl(urlString: String): String {
        val url = URL(urlString)
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.connectTimeout = 15000
        con.readTimeout = 15000

        // collecting all the JSON string
        var stb = StringBuilder()
        // run the code of the launched coroutine in a new thread
        withContext(Dispatchers.IO) {
            try {
                val responseCode = con.responseCode
                val inputStream = if (responseCode >= 400) {
                    con.errorStream
                } else {
                    con.inputStream
                }
                
                val bf = BufferedReader(InputStreamReader(inputStream))
                var line: String? = bf.readLine()
                while (line != null) { // keep reading until no more lines of text
                    stb.append(line + "\n")
                    line = bf.readLine()
                }
                bf.close()
            } catch (e: Exception) {
                e.printStackTrace()
                stb.append("{\"Error\":\"Failed to connect: ${e.message}\"}")
            } finally {
                con.disconnect()
            }
        }
        return stb.toString()
    }

    /**
     * Parses JSON response from the API into a Movie object
     */
    fun parseJSONToMovie(jsonStr: String): Movie? {
        try {
            val jsonObject = JSONObject(jsonStr)

            // Check if the API returned an error
            if (jsonObject.has("Error")) {
                println("Error from API: ${jsonObject.getString("Error")}") // Debug log
                return null
            }

            // Extract movie details
            val imdbID = jsonObject.optString("imdbID", UUID.randomUUID().toString())
            val title = jsonObject.optString("Title", "")
            val year = jsonObject.optString("Year", "")
            val rated = jsonObject.optString("Rated", "")
            val released = jsonObject.optString("Released", "")
            val runtime = jsonObject.optString("Runtime", "")
            val genre = jsonObject.optString("Genre", "")
            val director = jsonObject.optString("Director", "")
            val writer = jsonObject.optString("Writer", "")
            val actors = jsonObject.optString("Actors", "")
            val plot = jsonObject.optString("Plot", "")

            // Create and return a Movie object
            return Movie(
                imdbID = imdbID,
                title = title,
                year = year,
                rated = rated,
                released = released,
                runtime = runtime,
                genre = genre,
                director = director,
                writer = writer,
                actors = actors,
                plot = plot
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Parses basic movie information from search result item
     */
    private fun parseBasicMovieInfo(movieItem: JSONObject): Movie? {
        try {
            val imdbID = movieItem.optString("imdbID", UUID.randomUUID().toString())
            val title = movieItem.optString("Title", "")
            val year = movieItem.optString("Year", "")
            
            // For search results, we only get basic info, so we'll fill in placeholders
            return Movie(
                imdbID = imdbID,
                title = title,
                year = year,
                rated = "N/A",
                released = "N/A",
                runtime = "N/A",
                genre = "N/A",
                director = "N/A",
                writer = "N/A",
                actors = "N/A",
                plot = "N/A"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Parses search results JSON and returns a list of Movie objects
     * This will fetch both basic and detailed information for search results
     */
    suspend fun parseJSONToMovieList(jsonStr: String): List<Movie> {
        try {
            val jsonObject = JSONObject(jsonStr)
            val movies = mutableListOf<Movie>()
            
            // Check if the API returned an error or no results
            if (jsonObject.has("Error") || !jsonObject.has("Search")) {
                println("Search response error or no results: $jsonStr") // Debug log
                return emptyList()
            }
            
            // Extract search results
            val searchArray = jsonObject.getJSONArray("Search")
            println("Found ${searchArray.length()} movies in search results") // Debug log
            
            // First, get all the basic movie info from search results
            val basicMovies = mutableListOf<Movie>()
            for (i in 0 until searchArray.length()) {
                val movieItem = searchArray.getJSONObject(i)
                val movie = parseBasicMovieInfo(movieItem)
                if (movie != null) {
                    basicMovies.add(movie)
                }
            }
            
            // Then, for each movie found, try to get detailed info
            for (basicMovie in basicMovies) {
                try {
                    val movieDetails = getMovieById(basicMovie.imdbID)
                    val detailedMovie = parseJSONToMovie(movieDetails)
                    if (detailedMovie != null) {
                        movies.add(detailedMovie)
                    } else {
                        // If we can't get details, use the basic info
                        movies.add(basicMovie)
                    }
                } catch (e: Exception) {
                    // If fetching details fails, still use the basic movie info
                    movies.add(basicMovie)
                    e.printStackTrace()
                }
                
                // Stop after getting 10 movies (this is the typical page size)
                if (movies.size >= 10) {
                    println("Reached limit of 10 movies, stopping") // Debug log
                    break
                }
            }
            
            return movies
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Parses JSON and returns a formatted string with movie details
     */
    fun parseJSON(stb: StringBuilder): String {
        try {
            val jsonObject = JSONObject(stb.toString())

            // Check if the API returned an error
            if (jsonObject.has("Error")) {
                return "Error: ${jsonObject.getString("Error")}"
            }

            // Extract movie details
            val title = jsonObject.optString("Title", "N/A")
            val year = jsonObject.optString("Year", "N/A")
            val rated = jsonObject.optString("Rated", "N/A")
            val released = jsonObject.optString("Released", "N/A")
            val runtime = jsonObject.optString("Runtime", "N/A")
            val genre = jsonObject.optString("Genre", "N/A")
            val director = jsonObject.optString("Director", "N/A")
            val actors = jsonObject.optString("Actors", "N/A")
            val plot = jsonObject.optString("Plot", "N/A")
            val imdbRating = jsonObject.optString("imdbRating", "N/A")

            // Format the movie details as a string
            return """
                Title: $title
                Year: $year
                Rated: $rated
                Released: $released
                Runtime: $runtime
                Genre: $genre
                Director: $director
                Actors: $actors
                
                Plot: $plot
                
                IMDb Rating: $imdbRating
            """.trimIndent()
        } catch (e: Exception) {
            return "Error parsing movie data: ${e.message}"
        }
    }
} 