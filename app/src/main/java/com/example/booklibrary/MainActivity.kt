package com.example.booklibrary

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {

    private lateinit var booksList: ArrayList<BookRVModal>
    private lateinit var loadingPB: ProgressBar
    private lateinit var searchEdt: EditText
    private lateinit var searchBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initializing UI elements
        loadingPB = findViewById(R.id.idLoadingPB)
        searchEdt = findViewById(R.id.idEdtSearchBooks)
        searchBtn = findViewById(R.id.idBtnSearch)

        // Search button click listener
        searchBtn.setOnClickListener {
            val query = searchEdt.text.toString().trim()
            if (query.isEmpty()) {
                searchEdt.error = "Please enter a search query"
            } else {
                loadingPB.visibility = View.VISIBLE
                getBooksData(query)
            }
        }
    }

    private fun getBooksData(searchQuery: String) {
        booksList = ArrayList()
        val url = "https://www.googleapis.com/books/v1/volumes?q=$searchQuery"

        val queue = Volley.newRequestQueue(this@MainActivity)
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            loadingPB.visibility = View.GONE
            booksList.clear()

            try {
                val itemsArray = response.getJSONArray("items")
                for (i in 0 until itemsArray.length()) {
                    val itemsObj = itemsArray.getJSONObject(i)
                    val volumeObj = itemsObj.getJSONObject("volumeInfo")

                    val title = volumeObj.optString("title", "No Title")
                    val subtitle = volumeObj.optString("subtitle", "")
                    val publisher = volumeObj.optString("publisher", "Unknown Publisher")
                    val publishedDate = volumeObj.optString("publishedDate", "Unknown Date")
                    val description = volumeObj.optString("description", "No Description")
                    val pageCount = volumeObj.optInt("pageCount", 0)
                    val previewLink = volumeObj.optString("previewLink", "")
                    val infoLink = volumeObj.optString("infoLink", "")

                    // Handle authors array safely
                    val authorsArrayList = ArrayList<String>()
                    val authorsArray = volumeObj.optJSONArray("authors")
                    authorsArray?.let {
                        for (j in 0 until it.length()) {
                            authorsArrayList.add(it.optString(j))
                        }
                    }

                    // Get image links safely
                    val imageLinks = volumeObj.optJSONObject("imageLinks")
                    var thumbnail = imageLinks?.optString("thumbnail", "") ?: ""

                    // Convert HTTP to HTTPS
                    if (thumbnail.startsWith("http://")) {
                        thumbnail = thumbnail.replace("http://", "https://")
                    }

                    // Handle Buy Link safely
                    val saleInfoObj = itemsObj.optJSONObject("saleInfo")
                    var buyLink = saleInfoObj?.optString("buyLink", "") ?: ""

                    // Ensure buyLink starts with HTTPS
                    if (buyLink.isNotEmpty() && !buyLink.startsWith("http")) {
                        buyLink = "https://$buyLink"
                    }

                    // Creating Book Object
                    val bookInfo = BookRVModal(
                        title, subtitle, authorsArrayList, publisher, publishedDate,
                        description, pageCount, thumbnail, previewLink, infoLink, buyLink
                    )

                    booksList.add(bookInfo)
                }

                // Update RecyclerView after parsing all books
                val adapter = BookRVAdapter(booksList, this@MainActivity)
                val layoutManager = GridLayoutManager(this, 3)
                val mRecyclerView = findViewById<RecyclerView>(R.id.idRVBooks)

                mRecyclerView.layoutManager = layoutManager
                mRecyclerView.adapter = adapter

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error parsing data", Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            loadingPB.visibility = View.GONE
            Toast.makeText(this@MainActivity, "No books found..", Toast.LENGTH_SHORT).show()
        })

        queue.add(request)
    }
}
