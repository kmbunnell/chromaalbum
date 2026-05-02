package com.example.chromaalbum.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chromaalbum.R
import com.example.chromaalbum.domain.model.Album
import com.example.chromaalbum.ui.theme.mapToDarkColorScheme
import com.example.chromaalbum.ui.theme.mapToLightColorScheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleSelect: () -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()
    val albumScheme =
        remember(album.palette, isDark) {
            if (isDark) mapToDarkColorScheme(album.palette) else mapToLightColorScheme(album.palette)
        }

    val albumContentDescription = stringResource(R.string.album_card_content_description, album.name)
    ElevatedCard(
        modifier =
            modifier
                .testTag("album_card")
                .semantics { contentDescription = albumContentDescription }
                .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        colors = CardDefaults.elevatedCardColors(containerColor = albumScheme.primaryContainer),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    tint = albumScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = album.name,
                    fontWeight = FontWeight.Medium,
                    color = albumScheme.onPrimaryContainer,
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                )
            }
        }
    }
}
