package com.example.bookdy.login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.bookdy.R
import com.example.bookdy.databinding.FragmentUserBinding
import com.example.bookdy.utils.current_username
import com.example.bookdy.utils.global_token
import com.example.bookdy.utils.isLogin
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)

        sharedPref = requireActivity().getSharedPreferences("language", Context.MODE_PRIVATE)

        val language = sharedPref.getString(getString(R.string.language_key), "en") ?: "en"
        if (language == "en") {
            binding.en.setTextColor(resources.getColor(R.color.lang_selected))
        } else {
            binding.vi.setTextColor(resources.getColor(R.color.lang_selected))
        }

        binding.name.text = current_username

        binding.cloud.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_user_to_cloudFragment)
        }

        binding.vi.setOnClickListener {
            with (sharedPref.edit()) {
                putString(getString(R.string.language_key), "vi")
                apply()
            }
            binding.en.setTextColor(resources.getColor(R.color.lang_unselected))
            binding.vi.setTextColor(resources.getColor(R.color.lang_selected))
        }

        binding.en.setOnClickListener {
            with (sharedPref.edit()) {
                putString(getString(R.string.language_key), "en")
                apply()
            }
            binding.en.setTextColor(resources.getColor(R.color.lang_selected))
            binding.vi.setTextColor(resources.getColor(R.color.lang_unselected))
        }

        binding.changePassword.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_user_to_navigation_changepwd)
        }

        binding.logout.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_user_to_navigation_login)
            loginViewModel.token = null
            global_token = ""
            isLogin = false
            current_username = ""
        }

        return binding.root
    }

}