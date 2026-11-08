package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.components.CalculatorDisguiseView
import com.example.ui.components.PinInputDialog
import com.example.ui.screens.AppsManagerScreen
import com.example.ui.screens.DrawerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SecurityAmber
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle Quick Lock on lifecycle pause/stop
    DisposableEffect(lifecycleOwner, uiState.isQuickLockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && uiState.isQuickLockEnabled) {
                viewModel.lockVault()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Status message handling
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
            }
            viewModel.clearStatusMessage()
        }
    }

    // Calculator Disguise Mode Active check
    val isShowingDisguise = uiState.isDisguiseModeEnabled && !uiState.isVaultUnlocked

    if (isShowingDisguise) {
        // Disguised as a real calculator
        CalculatorDisguiseView(
            expression = uiState.calcExpression,
            result = uiState.calcResult,
            onKeyClick = { key -> viewModel.onCalcKey(key) },
            onDirectUnlockClick = { viewModel.openPinDialog() }
        )
    } else {
        // Full Vault & App Hider UI
        BackHandler(enabled = uiState.currentTab != MainTab.DRAWER) {
            viewModel.setTab(MainTab.DRAWER)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (uiState.currentTab) {
                                MainTab.DRAWER -> "درج التطبيقات (الظاهرة)"
                                MainTab.APPS_MANAGER -> "إدارة وإخفاء التطبيقات"
                                MainTab.VAULT -> "الخزنة السرية"
                                MainTab.SETTINGS -> "الأمان والتمويه"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color(0xFFF8FAFC)
                        )
                    },
                    actions = {
                        // Refresh installed apps list
                        IconButton(
                            onClick = { viewModel.refreshApps() },
                            modifier = Modifier.testTag("action_refresh_apps")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث التطبيقات",
                                tint = CyberCyan
                            )
                        }

                        // Lock vault button
                        IconButton(
                            onClick = {
                                viewModel.lockVault()
                                Toast.makeText(context, "تم قفل الخزنة", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("action_lock_vault")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "قفل الخزنة",
                                tint = SecurityAmber
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ObsidianBlack,
                        titleContentColor = Color(0xFFF8FAFC)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CardSurfaceDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_navigation_bar")
                ) {
                    val navItems = listOf(
                        NavigationItemData(
                            tab = MainTab.DRAWER,
                            label = "الدرج",
                            icon = Icons.Default.Apps,
                            tag = "nav_drawer"
                        ),
                        NavigationItemData(
                            tab = MainTab.APPS_MANAGER,
                            label = "الإدارة",
                            icon = Icons.Default.ViewList,
                            tag = "nav_manager"
                        ),
                        NavigationItemData(
                            tab = MainTab.VAULT,
                            label = "الخزنة",
                            icon = Icons.Default.Shield,
                            tag = "nav_vault",
                            badgeCount = uiState.hiddenPackages.size
                        ),
                        NavigationItemData(
                            tab = MainTab.SETTINGS,
                            label = "الأمان",
                            icon = Icons.Default.Settings,
                            tag = "nav_settings"
                        )
                    )

                    navItems.forEach { item ->
                        val selected = uiState.currentTab == item.tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.setTab(item.tab) },
                            icon = {
                                if (item.badgeCount != null && item.badgeCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = SecurityAmber) {
                                                Text(
                                                    text = item.badgeCount.toString(),
                                                    color = Color.Black,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00363F),
                                selectedTextColor = CyberCyan,
                                indicatorColor = CyberCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = ObsidianBlack,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = uiState.currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        MainTab.DRAWER -> {
                            DrawerScreen(
                                allApps = uiState.allApps,
                                hiddenPackagesCount = uiState.hiddenPackages.size,
                                searchQuery = uiState.searchQuery,
                                isLoading = uiState.isLoading,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onLaunchApp = { pkg ->
                                    val launched = viewModel.launchApp(pkg)
                                    if (!launched) {
                                        Toast.makeText(context, "تعذر تشغيل التطبيق", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onOpenVault = { viewModel.setTab(MainTab.VAULT) },
                                onOpenAppDetails = { pkg -> viewModel.openAppDetailsSettings(pkg) }
                            )
                        }

                        MainTab.APPS_MANAGER -> {
                            AppsManagerScreen(
                                allApps = uiState.allApps,
                                searchQuery = uiState.searchQuery,
                                isLoading = uiState.isLoading,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onToggleHide = { app -> viewModel.toggleAppHidden(app) },
                                onLaunchApp = { pkg -> viewModel.launchApp(pkg) },
                                onOpenAppDetails = { pkg -> viewModel.openAppDetailsSettings(pkg) }
                            )
                        }

                        MainTab.VAULT -> {
                            VaultScreen(
                                allApps = uiState.allApps,
                                isDecoyMode = uiState.isDecoyMode,
                                onUnhidePackage = { pkg -> viewModel.unhidePackage(pkg) },
                                onLaunchApp = { pkg ->
                                    val launched = viewModel.launchApp(pkg)
                                    if (!launched) {
                                        Toast.makeText(context, "تعذر تشغيل التطبيق", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onOpenAppDetails = { pkg -> viewModel.openAppDetailsSettings(pkg) },
                                onLockVault = {
                                    viewModel.lockVault()
                                    Toast.makeText(context, "تم قفل الخزنة", Toast.LENGTH_SHORT).show()
                                },
                                onNavigateToManager = { viewModel.setTab(MainTab.APPS_MANAGER) }
                            )
                        }

                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                isDisguiseModeEnabled = uiState.isDisguiseModeEnabled,
                                isQuickLockEnabled = uiState.isQuickLockEnabled,
                                hasDecoyPin = viewModel.preferences.hasDecoyPin(),
                                hiddenAppsCount = uiState.hiddenPackages.size,
                                totalAppsCount = uiState.allApps.size,
                                onToggleDisguiseMode = { viewModel.setDisguiseMode(it) },
                                onToggleQuickLock = { viewModel.setQuickLock(it) },
                                onChangePinClick = { viewModel.setShowChangePin(true) },
                                onDecoyPinClick = { viewModel.setShowDecoyPin(true) },
                                onOpenHomeSettingsClick = { viewModel.openHomeSettings() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs: PIN Unlock Dialog
    if (uiState.showPinDialog) {
        PinInputDialog(
            title = "فتح الخزنة السرية",
            subtitle = "أدخل رمز PIN للدخول إلى التطبيقات المخفية",
            onPinSubmit = { pin ->
                val ok = viewModel.unlockVaultWithPin(pin)
                if (ok) {
                    Toast.makeText(context, "تم فتح الخزنة بنجاح", Toast.LENGTH_SHORT).show()
                }
                ok
            },
            onDismiss = { viewModel.closePinDialog() }
        )
    }

    // Dialogs: Change Secret PIN Dialog
    if (uiState.showChangePinDialog) {
        PinInputDialog(
            title = "تعيين رمز PIN جديد",
            subtitle = "أدخل الرمز الجديد المكون من 4 أرقام",
            isSetupMode = true,
            onPinSubmit = { newPin ->
                viewModel.updatePin(newPin)
                Toast.makeText(context, "تم تغيير الرمز السري بنجاح", Toast.LENGTH_SHORT).show()
                true
            },
            onDismiss = { viewModel.setShowChangePin(false) }
        )
    }

    // Dialogs: Set Decoy PIN Dialog
    if (uiState.showDecoyPinDialog) {
        PinInputDialog(
            title = "تعيين رمز التمويه (خزنة وهمية)",
            subtitle = "رمز بديل يفتح خزنة خالية للحماية في حالات الطوارئ",
            isSetupMode = true,
            onPinSubmit = { decoyPin ->
                viewModel.updateDecoyPin(decoyPin)
                Toast.makeText(context, "تم تعيين رمز التمويه بنجاح", Toast.LENGTH_SHORT).show()
                true
            },
            onDismiss = { viewModel.setShowDecoyPin(false) }
        )
    }
}

private data class NavigationItemData(
    val tab: MainTab,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String,
    val badgeCount: Int? = null
)
