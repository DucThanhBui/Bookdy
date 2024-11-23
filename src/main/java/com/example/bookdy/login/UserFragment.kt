package com.example.bookdy.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.bookdy.R
import com.example.bookdy.databinding.FragmentUserBinding
import com.example.bookdy.utils.current_username
import com.example.bookdy.utils.isLogin

class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)

        binding.name.text = current_username

        binding.changePassword.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_user_to_navigation_changepwd)
        }

        binding.logout.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_user_to_navigation_login)
            loginViewModel.token = null
            isLogin = false
            current_username = ""
        }

        return binding.root
    }

}