package com.atom.bluetoothfitnessapplication.presentation.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.atom.bluetoothfitnessapplication.R
import com.atom.bluetoothfitnessapplication.presentation.activities.MainActivity
import com.atom.bluetoothfitnessapplication.utilities.Constants

class QuickStartWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val isConnected = prefs.getBoolean(Constants.KEY_BLUETOOTH_CONNECTED, false)

        provideContent {
            GlanceTheme {
                Content(context, isConnected)
            }
        }
    }

    @Composable
    private fun Content(context: Context, isConnected: Boolean) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_background))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.padding(bottom = 8.dp)
            ) {
                Image(
                    provider = ImageProvider(
                        if (isConnected) R.drawable.round_bluetooth_connected_24
                        else R.drawable.round_bluetooth_disabled_24
                    ),
                    contentDescription = null,
                    modifier = GlanceModifier.size(18.dp)
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = if (isConnected) "Sensor Ready" else "Disconnected",
                    style = TextStyle(
                        color = ColorProvider(R.color.white),
                        fontSize = 12.sp
                    )
                )
            }

            Text(
                text = "Quick Start",
                style = TextStyle(
                    color = ColorProvider(R.color.white),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(12.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WidgetActionChip(context, context.getString(R.string.push_up), R.drawable.push_ups, "Push")
                Spacer(GlanceModifier.width(10.dp))
                WidgetActionChip(context, context.getString(R.string.plank), R.drawable.plank, "Plank")
                Spacer(GlanceModifier.width(10.dp))
                WidgetActionChip(context, context.getString(R.string.skipping), R.drawable.skipping, "Skip")
            }
        }
    }

    @Composable
    private fun WidgetActionChip(context: Context, exerciseName: String, iconRes: Int, label: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_EXERCISE", exerciseName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Column(
            modifier = GlanceModifier
                .width(64.dp)
                .clickable(actionStartActivity(intent)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = GlanceModifier
                    .size(44.dp)
                    .background(ImageProvider(R.drawable.chip_background)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = null,
                    modifier = GlanceModifier.size(28.dp)
                )
            }
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(R.color.white),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

class QuickStartWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickStartWidget()
}
