package com.maincharacter.shared.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HealthDataIntegration {

    private val _permissionState = MutableStateFlow(
        PermissionState(
            isAuthorized = false,
            isDenied = false,
            isNotDetermined = true,
            authorizedTypes = emptySet(),
            deniedTypes = emptySet(),
            notDeterminedTypes = HealthDataType.entries.toSet()
        )
    )
    private val _healthData = MutableStateFlow(emptyHealthData())
    private val _healthStats = MutableStateFlow(emptyHealthStats())

    val permissionState: StateFlow<PermissionState> = _permissionState
    val healthData: StateFlow<HealthData> = _healthData
    val healthStats: StateFlow<HealthStats> = _healthStats

    fun checkPermissions(): PermissionCheckResult =
        PermissionCheckResult(
            success = true,
            permissionState = _permissionState.value,
            error = null
        )

    fun requestPermissions(): PermissionRequestResult =
        PermissionRequestResult(
            success = false,
            permissionState = _permissionState.value,
            newlyAuthorized = emptySet(),
            newlyDenied = emptySet(),
            error = "Health platform integration is not configured yet."
        )

    fun getHealthDataTypes(): List<HealthDataType> = HealthDataType.entries

    fun queryHealthData(
        dataType: HealthDataType,
        startTime: Long,
        endTime: Long
    ): HealthDataQueryResult = HealthDataQueryResult(
        success = false,
        data = null,
        error = "Health data query is not implemented yet."
    )

    fun querySteps(startTime: Long, endTime: Long): StepsData? = null

    fun queryDistance(startTime: Long, endTime: Long): DistanceData? = null

    fun queryActiveEnergy(startTime: Long, endTime: Long): ActiveEnergyData? = null

    fun queryHeartRate(startTime: Long, endTime: Long): HeartRateData? = null

    fun querySleep(startTime: Long, endTime: Long): SleepData? = null

    fun queryWorkouts(startTime: Long, endTime: Long): List<WorkoutData> = emptyList()

    fun queryTodayData(): TodayHealthData? = TodayHealthData(
        steps = 0,
        distance = 0f,
        activeEnergy = 0,
        sleep = 0f,
        workouts = emptyList(),
        date = ""
    )

    fun queryWeekData(): WeekHealthData? = WeekHealthData(
        dailyData = emptyList(),
        totalSteps = 0,
        totalDistance = 0f,
        totalActiveEnergy = 0,
        averageSleep = 0f,
        totalWorkouts = 0
    )

    fun queryMonthData(): MonthHealthData? = MonthHealthData(
        weeklyData = emptyList(),
        totalSteps = 0,
        totalDistance = 0f,
        totalActiveEnergy = 0,
        averageSleep = 0f,
        totalWorkouts = 0
    )

    fun getStepsGoal(): Int = 10000

    fun getDistanceGoal(): Float = 5000f

    fun getActiveEnergyGoal(): Int = 500

    fun getSleepGoal(): Int = 8

    fun isGoalMet(dataType: HealthDataType, value: Float): Boolean = when (dataType) {
        HealthDataType.STEPS -> value >= getStepsGoal()
        HealthDataType.DISTANCE -> value >= getDistanceGoal()
        HealthDataType.ACTIVE_ENERGY -> value >= getActiveEnergyGoal()
        HealthDataType.SLEEP -> value >= getSleepGoal()
        HealthDataType.HEART_RATE, HealthDataType.WORKOUTS -> false
    }

    fun getGoalProgress(dataType: HealthDataType): Float = 0f

    fun getGoalPercentage(dataType: HealthDataType): Float = 0f

    fun getHealthSummary(): HealthSummary = HealthSummary(
        stepsScore = 0f,
        distanceScore = 0f,
        energyScore = 0f,
        sleepScore = 0f,
        workoutScore = 0f,
        overallScore = 0f,
        date = ""
    )

    fun getHealthRecommendations(): List<HealthRecommendation> = emptyList()

    fun getHealthScore(): Float = 0f

    fun getHealthTrend(): HealthTrend = HealthTrend.UNKNOWN

    fun syncHealthData(): SyncResult = SyncResult(
        success = false,
        syncedDataTypes = emptySet(),
        syncDuration = 0L,
        error = "Health platform integration is not configured yet."
    )

    fun clearHealthData() {
        _healthData.value = emptyHealthData()
        _healthStats.value = emptyHealthStats()
    }
}

data class PermissionState(
    val isAuthorized: Boolean,
    val isDenied: Boolean,
    val isNotDetermined: Boolean,
    val authorizedTypes: Set<HealthDataType>,
    val deniedTypes: Set<HealthDataType>,
    val notDeterminedTypes: Set<HealthDataType>
) {
    fun canAccess(dataType: HealthDataType): Boolean = dataType in authorizedTypes

    fun getAuthorizationRate(): Float = rateOf(authorizedTypes.size)

    fun getDeniedRate(): Float = rateOf(deniedTypes.size)

    fun getNotDeterminedRate(): Float = rateOf(notDeterminedTypes.size)

    private fun rateOf(size: Int): Float {
        val total = HealthDataType.entries.size
        return if (total == 0) 0f else size * 100f / total
    }
}

data class PermissionCheckResult(
    val success: Boolean,
    val permissionState: PermissionState,
    val error: String?
)

data class PermissionRequestResult(
    val success: Boolean,
    val permissionState: PermissionState,
    val newlyAuthorized: Set<HealthDataType>,
    val newlyDenied: Set<HealthDataType>,
    val error: String?
)

data class HealthData(
    val steps: StepsData?,
    val distance: DistanceData?,
    val activeEnergy: ActiveEnergyData?,
    val heartRate: HeartRateData?,
    val sleep: SleepData?,
    val workouts: List<WorkoutData>,
    val timestamp: Long
) {
    fun hasStepsData(): Boolean = steps != null
    fun hasDistanceData(): Boolean = distance != null
    fun hasActiveEnergyData(): Boolean = activeEnergy != null
    fun hasHeartRateData(): Boolean = heartRate != null
    fun hasSleepData(): Boolean = sleep != null
    fun hasWorkoutData(): Boolean = workouts.isNotEmpty()

    fun getTotalDataTypes(): Int =
        listOf(
            hasStepsData(),
            hasDistanceData(),
            hasActiveEnergyData(),
            hasHeartRateData(),
            hasSleepData(),
            hasWorkoutData()
        ).count { it }

    fun getAvailableDataTypes(): List<HealthDataType> = buildList {
        if (hasStepsData()) add(HealthDataType.STEPS)
        if (hasDistanceData()) add(HealthDataType.DISTANCE)
        if (hasActiveEnergyData()) add(HealthDataType.ACTIVE_ENERGY)
        if (hasHeartRateData()) add(HealthDataType.HEART_RATE)
        if (hasSleepData()) add(HealthDataType.SLEEP)
        if (hasWorkoutData()) add(HealthDataType.WORKOUTS)
    }
}

data class StepsData(
    val count: Int,
    val startTime: Long,
    val endTime: Long,
    val hourlyData: Map<Int, Int>
) {
    fun getAveragePerHour(): Float =
        if (hourlyData.isEmpty()) 0f else count.toFloat() / hourlyData.size

    fun getAveragePerMinute(): Float {
        val minutes = (endTime - startTime) / 60000f
        return if (minutes <= 0f) 0f else count / minutes
    }

    fun getPeakHour(): Int? = hourlyData.maxByOrNull { it.value }?.key

    fun getPeakCount(): Int = hourlyData.values.maxOrNull() ?: 0
}

data class DistanceData(
    val meters: Float,
    val startTime: Long,
    val endTime: Long,
    val hourlyData: Map<Int, Float>
) {
    fun getKilometers(): Float = meters / 1000f
    fun getMiles(): Float = meters * 0.000621371f
    fun getAveragePerHour(): Float =
        if (hourlyData.isEmpty()) 0f else meters / hourlyData.size
    fun getPeakHour(): Int? = hourlyData.maxByOrNull { it.value }?.key
    fun getPeakDistance(): Float = hourlyData.values.maxOrNull() ?: 0f
}

data class ActiveEnergyData(
    val kilocalories: Int,
    val startTime: Long,
    val endTime: Long,
    val hourlyData: Map<Int, Int>
) {
    fun getAveragePerHour(): Float =
        if (hourlyData.isEmpty()) 0f else kilocalories.toFloat() / hourlyData.size
    fun getPeakHour(): Int? = hourlyData.maxByOrNull { it.value }?.key
    fun getPeakEnergy(): Int = hourlyData.values.maxOrNull() ?: 0
}

data class HeartRateData(
    val samples: List<HeartRateSample>,
    val startTime: Long,
    val endTime: Long
) {
    fun getAverage(): Float = if (samples.isEmpty()) 0f else samples.map { it.value }.average().toFloat()
    fun getMin(): Float? = samples.minByOrNull { it.value }?.value
    fun getMax(): Float? = samples.maxByOrNull { it.value }?.value
    fun getRestingHeartRate(): Float? =
        samples.sortedBy { it.value }
            .take((samples.size / 4).coerceAtLeast(1))
            .takeIf { it.isNotEmpty() }
            ?.map { it.value }
            ?.average()
            ?.toFloat()

    fun getHeartRateZones(): Map<HeartRateZone, Int> =
        samples.groupingBy {
            when {
                it.value < 60f -> HeartRateZone.RESTING
                it.value < 100f -> HeartRateZone.FAT_BURN
                it.value < 140f -> HeartRateZone.CARDIO
                else -> HeartRateZone.PEAK
            }
        }.eachCount()
}

data class HeartRateSample(
    val value: Float,
    val timestamp: Long
)

data class SleepData(
    val durationMs: Long,
    val startTime: Long,
    val endTime: Long,
    val stages: Map<SleepStage, Long>,
    val quality: SleepQuality
) {
    fun getHours(): Float = durationMs / 3600000f
    fun getMinutes(): Int = (durationMs / 60000L).toInt()
    fun getDeepSleepHours(): Float = (stages[SleepStage.DEEP] ?: 0L) / 3600000f
    fun getLightSleepHours(): Float = (stages[SleepStage.LIGHT] ?: 0L) / 3600000f
    fun getRemSleepHours(): Float = (stages[SleepStage.REM] ?: 0L) / 3600000f
    fun getAwakeHours(): Float = (stages[SleepStage.AWAKE] ?: 0L) / 3600000f
    fun getSleepEfficiency(): Float {
        if (durationMs <= 0L) return 0f
        val awake = stages[SleepStage.AWAKE] ?: 0L
        return ((durationMs - awake).coerceAtLeast(0L) * 100f) / durationMs
    }
}

data class WorkoutData(
    val id: String,
    val type: WorkoutType,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val distance: Float?,
    val energy: Int?,
    val heartRate: HeartRateData?
) {
    fun getDurationMinutes(): Float = durationMs / 60000f
    fun getDurationHours(): Float = durationMs / 3600000f
    fun getAveragePace(): Float? {
        val km = (distance ?: return null) / 1000f
        val minutes = getDurationMinutes()
        return if (km <= 0f || minutes <= 0f) null else minutes / km
    }
    fun getAverageHeartRate(): Float? = heartRate?.getAverage()
}

data class TodayHealthData(
    val steps: Int,
    val distance: Float,
    val activeEnergy: Int,
    val sleep: Float,
    val workouts: List<WorkoutData>,
    val date: String
) {
    fun getStepsProgress(): Float = (steps / 10000f).coerceIn(0f, 1f)
    fun getDistanceProgress(): Float = (distance / 5000f).coerceIn(0f, 1f)
    fun getActiveEnergyProgress(): Float = (activeEnergy / 500f).coerceIn(0f, 1f)
    fun getSleepProgress(): Float = (sleep / 8f).coerceIn(0f, 1f)
    fun getOverallProgress(): Float =
        (getStepsProgress() + getDistanceProgress() + getActiveEnergyProgress() + getSleepProgress()) / 4f
}

data class WeekHealthData(
    val dailyData: List<TodayHealthData>,
    val totalSteps: Int,
    val totalDistance: Float,
    val totalActiveEnergy: Int,
    val averageSleep: Float,
    val totalWorkouts: Int
) {
    fun getAverageSteps(): Int = if (dailyData.isEmpty()) 0 else totalSteps / dailyData.size
    fun getAverageDistance(): Float = if (dailyData.isEmpty()) 0f else totalDistance / dailyData.size
    fun getAverageActiveEnergy(): Int = if (dailyData.isEmpty()) 0 else totalActiveEnergy / dailyData.size
    fun getStepsTrend(): HealthTrend = HealthTrend.UNKNOWN
    fun getDistanceTrend(): HealthTrend = HealthTrend.UNKNOWN
    fun getActiveEnergyTrend(): HealthTrend = HealthTrend.UNKNOWN
    fun getSleepTrend(): HealthTrend = HealthTrend.UNKNOWN
}

data class MonthHealthData(
    val weeklyData: List<WeekHealthData>,
    val totalSteps: Int,
    val totalDistance: Float,
    val totalActiveEnergy: Int,
    val averageSleep: Float,
    val totalWorkouts: Int
) {
    fun getAverageStepsPerDay(): Int {
        val days = weeklyData.size * 7
        return if (days == 0) 0 else totalSteps / days
    }

    fun getAverageDistancePerDay(): Float {
        val days = weeklyData.size * 7
        return if (days == 0) 0f else totalDistance / days
    }

    fun getAverageActiveEnergyPerDay(): Int {
        val days = weeklyData.size * 7
        return if (days == 0) 0 else totalActiveEnergy / days
    }

    fun getStepsTrend(): HealthTrend = HealthTrend.UNKNOWN
    fun getDistanceTrend(): HealthTrend = HealthTrend.UNKNOWN
    fun getActiveEnergyTrend(): HealthTrend = HealthTrend.UNKNOWN
    fun getSleepTrend(): HealthTrend = HealthTrend.UNKNOWN
}

data class HealthStats(
    val totalQueries: Int,
    val successfulQueries: Int,
    val failedQueries: Int,
    val lastSyncTime: Long,
    val totalStepsRecorded: Long,
    val totalDistanceRecorded: Float,
    val totalEnergyRecorded: Long,
    val totalSleepRecorded: Long,
    val totalWorkoutsRecorded: Int
) {
    fun getSuccessRate(): Float = if (totalQueries == 0) 0f else successfulQueries * 100f / totalQueries
    fun getFailureRate(): Float = if (totalQueries == 0) 0f else failedQueries * 100f / totalQueries
    fun getAverageStepsPerQuery(): Float = if (totalQueries == 0) 0f else totalStepsRecorded.toFloat() / totalQueries
    fun getAverageDistancePerQuery(): Float = if (totalQueries == 0) 0f else totalDistanceRecorded / totalQueries
    fun getAverageEnergyPerQuery(): Float = if (totalQueries == 0) 0f else totalEnergyRecorded.toFloat() / totalQueries
    fun getAverageSleepPerQuery(): Float = if (totalQueries == 0) 0f else totalSleepRecorded.toFloat() / totalQueries
    fun getTimeSinceLastSync(): Long = if (lastSyncTime == 0L) 0L else System.currentTimeMillis() - lastSyncTime
    fun getDaysSinceLastSync(): Float = getTimeSinceLastSync() / 86400000f
}

data class HealthSummary(
    val stepsScore: Float,
    val distanceScore: Float,
    val energyScore: Float,
    val sleepScore: Float,
    val workoutScore: Float,
    val overallScore: Float,
    val date: String
) {
    fun getGrade(): HealthGrade = when {
        overallScore >= 90f -> HealthGrade.A
        overallScore >= 80f -> HealthGrade.B
        overallScore >= 70f -> HealthGrade.C
        overallScore >= 60f -> HealthGrade.D
        else -> HealthGrade.F
    }

    fun getFeedback(): String = when (getGrade()) {
        HealthGrade.A -> "Excellent health."
        HealthGrade.B -> "Good health."
        HealthGrade.C -> "Fair health."
        HealthGrade.D -> "Needs improvement."
        HealthGrade.F -> "Health score is very low."
    }

    fun getImprovementSuggestions(): List<String> = emptyList()
}

data class HealthRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val actionable: Boolean
)

data class SyncResult(
    val success: Boolean,
    val syncedDataTypes: Set<HealthDataType>,
    val syncDuration: Long,
    val error: String?
)

data class HealthDataQueryResult(
    val success: Boolean,
    val data: Any?,
    val error: String?
)

enum class HealthDataType {
    STEPS,
    DISTANCE,
    ACTIVE_ENERGY,
    HEART_RATE,
    SLEEP,
    WORKOUTS
}

enum class HeartRateZone {
    RESTING,
    FAT_BURN,
    CARDIO,
    PEAK
}

enum class SleepStage {
    AWAKE,
    REM,
    LIGHT,
    DEEP
}

enum class SleepQuality {
    POOR,
    FAIR,
    GOOD,
    EXCELLENT
}

enum class WorkoutType {
    RUNNING,
    WALKING,
    CYCLING,
    SWIMMING,
    STRENGTH_TRAINING,
    YOGA,
    OTHER
}

enum class HealthTrend {
    IMPROVING,
    STABLE,
    DECLINING,
    UNKNOWN
}

enum class HealthGrade {
    A,
    B,
    C,
    D,
    F
}

enum class RecommendationType {
    ACTIVITY,
    SLEEP,
    NUTRITION,
    STRESS_MANAGEMENT,
    HYDRATION,
    CONSISTENCY
}

enum class RecommendationPriority {
    HIGH,
    MEDIUM,
    LOW
}

private fun emptyHealthData(): HealthData = HealthData(
    steps = null,
    distance = null,
    activeEnergy = null,
    heartRate = null,
    sleep = null,
    workouts = emptyList(),
    timestamp = 0L
)

private fun emptyHealthStats(): HealthStats = HealthStats(
    totalQueries = 0,
    successfulQueries = 0,
    failedQueries = 0,
    lastSyncTime = 0L,
    totalStepsRecorded = 0L,
    totalDistanceRecorded = 0f,
    totalEnergyRecorded = 0L,
    totalSleepRecorded = 0L,
    totalWorkoutsRecorded = 0
)
