package com.github.ikuto0815.compass.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.ikuto0815.compass.databinding.FragmentHomeBinding
import com.github.ikuto0815.compass.helper.Settings
import com.github.ikuto0815.compass.helper.defaults
import com.github.ikuto0815.compass.helper.injectCallback

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()

        val web = binding.web
        val url = Settings.getValue("url", "https://riichi.berlin-mahjong.club")

        url?.apply {
            web.defaults(url)
            web.injectCallback()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}