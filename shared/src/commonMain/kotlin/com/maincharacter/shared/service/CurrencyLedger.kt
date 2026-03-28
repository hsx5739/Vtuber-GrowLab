package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CurrencyLedger {
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions
    
    private val _transactionHistory = MutableStateFlow<Map<String, List<Transaction>>>(emptyMap())
    val transactionHistory: StateFlow<Map<String, List<Transaction>>> = _transactionHistory
    
    fun recordTransaction(
        currencyId: CurrencyId,
        amount: Int,
        reasonCode: ReasonCode,
        metadata: Map<String, String> = emptyMap()
    ): Transaction {
        val traceId = generateTraceId(currencyId, reasonCode)
        
        val transaction = Transaction(
            currencyId = currencyId,
            amount = amount,
            reasonCode = reasonCode,
            timestamp = System.currentTimeMillis(),
            traceId = traceId,
            metadata = metadata
        )
        
        _transactions.value = _transactions.value + transaction
        
        val history = _transactionHistory.value[currencyId.name] ?: emptyList()
        _transactionHistory.value = _transactionHistory.value + (currencyId.name to (history + transaction))
        
        return transaction
    }
    
    fun recordIncome(
        currencyId: CurrencyId,
        amount: Int,
        reasonCode: ReasonCode,
        source: String = "unknown"
    ): Transaction {
        return recordTransaction(
            currencyId = currencyId,
            amount = amount,
            reasonCode = reasonCode,
            metadata = mapOf(
                "type" to "income",
                "source" to source
            )
        )
    }
    
    fun recordExpense(
        currencyId: CurrencyId,
        amount: Int,
        reasonCode: ReasonCode,
        target: String = "unknown"
    ): Transaction {
        return recordTransaction(
            currencyId = currencyId,
            amount = -amount,
            reasonCode = reasonCode,
            metadata = mapOf(
                "type" to "expense",
                "target" to target
            )
        )
    }
    
    fun recordTransfer(
        fromCurrencyId: CurrencyId,
        toCurrencyId: CurrencyId,
        amount: Int,
        reasonCode: ReasonCode,
        exchangeRate: Float = 1.0f
    ): Pair<Transaction, Transaction> {
        val fromTransaction = recordTransaction(
            currencyId = fromCurrencyId,
            amount = -amount,
            reasonCode = reasonCode,
            metadata = mapOf(
                "type" to "transfer_out",
                "toCurrency" to toCurrencyId.name,
                "exchangeRate" to exchangeRate.toString()
            )
        )
        
        val toTransaction = recordTransaction(
            currencyId = toCurrencyId,
            amount = (amount * exchangeRate).toInt(),
            reasonCode = reasonCode,
            metadata = mapOf(
                "type" to "transfer_in",
                "fromCurrency" to fromCurrencyId.name,
                "exchangeRate" to exchangeRate.toString()
            )
        )
        
        return Pair(fromTransaction, toTransaction)
    }
    
    fun getTransactionsByCurrency(currencyId: CurrencyId): List<Transaction> {
        return _transactions.value.filter { it.currencyId == currencyId }
    }
    
    fun getTransactionsByReason(reasonCode: ReasonCode): List<Transaction> {
        return _transactions.value.filter { it.reasonCode == reasonCode }
    }
    
    fun getTransactionsByTimeRange(startTime: Long, endTime: Long): List<Transaction> {
        return _transactions.value.filter { 
            it.timestamp >= startTime && it.timestamp <= endTime 
        }
    }
    
    fun getRecentTransactions(count: Int): List<Transaction> {
        return _transactions.value.takeLast(count)
    }
    
    fun getIncomeTotal(currencyId: CurrencyId, startTime: Long? = null, endTime: Long? = null): Int {
        val transactions = if (startTime != null && endTime != null) {
            getTransactionsByTimeRange(startTime, endTime)
        } else {
            _transactions.value
        }
        
        return transactions
            .filter { it.currencyId == currencyId && it.isIncome() }
            .sumOf { it.amount }
    }
    
    fun getExpenseTotal(currencyId: CurrencyId, startTime: Long? = null, endTime: Long? = null): Int {
        val transactions = if (startTime != null && endTime != null) {
            getTransactionsByTimeRange(startTime, endTime)
        } else {
            _transactions.value
        }
        
        return transactions
            .filter { it.currencyId == currencyId && it.isExpense() }
            .sumOf { it.amount }
    }
    
    fun getNetBalance(currencyId: CurrencyId): Int {
        return _transactions.value
            .filter { it.currencyId == currencyId }
            .sumOf { it.amount }
    }
    
    fun getTransactionByTraceId(traceId: String): Transaction? {
        return _transactions.value.find { it.traceId == traceId }
    }
    
    fun hasTransaction(traceId: String): Boolean {
        return _transactions.value.any { it.traceId == traceId }
    }
    
    fun clearTransactions() {
        _transactions.value = emptyList()
        _transactionHistory.value = emptyMap()
    }
    
    fun clearTransactionsBefore(timestamp: Long) {
        _transactions.value = _transactions.value.filter { it.timestamp >= timestamp }
        _transactionHistory.value = _transactionHistory.value.mapValues { (_, transactions) ->
            transactions.filter { it.timestamp >= timestamp }
        }
    }
    
    fun clearTransactionsByCurrency(currencyId: CurrencyId) {
        _transactions.value = _transactions.value.filterNot { it.currencyId == currencyId }
        _transactionHistory.value = _transactionHistory.value - currencyId.name
    }
    
    fun getDailyIncome(currencyId: CurrencyId, date: String): Int {
        val dayStart = parseDateToTimestamp(date)
        val dayEnd = dayStart + 24 * 60 * 60 * 1000
        
        return getIncomeTotal(currencyId, dayStart, dayEnd)
    }
    
    fun getDailyExpense(currencyId: CurrencyId, date: String): Int {
        val dayStart = parseDateToTimestamp(date)
        val dayEnd = dayStart + 24 * 60 * 60 * 1000
        
        return getExpenseTotal(currencyId, dayStart, dayEnd)
    }
    
    fun getWeeklyIncome(currencyId: CurrencyId, weekStart: String): Int {
        val startTimestamp = parseDateToTimestamp(weekStart)
        val endTimestamp = startTimestamp + 7 * 24 * 60 * 60 * 1000
        
        return getIncomeTotal(currencyId, startTimestamp, endTimestamp)
    }
    
    fun getWeeklyExpense(currencyId: CurrencyId, weekStart: String): Int {
        val startTimestamp = parseDateToTimestamp(weekStart)
        val endTimestamp = startTimestamp + 7 * 24 * 60 * 60 * 1000
        
        return getExpenseTotal(currencyId, startTimestamp, endTimestamp)
    }
    
    private fun generateTraceId(currencyId: CurrencyId, reasonCode: ReasonCode): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "${currencyId.name}_${reasonCode.name}_${timestamp}_$random"
    }
    
    private fun parseDateToTimestamp(date: String): Long {
        return try {
            java.time.LocalDate.parse(date)
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }
}