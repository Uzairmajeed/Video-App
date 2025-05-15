import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videoapp.VideoSelectionListener
import kotlinx.coroutines.delay


data class ShowCategory(
    val id: String,
    val label: String,
    val isSelected: Boolean = false
)


val categories = listOf(
        ShowCategory("1", "TV Shows"),
        ShowCategory("2", "Movies"),
        ShowCategory("3", "Recent"),
        ShowCategory("4", "Live"),
        ShowCategory("5", "Recommended")
    )


// Data Class To Represent A Media Item
data class MediaItemList(
    val id: String,
    val title: String,
    val channel: String,
    val channelLogo: String?, // URL or resource for channel logo (null for default)
    val thumbnailIcon: ImageVector, // Use Jetpack Compose vector icon as fallback
    val logoIcon: ImageVector, // ImageVector for logos now
    val progress: Float, // 0.0f to 1.0f
    val timeRemaining: String,
    val videoUrl: String
)

// Time Periods With Their Shows
data class TimePeriod(
    val time: String,
    val shows: List<MediaItemList>
)

//  Dummy Data Structure For Different Time Periods
// Dummy Data Provider
object MediaDataProvider {
val timePeriods = listOf(
    TimePeriod(
        time = "Now 4:30 pm",
        shows = listOf(
            MediaItemList(
                id = "1",
                title = "S02 E01 - Animal Mealtime",
                channel = "Brave Wilderness",
                channelLogo = "brave_wilderness_logo",
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.75f,
                timeRemaining = "15m left",
                videoUrl = "https://live-hls-web-aje.getaj.net/AJE/01.m3u8"
            ),
            MediaItemList(
                id = "2",
                title = "Kitchen Delight",
                channel = "Tastomade",
                channelLogo = "tastomade_logo",
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.4f,
                timeRemaining = "8m left",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            ),
            MediaItemList(
                id = "3",
                title = "S04 E08 - Beyond Belief: Fact or Fiction",
                channel = "Beyond Belief",
                channelLogo = "beyond_belief_logo",
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.2f,
                timeRemaining = "8m left",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            ),
            MediaItemList(
                id = "4",
                title = "Watts Up for the Holidays",
                channel = "GustoTV",
                channelLogo = "gustotv_logo",
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.9f,
                timeRemaining = "7m left",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
            ),
            MediaItemList(
                id = "5",
                title = "S04 E08 - In the Hands of Giants",
                channel = "Homicide City",
                channelLogo = "homicide_city_logo",
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.6f,
                timeRemaining = "7m left",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
            ),


        )
    ),
    TimePeriod(
        time = "5:00 pm",
        shows = listOf(
            MediaItemList(
                id = "6",
                title = "S02 E02 - Stung by a Warrior Wasp",
                channel = "Brave Wilderness",
                channelLogo = null,
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.0f,
                timeRemaining = "30m",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            ),
            MediaItemList(
                id = "7",
                title = "Two for the Prize",
                channel = "Food Network",
                channelLogo = null,
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.0f,
                timeRemaining = "30m",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            ),
            MediaItemList(
                id = "8",
                title = "S04 E09 - Beyond Reproach",
                channel = "Beyond Belief",
                channelLogo = null,
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.0f,
                timeRemaining = "30m",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            ),
            MediaItemList(
                id = "9",
                title = "Flour Power Holiday Specials",
                channel = "GustoTV",
                channelLogo = null,
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.0f,
                timeRemaining = "30m",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            ),
            MediaItemList(
                id = "10",
                title = "S04 E09 - Lie Down with Dogs",
                channel = "Homicide City",
                channelLogo = null,
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.0f,
                timeRemaining = "30m",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            )
        )
    ),
    TimePeriod(
        time = "6:00 pm",
        shows = listOf(
            MediaItemList(
                id = "11",
                title = "Live AlJazeera News",
                channel = "AlJazeera",
                channelLogo = null,
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.0f,
                timeRemaining = "Live",
                videoUrl = "https://live-hls-web-aje.getaj.net/AJE/01.m3u8"
            ),
            MediaItemList(
                id = "12",
                title = "Into the Wild Season Finale",
                channel = "National Geographic",
                channelLogo = null,
                thumbnailIcon = Icons.Default.Info,
                logoIcon = Icons.Default.MailOutline,
                progress = 0.0f,
                timeRemaining = "45m",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
            )
        )
    )
)
}


//This Composable  Is For Other Contents Of Profile Fragment ..Showing List Of Videos Under Different Time Slots
@Composable
fun OtherContents(videoSelectionListener: VideoSelectionListener) {
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryId by remember { mutableStateOf(categories.first().id) }


    var timePeriods = MediaDataProvider.timePeriods
    // Automatically select the first item when the UI is first composed
    LaunchedEffect(Unit) {
        if (timePeriods.isNotEmpty() && timePeriods[0].shows.isNotEmpty()) {
            selectedItemId = timePeriods[0].shows[0].id
            // Add a slight delay to ensure the player is fully initialized
            delay(100)
            videoSelectionListener.onVideoSelected(timePeriods[0].shows[0].videoUrl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark background similar to the image
    ) {
        Column {
            // 🔹 Insert Chips ABOVE the time section
            ShowCategoryChips(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it.id }
            )
            // Horizontal scrollable container for time periods and their content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
            ) {
                // For each time period, create a column with its shows
                timePeriods.forEach { timePeriod ->
                    Column(
                        modifier = Modifier
                            .width(300.dp) // Fixed width for each time period column
                            .fillMaxHeight()
                    ) {
                        // Time period header
                        Text(
                            text = timePeriod.time,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )

                        // Shows for this time period
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(timePeriod.shows) { show ->
                                MediaListItem(
                                    mediaItemList = show,
                                    isSelected = show.id == selectedItemId,
                                    is430PM = timePeriod.time.contains("4:30"),
                                    onClick = {
                                        selectedItemId = show.id
                                        videoSelectionListener.onVideoSelected(show.videoUrl)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }


    }
}


// This Composable Is  For each item ..Having Also Thumbnail For Initial Time Only ..
@Composable
fun MediaListItem(
    mediaItemList: MediaItemList,
    isSelected: Boolean,
    is430PM: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 8.dp)
    ) {
        // Channel logo/thumbnail - only for 4:30 PM time slot
        if (is430PM) {
            // Separate thumbnail box with its own styling
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color.Red else Color(0xFF3B3A46)) // Purple background, Red when selected
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                // Display channel name or brand
                Text(
                    text = mediaItemList.channel.split(" ").firstOrNull() ?: "",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Content box (separate from thumbnail)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .let {
                    if (isSelected) {
                        it.border(width = 2.dp, color = Color.Red, shape = RoundedCornerShape(8.dp))
                    } else {
                        it
                    }
                }
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = mediaItemList.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = mediaItemList.channel,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = mediaItemList.progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp),
                        color = Color.White,
                        trackColor = Color(0xFF444444)
                    )

                    Text(
                        text = mediaItemList.timeRemaining,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}



@Composable
fun ShowCategoryChips(
    categories: List<ShowCategory>,
    selectedCategoryId: String?,
    onCategorySelected: (ShowCategory) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category.id == selectedCategoryId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color.White else Color(0xFF2C2C2C))
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category.label,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
