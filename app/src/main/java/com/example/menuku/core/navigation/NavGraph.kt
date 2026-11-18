package com.example.menuku.core.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.menuku.features.inventory.PantryViewModel

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.menuku.core.components.MenuKuBottomBar
import com.example.menuku.data.repository.MenuKuRepository
import com.example.menuku.features.auth.AuthScreen
import com.example.menuku.features.auth.LoginScreen
import com.example.menuku.features.auth.RegisterScreen
import com.example.menuku.features.hub.HubScreen
import com.example.menuku.features.inventory.AddIngredientScreen
import com.example.menuku.features.inventory.IngredientDetailScreen
import com.example.menuku.features.inventory.InventoryScreen
import com.example.menuku.features.journal.TasteStoryDetailScreen
import com.example.menuku.features.journal.TasteStoryGalleryScreen
import com.example.menuku.features.journal.WriteTasteStoryScreen
import com.example.menuku.features.profile.NotificationCenterScreen
import com.example.menuku.features.profile.ProfileScreen
import com.example.menuku.features.recipe.CreateCollectionScreen
import com.example.menuku.features.recipe.CreateRecipeScreen
import com.example.menuku.features.recipe.RecipeCatalogScreen
import com.example.menuku.features.recipe.RecipeCollectionScreen
import com.example.menuku.features.recipe.RecipeDetailScreen
import com.example.menuku.features.schedule.ScheduleAddScreen
import com.example.menuku.features.schedule.ScheduleDetailScreen
import com.example.menuku.features.schedule.ScheduleWeeklyScreen
import com.example.menuku.features.shopping.AddShoppingItemScreen
import com.example.menuku.features.shopping.ShoppingItemDetailScreen
import com.example.menuku.features.shopping.ShoppingScreen

@Composable
fun MenuKuApp(


) {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
        Column {
            Text("Konfigurasi Firebase belum tersedia", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Text("Daftarkan aplikasi Android pada proyek Firebase Anda, lalu letakkan google-services.json di folder app. Panduan ada di README proyek.")
        }
        return
    }
    val repository = remember { MenuKuRepository(context) }
    DisposableEffect(repository) { onDispose { repository.close() } }
    val syncError by repository.syncError.collectAsState()
    val pendingWrites by repository.pendingWrites.collectAsState()
    val startRoute = remember { if (repository.hasSession) Screen.Hub.route else Screen.Login.route }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var activeRoute by remember { mutableStateOf(Screen.Login.route) }
    if (currentRoute != null) {
        activeRoute = currentRoute
    }

    val bottomBarRoutes = listOf(
        Screen.Hub.route,
        Screen.Recipes.route,
        Screen.Schedule.route,
        Screen.Inventory.route,
        Screen.Shopping.route
    )

    val showBottomBar = activeRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MenuKuBottomBar(
                    currentRoute = activeRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Hub.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column {
        if (syncError != null) {
            Text(syncError.orEmpty())
            TextButton(onClick = repository::dismissSyncError) { Text("Tutup pesan") }
        } else if (pendingWrites) Text("Perubahan tersimpan di perangkat; menunggu sinkronisasi server.")
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login Screen (Formulir Masuk)
            composable(Screen.Login.route) {
                LoginScreen(
                    repository = repository,
                    onLoginSuccess = {
                        navController.navigate(Screen.Hub.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            // Register Screen (Formulir Pendaftaran)
            composable(Screen.Register.route) {
                RegisterScreen(
                    repository = repository,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Hub.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // Auth Screen (Legacy alias to Login)
            composable(Screen.Auth.route) {
                LoginScreen(
                    repository = repository,
                    onLoginSuccess = {
                        navController.navigate(Screen.Hub.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            // Hub Screen (Dashboard)
            composable(Screen.Hub.route) {
                HubScreen(
                    repository = repository,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Recipe Catalogue & Collections
            composable(Screen.Recipes.route) {



                RecipeCatalogScreen(

                    repository = repository,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: "soto-ayam"
                RecipeDetailScreen(
                    recipeId = recipeId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = Screen.RecipeCollection.route,
                arguments = listOf(navArgument("collectionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId") ?: "diet-sehat"
                RecipeCollectionScreen(
                    collectionId = collectionId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.CreateCollection.route) {
                CreateCollectionScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CreateRecipe.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")
                CreateRecipeScreen(
                    recipeId = recipeId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Inventory & Ingredients
            composable(Screen.Inventory.route) {
                InventoryScreen(
                    viewModel = viewModel(factory = PantryViewModel.Factory(repository)),
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = Screen.IngredientDetail.route,
                arguments = listOf(navArgument("stockId") { type = NavType.StringType })
            ) { backStackEntry ->
                val stockId = backStackEntry.arguments?.getString("stockId") ?: "stk-1"
                IngredientDetailScreen(
                    stockId = stockId,
                    viewModel = viewModel(factory = PantryViewModel.Factory(repository)),
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.AddIngredient.route) {
                AddIngredientScreen(
                    viewModel = viewModel(factory = PantryViewModel.Factory(repository)),
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Meal Schedule
            composable(Screen.Schedule.route) {
                ScheduleWeeklyScreen(
                    repository = repository,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = Screen.ScheduleDetail.route,
                arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: "sch-2"
                ScheduleDetailScreen(
                    scheduleId = scheduleId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.AddSchedule.route) {
                ScheduleAddScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Shopping List
            composable(Screen.Shopping.route) {
                ShoppingScreen(
                    repository = repository,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = Screen.ShoppingItemDetail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: "shp-1"
                ShoppingItemDetailScreen(
                    itemId = itemId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.AddShoppingItem.route) {
                AddShoppingItemScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Taste Story Journal
            composable(Screen.TasteStoryGallery.route) {
                TasteStoryGalleryScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = Screen.TasteStoryDetail.route,
                arguments = listOf(navArgument("storyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val storyId = backStackEntry.arguments?.getString("storyId") ?: "tst-1"
                TasteStoryDetailScreen(
                    storyId = storyId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.WriteTasteStory.route) {
                WriteTasteStoryScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Profile & Preferences
            composable(Screen.Profile.route) {
                ProfileScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route ->
                        if (route == Screen.Login.route || route == Screen.Auth.route) {
                            repository.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.navigate(route)
                        }
                    }
                )
            }

            // Notifications
            composable(Screen.NotificationCenter.route) {
                NotificationCenterScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
        }
        }
    }
}
