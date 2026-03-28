package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CurrencyLedgerTest {
    
    private val ledger = CurrencyLedger()
    
    @Test
    fun testRecordTransaction() {
        val transaction = ledger.recordTransaction(
            currencyId = CurrencyId.STAR_DUST,
            amount = 100,
            reasonCode = ReasonCode.TASK_COMPLETE
        )
        
        assertEquals(CurrencyId.STAR_DUST, transaction.currencyId)
        assertEquals(100, transaction.amount)
        assertEquals(ReasonCode.TASK_COMPLETE, transaction.reasonCode)
        assertTrue(transaction.isIncome())
        assertFalse(transaction.isExpense())
        assertEquals(100, transaction.getAbsoluteAmount())
        
        val transactions = ledger.transactions.value
        assertEquals(1, transactions.size)
    }
    
    @Test
    fun testRecordIncome() {
        val transaction = ledger.recordIncome(
            currencyId = CurrencyId.MOON_GLOW,
            amount = 50,
            reasonCode = ReasonCode.SIGN_IN,
            source = "daily_sign_in"
        )
        
        assertEquals(50, transaction.amount)
        assertTrue(transaction.isIncome())
        assertEquals("income", transaction.metadata["type"])
        assertEquals("daily_sign_in", transaction.metadata["source"])
    }
    
    @Test
    fun testRecordExpense() {
        val transaction = ledger.recordExpense(
            currencyId = CurrencyId.STAR_DUST,
            amount = 200,
            reasonCode = ReasonCode.SHOP,
            target = "item_001"
        )
        
        assertEquals(-200, transaction.amount)
        assertTrue(transaction.isExpense())
        assertEquals("expense", transaction.metadata["type"])
        assertEquals("item_001", transaction.metadata["target"])
    }
    
    @Test
    fun testRecordTransfer() {
        val (fromTransaction, toTransaction) = ledger.recordTransfer(
            fromCurrencyId = CurrencyId.MOON_GLOW,
            toCurrencyId = CurrencyId.STAR_DUST,
            amount = 10,
            reasonCode = ReasonCode.SHOP,
            exchangeRate = 10.0f
        )
        
        assertEquals(-10, fromTransaction.amount)
        assertEquals(CurrencyId.MOON_GLOW, fromTransaction.currencyId)
        assertEquals("transfer_out", fromTransaction.metadata["type"])
        
        assertEquals(100, toTransaction.amount)
        assertEquals(CurrencyId.STAR_DUST, toTransaction.currencyId)
        assertEquals("transfer_in", toTransaction.metadata["type"])
    }
    
    @Test
    fun testGetTransactionsByCurrency() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordIncome(CurrencyId.MOON_GLOW, 50, ReasonCode.SIGN_IN)
        ledger.recordIncome(CurrencyId.STAR_DUST, 200, ReasonCode.TASK_COMPLETE)
        
        val starDustTransactions = ledger.getTransactionsByCurrency(CurrencyId.STAR_DUST)
        assertEquals(2, starDustTransactions.size)
        
        val moonGlowTransactions = ledger.getTransactionsByCurrency(CurrencyId.MOON_GLOW)
        assertEquals(1, moonGlowTransactions.size)
    }
    
    @Test
    fun testGetTransactionsByReason() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordIncome(CurrencyId.MOON_GLOW, 50, ReasonCode.SIGN_IN)
        ledger.recordIncome(CurrencyId.STAR_DUST, 200, ReasonCode.TASK_COMPLETE)
        
        val taskTransactions = ledger.getTransactionsByReason(ReasonCode.TASK_COMPLETE)
        assertEquals(2, taskTransactions.size)
        
        val signInTransactions = ledger.getTransactionsByReason(ReasonCode.SIGN_IN)
        assertEquals(1, signInTransactions.size)
    }
    
    @Test
    fun testGetTransactionsByTimeRange() {
        val startTime = System.currentTimeMillis() - 10000
        val endTime = System.currentTimeMillis() + 10000
        
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        
        val transactions = ledger.getTransactionsByTimeRange(startTime, endTime)
        assertEquals(1, transactions.size)
        
        val emptyTransactions = ledger.getTransactionsByTimeRange(startTime, startTime + 1000)
        assertEquals(0, emptyTransactions.size)
    }
    
    @Test
    fun testGetRecentTransactions() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordIncome(CurrencyId.MOON_GLOW, 50, ReasonCode.SIGN_IN)
        ledger.recordIncome(CurrencyId.STAR_DUST, 200, ReasonCode.TASK_COMPLETE)
        
        val recent = ledger.getRecentTransactions(2)
        assertEquals(2, recent.size)
        assertEquals(ReasonCode.TASK_COMPLETE, recent[0].reasonCode)
        assertEquals(ReasonCode.SIGN_IN, recent[1].reasonCode)
    }
    
    @Test
    fun testGetIncomeTotal() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordExpense(CurrencyId.STAR_DUST, 50, ReasonCode.SHOP)
        ledger.recordIncome(CurrencyId.STAR_DUST, 200, ReasonCode.TASK_COMPLETE)
        
        val incomeTotal = ledger.getIncomeTotal(CurrencyId.STAR_DUST)
        assertEquals(300, incomeTotal)
    }
    
    @Test
    fun testGetExpenseTotal() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordExpense(CurrencyId.STAR_DUST, 50, ReasonCode.SHOP)
        ledger.recordExpense(CurrencyId.STAR_DUST, 30, ReasonCode.SHOP)
        
        val expenseTotal = ledger.getExpenseTotal(CurrencyId.STAR_DUST)
        assertEquals(80, expenseTotal)
    }
    
    @Test
    fun testGetNetBalance() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordExpense(CurrencyId.STAR_DUST, 50, ReasonCode.SHOP)
        ledger.recordIncome(CurrencyId.STAR_DUST, 200, ReasonCode.TASK_COMPLETE)
        
        val netBalance = ledger.getNetBalance(CurrencyId.STAR_DUST)
        assertEquals(250, netBalance)
    }
    
    @Test
    fun testGetTransactionByTraceId() {
        val transaction = ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        
        val found = ledger.getTransactionByTraceId(transaction.traceId)
        assertNotNull(found)
        assertEquals(transaction.traceId, found?.traceId)
        
        val notFound = ledger.getTransactionByTraceId("invalid_trace_id")
        assertNull(notFound)
    }
    
    @Test
    fun testHasTransaction() {
        val transaction = ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        
        assertTrue(ledger.hasTransaction(transaction.traceId))
        assertFalse(ledger.hasTransaction("invalid_trace_id"))
    }
    
    @Test
    fun testClearTransactions() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordIncome(CurrencyId.MOON_GLOW, 50, ReasonCode.SIGN_IN)
        
        ledger.clearTransactions()
        
        assertEquals(0, ledger.transactions.value.size)
        assertEquals(0, ledger.transactionHistory.value.size)
    }
    
    @Test
    fun testClearTransactionsBefore() {
        val now = System.currentTimeMillis()
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordIncome(CurrencyId.MOON_GLOW, 50, ReasonCode.SIGN_IN)
        
        ledger.clearTransactionsBefore(now + 10000)
        
        assertEquals(0, ledger.transactions.value.size)
    }
    
    @Test
    fun testClearTransactionsByCurrency() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordIncome(CurrencyId.MOON_GLOW, 50, ReasonCode.SIGN_IN)
        
        ledger.clearTransactionsByCurrency(CurrencyId.STAR_DUST)
        
        val transactions = ledger.transactions.value
        assertEquals(1, transactions.size)
        assertEquals(CurrencyId.MOON_GLOW, transactions[0].currencyId)
    }
    
    @Test
    fun testTransactionHistory() {
        ledger.recordIncome(CurrencyId.STAR_DUST, 100, ReasonCode.TASK_COMPLETE)
        ledger.recordIncome(CurrencyId.MOON_GLOW, 50, ReasonCode.SIGN_IN)
        ledger.recordIncome(CurrencyId.STAR_DUST, 200, ReasonCode.TASK_COMPLETE)
        
        val starDustHistory = ledger.transactionHistory.value["STAR_DUST"]
        assertNotNull(starDustHistory)
        assertEquals(2, starDustHistory.size)
        
        val moonGlowHistory = ledger.transactionHistory.value["MOON_GLOW"]
        assertNotNull(moonGlowHistory)
        assertEquals(1, moonGlowHistory.size)
    }
}

class WalletTest {
    
    @Test
    fun testGetBalance() {
        val wallet = Wallet(
            starDust = 100,
            moonGlow = 50,
            gachaTickets = mapOf("NORMAL" to 5, "SSR" to 1)
        )
        
        assertEquals(100, wallet.getBalance(CurrencyId.STAR_DUST))
        assertEquals(50, wallet.getBalance(CurrencyId.MOON_GLOW))
        assertEquals(5, wallet.getBalance(CurrencyId.GACHA_TICKET_NORMAL))
        assertEquals(1, wallet.getBalance(CurrencyId.GACHA_TICKET_SSR))
        assertEquals(0, wallet.getBalance(CurrencyId.GACHA_TICKET_RARE))
    }
    
    @Test
    fun testAddCurrency() {
        val wallet = Wallet(starDust = 100, moonGlow = 50)
        
        val newWallet = wallet.addCurrency(CurrencyId.STAR_DUST, 50)
        assertEquals(150, newWallet.starDust)
        
        val newWallet2 = newWallet.addCurrency(CurrencyId.MOON_GLOW, 20)
        assertEquals(70, newWallet2.moonGlow)
        
        val newWallet3 = newWallet2.addCurrency(CurrencyId.GACHA_TICKET_NORMAL, 3)
        assertEquals(3, newWallet3.gachaTickets["NORMAL"])
    }
    
    @Test
    fun testDeductCurrency() {
        val wallet = Wallet(starDust = 100, moonGlow = 50, gachaTickets = mapOf("NORMAL" to 5))
        
        val newWallet = wallet.deductCurrency(CurrencyId.STAR_DUST, 30)
        assertNotNull(newWallet)
        assertEquals(70, newWallet.starDust)
        
        val newWallet2 = newWallet?.deductCurrency(CurrencyId.MOON_GLOW, 20)
        assertNotNull(newWallet2)
        assertEquals(30, newWallet2.moonGlow)
        
        val failedWallet = newWallet2?.deductCurrency(CurrencyId.STAR_DUST, 1000)
        assertNull(failedWallet)
    }
    
    @Test
    fun testHasEnough() {
        val wallet = Wallet(starDust = 100, moonGlow = 50, gachaTickets = mapOf("NORMAL" to 5))
        
        assertTrue(wallet.hasEnough(CurrencyId.STAR_DUST, 50))
        assertTrue(wallet.hasEnough(CurrencyId.STAR_DUST, 100))
        assertFalse(wallet.hasEnough(CurrencyId.STAR_DUST, 101))
        
        assertTrue(wallet.hasEnough(CurrencyId.GACHA_TICKET_NORMAL, 5))
        assertFalse(wallet.hasEnough(CurrencyId.GACHA_TICKET_NORMAL, 6))
    }
    
    @Test
    fun testGetTotalValue() {
        val wallet = Wallet(
            starDust = 100,
            moonGlow = 50,
            gachaTickets = mapOf("NORMAL" to 5, "SSR" to 1)
        )
        
        val totalValue = wallet.getTotalValue()
        assertEquals(100 + 50 * 10 + 5 + 1, totalValue)
    }
    
    @Test
    fun testCurrencyBalanceFromWallet() {
        val wallet = Wallet(
            starDust = 100,
            moonGlow = 50,
            gachaTickets = mapOf("NORMAL" to 5, "SSR" to 1)
        )
        
        val balances = CurrencyBalance.fromWallet(wallet)
        assertEquals(6, balances.size)
        
        val starDustBalance = balances.find { it.currencyId == CurrencyId.STAR_DUST }
        assertNotNull(starDustBalance)
        assertEquals(100, starDustBalance.balance)
        assertEquals("星尘", starDustBalance.name)
        assertEquals("✨", starDustBalance.icon)
        
        val moonGlowBalance = balances.find { it.currencyId == CurrencyId.MOON_GLOW }
        assertNotNull(moonGlowBalance)
        assertEquals(50, moonGlowBalance.balance)
        assertEquals("月光", moonGlowBalance.name)
        assertEquals("🌙", moonGlowBalance.icon)
    }
    
    @Test
    fun testTransactionProperties() {
        val transaction = Transaction(
            currencyId = CurrencyId.STAR_DUST,
            amount = 100,
            reasonCode = ReasonCode.TASK_COMPLETE,
            traceId = "test_trace_id"
        )
        
        assertTrue(transaction.isIncome())
        assertFalse(transaction.isExpense())
        assertEquals(100, transaction.getAbsoluteAmount())
        
        val expenseTransaction = transaction.copy(amount = -50)
        assertFalse(expenseTransaction.isIncome())
        assertTrue(expenseTransaction.isExpense())
        assertEquals(50, expenseTransaction.getAbsoluteAmount())
    }
}