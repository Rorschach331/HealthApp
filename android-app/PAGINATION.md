# 分页加载和服务器地址实时生效 - 实现说明

## 🎯 实现的功能

### 1. ✅ 真正的分页加载

- **服务端分页**：每次从服务器加载 20 条记录
- **滚动加载**：滚动到底部自动加载下一页
- **下拉刷新**：下拉刷新重置到第一页
- **加载状态**：显示加载中和加载更多的指示器
- **分页信息**：显示当前页/总页数/总记录数

### 2. ✅ 服务器地址实时生效

- **共享 ViewModel**：所有屏幕共享同一个 ViewModel 实例
- **立即刷新**：修改服务器地址后自动刷新数据
- **无需清缓存**：修改后立即生效，无需重启应用

### 3. ✅ 筛选功能优化

- **服务端筛选**：筛选条件发送到服务器
- **应用筛选**：点击"应用筛选"按钮后才发送请求
- **重置筛选**：一键重置所有筛选条件

## 📋 技术实现

### ViewModel 改进

```kotlin
class MainViewModel : ViewModel() {
    // 分页状态
    private var currentPage = 1
    private val pageSize = 20

    // 筛选状态
    private val _filterName = MutableStateFlow("")
    private val _filterStart = MutableStateFlow("")
    private val _filterEnd = MutableStateFlow("")

    // 加载状态
    private val _loading = MutableStateFlow(false)
    private val _loadingMore = MutableStateFlow(false)

    // 元数据
    private val _meta = MutableStateFlow(Meta(0, 1, 20, 0))

    // 分页加载
    fun fetchRecords(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            _loading.value = true
        } else {
            _loadingMore.value = true
        }

        // 调用 API 获取数据
        val response = api.getRecords(
            start = filterStart,
            end = filterEnd,
            name = filterName,
            page = currentPage,
            pageSize = pageSize
        )

        // 追加或替换数据
        if (reset) {
            _records.value = response.data
        } else {
            _records.value = _records.value + response.data
        }
    }

    // 加载更多
    fun loadMore() {
        if (currentPage >= _meta.value.totalPages) return
        currentPage++
        fetchRecords(reset = false)
    }

    // 刷新数据（用于服务器地址变更）
    fun refreshData() {
        fetchUsers()
        fetchRecords(reset = true)
    }
}
```

### HistoryScreen 改进

```kotlin
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val listState = rememberLazyListState()

    // 检测滚动到底部
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= records.size - 3) {
                    viewModel.loadMore()
                }
            }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.fetchRecords(reset = true) }
    ) {
        LazyColumn(state = listState) {
            items(records) { record ->
                RecordCard(record)
            }

            // 加载更多指示器
            if (loadingMore) {
                item {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
```

### MainActivity 改进

```kotlin
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // 所有屏幕共享同一个 ViewModel 实例
    val mainViewModel: MainViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Input.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Input.route) { InputScreen(mainViewModel) }
            composable(Screen.List.route) { HistoryScreen(mainViewModel) }
            composable(Screen.Chart.route) { ChartScreen(mainViewModel) }
            composable(Screen.Settings.route) { SettingsScreen(mainViewModel) }
        }
    }
}
```

### SettingsScreen 改进

```kotlin
@Composable
fun SettingsScreen(mainViewModel: MainViewModel) {
    Button(
        onClick = {
            prefs.saveBaseUrl(url)
            RetrofitClient.setBaseUrl(url)
            // 立即刷新数据以验证新地址
            mainViewModel.refreshData()
            Toast.makeText(context, "设置已保存并刷新数据", Toast.LENGTH_SHORT).show()
        }
    ) {
        Text("保存并刷新")
    }
}
```

## 🔄 数据流

1. **初始加载**：

   - App 启动 → ViewModel.init() → fetchRecords(reset=true) → 加载第 1 页

2. **滚动加载**：

   - 滚动到底部 → 检测到 lastVisibleIndex → loadMore() → currentPage++ → fetchRecords(reset=false) → 追加数据

3. **下拉刷新**：

   - 下拉 → onRefresh → fetchRecords(reset=true) → currentPage=1 → 替换数据

4. **筛选**：

   - 设置筛选条件 → 点击"应用筛选" → setFilter() → fetchRecords(reset=true) → 加载筛选后的第 1 页

5. **服务器地址变更**：
   - 修改地址 → 保存 → RetrofitClient.setBaseUrl() → refreshData() → 重新加载用户和记录

## 📊 状态管理

| 状态          | 类型           | 说明                               |
| ------------- | -------------- | ---------------------------------- |
| `records`     | `List<Record>` | 当前显示的记录列表                 |
| `loading`     | `Boolean`      | 首次加载或刷新中                   |
| `loadingMore` | `Boolean`      | 加载更多中                         |
| `meta`        | `Meta`         | 分页元数据（总数、当前页、总页数） |
| `filterName`  | `String`       | 姓名筛选条件                       |
| `filterStart` | `String`       | 开始日期筛选条件                   |
| `filterEnd`   | `String`       | 结束日期筛选条件                   |

## 🎨 UI 反馈

1. **首次加载**：SwipeRefresh 显示加载动画
2. **加载更多**：列表底部显示 CircularProgressIndicator
3. **无更多数据**：显示"已加载全部"
4. **分页信息**：显示"第 X / Y 页，共 Z 条记录"
5. **服务器地址变更**：Toast 提示"设置已保存并刷新数据"

## ✅ 优势

1. **性能优化**：每次只加载 20 条，减少内存占用和网络流量
2. **用户体验**：流畅的滚动加载，无需手动点击"加载更多"
3. **实时生效**：服务器地址修改后立即生效，无需重启
4. **状态同步**：所有屏幕共享同一个数据源，状态一致

## 🚀 后续优化

1. **预加载**：提前加载下一页，减少等待时间
2. **缓存策略**：缓存已加载的数据，减少重复请求
3. **错误处理**：网络错误时的重试机制
4. **离线支持**：使用 Room 数据库缓存数据
