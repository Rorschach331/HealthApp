package com.example.healthapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(viewModel: MainViewModel) {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }

    val users by viewModel.users.collectAsState()
    val context = LocalContext.current
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    LaunchedEffect(users) {
        if (users.isNotEmpty() && selectedUser.isEmpty()) {
            selectedUser = users[0]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("记录血压", style = MaterialTheme.typography.headlineMedium)

        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                OutlinedTextField(
                    value = dateFormat.format(selectedDateTime.time),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("记录时间") },
                    trailingIcon = {
                        IconButton(onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    selectedDateTime.set(year, month, day)
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                                            selectedDateTime.set(Calendar.MINUTE, minute)
                                        },
                                        selectedDateTime.get(Calendar.HOUR_OF_DAY),
                                        selectedDateTime.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                selectedDateTime.get(Calendar.YEAR),
                                selectedDateTime.get(Calendar.MONTH),
                                selectedDateTime.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择日期时间")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = systolic,
                    onValueChange = { systolic = it },
                    label = { Text("收缩压 (高压) mmHg") },
                    placeholder = { Text("120") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { diastolic = it },
                    label = { Text("舒张压 (低压) mmHg") },
                    placeholder = { Text("80") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pulse,
                    onValueChange = { pulse = it },
                    label = { Text("心率 (次/分) - 选填") },
                    placeholder = { Text("75") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                if (systolic.isBlank() || diastolic.isBlank() || selectedUser.isBlank()) {
                    Toast.makeText(context, "请填写完整信息", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.addRecord(
                    systolic.toInt(),
                    diastolic.toInt(),
                    pulse.toIntOrNull(),
                    selectedUser,
                    selectedDateTime.time
                ) {
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                    systolic = ""
                    diastolic = ""
                    pulse = ""
                    selectedDateTime = Calendar.getInstance()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存记录")
        }
    }
}
