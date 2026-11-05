package com.euiyoul.todo.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.euiyoul.todo.ui.edit.TodoEditScreen
import com.euiyoul.todo.ui.list.TodoListScreen
import com.euiyoul.todo.ui.theme.ToDoTheme

class MainActivity : ComponentActivity() {

    /** 위젯·알림에서 들어온 요청으로 열어야 할 화면 (처리 후 null) */
    private var pendingRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) pendingRoute = routeFor(intent)

        setContent {
            ToDoTheme {
                val navController = rememberNavController()

                LaunchedEffect(pendingRoute) {
                    val route = pendingRoute ?: return@LaunchedEffect
                    navController.popBackStack(Routes.LIST, inclusive = false)
                    if (route != Routes.LIST) navController.navigate(route)
                    pendingRoute = null
                }

                NavHost(navController, startDestination = Routes.LIST) {
                    composable(Routes.LIST) {
                        TodoListScreen(
                            onAdd = { navController.navigate(Routes.edit()) },
                            onEdit = { id -> navController.navigate(Routes.edit(id)) },
                        )
                    }
                    composable(
                        Routes.EDIT,
                        arguments = listOf(navArgument(Routes.ARG_ID) {
                            type = NavType.LongType
                            defaultValue = Routes.NEW_ID
                        }),
                    ) {
                        TodoEditScreen(onDone = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingRoute = routeFor(intent)
    }

    private fun routeFor(intent: Intent): String? = when {
        intent.getBooleanExtra(EXTRA_OPEN_ADD, false) -> Routes.edit()
        intent.hasExtra(EXTRA_EDIT_ID) -> Routes.edit(intent.getLongExtra(EXTRA_EDIT_ID, Routes.NEW_ID))
        intent.getBooleanExtra(EXTRA_OPEN_LIST, false) -> Routes.LIST
        else -> null
    }

    companion object {
        private const val EXTRA_OPEN_ADD = "open_add"
        private const val EXTRA_OPEN_LIST = "open_list"
        private const val EXTRA_EDIT_ID = "edit_id"

        private fun baseIntent(context: Context) = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        fun listIntent(context: Context): Intent =
            baseIntent(context).putExtra(EXTRA_OPEN_LIST, true)

        fun addIntent(context: Context): Intent =
            baseIntent(context).putExtra(EXTRA_OPEN_ADD, true)

        fun editIntent(context: Context, todoId: Long): Intent =
            baseIntent(context).putExtra(EXTRA_EDIT_ID, todoId)
    }
}

object Routes {
    const val LIST = "list"
    const val ARG_ID = "id"
    const val EDIT = "edit?$ARG_ID={$ARG_ID}"
    const val NEW_ID = -1L

    fun edit(id: Long = NEW_ID) = "edit?$ARG_ID=$id"
}
