package com.example.bookdy.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.bookdy.data.modeljson.User
import com.example.bookdy.databinding.FragmentLoginBinding
import com.example.bookdy.network.BookApiService
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.buttonLogin.setOnClickListener {
            val username = binding.editUsername.editText?.text.toString()
            val password = binding.editPassword.editText?.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val listBooks = BookApiService.retrofitService.getAllBooks(User(username, password))
                }
            }
        }



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}