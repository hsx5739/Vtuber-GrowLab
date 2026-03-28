package com.maincharacter.android

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
    private var textToSpeech: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false
    private var pendingSpeech: Pair<String, String>? = null

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return

        textToSpeech?.language = Locale.SIMPLIFIED_CHINESE
        isReady = true
        pendingSpeech?.let { (text, utteranceId) ->
            speakNow(text, utteranceId)
            pendingSpeech = null
        }
    }

    fun speak(text: String, utteranceId: String) {
        stop()
        if (text.isBlank()) return

        if (!isReady) {
            pendingSpeech = text to utteranceId
            return
        }

        speakNow(text, utteranceId)
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
    }

    private fun speakNow(text: String, utteranceId: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
}
