package com.example.healthapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthapp.api.RetrofitClient
import com.example.healthapp.utils.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(onConfigured: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var url by remember { mutableStateOf("http://10.0.2.2:3000/") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "欢迎使用健康管理",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "首次使用需要配置后端服务地址",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "后端服务地址",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    "• 模拟器默认地址: http://10.0.2.2:3000/\n• 局域网地址示例: http://192.168.1.5:3000/\n• 确保后端服务已启动",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("API Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("http://10.0.2.2:3000/") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (url.isBlank()) {
                            Toast.makeText(context, "请输入后端地址", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        try {
                            prefs.saveBaseUrl(url)
                            RetrofitClient.setBaseUrl(url)
                            Toast.makeText(context, "配置成功", Toast.LENGTH_SHORT).show()
                            onConfigured()
                        } catch (e: Exception) {
                            Toast.makeText(context, "配置失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("开始使用")
                }
            }
        }
    }
}
