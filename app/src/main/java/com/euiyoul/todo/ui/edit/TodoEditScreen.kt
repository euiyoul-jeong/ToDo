package com.euiyoul.todo.ui.edit

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euiyoul.todo.R
import com.euiyoul.todo.data.Priority
import com.euiyoul.todo.reminder.ReminderReceiver
import com.euiyoul.todo.reminder.ReminderScheduler
import com.euiyoul.todo.repository
import com.euiyoul.todo.ui.list.ColorDot
import com.euiyoul.todo.util.formatDate
import com.euiyoul.todo.util.formatTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

private val DefaultDueTime: LocalTime = LocalTime.of(9, 0)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TodoEditScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val repository = context.repository
    val viewModel: TodoEditViewModel = viewModel {
        TodoEditViewModel(repository, createSavedStateHandle())
    }
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    var notificationGranted by remember { mutableStateOf(ReminderReceiver.hasNotificationPermission(context)) }
    var exactAlarmAllowed by remember { mutableStateOf(ReminderScheduler.canScheduleExact(context)) }
    LifecycleResumeEffect(Unit) {
        notificationGranted = ReminderReceiver.hasNotificationPermission(context)
        exactAlarmAllowed = ReminderScheduler.canScheduleExact(context)
        onPauseOrDispose { }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notificationGranted = granted }

    val titleFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (viewModel.isNew) titleFocus.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (viewModel.isNew) R.string.add_todo else R.string.edit_todo)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!viewModel.isNew) {
                        IconButton(onClick = { viewModel.delete(onDone) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (!viewModel.loaded) return@Scaffold
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text(stringResource(R.string.field_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
            )
            OutlinedTextField(
                value = viewModel.memo,
                onValueChange = { viewModel.memo = it },
                label = { Text(stringResource(R.string.field_memo)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            // 마감일
            SectionLabel(stringResource(R.string.field_due))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.padding(start = 6.dp))
                    Text(viewModel.dueAt?.let { formatDate(it) } ?: stringResource(R.string.no_due))
                }
                viewModel.dueAt?.let { due ->
                    Spacer(Modifier.padding(start = 8.dp))
                    OutlinedButton(onClick = { showTimePicker = true }) { Text(formatTime(due)) }
                    IconButton(onClick = { viewModel.updateDue(null) }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_due))
                    }
                }
            }

            // 알림
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.reminder), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(
                            if (viewModel.dueAt == null) R.string.reminder_need_due else R.string.reminder_desc
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = viewModel.reminder,
                    enabled = viewModel.dueAt != null,
                    onCheckedChange = { on ->
                        viewModel.reminder = on
                        if (on && !notificationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                )
            }
            if (viewModel.reminder) {
                if (!notificationGranted) {
                    WarningWithAction(
                        message = stringResource(R.string.notification_permission_denied),
                        action = stringResource(R.string.open_settings),
                        onAction = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            )
                        },
                    )
                } else if (!exactAlarmAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    WarningWithAction(
                        message = stringResource(R.string.exact_alarm_hint),
                        action = stringResource(R.string.allow),
                        onAction = {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    .setData(Uri.parse("package:${context.packageName}"))
                            )
                        },
                    )
                }
            }

            // 중요도
            SectionLabel(stringResource(R.string.field_priority))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Priority.entries.forEachIndexed { index, p ->
                    SegmentedButton(
                        selected = viewModel.priority == p,
                        onClick = { viewModel.priority = p },
                        shape = SegmentedButtonDefaults.itemShape(index, Priority.entries.size),
                        icon = { ColorDot(Color(p.color), size = 8) },
                    ) {
                        Text(stringResource(p.labelRes))
                    }
                }
            }

            // 카테고리
            SectionLabel(stringResource(R.string.field_category))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = viewModel.categoryId == null,
                    onClick = { viewModel.categoryId = null },
                    label = { Text(stringResource(R.string.no_category)) },
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = viewModel.categoryId == category.id,
                        onClick = { viewModel.categoryId = category.id },
                        label = { Text(category.name) },
                        leadingIcon = { ColorDot(Color(category.color)) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.save(onDone) },
                enabled = viewModel.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showDatePicker) {
        val zone = ZoneId.systemDefault()
        val current = viewModel.dueAt?.let { Instant.ofEpochMilli(it).atZone(zone) }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (current?.toLocalDate() ?: LocalDate.now(zone))
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { utcMillis ->
                        val date = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        val time = current?.toLocalTime() ?: DefaultDueTime
                        viewModel.updateDue(date.atTime(time).atZone(zone).toInstant().toEpochMilli())
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    val dueForTime = viewModel.dueAt
    if (showTimePicker && dueForTime != null) {
        val zone = ZoneId.systemDefault()
        val current = Instant.ofEpochMilli(dueForTime).atZone(zone)
        val timeState = rememberTimePickerState(
            initialHour = current.hour, initialMinute = current.minute, is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val updated = current.toLocalDate()
                        .atTime(timeState.hour, timeState.minute)
                        .atZone(zone).toInstant().toEpochMilli()
                    viewModel.updateDue(updated)
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = { TimePicker(state = timeState) },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun WarningWithAction(message: String, action: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAction) { Text(action) }
    }
}
