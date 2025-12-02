package com.example.healthapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.api.RetrofitClient
import com.example.healthapp.utils.ExportUtils
import com.example.healthapp.utils.PreferenceManager
import com.example.healthapp.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        
        // 服务授权
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "服务授权",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                var authCode by remember { mutableStateOf(prefs.getAuthCode()) }
                var isAuthenticating by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                
                OutlinedTextField(
                    value = authCode,
                    onValueChange = { authCode = it },
                    label = { Text("授权码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (authCode.isBlank()) {
                            Toast.makeText(context, "请输入授权码", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            isAuthenticating = true
                            try {
                                val response = RetrofitClient.apiService.login(com.example.healthapp.api.LoginRequest(authCode))
                                prefs.saveAuthToken(response.token)
                                prefs.saveAuthCode(authCode) // 保存授权码用于自动刷新
                                Toast.makeText(context, "授权验证成功", Toast.LENGTH_SHORT).show()
                                mainViewModel.refreshData() // 授权成功后刷新数据
                            } catch (e: Exception) {
                                Toast.makeText(context, "授权失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isAuthenticating = false
                            }
                        }
                    },
                    enabled = !isAuthenticating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("验证并保存")
                    }
                }
            }
        }
        
        // 数据导出功能
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "数据导出",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val scope = rememberCoroutineScope()
                val records by mainViewModel.records.collectAsState()
                
                // Excel 导出 Launcher
                val excelLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val success = withContext(Dispatchers.IO) {
                                ExportUtils.writeExcelToUri(context, it, records)
                            }
                            if (success) {
                                Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                
                // PDF 导出 Launcher
                val pdfLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val success = withContext(Dispatchers.IO) {
                                ExportUtils.writePdfToUri(context, it, records)
                            }
                            if (success) {
                                Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                
                Button(
                    onClick = {
                        excelLauncher.launch("health_data_${System.currentTimeMillis()}.xlsx")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("导出为 Excel")
                }
                
                OutlinedButton(
                    onClick = {
                        pdfLauncher.launch("health_report_${System.currentTimeMillis()}.pdf")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("导出为 PDF")
                }
            }
        }
        
        // 版本信息
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            Text(
                text = "当前版本: v$versionName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
