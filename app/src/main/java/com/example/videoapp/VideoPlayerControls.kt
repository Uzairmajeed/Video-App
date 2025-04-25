package com.example.videoapp


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.exoplayer2.ExoPlayer

/**
 * Custom video player controls that can be reused across different player states
 * Makes it easy to add more control elements in the future
 */
@Composable
fun VideoPlayerControls(
    exoPlayer: ExoPlayer,
    videoProgress: Float,
    videoDuration: Long,
    isPlaying: Boolean,
    isAtLiveEdge: Boolean,
    onSeekComplete: (Float) -> Unit,
    onExpandCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Custom seek bar at the bottom
        CustomSeekBar(
            progress = videoProgress,
            onProgressChange = { newProgress ->
                // Update player position when user drags the seek bar
                val newPosition = (videoDuration * newProgress).toLong()
                exoPlayer.seekTo(newPosition)
                onSeekComplete(newProgress)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(16.dp)
        )
    }
}

/**
 * Custom seekbar component with draggable thumb
 */
@Composable
fun CustomSeekBar(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidth by remember { mutableStateOf(0) }

    // Define explicit measurements
    val trackHeight = 2.dp
    val thumbSize = 10.dp

    // This Box contains everything and handles size calculations
    Box(
        modifier = modifier
            .onSizeChanged { size ->
                trackWidth = size.width
            }
            // Height matches the thumb for proper centering
            .height(thumbSize)
            .fillMaxWidth()
    ) {

        // Progress track (red)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart) // Align to left center
                .fillMaxWidth(progress)
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(Color.Red)
        )

        // Thumb
        if (trackWidth > 0) {
            val thumbOffset = with(LocalDensity.current) {
                // Calculate offset based on progress, accounting for thumb width
                (trackWidth * progress - thumbSize.toPx() / 2).coerceAtLeast(0f).toDp()
            }

            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Color.Red)
                    .align(Alignment.CenterStart)
                    // Add shadow for depth
            )
        }

        // Touch area (invisible, for better interaction)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {},
                        onDragStart = {},
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                            onProgressChange(newProgress)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onProgressChange(newProgress)
                    }
                }
        )
    }
}
/**
 * Component for the live badge shown in the player
 */
@Composable
fun LiveBadge(
    isAtLiveEdge: Boolean,
    onLiveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isAtLiveEdge) Color.Red else Color.DarkGray)
            .clickable {
                onLiveClick()
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = "LIVE", color = Color.White, fontSize = 13.sp)
    }
}

/**
 * Additional video control components can be added here in the future
 */