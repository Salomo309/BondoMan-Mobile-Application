package com.example.bondoman.ui.graph

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bondoman.MainActivity
import com.example.bondoman.databinding.FragmentGraphBinding
import com.example.bondoman.models.TransactionSummary
import com.example.bondoman.ui.transaction.TransactionViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlin.math.roundToInt

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionViewModel = (requireActivity() as MainActivity).getTransactionViewModel()

        transactionViewModel.transactionSummary.observe(viewLifecycleOwner) {summaries ->
            if (summaries.isEmpty()) {
                binding.graphShadeOverlay.visibility = View.VISIBLE
            } else {
                createPieChart()
                binding.graphShadeOverlay.visibility = View.GONE
            }
        }
    }

    private fun createPieChart() {
        val pieChart = binding.pieChart

        val entries = ArrayList<PieEntry>()
        val summaryPercentage = getTransactionSummaryPercentage()
        if (summaryPercentage[0] != 0f) {
            entries.add(PieEntry(summaryPercentage[0], "Pemasukan"))
        }
        if (summaryPercentage[1] != 0f) {
            entries.add(PieEntry(summaryPercentage[1], "Pengeluaran"))
        }

        val dataSet = PieDataSet(entries, "Pie Chart")
        dataSet.setColors(Color.parseColor("#87A922"), Color.parseColor("#FDA403"))

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))
        pieChart.setUsePercentValues(true)
        dataSet.valueTextSize = 18f
        pieChart.data = pieData

        pieChart.legend.isEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.invalidate()
    }

    private fun getTransactionSummaryPercentage() : FloatArray {
        val summaries = transactionViewModel.transactionSummary.value!!

        if (summaries.size == 1) {
            return if (summaries[0].category == "Pemasukan") {
                floatArrayOf(100f, 0f)
            } else {
                floatArrayOf(0f, 100f)
            }
        } else {
            var idxPemasukan = 0
            var idxPengeluaran = 1
            if (summaries[0].category != "Pemasukan") {
                idxPemasukan = 1
                idxPengeluaran = 0
            }

            val totalSum = summaries[0].totalAmount + summaries[1].totalAmount
            val percentPemasukan =  (((summaries[idxPemasukan].totalAmount)/totalSum) * 1000).roundToInt() / 1000f
            val percentPengeluaran =  (((summaries[idxPengeluaran].totalAmount)/totalSum) * 1000).roundToInt() / 1000f

            return floatArrayOf(percentPemasukan, percentPengeluaran)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}