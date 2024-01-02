package com.playmate.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.playmate.R
import com.playmate.DataBase
import com.playmate.databinding.FragmentProfileBinding
import com.playmate.ui.authentication.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logoutButton: Button = binding.root.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            // Déconnexion de l'utilisateur
            logoutUser()
        }
        return root
    }

    private fun logoutUser() {
        val userDBHelper = DataBase(requireContext())
        userDBHelper.clearLoggedInUser()

        // Redirection vers l'écran de connexion (LoginActivity)
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}