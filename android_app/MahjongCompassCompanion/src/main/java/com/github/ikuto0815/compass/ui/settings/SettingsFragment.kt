package com.github.ikuto0815.compass.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.ikuto0815.compass.databinding.FragmentSettingsBinding
import com.github.ikuto0815.compass.helper.Bluetooth
import com.github.ikuto0815.compass.helper.Settings

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val compassAddressTV: TextView = binding.editCompassAddress
        compassAddressTV.text = Settings.getValue("address")

        val statusObserver = Observer<String> { status -> binding.textStatus.text = status }
        val connectButtonObserver = Observer<Boolean> { status -> binding.disconnectButton.isEnabled = status }

        settingsViewModel.statusText.observe(viewLifecycleOwner, statusObserver)
        settingsViewModel.disconnectButton.observe(viewLifecycleOwner, connectButtonObserver)

        binding.disconnectButton.setOnClickListener { _ -> Bluetooth.disconnect() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}