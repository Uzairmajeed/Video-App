package com.example.videoapp

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create the player instance here
        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse("https://live-hls-web-aje.getaj.net/AJE/01.m3u8"))
                .setLiveConfiguration(
                    MediaItem.LiveConfiguration.Builder()
                        .setMaxPlaybackSpeed(1.02f)
                        .build()
                )
                .build()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                exoPlayer?.let { player ->
                    ProfileScreen(player)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}

@Composable
fun ProfileScreen(exoPlayer: ExoPlayer) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    // Screen dimensions
    val screenWidth = configuration.screenWidthDp.dp

    // Video dimensions
    val videoHeight = (screenWidth * 9f) / 16f
    val miniPlayerHeight = 60.dp

    // Track container size for calculations
    var containerSize by remember { mutableStateOf(IntSize(0, 0)) }
    val containerHeightPx = with(density) { containerSize.height.toDp() }

    // Animation state
    var dragProgress by remember { mutableStateOf(0f) }
    var isPlaying by remember { mutableStateOf(true) }
    var isAtLiveEdge by remember { mutableStateOf(true) }

    // Track current position and duration for seekbar
    var videoProgress by remember { mutableStateOf(0f) }
    var videoDuration by remember { mutableStateOf(0L) }

    // Calculate animation values
    val videoBoxHeight = lerp(videoHeight, miniPlayerHeight, dragProgress)
    val videoBoxOffset = lerp(0.dp, containerHeightPx - miniPlayerHeight, dragProgress)

    // New state variables for play/pause overlay
    var showControls by remember { mutableStateOf(false) }
    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0f,
        animationSpec = tween(300)
    )

    // Handle auto-hiding of controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(1000) // Hide controls after 2 seconds
            showControls = false
        }
    }

    // Function to handle player view taps
    val handlePlayerTap = {
        // Toggle play/pause state
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        // Show controls
        showControls = true
    }

    // Update position periodically
    LaunchedEffect(Unit) {
        while (true) {
            val position = exoPlayer.currentPosition
            val duration = exoPlayer.duration.coerceAtLeast(1L)
            videoProgress = (position / duration.toFloat()).coerceIn(0f, 1f)
            videoDuration = duration
            delay(500) // Update every half second
        }
    }

    // Live edge check
    LaunchedEffect(Unit) {
        while (true) {
            val timeline = exoPlayer.currentTimeline
            if (!timeline.isEmpty) {
                val window = Timeline.Window()
                timeline.getWindow(exoPlayer.currentMediaItemIndex, window)
                val liveEdge = window.defaultPositionMs
                val currentPosition = exoPlayer.currentPosition
                val isLive = window.isLive
                val isSeekable = window.isSeekable
                val behindLive = (currentPosition + 3000) < liveEdge
                isAtLiveEdge = !(isLive && isSeekable && behindLive)
            }
            delay(2000)
        }
    }

    // Player lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Player state listener
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    videoDuration = exoPlayer.duration.coerceAtLeast(1L)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Animation helper function
    fun animateDragTo(targetValue: Float) {
        coroutineScope.launch {
            val animSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
            animate(
                initialValue = dragProgress,
                targetValue = targetValue,
                animationSpec = animSpec
            ) { value, _ -> dragProgress = value }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
    ) {
        // Main content area (dynamically adjusts based on video state)
        Column(modifier = Modifier.fillMaxSize()) {
            // Video spacer should be visible only when video is expanded
            // This will disappear as the video minimizes
            if (dragProgress < 1f) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(videoHeight * (1 - dragProgress))
                        .alpha(1f - dragProgress)
                )
            }

            // Content below video - expands to fill entire space when video is minimized
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Main Content Area",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
                )
            }
        }

        // Draggable video player container (always on top)
        Surface(
            modifier = Modifier
                .offset(y = videoBoxOffset)
                .fillMaxWidth()
                .height(videoBoxHeight)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            // Snap to nearest state
                            animateDragTo(if (dragProgress > 0.5f) 1f else 0f)
                        },
                        onDragCancel = {
                            // Same behavior as drag end
                            animateDragTo(if (dragProgress > 0.5f) 1f else 0f)
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            // Calculate drag progress based on screen height
                            val maxDragDistance = with(density) {
                                containerSize.height - miniPlayerHeight.toPx()
                            }
                            val newProgress = (dragProgress + (dragAmount / maxDragDistance))
                                .coerceIn(0f, 1f)
                            dragProgress = newProgress
                        }
                    )
                }
                .zIndex(10f),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Use conditional rendering based on drag progress
                // This completely recreates the PlayerView when switching between states
                if (dragProgress < 0.5f) {
                    // FULL-SIZE PLAYER VIEW
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(1f - dragProgress * 2f)
                    ) {
                        // Dedicated full-size player with custom controls
                        AndroidView(
                            factory = { ctx ->
                                // Create a completely separate PlayerView for full mode
                                PlayerView(ctx).apply {
                                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                    useController = false // Disable default controllers
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                                    player = exoPlayer

                                    // Force layout parameters
                                    layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                                .clickable(onClick = handlePlayerTap) // Add click listener for tap-to-pause

                        )
                        // Play/Pause button overlay in center (appears when controls are shown)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(controlsAlpha),
                            contentAlignment = Alignment.Center
                        ) {
                            // Play/Pause button with background
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    //.background(Color.Black.copy(alpha = 0.5f))
                                    .clickable(onClick = handlePlayerTap),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isPlaying) "⏸️" else "▶️",
                                    fontSize = 30.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // Custom controls overlay using our separate component
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                                //.padding(bottom = 15.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Using the custom seekbar from our separate file
                            CustomSeekBar(
                                progress = videoProgress,
                                onProgressChange = { newProgress ->
                                    // Update player position when user drags the seek bar
                                    val newPosition = (videoDuration * newProgress).toLong()
                                    exoPlayer.seekTo(newPosition)

                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp)
                                    .height(15.dp)
                            )
                        }

                        // LIVE badge and close button overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, start = 16.dp, end = 16.dp)
                                .zIndex(12f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Using the LiveBadge component from our separate file
                            LiveBadge(
                                isAtLiveEdge = isAtLiveEdge,
                                onLiveClick = {
                                    exoPlayer.seekToDefaultPosition()
                                    exoPlayer.playWhenReady = true
                                }
                            )

                            Text(
                                text = "❌",
                                fontSize = 15.sp,
                                color = Color.White,
                                modifier = Modifier.clickable { animateDragTo(1f) }
                            )
                        }
                    }
                } else {
                    // MINI PLAYER VIEW
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(dragProgress * 2f - 1f)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini video thumbnail (left area - takes about 25% width)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.25f)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Dedicated mini player view
                            AndroidView(
                                factory = { ctx ->
                                    // Create a completely separate PlayerView for mini mode
                                    PlayerView(ctx).apply {
                                        useController = false
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                                        player = exoPlayer

                                        // Force layout parameters
                                        layoutParams = FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Title and controls (right area)
                        Row(
                            modifier = Modifier
                                .weight(0.75f)
                                .fillMaxHeight()
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Title and subtitle
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "Video Title",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Live News",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }

                            // Control buttons
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = if (isPlaying) "⏸️" else "▶️",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .clickable {
                                            if (exoPlayer.isPlaying) {
                                                exoPlayer.pause()
                                            } else {
                                                exoPlayer.play()
                                            }
                                        }
                                )

                                Text(
                                    text = "⬆️",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .clickable { animateDragTo(0f) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Linear interpolation helper for dp values
private fun lerp(start: androidx.compose.ui.unit.Dp, stop: androidx.compose.ui.unit.Dp, fraction: Float): androidx.compose.ui.unit.Dp {
    return start + ((stop - start) * fraction)
}