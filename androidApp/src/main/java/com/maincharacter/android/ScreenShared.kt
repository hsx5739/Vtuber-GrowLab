package com.maincharacter.android

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun PlaceholderScreen(
    title: String,
    summary: String,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC8D1FF)
                )
            }
        }

        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(primaryLabel)
        }

        if (secondaryLabel != null && onSecondaryClick != null) {
            FilledTonalButton(
                onClick = onSecondaryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(secondaryLabel)
            }
        }
    }
}

@Composable
internal fun SampledResourceImage(
    resourceId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    reqHeightDp: Dp,
    useRgb565: Boolean = false
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val reqWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val reqHeightPx = with(density) { reqHeightDp.roundToPx() }

    val bitmap = remember(resourceId, reqWidthPx, reqHeightPx, useRgb565) {
        decodeSampledBitmapFromResource(
            resources = context.resources,
            resourceId = resourceId,
            reqWidth = reqWidthPx,
            reqHeight = reqHeightPx,
            useRgb565 = useRgb565
        )?.asImageBitmap()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

internal fun decodeSampledBitmapFromResource(
    resources: Resources,
    resourceId: Int,
    reqWidth: Int,
    reqHeight: Int,
    useRgb565: Boolean
): Bitmap? {
    val boundsOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(resources, resourceId, boundsOptions)

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)
        inPreferredConfig = if (useRgb565) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
        inDither = useRgb565
        inJustDecodeBounds = false
    }

    return BitmapFactory.decodeResource(resources, resourceId, decodeOptions)
}

internal fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize.coerceAtLeast(1)
}

