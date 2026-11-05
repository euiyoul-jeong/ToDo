package com.euiyoul.todo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.euiyoul.todo.R
import com.euiyoul.todo.data.CategoryEntity
import com.euiyoul.todo.data.TodoEntity
import com.euiyoul.todo.data.isOverdue
import com.euiyoul.todo.data.sortedActive
import com.euiyoul.todo.repository
import com.euiyoul.todo.ui.MainActivity
import com.euiyoul.todo.util.formatDue
import kotlinx.coroutines.flow.map

/** 홈 화면 위젯: 미완료 할 일 목록, 체크로 완료 처리, + 버튼으로 추가 화면 열기 */
class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.repository
        val initialTodos = repository.getActiveTodos().sortedActive()
        val initialCategories = repository.getCategories()
        val activeTodos = repository.todos.map { all -> all.filter { !it.isDone }.sortedActive() }

        provideContent {
            val todos by activeTodos.collectAsState(initialTodos)
            val categories by repository.categories.collectAsState(initialCategories)
            GlanceTheme {
                WidgetContent(context, todos, categories.associateBy { it.id })
            }
        }
    }
}

@Composable
private fun WidgetContent(
    context: Context,
    todos: List<TodoEntity>,
    categories: Map<Long, CategoryEntity>,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = context.getString(R.string.widget_title, todos.size),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(actionStartActivity(MainActivity.listIntent(context))),
            )
            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .cornerRadius(16.dp)
                    .background(GlanceTheme.colors.primary)
                    .clickable(actionStartActivity(MainActivity.addIntent(context))),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_add),
                    contentDescription = context.getString(R.string.add_todo),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimary),
                    modifier = GlanceModifier.size(20.dp),
                )
            }
        }

        if (todos.isEmpty()) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = context.getString(R.string.widget_empty),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 14.sp),
                )
            }
        } else {
            LazyColumn {
                items(todos, itemId = { it.id }) { todo ->
                    WidgetTodoRow(context, todo, todo.categoryId?.let { categories[it] })
                }
            }
        }
    }
}

@Composable
private fun WidgetTodoRow(context: Context, todo: TodoEntity, category: CategoryEntity?) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(actionStartActivity(MainActivity.editIntent(context, todo.id))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckBox(
            checked = false,
            onCheckedChange = actionRunCallback<ToggleDoneAction>(
                actionParametersOf(ToggleDoneAction.TodoIdKey to todo.id)
            ),
        )
        Box(
            modifier = GlanceModifier
                .size(8.dp)
                .cornerRadius(4.dp)
                .background(Color(todo.priority.color)),
        ) {}
        Spacer(GlanceModifier.width(8.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = todo.title,
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp),
            )
            val meta = listOfNotNull(todo.dueAt?.let { formatDue(it) }, category?.name)
            if (meta.isNotEmpty()) {
                Text(
                    text = meta.joinToString(" · "),
                    maxLines = 1,
                    style = TextStyle(
                        color = if (todo.isOverdue()) GlanceTheme.colors.error
                        else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
                )
            }
        }
    }
}

class ToggleDoneAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[TodoIdKey] ?: return
        context.repository.setDone(id, true)
    }

    companion object {
        val TodoIdKey = ActionParameters.Key<Long>("todo_id")
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}
