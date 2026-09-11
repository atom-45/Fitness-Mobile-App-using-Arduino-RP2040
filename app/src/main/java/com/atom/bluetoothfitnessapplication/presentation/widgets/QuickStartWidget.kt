package com.atom.bluetoothfitnessapplication.presentation.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.atom.bluetoothfitnessapplication.R
import com.atom.bluetoothfitnessapplication.presentation.activities.MainActivity
import com.atom.bluetoothfitnessapplication.utilities.Constants

class QuickStartWidget : GlanceAppWidget() {

    override suspend fun provideContent(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val isConnected = prefs.getBoolean(Constants.KEY_BLUETOOTH_CONNECTED, false)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ImageProvider(R.drawable.widget_background))
                        .padding(8.dp),
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
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = if (isConnected) "Ready" else "Disconnected",
                            style = TextStyle(
                                color = ColorProvider(android.graphics.Color.WHITE),
                                fontSize = 12.sp
                            )
                        )
                    }

                    Text(
                        text = "Quick Start",
                        style = TextStyle(
                            color = ColorProvider(android.graphics.Color.WHITE),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(GlanceModifier.height(8.dp))

                    Row(horizontalAlignment = Alignment.CenterHorizontally) {
                        QuickActionButton(context, context.getString(R.string.push_up), "Push Up")
                        Spacer(GlanceModifier.width(4.dp))
                        QuickActionButton(context, context.getString(R.string.plank), "Plank")
                        Spacer(GlanceModifier.width(4.dp))
                        QuickActionButton(context, context.getString(R.string.skipping), "Skip")
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun QuickActionButton(context: Context, exerciseName: String, label: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_EXERCISE", exerciseName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        Button(
            text = label,
            onClick = actionStartActivity(intent),
            modifier = GlanceModifier.width(60.dp)
        )
    }
}

class QuickStartWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickStartWidget()
}
