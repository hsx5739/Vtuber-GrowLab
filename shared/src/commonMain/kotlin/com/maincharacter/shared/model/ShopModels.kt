package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class ShopProduct(
    val productId: String,
    val productName: String,
    val productDescription: String,
    val productType: ProductType,
    val itemType: ItemType?,
    val itemId: String?,
    val skinId: String?,
    val skillCardId: String?,
    val iconPath: String,
    val rarity: ItemRarity,
    val price: Map<CurrencyType, Int>,
    val discountPrice: Map<CurrencyType, Int>?,
    val stock: Int?,
    val maxStock: Int?,
    val purchaseLimit: Int?,
    val purchaseCount: Int,
    val tags: List<String>,
    val isAvailable: Boolean,
    val isExclusive: Boolean,
    val isRecommended: Boolean,
    val isNew: Boolean,
    val startTime: Long?,
    val endTime: Long?,
    val requirements: ProductRequirements,
    val rewards: ProductRewards,
    val canAfford: Boolean = true,
    val metadata: Map<String, String> = emptyMap()
) {
    fun isCurrentlyAvailable(): Boolean {
        if (!isAvailable) return false
        
        val currentTime = System.currentTimeMillis()
        
        if (startTime != null && currentTime < startTime) {
            return false
        }
        
        if (endTime != null && currentTime > endTime) {
            return false
        }
        
        if (stock != null && stock <= 0) {
            return false
        }
        
        return true
    }
    
    fun isOutOfStock(): Boolean {
        return stock != null && stock <= 0
    }
    
    fun isLimitedStock(): Boolean {
        return maxStock != null
    }
    
    fun isLimitedPurchase(): Boolean {
        return purchaseLimit != null
    }
    
    fun canPurchase(purchaseCount: Int): Boolean {
        if (!isCurrentlyAvailable()) return false
        
        if (purchaseLimit != null && purchaseCount >= purchaseLimit) {
            return false
        }
        
        if (stock != null && stock <= 0) {
            return false
        }
        
        return true
    }
    
    fun getRemainingPurchases(): Int? {
        return purchaseLimit?.let { it - purchaseCount }
    }
    
    fun getStockPercentage(): Float? {
        val currentStock = stock ?: return null
        val maxStockValue = maxStock ?: return null
        return if (maxStockValue > 0) {
            (currentStock.toFloat() / maxStockValue.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getPurchaseProgressPercentage(): Float? {
        val purchaseLimitValue = purchaseLimit ?: return null
        return if (purchaseLimitValue > 0) {
            (purchaseCount.toFloat() / purchaseLimitValue.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun hasDiscount(): Boolean {
        return discountPrice != null && discountPrice.isNotEmpty()
    }
    
    fun getDiscountPercentage(): Float? {
        if (!hasDiscount()) return null
        
        val originalPrice = price.values.sum()
        val discountedPrice = discountPrice!!.values.sum()
        
        return if (originalPrice > 0) {
            ((originalPrice - discountedPrice).toFloat() / originalPrice.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun isTimeLimited(): Boolean {
        return startTime != null || endTime != null
    }
    
    fun getTimeUntilAvailable(): Long? {
        val startTime = startTime ?: return 0L
        val currentTime = System.currentTimeMillis()
        return (startTime - currentTime).coerceAtLeast(0L)
    }
    
    fun getTimeUntilExpired(): Long? {
        val endTime = endTime ?: return null
        val currentTime = System.currentTimeMillis()
        return (endTime - currentTime).coerceAtLeast(0L)
    }
    
    fun getFormattedStartTime(): String? {
        val timestamp = startTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getFormattedEndTime(): String? {
        val timestamp = endTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getRarityColor(): String {
        return when (rarity) {
            ItemRarity.COMMON -> "#FFFFFF"
            ItemRarity.UNCOMMON -> "#00FF00"
            ItemRarity.RARE -> "#00BFFF"
            ItemRarity.EPIC -> "#FFD700"
            ItemRarity.LEGENDARY -> "#FF69B4"
        }
    }
    
    fun getProductTypeDisplayName(): String {
        return when (productType) {
            ProductType.ITEM -> "道具"
            ProductType.SKIN -> "皮肤"
            ProductType.SKILL_CARD -> "技能卡"
            ProductType.CURRENCY -> "货币"
            ProductType.BUNDLE -> "礼包"
            ProductType.SPECIAL -> "特价"
        }
    }
}

@Serializable
data class RechargePackage(
    val packageId: String,
    val packageName: String,
    val packageDescription: String,
    val currencyType: CurrencyType,
    val currencyAmount: Int,
    val bonusAmount: Int,
    val price: Double,
    val originalPrice: Double?,
    val isRecommended: Boolean,
    val isNew: Boolean,
    val isLimited: Boolean,
    val limitCount: Int?,
    val purchaseCount: Int,
    val startTime: Long?,
    val endTime: Long?,
    val platform: PaymentPlatform,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getTotalAmount(): Int {
        return currencyAmount + bonusAmount
    }
    
    fun getBonusPercentage(): Float {
        return if (currencyAmount > 0) {
            (bonusAmount.toFloat() / currencyAmount.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun hasDiscount(): Boolean {
        return originalPrice != null && originalPrice > price
    }
    
    fun getDiscountPercentage(): Float? {
        if (!hasDiscount()) return null
        
        val originalPriceValue = originalPrice ?: return null
        return if (originalPriceValue > 0) {
            ((originalPriceValue - price).toFloat() / originalPriceValue.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun isAvailable(): Boolean {
        val currentTime = System.currentTimeMillis()
        
        if (startTime != null && currentTime < startTime) {
            return false
        }
        
        if (endTime != null && currentTime > endTime) {
            return false
        }
        
        if (isLimited && limitCount != null && purchaseCount >= limitCount) {
            return false
        }
        
        return true
    }
    
    fun isTimeLimited(): Boolean {
        return startTime != null || endTime != null
    }
    
    fun isPurchaseLimited(): Boolean {
        return isLimited && limitCount != null
    }
    
    fun getRemainingPurchases(): Int? {
        return limitCount?.let { it - purchaseCount }
    }
    
    fun getPurchaseProgressPercentage(): Float? {
        val limitCountValue = limitCount ?: return null
        return if (limitCountValue > 0) {
            (purchaseCount.toFloat() / limitCountValue.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getTimeUntilAvailable(): Long? {
        val startTime = startTime ?: return 0L
        val currentTime = System.currentTimeMillis()
        return (startTime - currentTime).coerceAtLeast(0L)
    }
    
    fun getTimeUntilExpired(): Long? {
        val endTime = endTime ?: return null
        val currentTime = System.currentTimeMillis()
        return (endTime - currentTime).coerceAtLeast(0L)
    }
    
    fun getFormattedStartTime(): String? {
        val timestamp = startTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getFormattedEndTime(): String? {
        val timestamp = endTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getPlatformDisplayName(): String {
        return when (platform) {
            PaymentPlatform.IOS -> "iOS"
            PaymentPlatform.ANDROID -> "Android"
            PaymentPlatform.WEB -> "Web"
            PaymentPlatform.ALL -> "全平台"
        }
    }
}

@Serializable
data class ProductRequirements(
    val minLevel: Int?,
    val maxLevel: Int?,
    val requiredItems: Map<String, Int>,
    val requiredCurrencies: Map<CurrencyType, Int>,
    val requiredAchievements: List<String>,
    val requiredQuests: List<String>,
    val requiredAttributes: Map<AttributeType, Int>,
    val vipLevel: Int?
) {
    fun hasRequirements(): Boolean {
        return minLevel != null ||
               maxLevel != null ||
               requiredItems.isNotEmpty() ||
               requiredCurrencies.isNotEmpty() ||
               requiredAchievements.isNotEmpty() ||
               requiredQuests.isNotEmpty() ||
               requiredAttributes.isNotEmpty() ||
               vipLevel != null
    }
    
    fun meetsRequirements(
        userLevel: Int,
        userItems: Map<String, Int>,
        userCurrencies: Map<CurrencyType, Int>,
        completedAchievements: List<String>,
        completedQuests: List<String>,
        userAttributes: Map<AttributeType, Int>,
        userVipLevel: Int
    ): Boolean {
        minLevel?.let { level ->
            if (userLevel < level) return false
        }
        
        maxLevel?.let { level ->
            if (userLevel > level) return false
        }
        
        requiredItems.forEach { (itemId, count) ->
            if ((userItems[itemId] ?: 0) < count) return false
        }
        
        requiredCurrencies.forEach { (currency, amount) ->
            if ((userCurrencies[currency] ?: 0) < amount) return false
        }
        
        requiredAchievements.forEach { achievement ->
            if (!completedAchievements.contains(achievement)) return false
        }
        
        requiredQuests.forEach { quest ->
            if (!completedQuests.contains(quest)) return false
        }
        
        requiredAttributes.forEach { (attribute, value) ->
            if ((userAttributes[attribute] ?: 0) < value) return false
        }
        
        vipLevel?.let { level ->
            if (userVipLevel < level) return false
        }
        
        return true
    }
}

@Serializable
data class ProductRewards(
    val items: Map<String, Int>,
    val currencies: Map<CurrencyType, Int>,
    val skins: List<String>,
    val skillCards: List<String>,
    val buffs: List<String>,
    val experience: Int,
    val vipExperience: Int
) {
    fun hasRewards(): Boolean {
        return items.isNotEmpty() ||
               currencies.isNotEmpty() ||
               skins.isNotEmpty() ||
               skillCards.isNotEmpty() ||
               buffs.isNotEmpty() ||
               experience > 0 ||
               vipExperience > 0
    }
    
    fun getTotalItemCount(): Int {
        return items.values.sum()
    }
    
    fun getTotalCurrencyAmount(): Int {
        return currencies.values.sum()
    }
    
    fun getSkinCount(): Int {
        return skins.size
    }
    
    fun getSkillCardCount(): Int {
        return skillCards.size
    }
    
    fun getBuffCount(): Int {
        return buffs.size
    }
}

@Serializable
data class ShopConfig(
    val version: String,
    val products: Map<String, ShopProduct>,
    val rechargePackages: Map<String, RechargePackage>,
    val categories: List<String>,
    val defaultCurrency: CurrencyType,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    fun getProduct(productId: String): ShopProduct? {
        return products[productId]
    }
    
    fun getAllProducts(): Map<String, ShopProduct> {
        return products
    }
    
    fun getProductsByType(productType: ProductType): List<ShopProduct> {
        return products.values.filter { it.productType == productType }
    }
    
    fun getAvailableProducts(): List<ShopProduct> {
        return products.values.filter { it.isCurrentlyAvailable() }
    }
    
    fun getExclusiveProducts(): List<ShopProduct> {
        return products.values.filter { it.isExclusive }
    }
    
    fun getRecommendedProducts(): List<ShopProduct> {
        return products.values.filter { it.isRecommended }
    }
    
    fun getNewProducts(): List<ShopProduct> {
        return products.values.filter { it.isNew }
    }
    
    fun getProductsByRarity(rarity: ItemRarity): List<ShopProduct> {
        return products.values.filter { it.rarity == rarity }
    }
    
    fun getRechargePackage(packageId: String): RechargePackage? {
        return rechargePackages[packageId]
    }
    
    fun getAllRechargePackages(): Map<String, RechargePackage> {
        return rechargePackages
    }
    
    fun getAvailableRechargePackages(): List<RechargePackage> {
        return rechargePackages.values.filter { it.isAvailable() }
    }
    
    fun getRecommendedRechargePackages(): List<RechargePackage> {
        return rechargePackages.values.filter { it.isRecommended }
    }
    
    fun getNewRechargePackages(): List<RechargePackage> {
        return rechargePackages.values.filter { it.isNew }
    }
    
    fun getCategoryList(): List<String> {
        return categories
    }
    
    fun getProductCount(): Int {
        return products.size
    }
    
    fun getAvailableProductCount(): Int {
        return getAvailableProducts().size
    }
    
    fun getRechargePackageCount(): Int {
        return rechargePackages.size
    }
    
    fun getAvailableRechargePackageCount(): Int {
        return getAvailableRechargePackages().size
    }
    
    fun getConfigVersionValue(): String {
        return version
    }
    
    fun getDefaultCurrencyValue(): CurrencyType {
        return defaultCurrency
    }
    
    fun getLastUpdatedAt(): Long {
        return lastUpdateTime
    }
    
    fun getFormattedLastUpdateTime(): String {
        val date = java.util.Date(lastUpdateTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceLastUpdate(): Long {
        return System.currentTimeMillis() - lastUpdateTime
    }
    
    fun getTimeSinceLastUpdateMinutes(): Float {
        return getTimeSinceLastUpdate() / 60000f
    }
}

@Serializable
data class PurchaseRecord(
    val recordId: String,
    val userId: String,
    val productId: String?,
    val packageId: String?,
    val productName: String,
    val productType: ProductType,
    val price: Map<CurrencyType, Int>,
    val realMoneyPrice: Double?,
    val paymentMethod: PaymentMethod?,
    val rewards: ProductRewards,
    val purchaseTime: Long,
    val status: PurchaseStatus,
    val metadata: Map<String, String> = emptyMap()
) {
    fun isCompleted(): Boolean {
        return status == PurchaseStatus.COMPLETED
    }
    
    fun isPending(): Boolean {
        return status == PurchaseStatus.PENDING
    }
    
    fun isFailed(): Boolean {
        return status == PurchaseStatus.FAILED
    }
    
    fun isRefunded(): Boolean {
        return status == PurchaseStatus.REFUNDED
    }
    
    fun getFormattedPurchaseTime(): String {
        val date = java.util.Date(purchaseTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSincePurchase(): Long {
        return System.currentTimeMillis() - purchaseTime
    }
    
    fun getTimeSincePurchaseMinutes(): Float {
        return getTimeSincePurchase() / 60000f
    }
    
    fun getTotalPrice(): Int {
        return price.values.sum()
    }
}

enum class ProductType {
    ITEM,
    SKIN,
    SKILL_CARD,
    CURRENCY,
    BUNDLE,
    SPECIAL
}

enum class PaymentPlatform {
    IOS,
    ANDROID,
    WEB,
    ALL
}

enum class PaymentMethod {
    WECHAT_PAY,
    ALIPAY,
    APPLE_PAY,
    GOOGLE_PAY,
    CREDIT_CARD,
    DEMO;
    
    fun getDisplayName(): String {
        return when (this) {
            WECHAT_PAY -> "微信支付"
            ALIPAY -> "支付宝"
            APPLE_PAY -> "Apple Pay"
            GOOGLE_PAY -> "Google Pay"
            CREDIT_CARD -> "信用卡"
            DEMO -> "演示支付"
        }
    }
    
    fun getDescription(): String {
        return when (this) {
            WECHAT_PAY -> "使用微信支付"
            ALIPAY -> "使用支付宝"
            APPLE_PAY -> "使用Apple Pay"
            GOOGLE_PAY -> "使用Google Pay"
            CREDIT_CARD -> "使用信用卡"
            DEMO -> "演示模式支付"
        }
    }
    
    fun getIcon(): String {
        return when (this) {
            WECHAT_PAY -> "💚"
            ALIPAY -> "💙"
            APPLE_PAY -> "🍎"
            GOOGLE_PAY -> "🔵"
            CREDIT_CARD -> "💳"
            DEMO -> "🎭"
        }
    }
}

enum class PurchaseStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}
