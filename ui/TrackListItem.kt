package org.wit.audioplayer.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.wit.audioplayer.data.entity.AudioTrack

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun TrackListItem(
    track: AudioTrack,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundBrush = null


    val borderStroke = if (isCurrentTrack && isPlaying) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentTrack) 4.dp else 1.dp
        ),
        border = borderStroke
    ) {
        Box(
            modifier = Modifier
                .background(brush = backgroundBrush ?: SolidColor(MaterialTheme.colorScheme.surface))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isCurrentTrack)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${track.artist ?: "Unknown Artist"} • ${track.album ?: "Unknown Album"}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCurrentTrack)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Text(
                        text = formatDuration(track.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrentTrack)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Playing" else "Paused",
                    tint = if (isCurrentTrack)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(32.dp)
                )
            }
        }
    }
}