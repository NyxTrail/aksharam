package `in`.digistorm.aksharam.activities.main

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.databinding.FragmentPrivacyBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment

class PrivacyFragment: Fragment() {
    private lateinit var fragmentPrivacyBinding: FragmentPrivacyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentPrivacyBinding = FragmentPrivacyBinding.inflate(inflater, container, false)
        fragmentPrivacyBinding.textView.text = HtmlCompat.fromHtml(getString(R.string.privacy_text), HtmlCompat.FROM_HTML_MODE_LEGACY)
        return fragmentPrivacyBinding.root
    }
}