package com.maincharacter.android

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

internal class OpenAiResponsesClient {
    fun generateReply(userMessage: String): String {
        val apiKey = BuildConfig.OPENAI_API_KEY.trim()
        if (apiKey.isEmpty()) {
            throw IllegalStateException("尚未配置 OPENAI_API_KEY，请先在 local.properties 中填写。")
        }

        val requestBody = JSONObject().apply {
            put("model", BuildConfig.OPENAI_MODEL)
            put(
                "input",
                JSONArray()
                    .put(
                        JSONObject().apply {
                            put("role", "system")
                            put(
                                "content",
                                JSONArray().put(
                                    JSONObject().apply {
                                        put("type", "input_text")
                                        put(
                                            "text",
                                            "你是用户的温柔陪伴角色。回复要自然、简短、口语化，优先用简体中文，通常控制在两三句话内。"
                                        )
                                    }
                                )
                            )
                        }
                    )
                    .put(
                        JSONObject().apply {
                            put("role", "user")
                            put(
                                "content",
                                JSONArray().put(
                                    JSONObject().apply {
                                        put("type", "input_text")
                                        put("text", userMessage)
                                    }
                                )
                            )
                        }
                    )
            )
            put("max_output_tokens", 160)
        }

        val connection = (
            URL("${BuildConfig.OPENAI_BASE_URL.trimEnd('/')}/v1/responses").openConnection()
                as HttpURLConnection
            ).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 30000
            doInput = true
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
        }

        return try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(requestBody.toString())
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(connection, statusCode in 200..299)

            if (statusCode !in 200..299) {
                throw IllegalStateException(parseErrorMessage(responseText))
            }

            parseReplyText(responseText)
        } catch (_: ConnectException) {
            throw IllegalStateException("无法连接 OpenAI 服务，请检查当前网络。")
        } catch (_: SocketTimeoutException) {
            throw IllegalStateException("OpenAI 响应超时，请稍后再试。")
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(connection: HttpURLConnection, success: Boolean): String {
        val stream = if (success) connection.inputStream else connection.errorStream
        if (stream == null) return ""

        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            buildString {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    append(line)
                }
            }
        }
    }

    private fun parseReplyText(responseText: String): String {
        val json = JSONObject(responseText)

        json.optString("output_text")
            .takeIf { it.isNotBlank() }
            ?.let { return it }

        val output = json.optJSONArray("output") ?: JSONArray()
        for (i in 0 until output.length()) {
            val item = output.optJSONObject(i) ?: continue
            val content = item.optJSONArray("content") ?: continue
            for (j in 0 until content.length()) {
                val contentItem = content.optJSONObject(j) ?: continue
                contentItem.optString("text")
                    .takeIf { it.isNotBlank() }
                    ?.let { return it }
            }
        }

        throw IllegalStateException("模型没有返回可显示的回复内容。")
    }

    private fun parseErrorMessage(responseText: String): String {
        return runCatching {
            val json = JSONObject(responseText)
            json.optJSONObject("error")?.optString("message")
        }.getOrNull().orEmpty().ifBlank {
            "请求失败，请稍后重试。"
        }
    }
}
