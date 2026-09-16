package com.example.data

import android.content.Context

/**
 * Persisted timing calibration for this device.
 *
 * Reaction times are measured from the vsync timestamp of the frame carrying the stimulus
 * to the kernel timestamp of the touch event. Two systematic overheads remain inside that
 * window, and this is where the correction for them lives:
 *
 *  - **Touch sampling quantisation.** A digitiser reports at a fixed rate, so a contact is
 *    stamped up to one sampling period late, half a period on average. Measurable here.
 *
 *  - **Panel latency.** The delay between vsync and the pixels actually emitting light.
 *    This CANNOT be measured on-device - it needs an external high-speed camera or a
 *    photodiode. It is never guessed: it stays zero unless the athlete enters a value they
 *    measured themselves.
 */
object CalibrationStore {

    private const val PREFS = "reaction_calibration"
    private const val KEY_TOUCH_SAMPLING_MS = "touch_sampling_offset_ms"
    private const val KEY_PANEL_LATENCY_MS = "panel_latency_ms"
    private const val KEY_CALIBRATED_AT = "calibrated_at"
    private const val KEY_REFRESH_HZ = "refresh_hz"

    /** Nothing above this is plausible for input quantisation; refuse obviously bad input. */
    private const val MAX_REASONABLE_OFFSET_MS = 60L

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(
        context: Context,
        touchSamplingOffsetMs: Long,
        panelLatencyMs: Long,
        refreshHz: Int
    ) {
        prefs(context).edit()
            .putLong(KEY_TOUCH_SAMPLING_MS, touchSamplingOffsetMs.coerceIn(0L, MAX_REASONABLE_OFFSET_MS))
            .putLong(KEY_PANEL_LATENCY_MS, panelLatencyMs.coerceIn(0L, MAX_REASONABLE_OFFSET_MS))
            .putInt(KEY_REFRESH_HZ, refreshHz)
            .putLong(KEY_CALIBRATED_AT, System.currentTimeMillis())
            .apply()
    }

    /** Total milliseconds to discount from a raw measurement. */
    fun totalOffsetMs(context: Context): Long {
        val p = prefs(context)
        val touch = p.getLong(KEY_TOUCH_SAMPLING_MS, 0L)
        val panel = p.getLong(KEY_PANEL_LATENCY_MS, 0L)
        return (touch + panel).coerceIn(0L, MAX_REASONABLE_OFFSET_MS * 2)
    }

    fun touchSamplingOffsetMs(context: Context): Long =
        prefs(context).getLong(KEY_TOUCH_SAMPLING_MS, 0L)

    fun panelLatencyMs(context: Context): Long =
        prefs(context).getLong(KEY_PANEL_LATENCY_MS, 0L)

    fun refreshHz(context: Context): Int =
        prefs(context).getInt(KEY_REFRESH_HZ, 0)

    fun calibratedAt(context: Context): Long =
        prefs(context).getLong(KEY_CALIBRATED_AT, 0L)

    fun isCalibrated(context: Context): Boolean = calibratedAt(context) > 0L

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
