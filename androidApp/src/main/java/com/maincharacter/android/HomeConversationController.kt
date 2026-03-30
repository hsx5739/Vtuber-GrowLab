package com.maincharacter.android

import android.media.AudioAttributes
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

internal class HomeConversationController(
    context: Context
) {
    var inputText by mutableStateOf("")
        private set

    var replyText by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var activeRequestId by mutableIntStateOf(0)
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val chatClient = OpenAiResponsesClient()
    private val speaker = CompanionSpeaker(context.applicationContext)
    private var requestJob: Job? = null
    private var autoDismissJob: Job? = null

    fun onInputChange(value: String) {
        inputText = value
    }

    fun sendCurrentMessage() {
        val message = inputText.trim()
        if (message.isEmpty()) return

        activeRequestId += 1
        val requestId = activeRequestId

        requestJob?.cancel()
        autoDismissJob?.cancel()
        speaker.stop()

        inputText = ""
        replyText = null
        isLoading = true

        requestJob = scope.launch {
            try {
                val reply = chatClient.generateReply(message)
                if (requestId != activeRequestId) return@launch

                replyText = reply
                isLoading = false
                speaker.speak(reply, "home-reply-$requestId")
                scheduleReplyDismiss(requestId)
            } catch (_: CancellationException) {
                if (requestId == activeRequestId) {
                    isLoading = false
                }
            } catch (error: Exception) {
                if (requestId != activeRequestId) return@launch

                replyText = error.message ?: "当前对话暂不可用，请稍后再试。"
                isLoading = false
                scheduleReplyDismiss(requestId)
            }
        }
    }

    fun speakSceneMessage(message: String, sceneIndex: Int): String? {
        return speaker.speak(message, "home-scene-$sceneIndex")
    }

    fun dispose() {
        requestJob?.cancel()
        autoDismissJob?.cancel()
        speaker.shutdown()
        scope.cancel()
    }

    private fun scheduleReplyDismiss(requestId: Int) {
        autoDismissJob?.cancel()
        autoDismissJob = scope.launch {
            delay(5_000)
            if (requestId == activeRequestId) {
                replyText = null
            }
        }
    }
}

private class CompanionSpeaker(
    context: Context
) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private var textToSpeech: TextToSpeech? = null
    private var isReady = false
    private var pendingSpeech: Pair<String, String>? = null
    private var unavailableReason: String? = "语音引擎正在初始化"
    private var initStartedAt = 0L

    init {
        initializeEngine()
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            unavailableReason = "系统语音引擎初始化失败，请检查设备 TTS 设置"
            return
        }

        val tts = textToSpeech ?: run {
            unavailableReason = "系统语音引擎不可用"
            return
        }

        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )

        val availableLocale = listOf(
            Locale.SIMPLIFIED_CHINESE,
            Locale.CHINESE,
            Locale.getDefault(),
            Locale.US
        ).firstOrNull { locale ->
            val result = tts.isLanguageAvailable(locale)
            result >= TextToSpeech.LANG_AVAILABLE
        }

        if (availableLocale == null) {
            unavailableReason = "当前设备没有可用的语音语言包"
            return
        }

        val languageResult = tts.setLanguage(availableLocale)
        if (languageResult < TextToSpeech.LANG_AVAILABLE) {
            unavailableReason = "语音语言设置失败，请安装系统 TTS 语言包"
            return
        }

        tts.setSpeechRate(1.0f)
        tts.setPitch(1.0f)
        isReady = true
        unavailableReason = null
        pendingSpeech?.let { (text, utteranceId) ->
            speakNow(text, utteranceId)
            pendingSpeech = null
        }
    }

    fun speak(text: String, utteranceId: String): String? {
        stop()
        if (text.isBlank()) return "没有可朗读的文本"

        if (!isReady && initStartedAt != 0L && System.currentTimeMillis() - initStartedAt > 4_000) {
            initializeEngine()
        }

        if (!isReady) {
            val tts = textToSpeech
            if (tts == null) {
                initializeEngine()
                return "系统语音引擎不可用，正在尝试重新初始化"
            }
            pendingSpeech = text to utteranceId
            return if (System.currentTimeMillis() - initStartedAt > 2_000) {
                "语音引擎启动超时，请检查系统 TTS 引擎或语言包"
            } else {
                unavailableReason
            }
        }

        return speakNow(text, utteranceId)
    }

    fun stop() {
        pendingSpeech = null
        textToSpeech?.stop()
    }

    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
        initStartedAt = 0L
    }

    private fun speakNow(text: String, utteranceId: String): String? {
        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            ?: return "系统语音引擎不可用"
        return if (result == TextToSpeech.SUCCESS) null else "语音播放失败，请检查媒体音量和 TTS 设置"
    }

    private fun initializeEngine() {
        textToSpeech?.shutdown()
        isReady = false
        initStartedAt = System.currentTimeMillis()
        unavailableReason = "语音引擎正在初始化"
        textToSpeech = TextToSpeech(appContext, this)
    }
}
