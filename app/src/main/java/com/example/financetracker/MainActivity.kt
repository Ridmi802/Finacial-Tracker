package com.example.financetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetracker.data.Transaction
import com.example.financetracker.data.TransactionType
import com.example.financetracker.databinding.ActivityMainBinding
import com.example.financetracker.databinding.DialogAddTransactionBinding
import com.example.financetracker.databinding.DialogReportsBinding
import com.example.financetracker.databinding.DialogSettingsBinding
import com.example.financetracker.databinding.DialogPieChartBinding
import com.example.financetracker.ui.TransactionAdapter
import com.example.financetracker.ui.TransactionViewModel
import com.example.financetracker.ui.TransactionViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (application as FinanceTrackerApplication).repository
        )
    }
    private val adapter = TransactionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Drawer setup
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_dashboard -> {
                    Toast.makeText(this, "Dashboard selected", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_transactions -> {
                    Toast.makeText(this, "Transactions selected", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_reports -> {
                    showReportsDialog()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_settings -> {
                    showSettingsDialog()
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_reports -> {
                showReportsDialog()
                true
            }
            R.id.menu_settings -> {
                showSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showReportsDialog() {
        val dialogBinding = DialogReportsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnMonthlyReport.setOnClickListener {
            showMonthlyReport()
            dialog.dismiss()
        }

        dialogBinding.btnCategoryReport.setOnClickListener {
            showCategoryReport()
            dialog.dismiss()
        }

        dialogBinding.btnIncomeVsExpense.setOnClickListener {
            showIncomeVsExpenseReport()
            dialog.dismiss()
        }

        dialogBinding.btnTrendAnalysis.setOnClickListener {
            showTrendAnalysis()
            dialog.dismiss()
        }

        dialogBinding.btnPieChart.setOnClickListener {
            showPieChart()
            dialog.dismiss()
        }

        dialogBinding.btnCancelReport.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMonthlyReport() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            val transactions = viewModel.getTransactionsByDateRange(startDate, endDate)
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val balance = income - expenses

            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val message = """
                Monthly Report for ${dateFormat.format(startDate)}
                
                Total Income: ${formatCurrency(income)}
                Total Expenses: ${formatCurrency(expenses)}
                Balance: ${formatCurrency(balance)}
                
                Top Categories:
                ${getTopCategories(transactions)}
            """.trimIndent()

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Monthly Report")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showCategoryReport() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            val transactions = viewModel.getTransactionsByDateRange(startDate, endDate)
            val categoryTotals = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            val message = buildString {
                append("Category-wise Report\n\n")
                categoryTotals.forEach { (category, total) ->
                    append("$category: ${formatCurrency(total)}\n")
                }
            }

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Category Report")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showIncomeVsExpenseReport() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            val transactions = viewModel.getTransactionsByDateRange(startDate, endDate)
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val savingsRate = if (income > 0) ((income - expenses) / income) * 100 else 0.0

            val message = """
                Income vs Expense Report
                
                Total Income: ${formatCurrency(income)}
                Total Expenses: ${formatCurrency(expenses)}
                Net Savings: ${formatCurrency(income - expenses)}
                Savings Rate: ${String.format("%.2f", savingsRate)}%
                
                ${if (savingsRate < 0) "Warning: You are spending more than you earn!" else ""}
            """.trimIndent()

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Income vs Expense")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showTrendAnalysis() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.add(Calendar.MONTH, -6)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            val transactions = viewModel.getTransactionsByDateRange(startDate, endDate)
            val monthlyData = transactions.groupBy {
                val cal = Calendar.getInstance()
                cal.time = it.date
                "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
            }.mapValues { (_, monthTransactions) ->
                val income = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expenses = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                Pair(income, expenses)
            }.toList().sortedBy { it.first }

            val message = buildString {
                append("6-Month Trend Analysis\n\n")
                monthlyData.forEach { (month, data) ->
                    append("$month:\n")
                    append("  Income: ${formatCurrency(data.first)}\n")
                    append("  Expenses: ${formatCurrency(data.second)}\n")
                    append("  Net: ${formatCurrency(data.first - data.second)}\n\n")
                }
            }

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Trend Analysis")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showPieChart() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            val transactions = viewModel.getTransactionsByDateRange(startDate, endDate)
            val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
            
            if (expenseTransactions.isEmpty()) {
                Toast.makeText(this@MainActivity, "No expense data available for this month", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val categoryTotals = expenseTransactions
                .groupBy { it.category }
                .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            val dialogBinding = DialogPieChartBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(this@MainActivity)
                .setView(dialogBinding.root)
                .create()

            // Create pie chart entries
            val entries = categoryTotals.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }

            val dataSet = PieDataSet(entries, "Expense Categories")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = android.graphics.Color.WHITE

            val data = PieData(dataSet)
            dialogBinding.pieChart.data = data
            dialogBinding.pieChart.description.isEnabled = false
            dialogBinding.pieChart.setEntryLabelTextSize(12f)
            dialogBinding.pieChart.setEntryLabelColor(android.graphics.Color.BLACK)
            dialogBinding.pieChart.legend.textSize = 12f
            dialogBinding.pieChart.animateY(1000)
            dialogBinding.pieChart.invalidate()

            dialogBinding.btnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun getTopCategories(transactions: List<Transaction>): String {
        return transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .joinToString("\n") { (category, amount) ->
                "$category: ${formatCurrency(amount)}"
            }
    }

    private fun showSettingsDialog() {
        val dialogBinding = DialogSettingsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Settings")
            .setView(dialogBinding.root)
            .create()

        // Load current settings
        val sharedPrefs = getSharedPreferences("FinanceTrackerPrefs", MODE_PRIVATE)
        dialogBinding.switchDarkMode.isChecked = sharedPrefs.getBoolean("dark_mode", false)
        dialogBinding.switchNotifications.isChecked = sharedPrefs.getBoolean("notifications", true)
        dialogBinding.switchCurrencyFormat.isChecked = sharedPrefs.getBoolean("use_local_currency", true)

        dialogBinding.btnSaveSettings.setOnClickListener {
            with(sharedPrefs.edit()) {
                putBoolean("dark_mode", dialogBinding.switchDarkMode.isChecked)
                putBoolean("notifications", dialogBinding.switchNotifications.isChecked)
                putBoolean("use_local_currency", dialogBinding.switchCurrencyFormat.isChecked)
                apply()
            }
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogBinding.btnCancelSettings.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun exportData() {
        lifecycleScope.launch {
            try {
                val transactions = viewModel.getAllTransactions()
                val csvData = StringBuilder()
                csvData.append("ID,Title,Amount,Type,Category,Description,Date\n")
                
                transactions.forEach { transaction ->
                    csvData.append("${transaction.id},${transaction.title},${transaction.amount},${transaction.type},${transaction.category},${transaction.description},${transaction.date}\n")
                }
                
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/csv"
                    putExtra(Intent.EXTRA_TITLE, "finance_tracker_export_${System.currentTimeMillis()}.csv")
                }
                startActivityForResult(intent, EXPORT_REQUEST_CODE)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Finance Tracker")
            .setMessage("Finance Tracker v1.0\n\n" +
                    "A simple app to track your income and expenses.\n\n" +
                    "Features:\n" +
                    "• Track income and expenses\n" +
                    "• Categorize transactions\n" +
                    "• View spending analysis\n" +
                    "• Backup and restore data\n" +
                    "• Export to CSV\n\n" +
                    "© 2024 Finance Tracker")
            .setPositiveButton("OK", null)
            .setNeutralButton("Rate App") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=$packageName")
                }
                startActivity(intent)
            }
            .show()
    }

    companion object {
        private const val EXPORT_REQUEST_CODE = 1003
    }

    private fun setupRecyclerView() {
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactions ->
                adapter.submitList(transactions)
            }
        }

        lifecycleScope.launch {
            viewModel.totalIncome.collectLatest { income ->
                binding.tvIncome.text = formatCurrency(income)
            }
        }

        lifecycleScope.launch {
            viewModel.totalExpenses.collectLatest { expenses ->
                binding.tvExpenses.text = formatCurrency(expenses)
            }
        }

        // Calculate and display balance
        lifecycleScope.launch {
            viewModel.totalIncome.collectLatest { income ->
                viewModel.totalExpenses.collectLatest { expenses ->
                    val balance = income - expenses
                    binding.tvBalance.text = formatCurrency(balance)
                }
            }
        }
    }

    private fun showAddTransactionDialog(transaction: Transaction? = null) {
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Setup category dropdown
        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Shopping", "Health", "Education", "Other")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.actvCategory.setAdapter(categoryAdapter)

        // Set default selection for transaction type
        dialogBinding.toggleGroup.check(if (transaction?.type == TransactionType.INCOME) R.id.btnIncome else R.id.btnExpense)

        // Pre-fill fields if editing
        transaction?.let {
            dialogBinding.etTitle.setText(it.title)
            dialogBinding.etAmount.setText(it.amount.toString())
            dialogBinding.actvCategory.setText(it.category, false)
            dialogBinding.etDescription.setText(it.description)
        }

        // Set click listeners
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val amountStr = dialogBinding.etAmount.text.toString().trim()
            val category = dialogBinding.actvCategory.text.toString().trim()
            val description = dialogBinding.etDescription.text.toString().trim()
            val type = if (dialogBinding.toggleGroup.checkedButtonId == R.id.btnIncome) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }

            // Validate inputs
            when {
                title.isEmpty() -> {
                    dialogBinding.etTitle.error = "Title is required"
                    return@setOnClickListener
                }
                amountStr.isEmpty() -> {
                    dialogBinding.etAmount.error = "Amount is required"
                    return@setOnClickListener
                }
                amountStr.toDoubleOrNull() == null -> {
                    dialogBinding.etAmount.error = "Invalid amount"
                    return@setOnClickListener
                }
                category.isEmpty() -> {
                    dialogBinding.actvCategory.error = "Category is required"
                    return@setOnClickListener
                }
                !categories.contains(category) -> {
                    dialogBinding.actvCategory.error = "Invalid category"
                    return@setOnClickListener
                }
            }

            val amount = amountStr.toDouble()

            val newTransaction = transaction?.copy(
                title = title,
                amount = amount,
                category = category,
                type = type,
                description = description
            ) ?: Transaction(
                title = title,
                amount = amount,
                category = category,
                type = type,
                description = description
            )

            if (transaction == null) {
                viewModel.addTransaction(newTransaction)
                Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.updateTransaction(newTransaction)
                Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
    }
}