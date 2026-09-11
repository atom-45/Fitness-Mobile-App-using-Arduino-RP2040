package com.atom.bluetoothfitnessapplication.utilities

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.atom.bluetoothfitnessapplication.presentation.widgets.QuickStartWidget
import com.atom.bluetoothfitnessapplication.presentation.widgets.PerformanceSnapshotWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object WidgetHelper {
    private val scope = MainScope()

    fun updateWidgets(context: Context) {
        scope.launch {
            try {
                val glanceAppWidgetManager = GlanceAppWidgetManager(context)
                glanceAppWidgetManager.getGlanceIds(QuickStartWidget::class.java).forEach {
                    QuickStartWidget().update(context, it)
                }
                glanceAppWidgetManager.getGlanceIds(PerformanceSnapshotWidget::class.java).forEach {
                    PerformanceSnapshotWidget().update(context, it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveBluetoothStatus(context: Context, isConnected: Boolean) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(Constants.KEY_BLUETOOTH_CONNECTED, isConnected).apply()
        updateWidgets(context)
    }

    fun saveLastExercise(context: Context, type: String, value: Float, unit: String, secondary: String) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(Constants.KEY_LAST_EXERCISE_TYPE, type)
            .putFloat(Constants.KEY_LAST_EXERCISE_VALUE, value)
            .putString(Constants.KEY_LAST_EXERCISE_UNIT, unit)
            .putString(Constants.KEY_LAST_EXERCISE_SECONDARY, secondary)
            .apply()
        updateWidgets(context)
    }
}
