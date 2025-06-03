Here's a **README.md** file you can add to your GitHub project to explain your **DirectOmdbApiClient** Kotlin class for accessing the OMDb API:

---

# Movie Search App with OMDb API  
**A Kotlin-based Android application for searching and retrieving movie details using the OMDb API.**  

## 📌 Features  
- Search movies by title  
- Fetch detailed movie information (plot, actors, ratings, etc.)  
- Supports pagination for browsing multiple results  
- Uses `HttpURLConnection` for direct API calls  
- Coroutines for asynchronous network operations  
- JSON parsing to Kotlin `Movie` data class  

## 🔧 Setup  
1. **Get an OMDb API Key**  
   - Visit [OMDb API](http://www.omdbapi.com/apikey.aspx) and request a free API key.  
   - Replace `Constants.OMDb_API_KEY` in your project with your actual key.  

2. **Add Internet Permission**  
   Ensure your `AndroidManifest.xml` includes:  
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```

3. **Usage Example**  
   ```kotlin
   val apiClient = DirectOmdbApiClient()
   
   // Search movies by title (returns first 10 results)
   val searchResults = apiClient.searchMovies("Inception")
   
   // Get detailed info by IMDb ID
   val movieDetails = apiClient.getMovieById("tt1375666")
   
   // Parse JSON response to Movie object
   val movie = apiClient.parseJSONToMovie(jsonResponse)
   ```

## 📂 Project Structure  
- **`DirectOmdbApiClient.kt`** – Handles API requests and JSON parsing.  
- **`Movie.kt`** – Data class for storing movie details.  
- **`Constants.kt`** – Stores the API key and other constants.  

## 🛠️ Dependencies  
- Kotlin Coroutines (`kotlinx-coroutines-core`)  
- Standard Android SDK  

## 📝 Notes  
- The app uses **manual JSON parsing** with `org.json.JSONObject` (no Retrofit/GSON).  
- Network operations run on **background threads** via `Dispatchers.IO`.  
- Errors are logged and returned as JSON strings with `"Error"` keys.  

## 📄 License  
This project is open-source under the **MIT License**.  

