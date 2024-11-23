package com.example.bookdy.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.example.bookdy.R
import com.example.bookdy.databinding.FragmentChangePasswordBinding
import com.example.bookdy.utils.NetworkListener
import kotlinx.coroutines.launch
import kotlin.math.log

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val networkListener: NetworkListener by lazy { NetworkListener() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                    loginViewModel.networkStatus = status
                }
            }
        }

        binding.btConfirm.setOnClickListener {
            val oldPassword = binding.oldPassword.editText?.text.toString()
            val newPassword = binding.newPassword.editText?.text.toString()
            val newPassword2 = binding.newPassword2.editText?.text.toString()

            if (oldPassword.isNullOrEmpty() || newPassword.isNullOrEmpty() || newPassword2.isNullOrEmpty()) {
                Toast.makeText(requireContext(), resources.getString(R.string.input_require), Toast.LENGTH_SHORT).show()
            } else {
                if (newPassword == newPassword2) {
                    if (loginViewModel.networkStatus) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            if (loginViewModel.doChangePassword(oldPassword, newPassword)) {
                                Toast.makeText(requireContext(), resources.getString(R.string.change_password_success), Toast.LENGTH_SHORT).show()
                                Navigation.findNavController(requireView()).navigate(R.id.action_navigation_changepwd_to_navigation_user)
                            } else {
                                Toast.makeText(requireContext(), resources.getString(R.string.server_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        loginViewModel.showNetworkStatus(requireContext())
                    }
                } else {
                    Toast.makeText(requireContext(), resources.getString(R.string.password_not_match), Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }
}