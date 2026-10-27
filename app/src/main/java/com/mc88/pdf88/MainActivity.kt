package com.mc88.pdf88

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PdfAdapter
    private val pdfList = mutableListOf<PdfInfo>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private var currentThemeIndex = 0

    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val name = getFileName(it)
            val prefs = getSharedPreferences("pdf88_prefs", MODE_PRIVATE)
            val savedPdfs = prefs.getStringSet("pdfs", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            savedPdfs.add("$name|$it")
            prefs.edit().putStringSet("pdfs", savedPdfs).apply()
            loadPdfs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        val btnAddPdf = findViewById<Button>(R.id.btnAddPdf)
        val btnTheme = findViewById<ImageButton>(R.id.btnTheme)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = PdfAdapter(pdfList) { pdf ->
            val intent = Intent(this, ReaderActivity::class.java)
            intent.putExtra("pdfUri", pdf.uriString)
            intent.putExtra("pdfName", pdf.name)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        btnAddPdf.setOnClickListener {
            pickPdfLauncher.launch(arrayOf("application/pdf"))
        }

        btnTheme.setOnClickListener {
            currentThemeIndex = (currentThemeIndex + 1) % 3
            getSharedPreferences("pdf88_prefs", MODE_PRIVATE).edit().putInt("theme_index", currentThemeIndex).apply()
            recreate()
        }

        loadPdfs()
    }

    private fun loadPdfs() {
        val prefs = getSharedPreferences("pdf88_prefs", MODE_PRIVATE)
        currentThemeIndex = prefs.getInt("theme_index", 0)
        val savedPdfs = prefs.getStringSet("pdfs", mutableSetOf()) ?: mutableSetOf()
        
        pdfList.clear()
        for (item in savedPdfs) {
            val parts = item.split("|")
            if (parts.size == 2) {
                pdfList.add(PdfInfo(parts[0], parts[1]))
            }
        }
        pdfList.sortBy { it.name }
        
        adapter.notifyDataSetChanged()
        
        if (pdfList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("pdf88_prefs", MODE_PRIVATE)
        currentThemeIndex = prefs.getInt("theme_index", 0)
        when (currentThemeIndex) {
            1 -> setTheme(R.style.Theme_PDF88_Dark)
            2 -> setTheme(R.style.Theme_PDF88_HC)
            else -> setTheme(R.style.Theme_PDF88)
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "Document"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        return name
    }
}
