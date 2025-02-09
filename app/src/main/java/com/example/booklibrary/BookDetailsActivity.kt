package com.example.booklibrary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class BookDetailsActivity : AppCompatActivity() {

    // Variables for book details
    private lateinit var titleTV: TextView
    private lateinit var subtitleTV: TextView
    private lateinit var publisherTV: TextView
    private lateinit var descTV: TextView
    private lateinit var pageTV: TextView
    private lateinit var publisherDateTV: TextView
    private lateinit var previewBtn: Button
    private lateinit var buyBtn: Button
    private lateinit var bookIV: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)

        // Initializing views
        titleTV = findViewById(R.id.idTVTitle)
        subtitleTV = findViewById(R.id.idTVSubTitle)
        publisherTV = findViewById(R.id.idTVpublisher)
        descTV = findViewById(R.id.idTVDescription)
        pageTV = findViewById(R.id.idTVNoOfPages)
        publisherDateTV = findViewById(R.id.idTVPublishDate)
        previewBtn = findViewById(R.id.idBtnPreview)
        buyBtn = findViewById(R.id.idBtnBuy)
        bookIV = findViewById(R.id.idIVbook)

        // Getting book data from intent
        val title = intent.getStringExtra("title") ?: "No Title"
        val subtitle = intent.getStringExtra("subtitle") ?: ""
        val publisher = intent.getStringExtra("publisher") ?: "Unknown Publisher"
        val publishedDate = intent.getStringExtra("publishedDate") ?: "Unknown Date"
        val description = intent.getStringExtra("description") ?: "No Description Available"
        val pageCount = intent.getIntExtra("pageCount", 0)
        val thumbnail = intent.getStringExtra("thumbnail") ?: ""
        val previewLink = intent.getStringExtra("previewLink") ?: ""
        var buyLink = intent.getStringExtra("buyLink") ?: ""

        // Updating UI with book details
        titleTV.text = title
        subtitleTV.text = subtitle
        publisherTV.text = publisher
        publisherDateTV.text = "Published On: $publishedDate"
        descTV.text = description
        pageTV.text = "No of Pages: $pageCount"
        Picasso.get().load(thumbnail).into(bookIV)

        // Preview Button Click
        previewBtn.setOnClickListener {
            if (previewLink.isNotEmpty()) {
                val uri: Uri = Uri.parse(previewLink)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No Preview Link Available", Toast.LENGTH_SHORT).show()
            }
        }

        // Fix: Ensure Buy Link is properly formatted
        if (buyLink.isNotEmpty() && !buyLink.startsWith("http")) {
            buyLink = "https://$buyLink"
        }

        // Buy Button Click
        buyBtn.setOnClickListener {
            if (buyLink.isNotEmpty()) {
                val uri = Uri.parse(buyLink)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No Buy Link Available", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
