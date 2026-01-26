package com.afitech.absensi.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnRegister).setOnClickListener {
            // nanti logic daftar akun
            findNavController().navigateUp() // balik ke login dulu
        }
    }
}
