// TimeUtils.kt
package org.wit.audioplayer.ui

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.S)
fun formatDuration(milliseconds: Long): String {
    return if (milliseconds <= 0) {
        "00:00"
    } else {
        val duration = Duration.ofMillis(milliseconds)
        String.format(
            "%02d:%02d",
            duration.toMinutes(),
            duration.toSecondsPart()
        )
    }
}