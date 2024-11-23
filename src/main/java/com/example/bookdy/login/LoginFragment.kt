package com.example.bookdy.login

import android.content.Context
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
import androidx.navigation.fragment.findNavController
import com.example.bookdy.R
import com.example.bookdy.databinding.FragmentLoginBinding
import com.example.bookdy.utils.NetworkListener
import com.example.bookdy.utils.current_username
import com.example.bookdy.utils.isLogin
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val networkListener: NetworkListener by lazy { NetworkListener() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                    loginViewModel.networkStatus = status
                }
            }
        }

        binding.editUsername.editText?.setText(getLatestUsername())

        binding.buttonLogin.setOnClickListener {
            val username = binding.editUsername.editText?.text.toString()
            val password = binding.editPassword.editText?.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                if (loginViewModel.networkStatus) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val res = loginViewModel.loginUser(username, password)
                        if (res) {
                            Toast.makeText(requireContext(), resources.getText(R.string.login_success), Toast.LENGTH_SHORT).show()
                            current_username = username
                            isLogin = true
                            putLatestUsername(username)
                            //Navigation.findNavController(requireView()).popBackStack()
                            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_login_to_navigation_user)
                        } else {
                            Toast.makeText(requireContext(), resources.getText(R.string.login_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    loginViewModel.showNetworkStatus(requireContext())
                }
            } else {
                Toast.makeText(requireContext(), resources.getText(R.string.input_require), Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonRegister.setOnClickListener {
            if (loginViewModel.networkStatus) {
                Navigation.findNavController(requireView()).navigate(R.id.action_navigation_login_to_navigation_register)
            } else {
                loginViewModel.showNetworkStatus(requireContext())
            }
        }

        return binding.root
    }

    private fun putLatestUsername(username: String) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(LASTEST_USERNAME_KEY, username)
            apply()
        }
    }

    private fun getLatestUsername(): String {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: run {
            return ""
        }
        return sharedPref.getString(LASTEST_USERNAME_KEY, "") ?: ""
    }

    companion object {
        const val LASTEST_USERNAME_KEY = "lastest_username_key"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}