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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class to represent a media item
data class MediaItem(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnailIcon: ImageVector, // Use Jetpack Compose vector icon
    val logoIcon: ImageVector, // ImageVector for logos now
    val progress: Float, // 0.0f to 1.0f
    val timeRemaining: String,
    val videoUrl: String
)

// Dummy data with video URLs
val dummyMediaItems = listOf(
    MediaItem(
        id = "1",
        title = "S02 E01 - Animal Mealtime",
        channel = "Brave Wilderness",
        thumbnailIcon = Icons.Default.Info,
        logoIcon = Icons.Default.MailOutline, // default logo
        progress = 0.75f,
        timeRemaining = "15m left",
        videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    ),
    MediaItem(
        id = "2",
        title = "Kitchen Delight",
        channel = "Tastomade",
        thumbnailIcon = Icons.Default.Info,
        logoIcon = Icons.Default.MailOutline,
        progress = 0.4f,
        timeRemaining = "8m left",
        videoUrl = "https://example.com/videos/kitchen_delight.mp4"
    ),
    MediaItem(
        id = "3",
        title = "S04 E08 - Beyond Belief: Fact or Fiction",
        channel = "Beyond Belief",
        thumbnailIcon = Icons.Default.Info,
        logoIcon = Icons.Default.MailOutline,
        progress = 0.2f,
        timeRemaining = "8m left",
        videoUrl = "https://example.com/videos/beyond_belief_s04e08.mp4"
    ),
    MediaItem(
        id = "4",
        title = "Watts Up for the Holidays",
        channel = "GustoTV",
        thumbnailIcon = Icons.Default.Info,
        logoIcon = Icons.Default.MailOutline,
        progress = 0.9f,
        timeRemaining = "7m left",
        videoUrl = "https://example.com/videos/watts_holidays.mp4"
    ),
    MediaItem(
        id = "5",
        title = "S04 E08 - In the Hands of Giants",
        channel = "Homicide City",
        thumbnailIcon = Icons.Default.Info,
        logoIcon = Icons.Default.MailOutline,
        progress = 0.6f,
        timeRemaining = "7m left",
        videoUrl = "https://example.com/videos/homicide_city_s04e08.mp4"
    )
)

@Composable
fun OtherContents() {
    var selectedItemId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark background similar to the image
    ) {
        // Category chips section (TV Shows, Movies, Recent)
        CategoryChips()

        // Media items list
        MediaList(
            mediaItems = dummyMediaItems,
            selectedItemId = selectedItemId,
            onItemClick = { mediaItem ->
                selectedItemId = mediaItem.id
                println("Clicked on ${mediaItem.title}, video URL: ${mediaItem.videoUrl}")
            }
        )
    }
}

@Composable
fun CategoryChips() {
    val categories = listOf("TV Shows", "Movies", "Recent", "My List", "Downloads")
    var selectedCategory by remember { mutableStateOf("TV Shows") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Add padding at the start
        Spacer(modifier = Modifier.width(16.dp))

        // Create chips for each category
        categories.forEach { category ->
            CategoryChip(
                text = category,
                isSelected = category == selectedCategory,
                onSelected = { selectedCategory = category }
            )
        }

        // Add padding at the end
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.LightGray else Color(0xFF303030)
    val textColor = if (isSelected) Color.Black else Color.White

    Surface(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSelected() },
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun MediaList(
    mediaItems: List<MediaItem>,
    selectedItemId: String?,
    onItemClick: (MediaItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(mediaItems) { item ->
            MediaListItem(
                mediaItem = item,
                isSelected = item.id == selectedItemId,
                onClick = { onItemClick(item) }
            )
        }
    }
}


@Composable
fun MediaListItem(
    mediaItem: MediaItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderModifier = if (isSelected) {
        Modifier.border(width = 2.dp, color = Color.Red, shape = RoundedCornerShape(8.dp))
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .then(borderModifier)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // Thumbnail icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mediaItem.thumbnailIcon,
                    contentDescription = "Thumbnail icon",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Text + progress content
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = mediaItem.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = mediaItem.channel,
                    color = Color.Gray,
                    fontSize = 14.sp,
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
                        progress = mediaItem.progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp),
                        color = Color.White,
                        trackColor = Color(0xFF444444)
                    )

                    Text(
                        text = mediaItem.timeRemaining,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Logo icon
            Icon(
                imageVector = mediaItem.logoIcon,
                contentDescription = "Logo",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}
