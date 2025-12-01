package com.example.healthapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.api.RetrofitClient
import com.example.healthapp.utils.PreferenceManager
import com.example.healthapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(mainViewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var url by remember { mutableStateOf(prefs.getBaseUrl()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineMedium)
        
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "后端服务地址",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "默认模拟器地址: http://10.0.2.2:3000/\n局域网地址示例: http://192.168.1.5:3000/",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("API Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (url.isBlank()) {
                            Toast.makeText(context, "地址不能为空", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        try {
                            prefs.saveBaseUrl(url)
                            RetrofitClient.setBaseUrl(url)
                            // 立即刷新数据以验证新地址
                            mainViewModel.refreshData()
                            Toast.makeText(context, "设置已保存并刷新数据", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存并刷新")
                }
            }
        }
    }
}
