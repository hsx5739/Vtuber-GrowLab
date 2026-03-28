package com.maincharacter.shared.service

import com.maincharacter.shared.model.*

interface TaskProcessor {
    fun canStart(instance: TaskInstance, config: TaskConfig?): Boolean
    fun start(instance: TaskInstance, config: TaskConfig?): TaskProcessingResult
    fun updateProgress(instance: TaskInstance, progress: TaskProgress): TaskProcessingResult
    fun canComplete(instance: TaskInstance): Boolean
    fun complete(instance: TaskInstance): TaskProcessingResult
    fun canVerify(instance: TaskInstance): Boolean
    fun verify(instance: TaskInstance): TaskProcessingResult
    fun getProcessorType(): TaskType
}

class TaskProcessorRegistry {
    
    private val processors = mutableMapOf<TaskType, TaskProcessor>()
    
    fun register(processor: TaskProcessor) {
        processors[processor.getProcessorType()] = processor
    }
    
    fun unregister(type: TaskType) {
        processors.remove(type)
    }
    
    fun getProcessor(type: TaskType): TaskProcessor? {
        return processors[type]
    }
    
    fun getProcessorOrThrow(type: TaskType): TaskProcessor {
        return processors[type] 
            ?: throw IllegalArgumentException("No processor registered for type: $type")
    }
    
    fun hasProcessor(type: TaskType): Boolean {
        return processors.containsKey(type)
    }
    
    fun getRegisteredTypes(): Set<TaskType> {
        return processors.keys
    }
    
    fun initializeDefaultProcessors() {
        register(SelfReportTaskProcessor())
        register(TimerTaskProcessor())
        register(HealthDataTaskProcessor())
        register(DialogueTaskProcessor())
        register(ImageRecognitionTaskProcessor())
        register(VoiceDetectionTaskProcessor())
        register(EventTaskProcessor())
    }
}

class SelfReportTaskProcessor : TaskProcessor {
    
    override fun canStart(instance: TaskInstance, config: TaskConfig?): Boolean {
        return instance.state == TaskState.AVAILABLE
    }
    
    override fun start(instance: TaskInstance, config: TaskConfig?): TaskProcessingResult {
        if (!canStart(instance, config)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be started"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.start(),
            error = null
        )
    }
    
    override fun updateProgress(instance: TaskInstance, progress: TaskProgress): TaskProcessingResult {
        if (instance.state != TaskState.IN_PROGRESS) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task is not in progress"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.updateProgress(progress),
            error = null
        )
    }
    
    override fun canComplete(instance: TaskInstance): Boolean {
        return instance.state == TaskState.IN_PROGRESS
    }
    
    override fun complete(instance: TaskInstance): TaskProcessingResult {
        if (!canComplete(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be completed"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.complete(),
            error = null
        )
    }
    
    override fun canVerify(instance: TaskInstance): Boolean {
        return instance.state == TaskState.COMPLETED && !instance.isVerificationExpired()
    }
    
    override fun verify(instance: TaskInstance): TaskProcessingResult {
        if (!canVerify(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be verified"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.verify(),
            error = null
        )
    }
    
    override fun getProcessorType(): TaskType {
        return TaskType.A_SELF_REPORT
    }
}

class TimerTaskProcessor : TaskProcessor {
    
    override fun canStart(instance: TaskInstance, config: TaskConfig?): Boolean {
        return instance.state == TaskState.AVAILABLE && config?.durationSec != null
    }
    
    override fun start(instance: TaskInstance, config: TaskConfig?): TaskProcessingResult {
        if (!canStart(instance, config)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be started or missing duration config"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.start(),
            error = null
        )
    }
    
    override fun updateProgress(instance: TaskInstance, progress: TaskProgress): TaskProcessingResult {
        if (instance.state != TaskState.IN_PROGRESS) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task is not in progress"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.updateProgress(progress),
            error = null
        )
    }
    
    override fun canComplete(instance: TaskInstance): Boolean {
        return instance.state == TaskState.IN_PROGRESS && instance.progress.isComplete()
    }
    
    override fun complete(instance: TaskInstance): TaskProcessingResult {
        if (!canComplete(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be completed or progress not complete"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.complete(),
            error = null
        )
    }
    
    override fun canVerify(instance: TaskInstance): Boolean {
        return instance.state == TaskState.COMPLETED && !instance.isVerificationExpired()
    }
    
    override fun verify(instance: TaskInstance): TaskProcessingResult {
        if (!canVerify(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be verified"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.verify(),
            error = null
        )
    }
    
    override fun getProcessorType(): TaskType {
        return TaskType.B_TIMER
    }
}

class HealthDataTaskProcessor : TaskProcessor {
    
    override fun canStart(instance: TaskInstance, config: TaskConfig?): Boolean {
        return instance.state == TaskState.AVAILABLE
    }
    
    override fun start(instance: TaskInstance, config: TaskConfig?): TaskProcessingResult {
        if (!canStart(instance, config)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be started"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.start(),
            error = null
        )
    }
    
    override fun updateProgress(instance: TaskInstance, progress: TaskProgress): TaskProcessingResult {
        return TaskProcessingResult(
            success = false,
            instance = instance,
            error = "Health data tasks do not support manual progress updates"
        )
    }
    
    override fun canComplete(instance: TaskInstance): Boolean {
        return instance.state == TaskState.IN_PROGRESS
    }
    
    override fun complete(instance: TaskInstance): TaskProcessingResult {
        if (!canComplete(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be completed"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.complete(),
            error = null
        )
    }
    
    override fun canVerify(instance: TaskInstance): Boolean {
        return instance.state == TaskState.COMPLETED && !instance.isVerificationExpired()
    }
    
    override fun verify(instance: TaskInstance): TaskProcessingResult {
        if (!canVerify(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be verified"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.verify(),
            error = null
        )
    }
    
    override fun getProcessorType(): TaskType {
        return TaskType.C_HEALTH_DATA
    }
}

class DialogueTaskProcessor : TaskProcessor {
    
    override fun canStart(instance: TaskInstance, config: TaskConfig?): Boolean {
        return instance.state == TaskState.AVAILABLE && config?.dialogueId != null
    }
    
    override fun start(instance: TaskInstance, config: TaskConfig?): TaskProcessingResult {
        if (!canStart(instance, config)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be started or missing dialogue config"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.start(),
            error = null
        )
    }
    
    override fun updateProgress(instance: TaskInstance, progress: TaskProgress): TaskProcessingResult {
        if (instance.state != TaskState.IN_PROGRESS) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task is not in progress"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.updateProgress(progress),
            error = null
        )
    }
    
    override fun canComplete(instance: TaskInstance): Boolean {
        return instance.state == TaskState.IN_PROGRESS && instance.progress.isComplete()
    }
    
    override fun complete(instance: TaskInstance): TaskProcessingResult {
        if (!canComplete(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be completed or progress not complete"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.complete(),
            error = null
        )
    }
    
    override fun canVerify(instance: TaskInstance): Boolean {
        return instance.state == TaskState.COMPLETED && !instance.isVerificationExpired()
    }
    
    override fun verify(instance: TaskInstance): TaskProcessingResult {
        if (!canVerify(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be verified"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.verify(),
            error = null
        )
    }
    
    override fun getProcessorType(): TaskType {
        return TaskType.D_DIALOGUE
    }
}

class ImageRecognitionTaskProcessor : TaskProcessor {
    
    override fun canStart(instance: TaskInstance, config: TaskConfig?): Boolean {
        return instance.state == TaskState.AVAILABLE
    }
    
    override fun start(instance: TaskInstance, config: TaskConfig?): TaskProcessingResult {
        if (!canStart(instance, config)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be started"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.start(),
            error = null
        )
    }
    
    override fun updateProgress(instance: TaskInstance, progress: TaskProgress): TaskProcessingResult {
        return TaskProcessingResult(
            success = false,
            instance = instance,
            error = "Image recognition tasks do not support manual progress updates"
        )
    }
    
    override fun canComplete(instance: TaskInstance): Boolean {
        return instance.state == TaskState.IN_PROGRESS
    }
    
    override fun complete(instance: TaskInstance): TaskProcessingResult {
        if (!canComplete(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be completed"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.complete(),
            error = null
        )
    }
    
    override fun canVerify(instance: TaskInstance): Boolean {
        return instance.state == TaskState.COMPLETED && !instance.isVerificationExpired()
    }
    
    override fun verify(instance: TaskInstance): TaskProcessingResult {
        if (!canVerify(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be verified"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.verify(),
            error = null
        )
    }
    
    override fun getProcessorType(): TaskType {
        return TaskType.F_IMAGE_RECOGNITION
    }
}

class VoiceDetectionTaskProcessor : TaskProcessor {
    
    override fun canStart(instance: TaskInstance, config: TaskConfig?): Boolean {
        return instance.state == TaskState.AVAILABLE
    }
    
    override fun start(instance: TaskInstance, config: TaskConfig?): TaskProcessingResult {
        if (!canStart(instance, config)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be started"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.start(),
            error = null
        )
    }
    
    override fun updateProgress(instance: TaskInstance, progress: TaskProgress): TaskProcessingResult {
        return TaskProcessingResult(
            success = false,
            instance = instance,
            error = "Voice detection tasks do not support manual progress updates"
        )
    }
    
    override fun canComplete(instance: TaskInstance): Boolean {
        return instance.state == TaskState.IN_PROGRESS
    }
    
    override fun complete(instance: TaskInstance): TaskProcessingResult {
        if (!canComplete(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be completed"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.complete(),
            error = null
        )
    }
    
    override fun canVerify(instance: TaskInstance): Boolean {
        return instance.state == TaskState.COMPLETED && !instance.isVerificationExpired()
    }
    
    override fun verify(instance: TaskInstance): TaskProcessingResult {
        if (!canVerify(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be verified"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.verify(),
            error = null
        )
    }
    
    override fun getProcessorType(): TaskType {
        return TaskType.G_VOICE_DETECTION
    }
}

class EventTaskProcessor : TaskProcessor {
    
    override fun canStart(instance: TaskInstance, config: TaskConfig?): Boolean {
        return instance.state == TaskState.AVAILABLE && config?.eventId != null
    }
    
    override fun start(instance: TaskInstance, config: TaskConfig?): TaskProcessingResult {
        if (!canStart(instance, config)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be started or missing event config"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.start(),
            error = null
        )
    }
    
    override fun updateProgress(instance: TaskInstance, progress: TaskProgress): TaskProcessingResult {
        if (instance.state != TaskState.IN_PROGRESS) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task is not in progress"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.updateProgress(progress),
            error = null
        )
    }
    
    override fun canComplete(instance: TaskInstance): Boolean {
        return instance.state == TaskState.IN_PROGRESS && instance.progress.isComplete()
    }
    
    override fun complete(instance: TaskInstance): TaskProcessingResult {
        if (!canComplete(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be completed or progress not complete"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.complete(),
            error = null
        )
    }
    
    override fun canVerify(instance: TaskInstance): Boolean {
        return instance.state == TaskState.COMPLETED && !instance.isVerificationExpired()
    }
    
    override fun verify(instance: TaskInstance): TaskProcessingResult {
        if (!canVerify(instance)) {
            return TaskProcessingResult(
                success = false,
                instance = instance,
                error = "Task cannot be verified"
            )
        }
        
        return TaskProcessingResult(
            success = true,
            instance = instance.verify(),
            error = null
        )
    }
    
    override fun getProcessorType(): TaskType {
        return TaskType.H_EVENT
    }
}

data class TaskProcessingResult(
    val success: Boolean,
    val instance: TaskInstance,
    val error: String?
)