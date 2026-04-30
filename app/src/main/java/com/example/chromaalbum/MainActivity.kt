package com.example.chromaalbum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chromaalbum.navigation.AlbumRoute
import com.example.chromaalbum.navigation.HomeRoute
import com.example.chromaalbum.navigation.ViewerRoute
import com.example.chromaalbum.ui.screens.AlbumDetailScreen
import com.example.chromaalbum.ui.screens.HomeScreen
import com.example.chromaalbum.ui.screens.PhotoViewerScreen
import com.example.chromaalbum.ui.theme.ChromaalbumTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChromaalbumTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable<HomeRoute>(
                        enterTransition = { fadeIn(tween(300)) },
                        popEnterTransition = { fadeIn(tween(300)) },
                    ) {
                        HomeScreen(
                            onAlbumClick = { albumId ->
                                navController.navigate(AlbumRoute(albumId))
                            },
                        )
                    }
                    composable<AlbumRoute>(
                        enterTransition = { slideInHorizontally { it } },
                        popExitTransition = { slideOutHorizontally { it } },
                    ) { backStackEntry ->
                        val route = backStackEntry.toRoute<AlbumRoute>()
                        AlbumDetailScreen(
                            albumId = route.albumId,
                            onPhotoClick = { index ->
                                navController.navigate(ViewerRoute(route.albumId, index))
                            },
                            onBack = { navController.navigateUp() },
                        )
                    }
                    composable<ViewerRoute>(
                        enterTransition = { slideInHorizontally { it } },
                        popExitTransition = { slideOutHorizontally { it } },
                    ) { backStackEntry ->
                        val route = backStackEntry.toRoute<ViewerRoute>()
                        PhotoViewerScreen(
                            albumId = route.albumId,
                            startIndex = route.startIndex,
                            onBack = { navController.navigateUp() },
                        )
                    }
                }
            }
        }
    }
}
