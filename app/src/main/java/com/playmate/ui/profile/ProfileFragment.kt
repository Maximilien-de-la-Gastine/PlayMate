package com.playmate.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.playmate.DataBase
import com.playmate.databinding.FragmentProfileBinding
import com.playmate.ui.authentication.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textViewUserName: TextView = binding.textViewUserName
        val scoreTextView: TextView = binding.scoreTextView

        val userDBHelper = DataBase(requireContext())
        val currentUserName = userDBHelper.getCurrentUsername()
        val userScore = userDBHelper.getUserScore(currentUserName)

        textViewUserName.text = "Nom d'utilisateur : $currentUserName"
        scoreTextView.text = "Score : $userScore"

        val logoutButton: Button = binding.logoutButton
        logoutButton.setOnClickListener {
            logoutUser()
        }

        return root
    }

    private fun logoutUser() {
        val userDBHelper = DataBase(requireContext())
        userDBHelper.clearLoggedInUser()

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
