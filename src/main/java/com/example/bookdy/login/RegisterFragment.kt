package com.example.bookdy.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.example.bookdy.R
import com.example.bookdy.data.modeljson.User
import com.example.bookdy.databinding.FragmentRegisterBinding
import com.example.bookdy.network.BookApiService
import com.example.bookdy.utils.NetworkListener
import kotlinx.coroutines.launch
import kotlin.math.log

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val networkListener: NetworkListener by lazy { NetworkListener() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                    loginViewModel.networkStatus = status
                }
            }
        }

        binding.buttonRegister.setOnClickListener {
            if (loginViewModel.networkStatus) {
                lifecycleScope.launch {
                    val username = binding.editName.editText?.text.toString()
                    val password = binding.editPassword.editText?.text.toString()
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        val user = User(username, password)
                        registerUser(user)
                    } else {
                        Toast.makeText(requireContext(), resources.getString(R.string.input_require), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                loginViewModel.showNetworkStatus(requireContext())
            }
        }

        binding.buttonLogin.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_register_to_navigation_login)
        }

        return binding.root
    }

    private suspend fun registerUser(user: User) {
        val response = BookApiService.retrofitService.register(user)
        if (response.status == 0) {
            Toast.makeText(requireContext(), resources.getString(R.string.register_success), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), resources.getString(R.string.username_existed), Toast.LENGTH_SHORT).show()
        }
    }

}