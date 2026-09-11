package com.atom.bluetoothfitnessapplication.presentation.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.atom.bluetoothfitnessapplication.R
import com.atom.bluetoothfitnessapplication.utilities.Constants

import java.util.Locale

class PerformanceSnapshotWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val type = prefs.getString(Constants.KEY_LAST_EXERCISE_TYPE, "No Data") ?: "No Data"
        val value = prefs.getFloat(Constants.KEY_LAST_EXERCISE_VALUE, 0f)
        val unit = prefs.getString(Constants.KEY_LAST_EXERCISE_UNIT, "") ?: ""
        val secondary = prefs.getString(Constants.KEY_LAST_EXERCISE_SECONDARY, "") ?: ""

        provideContent {
            GlanceTheme {
                Content(type, value, unit, secondary)
            }
        }
    }

    @Composable
    private fun Content(type: String, value: Float, unit: String, secondary: String) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_background))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Last Session",
                    style = TextStyle(color = ColorProvider(R.color.grey_six), fontSize = 12.sp)
                )
                Text(
                    text = type,
                    style = TextStyle(
                        color = ColorProvider(R.color.white),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (value > 0) String.format(Locale.getDefault(), "%.1f %s", value, unit) else "---",
                    style = TextStyle(
                        color = ColorProvider(R.color.mint_green),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (secondary.isNotEmpty()) {
                    Text(
                        text = secondary,
                        style = TextStyle(color = ColorProvider(R.color.grey_six), fontSize = 12.sp)
                    )
                }
            }

            Box(
                modifier = GlanceModifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_progress_ring_placeholder),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
        }
    }
}

class PerformanceSnapshotWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PerformanceSnapshotWidget()
}
