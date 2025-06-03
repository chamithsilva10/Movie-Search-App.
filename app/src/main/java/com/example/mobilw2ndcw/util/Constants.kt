package com.example.mobilw2ndcw.util

/** Constants and utility methods for OMDB API */
object Constants {
    /** OMDB API key */
    const val OMDb_API_KEY = "296660b3"
    
    /** OMDB Base URL */
    const val OMDb_BASE_URL = "https://www.omdbapi.com/"
    
    /** Create URL for title search */
    fun getOmdbUrlWithTitle(title: String): String {
        return "${OMDb_BASE_URL}?t=${title}&apikey=${OMDb_API_KEY}"
    }
    
    /** Create URL for ID search */
    fun getOmdbUrlWithId(imdbId: String): String {
        return "${OMDb_BASE_URL}?i=${imdbId}&apikey=${OMDb_API_KEY}"
    }
    
    /** Create URL for keyword search */
    fun getOmdbSearchUrl(searchTerm: String): String {
        return "${OMDb_BASE_URL}?s=${searchTerm}&apikey=${OMDb_API_KEY}"
    }
} 