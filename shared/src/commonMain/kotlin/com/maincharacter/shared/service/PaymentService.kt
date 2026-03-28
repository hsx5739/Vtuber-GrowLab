package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class PaymentService(
    private val isSandboxMode: Boolean = true
) {
    
    private val _paymentTransactions = MutableStateFlow<Map<String, PaymentTransaction>>(emptyMap())
    val paymentTransactions: StateFlow<Map<String, PaymentTransaction>> = _paymentTransactions
    
    private val _paymentHistory = MutableStateFlow<List<PaymentHistoryEntry>>(emptyList())
    val paymentHistory: StateFlow<List<PaymentHistoryEntry>> = _paymentHistory
    
    fun processPayment(
        userId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        productId: String? = null,
        packageId: String? = null
    ): PaymentResult {
        if (isSandboxMode) {
            return processSandboxPayment(
                userId,
                amount,
                paymentMethod,
                productId,
                packageId
            )
        }
        
        return processRealPayment(
            userId,
            amount,
            paymentMethod,
            productId,
            packageId
        )
    }
    
    private fun processSandboxPayment(
        userId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        productId: String?,
        packageId: String?
    ): PaymentResult {
        val transactionId = generateTransactionId()
        
        val transaction = PaymentTransaction(
            transactionId = transactionId,
            userId = userId,
            amount = amount,
            currency = "CNY",
            paymentMethod = paymentMethod,
            productId = productId,
            packageId = packageId,
            status = PaymentStatus.PENDING,
            createTime = System.currentTimeMillis(),
            updateTime = System.currentTimeMillis(),
            metadata = mapOf("sandbox" to "true")
        )
        
        val updatedTransactions = _paymentTransactions.value.toMutableMap()
        updatedTransactions[transactionId] = transaction
        _paymentTransactions.value = updatedTransactions
        
        val historyEntry = PaymentHistoryEntry(
            id = generateHistoryId(),
            userId = userId,
            transactionId = transactionId,
            amount = amount,
            paymentMethod = paymentMethod,
            status = PaymentStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )
        
        val updatedHistory = _paymentHistory.value.toMutableList()
        updatedHistory.add(historyEntry)
        _paymentHistory.value = updatedHistory
        
        val success = simulatePaymentSuccess()
        
        val finalStatus = if (success) {
            PaymentStatus.SUCCESS
        } else {
            PaymentStatus.FAILED
        }
        
        val updatedTransaction = transaction.copy(
            status = finalStatus,
            updateTime = System.currentTimeMillis()
        )
        
        updatedTransactions[transactionId] = updatedTransaction
        _paymentTransactions.value = updatedTransactions
        
        val updatedHistoryEntry = historyEntry.copy(
            status = finalStatus
        )
        
        updatedHistory[updatedHistory.size - 1] = updatedHistoryEntry
        _paymentHistory.value = updatedHistory
        
        return PaymentResult(
            transactionId = transactionId,
            success = success,
            amount = amount,
            paymentMethod = paymentMethod,
            reason = if (success) {
                "Sandbox payment successful"
            } else {
                "Sandbox payment failed"
            },
            metadata = mapOf("sandbox" to "true")
        )
    }
    
    private fun processRealPayment(
        userId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        productId: String?,
        packageId: String?
    ): PaymentResult {
        val transactionId = generateTransactionId()
        
        val transaction = PaymentTransaction(
            transactionId = transactionId,
            userId = userId,
            amount = amount,
            currency = "CNY",
            paymentMethod = paymentMethod,
            productId = productId,
            packageId = packageId,
            status = PaymentStatus.PENDING,
            createTime = System.currentTimeMillis(),
            updateTime = System.currentTimeMillis(),
            metadata = mapOf("sandbox" to "false")
        )
        
        val updatedTransactions = _paymentTransactions.value.toMutableMap()
        updatedTransactions[transactionId] = transaction
        _paymentTransactions.value = updatedTransactions
        
        val historyEntry = PaymentHistoryEntry(
            id = generateHistoryId(),
            userId = userId,
            transactionId = transactionId,
            amount = amount,
            paymentMethod = paymentMethod,
            status = PaymentStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )
        
        val updatedHistory = _paymentHistory.value.toMutableList()
        updatedHistory.add(historyEntry)
        _paymentHistory.value = updatedHistory
        
        return PaymentResult(
            transactionId = transactionId,
            success = false,
            amount = amount,
            paymentMethod = paymentMethod,
            reason = "Real payment processing not implemented",
            metadata = mapOf("sandbox" to "false", "not_implemented" to "true")
        )
    }
    
    private fun simulatePaymentSuccess(): Boolean {
        return (1..100).random() > 10
    }
    
    fun refundPayment(
        transactionId: String,
        reason: String
    ): RefundResult {
        val transaction = _paymentTransactions.value[transactionId]
        
        if (transaction == null) {
            return RefundResult(
                refundId = generateRefundId(),
                transactionId = transactionId,
                success = false,
                amount = 0.0,
                reason = "Transaction not found: $transactionId"
            )
        }
        
        if (transaction.status != PaymentStatus.SUCCESS) {
            return RefundResult(
                refundId = generateRefundId(),
                transactionId = transactionId,
                success = false,
                amount = transaction.amount,
                reason = "Cannot refund non-successful transaction"
            )
        }
        
        val refundId = generateRefundId()
        
        val refundTransaction = PaymentTransaction(
            transactionId = refundId,
            userId = transaction.userId,
            amount = -transaction.amount,
            currency = transaction.currency,
            paymentMethod = transaction.paymentMethod,
            productId = transaction.productId,
            packageId = transaction.packageId,
            status = PaymentStatus.SUCCESS,
            createTime = System.currentTimeMillis(),
            updateTime = System.currentTimeMillis(),
            metadata = mapOf(
                "refund" to "true",
                "original_transaction" to transactionId,
                "refund_reason" to reason
            )
        )
        
        val updatedTransactions = _paymentTransactions.value.toMutableMap()
        updatedTransactions[refundId] = refundTransaction
        _paymentTransactions.value = updatedTransactions
        
        val historyEntry = PaymentHistoryEntry(
            id = generateHistoryId(),
            userId = transaction.userId,
            transactionId = refundId,
            amount = -transaction.amount,
            paymentMethod = transaction.paymentMethod,
            status = PaymentStatus.SUCCESS,
            timestamp = System.currentTimeMillis()
        )
        
        val updatedHistory = _paymentHistory.value.toMutableList()
        updatedHistory.add(historyEntry)
        _paymentHistory.value = updatedHistory
        
        return RefundResult(
            refundId = refundId,
            transactionId = transactionId,
            success = true,
            amount = transaction.amount,
            reason = "Refund processed successfully"
        )
    }
    
    fun queryPaymentStatus(transactionId: String): PaymentStatus? {
        val transaction = _paymentTransactions.value[transactionId]
        return transaction?.status
    }
    
    fun getPaymentTransaction(transactionId: String): PaymentTransaction? {
        return _paymentTransactions.value[transactionId]
    }
    
    fun getUserPaymentTransactions(userId: String): List<PaymentTransaction> {
        return _paymentTransactions.value.values.filter { it.userId == userId }
    }
    
    fun getUserPaymentHistory(userId: String): List<PaymentHistoryEntry> {
        return _paymentHistory.value.filter { it.userId == userId }
    }
    
    fun getTotalPaymentAmount(userId: String): Double {
        return getUserPaymentTransactions(userId)
            .filter { it.status == PaymentStatus.SUCCESS && it.amount > 0 }
            .sumOf { it.amount }
    }
    
    fun getTotalRefundAmount(userId: String): Double {
        return getUserPaymentTransactions(userId)
            .filter { it.status == PaymentStatus.SUCCESS && it.amount < 0 }
            .sumOf { Math.abs(it.amount) }
    }
    
    fun getPaymentStatistics(userId: String): PaymentStatistics {
        val transactions = getUserPaymentTransactions(userId)
        
        val successfulPayments = transactions.filter { it.status == PaymentStatus.SUCCESS && it.amount > 0 }
        val failedPayments = transactions.filter { it.status == PaymentStatus.FAILED }
        val refunds = transactions.filter { it.status == PaymentStatus.SUCCESS && it.amount < 0 }
        
        return PaymentStatistics(
            totalPayments = successfulPayments.size,
            totalAmount = successfulPayments.sumOf { it.amount },
            totalRefunds = refunds.size,
            totalRefundAmount = refunds.sumOf { Math.abs(it.amount) },
            failedPayments = failedPayments.size,
            successRate = if (transactions.isNotEmpty()) {
                (successfulPayments.size.toFloat() / transactions.size.toFloat()) * 100f
            } else {
                0f
            }
        )
    }
    
    fun clearPaymentData(): Int {
        val count = _paymentTransactions.value.size
        _paymentTransactions.value = emptyMap()
        _paymentHistory.value = emptyList()
        return count
    }
    
    fun isSandboxMode(): Boolean {
        return isSandboxMode
    }
    
    fun getSupportedPaymentMethods(): List<PaymentMethod> {
        return if (isSandboxMode) {
            PaymentMethod.values().toList()
        } else {
            listOf(
                PaymentMethod.WECHAT_PAY,
                PaymentMethod.ALIPAY,
                PaymentMethod.APPLE_PAY,
                PaymentMethod.GOOGLE_PAY,
                PaymentMethod.CREDIT_CARD
            )
        }
    }
    
    private fun generateTransactionId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "txn_${timestamp}_$random"
    }
    
    private fun generateRefundId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "refund_${timestamp}_$random"
    }
    
    private fun generateHistoryId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "pay_hist_${timestamp}_$random"
    }
}

@Serializable
data class PaymentTransaction(
    val transactionId: String,
    val userId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: PaymentMethod,
    val productId: String?,
    val packageId: String?,
    val status: PaymentStatus,
    val createTime: Long,
    val updateTime: Long,
    val metadata: Map<String, String> = emptyMap()
) {
    fun isSuccessful(): Boolean {
        return status == PaymentStatus.SUCCESS
    }
    
    fun isPending(): Boolean {
        return status == PaymentStatus.PENDING
    }
    
    fun isFailed(): Boolean {
        return status == PaymentStatus.FAILED
    }
    
    fun isRefund(): Boolean {
        return amount < 0
    }
    
    fun getFormattedCreateTime(): String {
        val date = java.util.Date(createTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getFormattedUpdateTime(): String {
        val date = java.util.Date(updateTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceCreation(): Long {
        return System.currentTimeMillis() - createTime
    }
    
    fun getTimeSinceCreationMinutes(): Float {
        return getTimeSinceCreation() / 60000f
    }
    
    fun getFormattedAmount(): String {
        return String.format("%.2f", amount)
    }
}

@Serializable
data class PaymentResult(
    val transactionId: String,
    val success: Boolean,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val reason: String,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class RefundResult(
    val refundId: String,
    val transactionId: String,
    val success: Boolean,
    val amount: Double,
    val reason: String
)

@Serializable
data class PaymentHistoryEntry(
    val id: String,
    val userId: String,
    val transactionId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val timestamp: Long
) {
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSincePayment(): Long {
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSincePaymentMinutes(): Float {
        return getTimeSincePayment() / 60000f
    }
    
    fun getFormattedAmount(): String {
        return String.format("%.2f", amount)
    }
}

@Serializable
data class PaymentStatistics(
    val totalPayments: Int,
    val totalAmount: Double,
    val totalRefunds: Int,
    val totalRefundAmount: Double,
    val failedPayments: Int,
    val successRate: Float
) {
    fun getNetAmount(): Double {
        return totalAmount - totalRefundAmount
    }
    
    fun getAveragePaymentAmount(): Double {
        return if (totalPayments > 0) {
            totalAmount / totalPayments
        } else {
            0.0
        }
    }
    
    fun getAverageRefundAmount(): Double {
        return if (totalRefunds > 0) {
            totalRefundAmount / totalRefunds
        } else {
            0.0
        }
    }
    
    fun getFormattedTotalAmount(): String {
        return String.format("%.2f", totalAmount)
    }
    
    fun getFormattedTotalRefundAmount(): String {
        return String.format("%.2f", totalRefundAmount)
    }
    
    fun getFormattedNetAmount(): String {
        return String.format("%.2f", getNetAmount())
    }
    
    fun getFormattedSuccessRate(): String {
        return String.format("%.2f", successRate)
    }
}

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED
}
