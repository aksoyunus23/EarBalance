package com.earbalance.app.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.earbalance.app.R
import com.earbalance.app.databinding.FragmentHomeBinding
import com.earbalance.app.utils.TimeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) showPermissionRationale()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndRequestPermissions()
        setupObservers()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        viewModel.registerReceiver(requireContext())
        viewModel.loadTodayData()
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterReceiver(requireContext())
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun showPermissionRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("İzinler Gerekli")
            .setMessage("Ear Balance, Bluetooth kulaklığınızı otomatik algılamak için izinlere ihtiyaç duyar.")
            .setPositiveButton("Tekrar İzin Ver") { _, _ -> checkAndRequestPermissions() }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun setupObservers() {
        viewModel.isTracking.observe(viewLifecycleOwner) { updateTrackingUI(it) }
        viewModel.currentEarSide.observe(viewLifecycleOwner) { updateEarSideUI(it) }
        viewModel.elapsedSeconds.observe(viewLifecycleOwner) {
            binding.tvCurrentSessionTime.text = TimeUtils.formatDuration(it)
        }
        viewModel.todayLeftSeconds.observe(viewLifecycleOwner) {
            binding.tvLeftTime.text = TimeUtils.formatDuration(it)
        }
        viewModel.todayRightSeconds.observe(viewLifecycleOwner) {
            binding.tvRightTime.text = TimeUtils.formatDuration(it)
        }
        viewModel.balanceText.observe(viewLifecycleOwner) {
            binding.tvBalanceStatus.text = it
        }
        viewModel.leftPercent.observe(viewLifecycleOwner) { leftPct ->
            val rightPct = 100f - leftPct
            binding.tvLeftPercent.text = "%.0f%%".format(leftPct)
            binding.tvRightPercent.text = "%.0f%%".format(rightPct)
            binding.progressBalance.progress = leftPct.toInt()
        }
    }

    private fun setupButtons() {
        binding.btnLeft.setOnClickListener {
            if (viewModel.isTracking.value == true) showStopConfirmation()
            else viewModel.startTracking("LEFT", requireContext())
        }
        binding.btnBoth.setOnClickListener {
            if (viewModel.isTracking.value == true) showStopConfirmation()
            else viewModel.startTracking("BOTH", requireContext())
        }
        binding.btnRight.setOnClickListener {
            if (viewModel.isTracking.value == true) showStopConfirmation()
            else viewModel.startTracking("RIGHT", requireContext())
        }
        binding.btnStop.setOnClickListener { showStopConfirmation() }
    }

    private fun showStopConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Takibi Durdur")
            .setMessage("Mevcut oturumu durdurmak istiyor musun? Süre kaydedilecek.")
            .setPositiveButton("Durdur") { _, _ -> viewModel.stopTracking(requireContext()) }
            .setNegativeButton("Devam Et", null)
            .show()
    }

    private fun updateTrackingUI(isTracking: Boolean) {
        if (isTracking) {
            binding.cardCurrentSession.visibility = View.VISIBLE
            binding.btnStop.visibility = View.VISIBLE
            binding.tvConnectionStatus.text = "🟢 Takip Ediliyor"
            binding.tvConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_active))
        } else {
            binding.cardCurrentSession.visibility = View.GONE
            binding.btnStop.visibility = View.GONE
            binding.tvConnectionStatus.text = "⚫ Bağlantı Yok"
            binding.tvConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun updateEarSideUI(earSide: String) {
        binding.tvCurrentEar.text = when (earSide) {
            "LEFT" -> "Sol Kulak 👂"
            "RIGHT" -> "Sağ Kulak 👂"
            "BOTH" -> "Her İki Kulak 👂👂"
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
