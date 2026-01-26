package com.afitech.absensi.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLoginBinding.bind(view)

        // Dummy login → Home
        binding.btnLogin.setOnClickListener {
            findNavController()
                .navigate(R.id.action_loginFragment_to_homeFragment)
        }

        // Ke Register
        binding.btnRegister.setOnClickListener {
            findNavController()
                .navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}
