package com.example.booklibrary

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
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
    private lateinit var searchBarLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingPB = findViewById(R.id.idLoadingPB)
        searchEdt = findViewById(R.id.idEdtSearchBooks)
        searchBtn = findViewById(R.id.idBtnSearch)
        searchBarLayout = findViewById(R.id.idLLsearch)
        recyclerView = findViewById(R.id.idRVBooks)

        searchBtn.setOnClickListener {
            val query = searchEdt.text.toString().trim()
            if (query.isEmpty()) {
                searchEdt.error = "Please enter a search query"
            } else {
                loadingPB.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                getBooksData(query)
                moveSearchBarToTop()
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

                    val authorsArrayList = ArrayList<String>()
                    val authorsArray = volumeObj.optJSONArray("authors")
                    authorsArray?.let {
                        for (j in 0 until it.length()) {
                            authorsArrayList.add(it.optString(j))
                        }
                    }

                    val imageLinks = volumeObj.optJSONObject("imageLinks")
                    var thumbnail = imageLinks?.optString("thumbnail", "") ?: ""
                    if (thumbnail.startsWith("http://")) {
                        thumbnail = thumbnail.replace("http://", "https://")
                    }

                    val saleInfoObj = itemsObj.optJSONObject("saleInfo")
                    var buyLink = saleInfoObj?.optString("buyLink", "") ?: ""
                    if (buyLink.isNotEmpty() && !buyLink.startsWith("http")) {
                        buyLink = "https://$buyLink"
                    }

                    val bookInfo = BookRVModal(
                        title, subtitle, authorsArrayList, publisher, publishedDate,
                        description, pageCount, thumbnail, previewLink, infoLink, buyLink
                    )
                    booksList.add(bookInfo)
                }

                val adapter = BookRVAdapter(booksList, this@MainActivity)
                val layoutManager = GridLayoutManager(this, 3)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = adapter
                recyclerView.visibility = View.VISIBLE

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

    private fun moveSearchBarToTop() {
        val layoutParams = searchBarLayout.layoutParams as RelativeLayout.LayoutParams
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT)
        layoutParams.topMargin = 32
        searchBarLayout.layoutParams = layoutParams
    }
}
