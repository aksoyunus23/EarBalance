package com.earbalance.app.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.earbalance.app.R
import com.earbalance.app.data.database.DailySummary
import com.earbalance.app.data.database.UsageSession
import com.earbalance.app.databinding.FragmentStatisticsBinding
import com.earbalance.app.utils.TimeUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.apply {
                isEnabled = true
                textColor = Color.WHITE
            }
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                gridColor = Color.parseColor("#444444")
                setDrawAxisLine(false)
            }
            axisLeft.apply {
                textColor = Color.WHITE
                gridColor = Color.parseColor("#444444")
                setDrawAxisLine(false)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            setTouchEnabled(false)
        }
    }

    private fun setupRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupObservers() {
        viewModel.last7Days.observe(viewLifecycleOwner) { days ->
            updateChart(days)
            updateSummary(days)
        }
        viewModel.allSessions.observe(viewLifecycleOwner) { sessions ->
            binding.rvHistory.adapter = SessionAdapter(sessions)
        }
    }

    private fun updateChart(days: List<DailySummary>) {
        if (days.isEmpty()) return
        val sortedDays = days.sortedBy { it.date }
        val labels = sortedDays.map { TimeUtils.formatDateShort(it.date) }
        val leftEntries = sortedDays.mapIndexed { i, d -> BarEntry(i.toFloat(), (d.leftSeconds + d.bothSeconds) / 60f) }
        val rightEntries = sortedDays.mapIndexed { i, d -> BarEntry(i.toFloat(), (d.rightSeconds + d.bothSeconds) / 60f) }
        val leftDataSet = BarDataSet(leftEntries, "Sol Kulak").apply {
            color = Color.parseColor("#4A90E2")
            setDrawValues(false)
        }
        val rightDataSet = BarDataSet(rightEntries, "Sağ Kulak").apply {
            color = Color.parseColor("#E2784A")
            setDrawValues(false)
        }
        val barData = BarData(leftDataSet, rightDataSet).apply { barWidth = 0.3f }
        binding.barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.setCenterAxisLabels(true)
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = barData.getGroupWidth(0.3f, 0.05f) * sortedDays.size
            groupBars(0f, 0.3f, 0.05f)
            invalidate()
        }
    }

    private fun updateSummary(days: List<DailySummary>) {
        val totalLeft = days.sumOf { it.leftSeconds + it.bothSeconds }
        val totalRight = days.sumOf { it.rightSeconds + it.bothSeconds }
        val total = days.sumOf { it.totalSeconds }
        binding.tvWeeklyTotal.text = "Toplam: ${TimeUtils.formatDuration(total)}"
        binding.tvWeeklyLeft.text = "Sol: ${TimeUtils.formatDuration(totalLeft)}"
        binding.tvWeeklyRight.text = "Sağ: ${TimeUtils.formatDuration(totalRight)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SessionAdapter(private val sessions: List<UsageSession>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tvDate: android.widget.TextView = view.findViewById(R.id.tv_session_date)
        val tvEar: android.widget.TextView = view.findViewById(R.id.tv_session_ear)
        val tvDuration: android.widget.TextView = view.findViewById(R.id.tv_session_duration)
        val tvDevice: android.widget.TextView = view.findViewById(R.id.tv_session_device)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.tvDate.text = TimeUtils.formatDateDisplay(session.date)
        holder.tvEar.text = when (session.earSide) {
            "LEFT" -> "👂 Sol"
            "RIGHT" -> "👂 Sağ"
            "BOTH" -> "👂👂 Her İkisi"
            else -> session.earSide
        }
        holder.tvDuration.text = TimeUtils.formatDuration(session.durationSeconds)
        holder.tvDevice.text = if (session.deviceName.isNotEmpty()) session.deviceName else "Manuel"
    }

    override fun getItemCount() = sessions.size
    }
