package com.mc88.pdf88

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PdfAdapter(
    private val pdfList: List<PdfInfo>,
    private val onClick: (PdfInfo) -> Unit
) : RecyclerView.Adapter<PdfAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPdfName: TextView = view.findViewById(R.id.tvPdfName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pdf, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pdf = pdfList[position]
        holder.tvPdfName.text = pdf.name
        holder.itemView.setOnClickListener { onClick(pdf) }
    }

    override fun getItemCount() = pdfList.size
}
