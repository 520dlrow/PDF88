package com.mc88.pdf88

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReaderActivity : AppCompatActivity() {

    private var pdfRenderer: PdfRenderer? = null
    private var currentPage = 0
    private var pageCount = 0
    private lateinit var ivPdfPage: ImageView
    private lateinit var pdfUriString: String
    private lateinit var pdfName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("pdf88_prefs", MODE_PRIVATE)
        when (prefs.getInt("theme_index", 0)) {
            1 -> setTheme(R.style.Theme_PDF88_Dark)
            2 -> setTheme(R.style.Theme_PDF88_HC)
            else -> setTheme(R.style.Theme_PDF88)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        ivPdfPage = findViewById(R.id.ivPdfPage)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnBookmark = findViewById<Button>(R.id.btnBookmark)
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnZoomIn = findViewById<Button>(R.id.btnZoomIn)
        val btnZoomOut = findViewById<Button>(R.id.btnZoomOut)

        pdfUriString = intent.getStringExtra("pdfUri") ?: ""
        pdfName = intent.getStringExtra("pdfName") ?: ""

        try {
            val uri = Uri.parse(pdfUriString)
            val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            if (fileDescriptor != null) {
                pdfRenderer = PdfRenderer(fileDescriptor)
                pageCount = pdfRenderer?.pageCount ?: 0
                
                val savedPage = prefs.getInt("bm_$pdfUriString", 0)
                currentPage = if (savedPage < pageCount) savedPage else 0
                
                showPage(currentPage)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening PDF", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnBack.setOnClickListener { finish() }

        btnBookmark.setOnClickListener {
            prefs.edit().putInt("bm_$pdfUriString", currentPage).apply()
            Toast.makeText(this, "Page ${currentPage + 1} sauvegardée!", Toast.LENGTH_SHORT).show()
        }

        btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                showPage(currentPage)
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < pageCount - 1) {
                currentPage++
                showPage(currentPage)
            }
        }

        btnZoomIn.setOnClickListener {
            ivPdfPage.scaleX += 0.2f
            ivPdfPage.scaleY += 0.2f
        }

        btnZoomOut.setOnClickListener {
            if (ivPdfPage.scaleX > 0.6f) {
                ivPdfPage.scaleX -= 0.2f
                ivPdfPage.scaleY -= 0.2f
            }
        }
    }

    private fun showPage(pageIndex: Int) {
        try {
            val page = pdfRenderer?.openPage(pageIndex) ?: return
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            val canvas = android.graphics.Canvas(bitmap)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            ivPdfPage.setImageBitmap(bitmap)
            page.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer?.close()
    }
}
