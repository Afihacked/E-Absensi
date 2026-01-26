package com.afitech.absensi.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afitech.absensi.data.model.Absensi
import com.afitech.absensi.databinding.ItemAbsensiBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AbsensiAdapter :
    ListAdapter<Absensi, AbsensiAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Absensi>() {

            // identitas item (stabil untuk realtime)
            override fun areItemsTheSame(oldItem: Absensi, newItem: Absensi): Boolean {
                return oldItem.createdAt == newItem.createdAt
            }

            // isi item berubah atau tidak
            override fun areContentsTheSame(oldItem: Absensi, newItem: Absensi): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(
        private val binding: ItemAbsensiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Absensi) {
            binding.tvNama.text = item.nama
            binding.tvLokasi.text = item.lokasi
            binding.tvWaktu.text = formatWaktu(item.createdAt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAbsensiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ===============================
    // FORMAT WAKTU MANUSIAWI
    // ===============================
    private fun formatWaktu(createdAt: Long): String {
        if (createdAt <= 0L) return "-"

        val date = Date(createdAt)
        val calItem = Calendar.getInstance().apply { time = date }
        val calNow = Calendar.getInstance()

        val sdfJam = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        val sdfTanggal = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        return when {
            isSameDay(calItem, calNow) ->
                "Hari ini • ${sdfJam.format(date)}"

            isYesterday(calItem, calNow) ->
                "Kemarin • ${sdfJam.format(date)}"

            else ->
                "${sdfTanggal.format(date)} • ${sdfJam.format(date)}"
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(a: Calendar, b: Calendar): Boolean {
        b.add(Calendar.DAY_OF_YEAR, -1)
        val result = isSameDay(a, b)
        b.add(Calendar.DAY_OF_YEAR, +1) // restore
        return result
    }
}
