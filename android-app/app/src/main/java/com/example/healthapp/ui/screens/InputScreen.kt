package com.example.healthapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.healthapp.ui.components.Screen
import com.example.healthapp.viewmodel.MainViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(viewModel: MainViewModel, navController: NavController) {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }
    var pickedTime by remember { mutableStateOf(LocalTime.now()) }
    
    val dateDialogState = rememberMaterialDialogState()
    val timeDialogState = rememberMaterialDialogState()

    val users by viewModel.users.collectAsState()
    val context = LocalContext.current
    
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    LaunchedEffect(users) {
        if (users.isNotEmpty() && selectedUser.isEmpty()) {
            selectedUser = users[0]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Add scroll support
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("记录血压", style = MaterialTheme.typography.headlineMedium)

        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { // Increase spacing
                // User Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedUser,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("姓名") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user) },
                                onClick = {
                                    selectedUser = user
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Date Time Picker
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { // Increase spacing
                    // Date Input
                    Box(modifier = Modifier.weight(1.2f)) { // Give date more weight
                        OutlinedTextField(
                            value = pickedDate.format(dateFormatter),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("日期") },
                            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { dateDialogState.show() }
                        )
                    }

                    // Time Input
                    Box(modifier = Modifier.weight(0.8f)) {
                        OutlinedTextField(
                            value = pickedTime.format(timeFormatter),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("时间") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { timeDialogState.show() }
                        )
                    }
                }

                OutlinedTextField(
                    value = systolic,
                    onValueChange = { if (it.all { char -> char.isDigit() }) systolic = it },
                    label = { Text("收缩压 (高压)") },
                    placeholder = { Text("120") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { if (it.all { char -> char.isDigit() }) diastolic = it },
                    label = { Text("舒张压 (低压)") },
                    placeholder = { Text("80") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pulse,
                    onValueChange = { if (it.all { char -> char.isDigit() }) pulse = it },
                    label = { Text("心率 (选填)") },
                    placeholder = { Text("75") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Button(
            onClick = {
                if (systolic.isBlank() || diastolic.isBlank() || selectedUser.isBlank()) {
                    Toast.makeText(context, "请填写完整信息", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                val dateTime = pickedDate.atTime(pickedTime)
                val date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
                
                viewModel.addRecord(
                    systolic.toInt(),
                    diastolic.toInt(),
                    pulse.toIntOrNull(),
                    selectedUser,
                    date
                ) {
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                    systolic = ""
                    diastolic = ""
                    pulse = ""
                    pickedDate = LocalDate.now()
                    pickedTime = LocalTime.now()
                    
                    // Navigate to history screen
                    navController.navigate(Screen.List.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存记录")
        }
    }
    
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton("确定")
            negativeButton("取消")
        }
    ) {
        datepicker(
            initialDate = pickedDate,
            title = "选择日期"
        ) {
            pickedDate = it
        }
    }
    
    MaterialDialog(
        dialogState = timeDialogState,
        buttons = {
            positiveButton("确定")
            negativeButton("取消")
        }
    ) {
        timepicker(
            initialTime = pickedTime,
            title = "选择时间",
            is24HourClock = true
        ) {
            pickedTime = it
        }
    }
}
