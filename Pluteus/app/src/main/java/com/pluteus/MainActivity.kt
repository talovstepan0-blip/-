package com.pluteus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pluteus.domain.model.MediaItem
import com.pluteus.ui.screens.AddEditScreen
import com.pluteus.ui.screens.HomeScreen
import com.pluteus.ui.theme.PluteusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PluteusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory((application as PluteusApplication).repository))
    
    val items by viewModel.items.collectAsState(initial = emptyList())
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                items = items,
                onItemSelected = { item ->
                    navController.navigate("edit/${item.id}")
                },
                onAddClick = {
                    navController.navigate("add")
                },
                onDeleteItem = { item ->
                    viewModel.deleteItem(item)
                }
            )
        }
        
        composable("add") {
            AddEditScreen(
                item = null,
                onSave = { newItem ->
                    viewModel.saveItem(newItem)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "edit/{itemId}",
            arguments = listOf(androidx.navigation.navArgument("itemId") {
                type = androidx.navigation.NavType.LongType
            })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
            
            val item by viewModel.getItemById(itemId).collectAsState(initial = null)
            
            AddEditScreen(
                item = item,
                onSave = { updatedItem ->
                    viewModel.saveItem(updatedItem)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
