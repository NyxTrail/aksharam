package `in`.digistorm.aksharam.activities.main.fragments

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.databinding.FragmentPrivacyBinding
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

class PrivacyFragment: Fragment() {
    private lateinit var binding: FragmentPrivacyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyBinding.inflate(inflater, container, false)
        binding.textView.movementMethod = LinkMovementMethod.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(setOf(R.id.tabbedViewsFragment, R.id.initialisationScreen))
        )
    }
}