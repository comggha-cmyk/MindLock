package com.disciplinex.ui.screens

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.disciplinex.appblock.AppBlockViewModel
import com.disciplinex.data.entities.BlockedAppEntity
import com.disciplinex.ui.components.*
import com.disciplinex.ui.theme.DXColors

@Composable
fun AppBlockScreen(vm: AppBlockViewModel = hiltViewModel()) {
    val blockedApps by vm.blockedApps.collectAsState()
    val installedApps by vm.installedApps.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DXColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    SectionHeader("APP BLOCKER")
                    Text(
                        "Distraction Shield",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DXColors.OnBackground,
                        fontWeight = FontWeight.Black
                    )
                }
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = DXColors.Danger,
                    contentColor = Color.White,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Add blocked app")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Permission warning
            if (!vm.hasAccessibilityPermission) {
                PermissionWarningCard(
                    message = "Enable Accessibility Service to activate app blocking",
                    actionLabel = "ENABLE",
                    onClick = { vm.openAccessibilitySettings() }
                )
                Spacer(Modifier.height(16.dp))
            }

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    icon = Icons.Default.Block,
                    value = "${blockedApps.count { it.isBlocked }}",
                    label = "Blocked Apps",
                    color = DXColors.Danger,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Default.Shield,
                    value = "Active",
                    label = "Status",
                    color = if (vm.hasAccessibilityPermission) DXColors.Secondary else DXColors.Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Tab row
            TabRow(
                selectedTabIndex = tab,
                containerColor = DXColors.SurfaceVariant,
                contentColor = DXColors.Primary,
                indicator = { tabPositions ->
                    if (tab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[tab]),
                            color = DXColors.Danger
                        )
                    }
                }
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }) {
                    Text("BLOCKED (${blockedApps.size})", modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
                Tab(selected = tab == 1, onClick = { tab = 1 }) {
                    Text("ALL APPS", modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(16.dp))

            when (tab) {
                0 -> BlockedAppsList(
                    apps = blockedApps,
                    onToggle = { vm.toggleBlock(it) },
                    onRemove = { vm.removeBlockedApp(it) },
                    onTimerChange = { app, mins -> vm.updateWaitTimer(app, mins) }
                )
                1 -> AllAppsList(
                    apps = installedApps,
                    blockedPackages = blockedApps.map { it.packageName }.toSet(),
                    onBlock = { vm.addBlockedApp(it) }
                )
            }
        }
    }
}

@Composable
private fun BlockedAppsList(
    apps: List<BlockedAppEntity>,
    onToggle: (BlockedAppEntity) -> Unit,
    onRemove: (BlockedAppEntity) -> Unit,
    onTimerChange: (BlockedAppEntity, Int) -> Unit
) {
    if (apps.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Block,
            message = "No apps blocked.\nSwitch to 'All Apps' to add distractions.",
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(apps, key = { it.packageName }) { app ->
                BlockedAppCard(app, onToggle, onRemove, onTimerChange)
            }
        }
    }
}

@Composable
private fun BlockedAppCard(
    app: BlockedAppEntity,
    onToggle: (BlockedAppEntity) -> Unit,
    onRemove: (BlockedAppEntity) -> Unit,
    onTimerChange: (BlockedAppEntity, Int) -> Unit
) {
    NeonCard(glowColor = if (app.isBlocked) DXColors.Danger else DXColors.OnBackgroundFaint) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, style = MaterialTheme.typography.titleMedium, color = DXColors.OnBackground, fontWeight = FontWeight.Bold)
                Text(app.packageName, style = MaterialTheme.typography.labelMedium, color = DXColors.OnBackgroundMuted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("⏱ ${app.waitTimerMinutes}min wait", style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DXColors.Warning.copy(alpha = 0.15f),
                            selectedLabelColor = DXColors.Warning
                        )
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Switch(
                    checked = app.isBlocked,
                    onCheckedChange = { onToggle(app) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = DXColors.Danger,
                        checkedThumbColor = Color.White
                    )
                )
                IconButton(onClick = { onRemove(app) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = DXColors.OnBackgroundFaint, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AllAppsList(
    apps: List<Pair<String, String>>, // packageName to appName
    blockedPackages: Set<String>,
    onBlock: (Pair<String, String>) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(apps, search) {
        if (search.isBlank()) apps
        else apps.filter { it.second.contains(search, ignoreCase = true) }
    }

    Column {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search apps...", color = DXColors.OnBackgroundMuted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = DXColors.OnBackgroundMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DXColors.Primary,
                unfocusedBorderColor = DXColors.OnBackgroundFaint,
                focusedTextColor = DXColors.OnBackground,
                unfocusedTextColor = DXColors.OnBackground
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.first }) { (pkg, name) ->
                val isBlocked = pkg in blockedPackages
                NeonCard(glowColor = if (isBlocked) DXColors.Danger else DXColors.OnBackgroundFaint) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(name, style = MaterialTheme.typography.titleMedium, color = DXColors.OnBackground)
                            Text(pkg, style = MaterialTheme.typography.labelMedium, color = DXColors.OnBackgroundMuted)
                        }
                        if (isBlocked) {
                            Text("BLOCKED", style = MaterialTheme.typography.labelMedium, color = DXColors.Danger)
                        } else {
                            IconButton(onClick = { onBlock(pkg to name) }) {
                                Icon(Icons.Default.Add, "Block", tint = DXColors.Danger)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionWarningCard(message: String, actionLabel: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DXColors.Warning.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, DXColors.Warning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Warning, null, tint = DXColors.Warning, modifier = Modifier.size(20.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = DXColors.Warning)
        }
        TextButton(onClick = onClick) {
            Text(actionLabel, color = DXColors.Warning, style = MaterialTheme.typography.labelLarge)
        }
    }
}
