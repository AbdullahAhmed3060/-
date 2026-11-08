package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstalledAppInfo
import com.example.ui.components.AppListItem
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SecurityAmber

enum class AppsFilter {
    ALL,
    VISIBLE_ONLY,
    HIDDEN_ONLY
}

@Composable
fun AppsManagerScreen(
    allApps: List<InstalledAppInfo>,
    searchQuery: String,
    isLoading: Boolean,
    onSearchChange: (String) -> Unit,
    onToggleHide: (InstalledAppInfo) -> Unit,
    onLaunchApp: (String) -> Unit,
    onOpenAppDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(AppsFilter.ALL) }

    val filteredApps = remember(allApps, searchQuery, selectedFilter) {
        allApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                AppsFilter.ALL -> true
                AppsFilter.VISIBLE_ONLY -> !app.isHidden
                AppsFilter.HIDDEN_ONLY -> app.isHidden
            }

            matchesSearch && matchesFilter
        }
    }

    val totalHiddenCount = remember(allApps) { allApps.count { it.isHidden } }
    val totalVisibleCount = remember(allApps) { allApps.count { !it.isHidden } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text("ابحث لتحديد التطبيق المراد إخفاؤه...", fontSize = 14.sp, color = Color(0xFF64748B))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = CardSurfaceDark,
                unfocusedContainerColor = CardSurfaceDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("manager_search_input")
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == AppsFilter.ALL,
                onClick = { selectedFilter = AppsFilter.ALL },
                label = { Text("الكل (${allApps.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberCyan,
                    selectedLabelColor = Color(0xFF00363F),
                    containerColor = CardSurfaceDark,
                    labelColor = Color(0xFF94A3B8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == AppsFilter.ALL,
                    borderColor = Color(0xFF334155),
                    selectedBorderColor = CyberCyan
                ),
                modifier = Modifier.testTag("filter_chip_all")
            )

            FilterChip(
                selected = selectedFilter == AppsFilter.HIDDEN_ONLY,
                onClick = { selectedFilter = AppsFilter.HIDDEN_ONLY },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (selectedFilter == AppsFilter.HIDDEN_ONLY) Color(0xFF452B00) else SecurityAmber
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("المخفية ($totalHiddenCount)", fontSize = 12.sp)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SecurityAmber,
                    selectedLabelColor = Color(0xFF452B00),
                    containerColor = CardSurfaceDark,
                    labelColor = Color(0xFF94A3B8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == AppsFilter.HIDDEN_ONLY,
                    borderColor = Color(0xFF334155),
                    selectedBorderColor = SecurityAmber
                ),
                modifier = Modifier.testTag("filter_chip_hidden")
            )

            FilterChip(
                selected = selectedFilter == AppsFilter.VISIBLE_ONLY,
                onClick = { selectedFilter = AppsFilter.VISIBLE_ONLY },
                label = { Text("الظاهرة ($totalVisibleCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonEmerald,
                    selectedLabelColor = Color(0xFF003822),
                    containerColor = CardSurfaceDark,
                    labelColor = Color(0xFF94A3B8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == AppsFilter.VISIBLE_ONLY,
                    borderColor = Color(0xFF334155),
                    selectedBorderColor = NeonEmerald
                ),
                modifier = Modifier.testTag("filter_chip_visible")
            )
        }

        // List Content
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyberCyan)
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد تطبيقات مطابقة لهذا الفلتر",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("apps_manager_list"),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppListItem(
                        app = app,
                        onLaunch = { onLaunchApp(app.packageName) },
                        onToggleHide = { onToggleHide(app) },
                        onOpenSystemDetails = { onOpenAppDetails(app.packageName) }
                    )
                }
            }
        }
    }
}
