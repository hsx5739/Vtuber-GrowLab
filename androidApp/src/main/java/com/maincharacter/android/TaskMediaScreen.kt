package com.maincharacter.android

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.widget.MediaController
import android.widget.VideoView
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

private enum class MediaTaskStep {
    READY,
    PREVIEW
}

private enum class MediaTaskMode(
    val title: String,
    val summary: String,
    val actionLabel: String
) {
    PHOTO_UPLOAD(
        title = "上传拍照",
        summary = "从系统相册选择图片，选中后返回任务流并进入预览确认口径。",
        actionLabel = "选择照片"
    ),
    PHOTO_CAPTURE(
        title = "去拍照",
        summary = "进入拍照页前动态判断相机权限，授权后再调起拍照能力。",
        actionLabel = "开始拍摄"
    ),
    FOCUS_RECORD(
        title = "去录制",
        summary = "进入录制页前动态判断相机和录音权限，授权后再启动录制。",
        actionLabel = "开始录制"
    ),
    VIDEO_UPLOAD(
        title = "上传视频",
        summary = "从系统媒体库选择视频文件，上传成功后再回写任务完成状态。",
        actionLabel = "选择视频"
    )
}

private enum class PreviewMediaType {
    IMAGE,
    VIDEO
}

@Composable
fun PhotoUploadScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    onTaskCompleted: () -> Unit = {}
) {
    TaskMediaScreen(
        taskId = taskId,
        mode = MediaTaskMode.PHOTO_UPLOAD,
        onNavigateBack = onNavigateBack,
        onTaskCompleted = onTaskCompleted
    )
}

@Composable
fun PhotoCaptureScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    onTaskCompleted: () -> Unit = {}
) {
    TaskMediaScreen(
        taskId = taskId,
        mode = MediaTaskMode.PHOTO_CAPTURE,
        onNavigateBack = onNavigateBack,
        onTaskCompleted = onTaskCompleted
    )
}

@Composable
fun FocusRecordScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    onTaskCompleted: () -> Unit = {}
) {
    TaskMediaScreen(
        taskId = taskId,
        mode = MediaTaskMode.FOCUS_RECORD,
        onNavigateBack = onNavigateBack,
        onTaskCompleted = onTaskCompleted
    )
}

@Composable
fun VideoUploadScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    onTaskCompleted: () -> Unit = {}
) {
    TaskMediaScreen(
        taskId = taskId,
        mode = MediaTaskMode.VIDEO_UPLOAD,
        onNavigateBack = onNavigateBack,
        onTaskCompleted = onTaskCompleted
    )
}

@Composable
private fun TaskMediaScreen(
    taskId: String,
    mode: MediaTaskMode,
    onNavigateBack: () -> Unit,
    onTaskCompleted: () -> Unit
) {
    val context = LocalContext.current
    val task = resolveTaskBoardTask(taskId)
    var feedback by remember(taskId, mode) { mutableStateOf("等待开始") }
    var latestUri by remember(taskId, mode) { mutableStateOf<Uri?>(null) }
    var step by remember(taskId, mode) { mutableStateOf(MediaTaskStep.READY) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        latestUri = uri
        step = if (uri != null) MediaTaskStep.PREVIEW else MediaTaskStep.READY
        feedback = if (uri != null) {
            "已选择照片，进入预览确认阶段。"
        } else {
            "未选择照片。"
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        latestUri = uri
        step = if (uri != null) MediaTaskStep.PREVIEW else MediaTaskStep.READY
        feedback = if (uri != null) {
            "已选择视频，进入预览确认阶段。"
        } else {
            "未选择视频。"
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        step = if (success && latestUri != null) MediaTaskStep.PREVIEW else MediaTaskStep.READY
        feedback = if (success && latestUri != null) {
            "拍摄完成，已进入预览确认阶段。"
        } else {
            "拍摄未完成。"
        }
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        step = if (success && latestUri != null) MediaTaskStep.PREVIEW else MediaTaskStep.READY
        feedback = if (success && latestUri != null) {
            "录制完成，已进入预览确认阶段。"
        } else {
            "录制未完成。"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (canHandleIntent(context, Intent(MediaStore.ACTION_IMAGE_CAPTURE))) {
                val uri = createTempMediaUri(context, "jpg")
                latestUri = uri
                if (uri != null) {
                    takePictureLauncher.launch(uri)
                } else {
                    feedback = "临时照片文件创建失败。"
                }
            } else {
                feedback = "当前设备没有可用的拍照应用，无法继续拍摄。"
            }
        } else {
            feedback = "请先开启相机权限。"
        }
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val cameraGranted = result[Manifest.permission.CAMERA] == true
        val audioGranted = result[Manifest.permission.RECORD_AUDIO] == true
        if (cameraGranted && audioGranted) {
            if (canHandleIntent(context, Intent(MediaStore.ACTION_VIDEO_CAPTURE))) {
                val uri = createTempMediaUri(context, "mp4")
                latestUri = uri
                if (uri != null) {
                    captureVideoLauncher.launch(uri)
                } else {
                    feedback = "临时视频文件创建失败。"
                }
            } else {
                feedback = "当前设备没有可用的录像应用，无法继续录制。"
            }
        } else {
            feedback = "请先开启相机与录音权限。"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = mode.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = task?.title ?: "未找到任务",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF2F4FF)
                )
                Text(
                    text = mode.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC8D1FF)
                )
            }
        }

        Button(
            onClick = {
                when (mode) {
                    MediaTaskMode.PHOTO_UPLOAD -> {
                        step = MediaTaskStep.READY
                        feedback = "相册选择通常不需要单独申请权限，准备打开系统照片选择器。"
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }

                    MediaTaskMode.VIDEO_UPLOAD -> {
                        step = MediaTaskStep.READY
                        feedback = "准备打开系统视频选择器。"
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }

                    MediaTaskMode.PHOTO_CAPTURE -> {
                        if (hasPermission(context, Manifest.permission.CAMERA)) {
                            if (canHandleIntent(context, Intent(MediaStore.ACTION_IMAGE_CAPTURE))) {
                                val uri = createTempMediaUri(context, "jpg")
                                latestUri = uri
                                if (uri != null) {
                                    step = MediaTaskStep.READY
                                    takePictureLauncher.launch(uri)
                                } else {
                                    feedback = "临时照片文件创建失败。"
                                }
                            } else {
                                feedback = "当前设备没有可用的拍照应用，无法继续拍摄。"
                            }
                        } else {
                            feedback = "检测到未授权相机权限，正在请求授权。"
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }

                    MediaTaskMode.FOCUS_RECORD -> {
                        val cameraGranted = hasPermission(context, Manifest.permission.CAMERA)
                        val audioGranted = hasPermission(context, Manifest.permission.RECORD_AUDIO)
                        if (cameraGranted && audioGranted) {
                            if (canHandleIntent(context, Intent(MediaStore.ACTION_VIDEO_CAPTURE))) {
                                val uri = createTempMediaUri(context, "mp4")
                                latestUri = uri
                                if (uri != null) {
                                    step = MediaTaskStep.READY
                                    captureVideoLauncher.launch(uri)
                                } else {
                                    feedback = "临时视频文件创建失败。"
                                }
                            } else {
                                feedback = "当前设备没有可用的录像应用，无法继续录制。"
                            }
                        } else {
                            feedback = "检测到录制所需权限未完整授权，正在请求相机与录音权限。"
                            recordPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO
                                )
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(mode.actionLabel)
        }

        if (step == MediaTaskStep.PREVIEW && latestUri != null) {
            TaskMediaPreviewCard(
                mode = mode,
                latestUri = latestUri,
                onConfirm = {
                    markTaskCompleted(taskId)
                    feedback = when (mode) {
                        MediaTaskMode.PHOTO_UPLOAD,
                        MediaTaskMode.PHOTO_CAPTURE -> "已点击完成，下一步应回写任务 confirmed 记录并返回任务页。"

                        MediaTaskMode.FOCUS_RECORD -> "已点击确定，下一步应写入专注任务完成记录并清理临时视频。"
                        MediaTaskMode.VIDEO_UPLOAD -> "已点击上传完成，下一步应结算挑战奖励并返回任务详情。"
                    }
                    onTaskCompleted()
                },
                onRetry = {
                    feedback = when (mode) {
                        MediaTaskMode.PHOTO_UPLOAD -> "已返回重新选图流程。"
                        MediaTaskMode.PHOTO_CAPTURE -> "已返回重新拍摄流程。"
                        MediaTaskMode.FOCUS_RECORD -> "已返回重新录制流程。"
                        MediaTaskMode.VIDEO_UPLOAD -> "已返回重新选视频流程。"
                    }
                    latestUri = null
                    step = MediaTaskStep.READY
                }
            )
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF171C39)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "当前状态",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC8D1FF)
                )
                if (latestUri != null) {
                    Text(
                        text = "临时媒体：$latestUri",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8C4F6)
                    )
                }
                if (mode == MediaTaskMode.FOCUS_RECORD) {
                    Text(
                        text = "录制页会在按钮点击时动态判断相机与录音权限。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD6C7FF)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskMediaPreviewCard(
    mode: MediaTaskMode,
    latestUri: Uri?,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val previewType = when (mode) {
        MediaTaskMode.PHOTO_UPLOAD,
        MediaTaskMode.PHOTO_CAPTURE -> PreviewMediaType.IMAGE

        MediaTaskMode.FOCUS_RECORD,
        MediaTaskMode.VIDEO_UPLOAD -> PreviewMediaType.VIDEO
    }
    val previewBitmap = remember(latestUri, previewType) {
        latestUri?.let { loadPreviewBitmap(context, it, previewType) }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "预览确认",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = when (mode) {
                    MediaTaskMode.PHOTO_UPLOAD,
                    MediaTaskMode.PHOTO_CAPTURE -> "照片已准备完成，按需求需要由用户点击“完成”才真正记为任务完成。"

                    MediaTaskMode.FOCUS_RECORD -> "视频录制完成，按需求需要点击“确定”才真正记为专注任务完成。"
                    MediaTaskMode.VIDEO_UPLOAD -> "挑战视频已选中，按需求需要点击“上传完成”后才真正结算奖励。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Text(
                text = "媒体地址：$latestUri",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB8C4F6)
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF20264A)
            ) {
                if (previewType == PreviewMediaType.VIDEO && latestUri != null) {
                    VideoPreviewPlayer(
                        videoUri = latestUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                } else if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "任务媒体预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (previewType == PreviewMediaType.IMAGE) {
                            "当前无法读取照片预览，但已进入确认流程。"
                        } else {
                            "当前无法读取视频首帧预览，但已进入确认流程。"
                        },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC8D1FF)
                    )
                }
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (mode) {
                        MediaTaskMode.PHOTO_UPLOAD,
                        MediaTaskMode.PHOTO_CAPTURE -> "完成"

                        MediaTaskMode.FOCUS_RECORD -> "确定"
                        MediaTaskMode.VIDEO_UPLOAD -> "上传完成"
                    }
                )
            }
            FilledTonalButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (mode) {
                        MediaTaskMode.PHOTO_UPLOAD,
                        MediaTaskMode.PHOTO_CAPTURE -> "重新拍摄"

                        MediaTaskMode.FOCUS_RECORD -> "重新录制"
                        MediaTaskMode.VIDEO_UPLOAD -> "重新选择视频"
                    }
                )
            }
        }
    }
}

@Composable
private fun VideoPreviewPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            VideoView(viewContext).apply {
                val controller = MediaController(viewContext).also {
                    it.setAnchorView(this)
                }
                setMediaController(controller)
                setVideoURI(videoUri)
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    seekTo(1)
                    controller.show(0)
                }
            }
        },
        update = { videoView ->
            videoView.setVideoURI(videoUri)
            videoView.seekTo(1)
        }
    )
}

private fun hasPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun canHandleIntent(context: Context, intent: Intent): Boolean {
    return intent.resolveActivity(context.packageManager) != null
}

private fun loadPreviewBitmap(
    context: Context,
    uri: Uri,
    previewType: PreviewMediaType
): Bitmap? {
    return runCatching {
        when (previewType) {
            PreviewMediaType.IMAGE -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.setTargetSampleSize(2)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            }

            PreviewMediaType.VIDEO -> {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } finally {
                    retriever.release()
                }
            }
        }
    }.getOrNull()
}

private fun createTempMediaUri(context: Context, extension: String): Uri? {
    val mediaDir = File(context.cacheDir, "media").apply { mkdirs() }
    val tempFile = File.createTempFile("task_", ".$extension", mediaDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
