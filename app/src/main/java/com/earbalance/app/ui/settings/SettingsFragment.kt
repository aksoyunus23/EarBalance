package com.earbalance.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.earbalance.app.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupControls()
        setupSpinners()
    }

    private fun setupObservers() {
        viewModel.notificationsEnabled.observe(viewLifecycleOwner) {
            binding.switchNotifications.isChecked = it
        }
        viewModel.alertThreshold.observe(viewLifecycleOwner) { minutes ->
            val idx = listOf(10, 15, 20, 30, 45, 60).indexOf(minutes).coerceAtLeast(0)
            binding.spinnerThreshold.setSelection(idx)
        }
        viewModel.checkInterval.observe(viewLifecycleOwner) { minutes ->
            val idx = listOf(5, 10, 15, 30).indexOf(minutes).coerceAtLeast(0)
            binding.spinnerInterval.setSelection(idx)
        }
    }

    private fun setupControls() {
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
        }
        binding.btnResetData.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Verileri Sıfırla")
                .setMessage("Tüm kullanım geçmişi silinecek. Bu işlem geri alınamaz. Devam etmek istiyor musun?")
                .setPositiveButton("Evet, Sıfırla") { _, _ ->
                    viewModel.resetAllData {
                        requireActivity().runOnUiThread {
                            Snackbar.make(binding.root, "✅ Tüm veriler silindi", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }
    }

    private fun setupSpinners() {
        val thresholdOptions = listOf("10 dakika", "15 dakika", "20 dakika", "30 dakika", "45 dakika", "60 dakika")
        val thresholdValues = listOf(10, 15, 20, 30, 45, 60)
        val thresholdAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, thresholdOptions)
        binding.spinnerThreshold.adapter = thresholdAdapter
        binding.spinnerThreshold.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setAlertThreshold(thresholdValues[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val intervalOptions = listOf("5 dakika", "10 dakika", "15 dakika", "30 dakika")
        val intervalValues = listOf(5, 10, 15, 30)
        val intervalAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, intervalOptions)
        binding.spinnerInterval.adapter = intervalAdapter
        binding.spinnerInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setCheckInterval(intervalValues[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
