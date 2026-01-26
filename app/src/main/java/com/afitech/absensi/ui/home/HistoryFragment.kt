package com.afitech.absensi.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.FirestoreRepository
import com.afitech.absensi.databinding.FragmentHistoryBinding
import com.afitech.absensi.ui.history.AbsensiAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var binding: FragmentHistoryBinding
    private val adapter = AbsensiAdapter()
    private var listener: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHistoryBinding.bind(view)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            showEmpty()
            return
        }

        listener = FirestoreRepository.observeAbsensiByUser(
            uid = uid,
            onUpdate = { list ->
                adapter.submitList(list)
                if (list.isEmpty()) {
                    showEmpty()
                } else {
                    hideEmpty()
                }
            },
            onError = {
                // 🔥 JANGAN tampilkan toast
                // realtime Firestore error = normal
                showEmpty()
            }
        )
    }

    private fun showEmpty() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun hideEmpty() {
        binding.tvEmpty.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
        listener = null
    }
}
