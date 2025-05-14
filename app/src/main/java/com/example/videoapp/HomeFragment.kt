package com.example.videoapp

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class HomeFragment : Fragment() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var miniPlayerView: PlayerView
    private lateinit var rootContainer: ConstraintLayout
    private lateinit var playerContainer: FrameLayout
    private lateinit var miniPlayerContainer: CardView
    private lateinit var contentScrollView: ScrollView
    private lateinit var expandButton: ImageButton
    private lateinit var pausePlayButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var liveIndicator: TextView
    private lateinit var videoTitleMini: TextView
    private lateinit var videoSubtitleMini: TextView
    // Add this property to your class
    private lateinit var centerPlayPauseIndicator: ImageView

    // Track animation state
    private var isInMiniMode = false
    private var isDragging = false
    private var lastTouchY = 0f
    private var startDragY = 0f
    private var screenHeight = 0
    private var miniPlayerHeight = 0
    private var expandedPlayerHeight = 0
    private var currentAnimator: ValueAnimator? = null
    private var isAtLiveEdge = true

    // Add these properties to your class
    private lateinit var customSeekBar: SeekBar
    private lateinit var customControlsContainer: FrameLayout
    private var isDraggingSeekBar = false
    private val seekBarUpdateHandler = Handler(Looper.getMainLooper())
    private var isLiveContent = true

    private val categoryList = listOf("TV Shows", "Movies", "Recent", "Live", "Recommended")


    private val TAG = "BlankFragment"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate a new layout for the fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * onViewCreated - Sets up the views and relationships after layout inflation
     *
     * Called after onCreateView() when the fragment's view hierarchy is created.
     * This method finds all required views from the layout by ID and stores them
     * as class properties for later use.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        rootContainer = view.findViewById(R.id.rootContainer)
        playerContainer = view.findViewById(R.id.playerContainer)
        miniPlayerContainer = view.findViewById(R.id.miniPlayerContainer)
        playerView = view.findViewById(R.id.playerView)
        miniPlayerView = view.findViewById(R.id.miniPlayerView)
        contentScrollView = view.findViewById(R.id.contentScrollView)
        expandButton = view.findViewById(R.id.expandButton)
        pausePlayButton = view.findViewById(R.id.pausePlayButton)
        closeButton = view.findViewById(R.id.closeButton)
        liveIndicator = view.findViewById(R.id.liveIndicator)
        videoTitleMini = view.findViewById(R.id.videoTitleMini)
        videoSubtitleMini = view.findViewById(R.id.videoSubtitleMini)
        // In onViewCreated, initialize the view
        centerPlayPauseIndicator = view.findViewById(R.id.centerPlayPauseIndicator)

        // Find included layout views using the includes ID
        val playerControlsView = view.findViewById<View>(R.id.playerControls)
        customControlsContainer = playerControlsView.findViewById(R.id.customControlsContainer)
        customSeekBar = playerControlsView.findViewById(R.id.customSeekBar)

        // Initialize measurements
        miniPlayerHeight = resources.getDimensionPixelSize(R.dimen.mini_player_height)

        // Setup UI initially
        setupInitialState()
        setupListeners()

        setupCategoryRecyclerView()
        setupTimeSlots()
    }

    private fun setupCategoryRecyclerView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.showCategoryRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.adapter = ShowCategoryAdapter(categoryList) { selectedCategory ->
            Toast.makeText(requireContext(), "Selected: $selectedCategory", Toast.LENGTH_SHORT).show()
            // 🔥 Add filtering logic here later
        }
    }

    private fun setupTimeSlots() {
        val timeRecycler = view?.findViewById<RecyclerView>(R.id.timeSlotRecyclerView)
        timeRecycler?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        timeRecycler?.adapter = TimeSlotAdapter(MediaDataProvider.timePeriods)
    }




    /**
     * onStart - Fragment lifecycle method where we initialize the player
     *
     * This is called when the fragment is becoming visible to the user.
     * We initialize and set up the ExoPlayer instance here rather than earlier to
     * ensure resources aren't being used when the fragment isn't visible.
     */
    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    /**
     * onStop - Fragment lifecycle method where we release the player
     *
     * Called when the fragment is no longer visible to the user.
     * We release the ExoPlayer instance here to free up resources when the
     * fragment is not being displayed.
     */
    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    /**
     * onResume - Fragment lifecycle method for additional setup
     *
     * When the fragment resumes, we calculate dimensions that depend on the
     * actual rendered view height. This ensures correct proportions regardless
     * of screen size. We use post() to ensure measurements happen after layout.
     *
     * Here we calculate:
     * - Screen height for bounds calculation
     * - Player height based on 16:9 aspect ratio
     */
    override fun onResume() {
        super.onResume()
        // Get screen height when layout is ready
        rootContainer.post {
            screenHeight = rootContainer.height
            expandedPlayerHeight = (screenHeight * 9) / 26 // 16:9 aspect ratio

            // Set the expanded player height
           // playerContainer.layoutParams.height = expandedPlayerHeight
            playerContainer.requestLayout()

            Log.d(TAG, "Screen height: $screenHeight, Player height: $expandedPlayerHeight, Mini height: $miniPlayerHeight")
        }
    }

    /**
     * setupInitialState - Sets the initial UI state of the fragment
     *
     * Sets the visibility and position of UI elements before any user interaction.
     * Initially hides the mini player by setting it invisible and positioning it
     * off-screen (translated down). Also sets initial text content for mini player.
     */
    private fun setupInitialState() {
        // Initially hide mini player
        miniPlayerContainer.visibility = View.INVISIBLE
        miniPlayerContainer.translationY = miniPlayerHeight.toFloat()

        // Set video title and subtitle
        videoTitleMini.text = "Video Title"
        videoSubtitleMini.text = "Live News"
    }

    /**
     * setupListeners - Configures all touch and click listeners
     *
     * Establishes:
     * 1. Touch handling for player dragging interactions
     * 2. Button click listeners for controls
     * 3. Polling mechanism to check live edge status
     *
     * This comprehensive method connects all UI elements to their behaviors,
     * implementing gesture detection for dragging between expanded and mini player modes.
     * Uses MotionEvent handling to create a smooth, responsive drag experience.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {

        // Add custom seek bar listener
        customSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // If user is dragging the seek bar
                if (fromUser && player != null && isLiveContent) {
                    // Force non-live status as soon as user starts dragging backward
                    if (progress < 95) { // If not near the end of the seek bar
                        isAtLiveEdge = false
                        updateLiveIndicator()
                        Log.d(TAG, "Forced non-live during seek: progress=$progress")
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isDraggingSeekBar = true
                // Pause updates while dragging
                seekBarUpdateHandler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isDraggingSeekBar = false

                // Seek to the selected position
                player?.let {
                    val duration = it.duration
                    if (duration > 0) {
                        val seekPosition = (seekBar!!.progress.toLong() * duration) / 100
                        it.seekTo(seekPosition)

                        // Check live status - make this more aggressive
                        if (isLiveContent) {
                            // If user selected near maximum, consider it live
                            isAtLiveEdge = (seekBar.progress >= 95)
                            updateLiveIndicator()
                            Log.d(TAG, "Seek complete: progress=${seekBar.progress}, isAtLiveEdge=$isAtLiveEdge")
                        }
                    }
                }

                // Resume updates
                seekBarUpdateHandler.post(updateSeekBarRunnable)
            }
        })


        playerView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isInMiniMode) return@setOnTouchListener false

                    // Track initial position
                    startDragY = event.rawY
                    lastTouchY = event.rawY
                    currentAnimator?.cancel()

                    // Don't set isDragging yet - we'll set it in MOVE if there's significant movement
                    isDragging = false

                    Log.d(TAG, "ACTION_DOWN: startDragY=$startDragY")
                    false  // Let the event continue to other listeners
                }

                MotionEvent.ACTION_MOVE -> {
                    // Calculate movement
                    val deltaY = abs(event.rawY - startDragY)

                    // If there's significant movement, start dragging
                    if (deltaY > 10) {  // 10 pixels threshold
                        isDragging = true

                        // Your existing drag logic
                        val moveDelta = event.rawY - lastTouchY
                        val newTranslation = playerContainer.translationY + moveDelta
                        playerContainer.translationY = max(0f, newTranslation)

                        val dragDistance = playerContainer.translationY
                        val maxDragDistance = screenHeight - miniPlayerHeight.toFloat()
                        val dragProgress = min(1f, dragDistance / maxDragDistance)

                        updateInterfaceForDragProgress(dragProgress)
                        lastTouchY = event.rawY
                        return@setOnTouchListener true  // Consume the event only when dragging
                    }

                    false  // Not dragging yet, let others handle it
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        // Your existing end-drag logic
                        isDragging = false
                        val currentTranslation = playerContainer.translationY

                        if (currentTranslation > expandedPlayerHeight / 3) {
                            animateToMiniPlayer()
                        } else {
                            animateToExpandedPlayer()
                        }
                        true  // Consume the event if we were dragging
                    } else {
                        false  // Let click events pass through
                    }
                }

                else -> false
            }
        }

        playerView.setOnClickListener{
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    showAndFadePlayPauseIndicator(false)
                } else {
                    it.play()
                    showAndFadePlayPauseIndicator(true)

                }
            }
        }


        // Keep your existing implementation but add a button that seeks to live edge
        liveIndicator.setOnClickListener {
            player?.seekToDefaultPosition()  // Seek to actual live edge
            isAtLiveEdge = true
            updateLiveIndicator()
        }

        // Mini player container touch listener
        miniPlayerContainer.setOnTouchListener { _, event ->
            if (!isInMiniMode) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true
                    startDragY = event.rawY
                    lastTouchY = event.rawY
                    currentAnimator?.cancel()
                    Log.d(TAG, "Mini ACTION_DOWN: startDragY=$startDragY")
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isDragging) return@setOnTouchListener false

                    val deltaY = event.rawY - lastTouchY
                    val newTranslation = miniPlayerContainer.translationY + deltaY

                    // Allow dragging up or slightly down
                    if (newTranslation <= 0) {
                        // Dragging up to expand
                        miniPlayerContainer.translationY = newTranslation
                        val dragProgress = 1f - min(1f, abs(newTranslation) / (screenHeight - miniPlayerHeight).toFloat())
                        updateInterfaceForReverseProgress(dragProgress)
                        Log.d(TAG, "Mini ACTION_MOVE up: deltaY=$deltaY, translation=${miniPlayerContainer.translationY}, progress=$dragProgress")
                    } else if (newTranslation <= miniPlayerHeight / 2) {
                        // Allow slight downward movement but limit it
                        miniPlayerContainer.translationY = newTranslation
                        Log.d(TAG, "Mini ACTION_MOVE down: deltaY=$deltaY, translation=${miniPlayerContainer.translationY}")
                    }

                    lastTouchY = event.rawY
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!isDragging) return@setOnTouchListener false

                    isDragging = false
                    val currentTranslation = miniPlayerContainer.translationY
                    Log.d(TAG, "Mini ACTION_UP: translation=$currentTranslation")

                    // Determine gesture direction
                    if (currentTranslation < -miniPlayerHeight / 3) {
                        // Expand back to full player
                        animateToExpandedPlayer()
                    } else if (currentTranslation > miniPlayerHeight / 3) {
                        // Dismiss mini player
                        animateDismissMiniPlayer()
                    } else {
                        // Return to mini player state
                        animateToPosition(miniPlayerContainer, 0f)
                    }
                    true
                }

                else -> false
            }
        }

        // Button click listeners
        closeButton.setOnClickListener {
            Log.d(TAG, "Close button clicked")
            animateToMiniPlayer()
        }

        // In setupListeners() function, add this code:
        liveIndicator.setOnClickListener {
            Log.d(TAG, "Live indicator clicked")
            player?.let { exoPlayer ->
                val timeline = exoPlayer.currentTimeline
                if (!timeline.isEmpty) {
                    val window = Timeline.Window()
                    timeline.getWindow(exoPlayer.currentMediaItemIndex, window)
                    if (window.isLive) {
                        // Seek to live edge
                        exoPlayer.seekToDefaultPosition()
                        isAtLiveEdge = true
                        updateLiveIndicator()
                        Log.d(TAG, "Seeking to live edge")
                    }
                }
            }
        }

        expandButton.setOnClickListener {
            Log.d(TAG, "Expand button clicked")
            animateToExpandedPlayer()
        }

        pausePlayButton.setOnClickListener {
            Log.d(TAG, "Play/Pause button clicked")
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    pausePlayButton.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    it.play()
                    pausePlayButton.setImageResource(android.R.drawable.ic_media_pause)
                }
            }
        }

    }


    // Add this function to show and fade the indicator
    private fun showAndFadePlayPauseIndicator(isPlaying: Boolean) {
        // Update the icon based on play state
        centerPlayPauseIndicator.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )

        // Make sure it's visible
        centerPlayPauseIndicator.visibility = View.VISIBLE

        // Cancel any existing animations
        centerPlayPauseIndicator.animate().cancel()

        // Reset alpha and animate fade
        centerPlayPauseIndicator.alpha = 1.0f
        centerPlayPauseIndicator.animate()
            .alpha(0f)
            .setDuration(2000) // 2 seconds
            .setStartDelay(500) // Show for 0.5 seconds before fading
            .withEndAction {
                if (centerPlayPauseIndicator.alpha == 0f) {
                    centerPlayPauseIndicator.visibility = View.INVISIBLE
                }
            }
            .start()

        Log.d(TAG, "Play/Pause indicator animation started, isPlaying: $isPlaying")
    }


    // For updating the seek bar
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (player != null && !isDraggingSeekBar) {
                val duration = player!!.duration
                val position = player!!.currentPosition

                if (duration > 0) {
                    // Update seek bar progress (0-100%)
                    val progress = ((position * 100) / duration).toInt()
                    customSeekBar.progress = progress

                    // Update live edge status - more forgiving threshold (5 seconds)
                    // In updateSeekBarRunnable
                    if (isLiveContent) {
                        val timeBehindLive = duration - position
                        Log.d(TAG, "Time behind live: $timeBehindLive ms, isAtLiveEdge: $isAtLiveEdge")

                        // More permissive threshold - consider anything less than 45 seconds as "live"
                        if (isAtLiveEdge && timeBehindLive > 50000) {  // 50 seconds
                            // We were at live edge but now we're far behind
                            isAtLiveEdge = false
                            updateLiveIndicator()
                        } else if (!isAtLiveEdge && timeBehindLive < 45000) {  // 45 seconds
                            // We weren't at live edge but now we're close
                            isAtLiveEdge = true
                            updateLiveIndicator()
                        }
                    }
                }
            }

            // Schedule the next update
            seekBarUpdateHandler.postDelayed(this, 1000)
        }
    }

    /*
     * updateLiveIndicator - Updates the live indicator visual state
     *
     * Changes the background color of the live indicator based on whether
     * the playback position is at the live edge of the stream. Red indicates
     * the player is at the live edge, dark gray indicates it's behind.
     */
    private fun updateLiveIndicator() {
        Log.d(TAG, "Updating live indicator: isAtLiveEdge=$isAtLiveEdge")
        val newColor = if (isAtLiveEdge) Color.RED else Color.DKGRAY
        liveIndicator.setBackgroundColor(newColor)
    }


    /**
     * updateInterfaceForDragProgress - Updates UI elements during drag-down
     *
     * Adjusts opacity, visibility and position of UI components as the user
     * drags from expanded to mini player mode. The progress parameter ranges
     * from 0 (fully expanded) to 1 (mini player).
     *
     * This orchestrates:
     * - Fading between player views
     * - Showing/hiding mini player at appropriate times
     * - Adjusting content scroll position to maintain visual continuity
     */
    private fun updateInterfaceForDragProgress(progress: Float) {
        // Update UI elements based on drag progress (0 = expanded, 1 = mini)
        Log.d(TAG, "Updating interface for drag progress: $progress")

        miniPlayerContainer.alpha = progress
        playerContainer.alpha = 1 - progress

        // Show mini player once we've dragged a bit
        if (progress > 0.1f && miniPlayerContainer.visibility != View.VISIBLE) {
            Log.d(TAG, "Making mini player visible")
            miniPlayerContainer.visibility = View.VISIBLE
            miniPlayerContainer.translationY = 0f  // Ensure it's in the right position
        }

        // Add null check for customControlsContainer
        if (::customControlsContainer.isInitialized) {
            // Hide custom controls when going to mini mode
            customControlsContainer.alpha = 1 - progress
        }

        // Adjust content scroll position based on player movement
        val contentOffset = (expandedPlayerHeight - miniPlayerHeight) * progress
        contentScrollView.translationY = -contentOffset
    }

    /**
     * updateInterfaceForReverseProgress - Updates UI during upward drag
     *
     * Similar to updateInterfaceForDragProgress but handles the reverse transition
     * when dragging from mini player back to expanded mode. Manages visibility,
     * opacity and positioning of player views and content.
     *
     * Ensures smooth visual transition when expanding mini player by:
     * - Adjusting player translation
     * - Managing fade effects between views
     * - Repositioning content scroll area
     */
    private fun updateInterfaceForReverseProgress(progress: Float) {
        // Update for going from mini to expanded (0 = expanded, 1 = mini)
        Log.d(TAG, "Updating interface for reverse progress: $progress")

        playerContainer.translationY = (screenHeight - miniPlayerHeight) * progress
        playerContainer.alpha = 1 - progress
        miniPlayerContainer.alpha = progress

        // Show main player once we've dragged enough
        if (progress < 0.9f && playerContainer.visibility != View.VISIBLE) {
            Log.d(TAG, "Making main player visible")
            playerContainer.visibility = View.VISIBLE
        }

        // Add null check for customControlsContainer
        if (::customControlsContainer.isInitialized) {
            // Show custom controls when returning to full mode
            customControlsContainer.alpha = 1 - progress
        }

        // Adjust content scroll position
        val contentOffset = (expandedPlayerHeight - miniPlayerHeight) * progress
        contentScrollView.translationY = -contentOffset
    }

    /**
     * animateToMiniPlayer - Animates transition to mini player mode
     *
     * Creates and runs animations to smoothly transition from expanded to mini player.
     * Handles movement of the player container, opacity changes, and content repositioning.
     *
     * On completion:
     * - Updates mode tracking variables
     * - Transfers ExoPlayer instance to mini player view
     * - Updates visibility states of containers
     */
    private fun animateToMiniPlayer() {
        // Cancel any ongoing animations
        currentAnimator?.cancel()

        // Ensure mini player is visible
        miniPlayerContainer.visibility = View.VISIBLE
        miniPlayerContainer.translationY = 0f

        // Calculate the destination position for the main player
        val destinationY = screenHeight - miniPlayerHeight.toFloat()

        Log.d(TAG, "Animating to mini player. Current Y: ${playerContainer.translationY}, Destination Y: $destinationY")

        // Animate main player movement
        val animator = ValueAnimator.ofFloat(playerContainer.translationY, destinationY)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            playerContainer.translationY = value

            // Calculate progress for other UI elements
            val progress = value / destinationY
            updateInterfaceForDragProgress(progress)
        }
        animator.doOnEnd {
            isInMiniMode = true
            playerContainer.visibility = View.INVISIBLE

            // Hide controls in mini mode - add null check
            if (::customControlsContainer.isInitialized) {
                customControlsContainer.visibility = View.INVISIBLE
            }
            // Switch to mini player controls
            miniPlayerView.player = player
            playerView.player = null

            Log.d(TAG, "Animation to mini player complete")
        }
        animator.start()
        currentAnimator = animator
    }

    /**
     * animateToExpandedPlayer - Animates transition to expanded player
     *
     * Handles animation from mini player to expanded state with proper
     * fade effects and position changes. Has different animation paths depending
     * on whether we're coming from mini mode or just canceling a partial drag.
     *
     * Carefully manages:
     * - Player view transitions
     * - Content scroll positioning
     * - ExoPlayer instance transfer between views
     * - Visibility states of containers
     */
    private fun animateToExpandedPlayer() {
        // Cancel any ongoing animations
        currentAnimator?.cancel()

        // Ensure main player is visible
        playerContainer.visibility = View.VISIBLE

        Log.d(TAG, "Animating to expanded player. Current mini Y: ${miniPlayerContainer.translationY}, Current player Y: ${playerContainer.translationY}")

        if (isInMiniMode) {
            // Coming from mini player state
            val animator = ValueAnimator.ofFloat(miniPlayerContainer.translationY, -screenHeight.toFloat())
            animator.duration = 300
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                miniPlayerContainer.translationY = value

                // Calculate progress for reverting from mini to full
                val distance = screenHeight.toFloat()
                val progress = 1f + (value / distance)
                updateInterfaceForReverseProgress(max(0f, progress))

                // Reset main player translation as we approach expanded state
                if (progress < 0.5f) {
                    playerContainer.translationY = 0f
                }
            }
            animator.doOnEnd {
                miniPlayerContainer.visibility = View.INVISIBLE
                miniPlayerContainer.translationY = miniPlayerHeight.toFloat()
                playerContainer.translationY = 0f
                contentScrollView.translationY = 0f
                isInMiniMode = false
                // Show controls in expanded mode

                // Show controls in expanded mode - add null check
                if (::customControlsContainer.isInitialized) {
                    customControlsContainer.visibility = View.VISIBLE
                }

                // Switch to main player controls
                playerView.player = player
                miniPlayerView.player = null

                Log.d(TAG, "Animation from mini to expanded complete")
            }
            animator.start()
            currentAnimator = animator
        } else {
            // Already in expanded view, just need to animate back to position
            val animator = ValueAnimator.ofFloat(playerContainer.translationY, 0f)
            animator.duration = 300
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                playerContainer.translationY = value

                // Calculate progress
                val maxDragDistance = screenHeight - miniPlayerHeight.toFloat()
                val progress = value / maxDragDistance
                updateInterfaceForDragProgress(progress)
            }
            animator.doOnEnd {
                miniPlayerContainer.visibility = View.INVISIBLE
                contentScrollView.translationY = 0f
                isInMiniMode = false

                Log.d(TAG, "Animation to expanded complete")
            }
            animator.start()
            currentAnimator = animator
        }
    }

    /**
     * animateDismissMiniPlayer - Animates the mini player offscreen
     *
     * Slides the mini player downward and out of view, then hides it
     * and pauses playback. Used when user drags mini player downward
     * past the dismissal threshold.
     */
    private fun animateDismissMiniPlayer() {
        // Animate mini player off screen (downward)
        Log.d(TAG, "Dismissing mini player")

        val animator = ValueAnimator.ofFloat(miniPlayerContainer.translationY, miniPlayerHeight.toFloat())
        animator.duration = 200
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            miniPlayerContainer.translationY = value
        }
        animator.doOnEnd {
            isInMiniMode = false
            miniPlayerContainer.visibility = View.INVISIBLE
            player?.pause()

            Log.d(TAG, "Mini player dismissed")
        }
        animator.start()
        currentAnimator = animator
    }

    /**
     * animateToPosition - Generic animation utility for view translation
     *
     * Provides a reusable animation mechanism to smoothly move a view
     * to a specified Y translation position. Uses standard Android
     * ValueAnimator with a deceleration interpolator for natural motion.
     *
     * @param view The view to animate
     * @param position The target Y translation value
     */
    private fun animateToPosition(view: View, position: Float) {
        // Generic animation to position
        Log.d(TAG, "Animating view to position: $position")

        val animator = ValueAnimator.ofFloat(view.translationY, position)
        animator.duration = 200
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animator ->
            view.translationY = animator.animatedValue as Float
        }
        animator.start()
        currentAnimator = animator
    }

    /**
     * initializePlayer - Creates and configures the ExoPlayer instance
     *
     * Sets up a new ExoPlayer, configures it with appropriate settings, attaches it
     * to the correct PlayerView (depending on current mode), and prepares it to play
     * a live HLS stream. Also configures playback listeners to update UI based on
     * player state changes.
     *
     * Key configurations:
     * - Different resize modes for main/mini player views
     * - Live stream playback settings
     * - Play/pause button state synchronization
     */
    private fun initializePlayer() {
        Log.d(TAG, "Initializing player")

        player = ExoPlayer.Builder(requireContext()).build()

        // Set player to appropriate view based on current state
        if (isInMiniMode) {
            miniPlayerView.player = player
        } else {
            playerView.player = player
        }

        // Configure player settings
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        miniPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        // Hide mini player controller
        miniPlayerView.useController = false

        // Load the media
        val liveStreamUrl = Uri.parse("https://live-hls-web-aje.getaj.net/AJE/01.m3u8")
        val mediaItem = MediaItem.Builder()
            .setUri(liveStreamUrl)
            .setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setMaxPlaybackSpeed(1.02f)
                    .build()
            )
            .build()

        // Setup player listeners
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "Player playing state changed: $isPlaying")
                pausePlayButton.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            }
        })

        // Add detection for live content
        player?.addListener(object : Player.Listener {
            // Add this to the player's listener in initializePlayer()
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "Player playing state changed: $isPlaying")
                pausePlayButton.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )

                // Optional: Update live indicator based on playback state
                // If video is paused, you might want to show the live indicator as gray
                // regardless of position
                if (isLiveContent) {
                    if (!isPlaying) {
                        // When paused, always show as non-live
                        liveIndicator.setBackgroundColor(Color.DKGRAY)
                    } else {
                        // When playing, use the actual live edge status
                        updateLiveIndicator()
                    }
                }
            }

            // In the initializePlayer() function, add this to the onPlaybackStateChanged listener:
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    // Check if content is live
                    player?.let { exoPlayer ->
                        val timeline = exoPlayer.currentTimeline
                        if (!timeline.isEmpty) {
                            val window = Timeline.Window()
                            timeline.getWindow(exoPlayer.currentMediaItemIndex, window)
                            isLiveContent = window.isLive

                            // Initialize live edge status
                            if (isLiveContent) {
                                // Assume we start at live edge
                                isAtLiveEdge = true
                                updateLiveIndicator()
                            }

                            // Start seek bar updates regardless of content type
                            seekBarUpdateHandler.post(updateSeekBarRunnable)
                        }
                    }
                }
            }
        })

        // Prepare player
        // In initializePlayer() right after prepare()
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            // Force seek to live edge
            seekToDefaultPosition()
        }


    }

    /**
     * releasePlayer - Cleans up the ExoPlayer instance
     *
     * Properly releases the ExoPlayer when no longer needed to free resources
     * and prevent memory leaks. Sets the player reference to null afterward.
     */
    private fun releasePlayer() {
        Log.d(TAG, "Releasing player")
        // Remove callbacks to prevent memory leaks
        seekBarUpdateHandler.removeCallbacks(updateSeekBarRunnable)
        player?.release()
        player = null
    }
}