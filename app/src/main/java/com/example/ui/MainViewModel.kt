package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.model.InstalledAppInfo
import com.example.repository.AppsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    DRAWER,       // درج التطبيقات (التطبيقات غير المخفية فقط)
    APPS_MANAGER, // إدارة التطبيقات (الكل مع خيار الإخفاء)
    VAULT,        // الخزنة السرية (التطبيقات المخفية)
    SETTINGS      // الإعدادات والأمان
}

data class UiState(
    val currentTab: MainTab = MainTab.DRAWER,
    val searchQuery: String = "",
    val allApps: List<InstalledAppInfo> = emptyList(),
    val hiddenPackages: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isVaultUnlocked: Boolean = false,
    val isDecoyMode: Boolean = false,
    val isDisguiseModeEnabled: Boolean = true,
    val isQuickLockEnabled: Boolean = true,
    val showPinDialog: Boolean = false,
    val showChangePinDialog: Boolean = false,
    val showDecoyPinDialog: Boolean = false,
    val showSetupPinDialog: Boolean = false,
    val calcExpression: String = "",
    val calcResult: String = "",
    val statusMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppsRepository(application, db.hiddenAppDao())
    val preferences = PreferencesManager(application)

    private val _uiState = MutableStateFlow(
        UiState(
            isDisguiseModeEnabled = preferences.isDisguiseModeEnabled,
            isQuickLockEnabled = preferences.isQuickLockEnabled,
            isVaultUnlocked = !preferences.isDisguiseModeEnabled // If disguise is off, start in unlocked state or require PIN on vault tab
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeHiddenApps()
    }

    private fun observeHiddenApps() {
        viewModelScope.launch {
            repository.hiddenPackageNamesFlow.collectLatest { pkgs ->
                val pkgSet = pkgs.toSet()
                _uiState.update { it.copy(hiddenPackages = pkgSet) }
                refreshApps(pkgSet)
            }
        }
    }

    fun refreshApps(hiddenSet: Set<String> = _uiState.value.hiddenPackages) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val apps = repository.getInstalledLaunchableApps(hiddenSet)
            _uiState.update {
                it.copy(
                    allApps = apps,
                    isLoading = false
                )
            }
        }
    }

    fun setTab(tab: MainTab) {
        if (tab == MainTab.VAULT && !_uiState.value.isVaultUnlocked) {
            // Require PIN unlock before viewing the vault
            _uiState.update { it.copy(showPinDialog = true) }
            return
        }
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleAppHidden(app: InstalledAppInfo) {
        viewModelScope.launch {
            if (app.isHidden) {
                repository.unhideApp(app.packageName)
                _uiState.update { it.copy(statusMessage = "تمت استعادة ${app.appName} إلى القائمة") }
            } else {
                repository.hideApp(app.packageName, app.appName, app.isSystemApp)
                _uiState.update { it.copy(statusMessage = "تم إخفاء ${app.appName} بنجاح في الخزنة") }
            }
        }
    }

    fun unhidePackage(packageName: String) {
        viewModelScope.launch {
            repository.unhideApp(packageName)
            _uiState.update { it.copy(statusMessage = "تمت استعادة التطبيق بنجاح") }
        }
    }

    fun launchApp(packageName: String): Boolean {
        return repository.launchApp(packageName)
    }

    fun openAppDetailsSettings(packageName: String) {
        repository.openAppDetailsSettings(packageName)
    }

    fun openHomeSettings() {
        repository.openHomeSettings()
    }

    fun unlockVaultWithPin(pin: String): Boolean {
        if (preferences.isPinCorrect(pin)) {
            _uiState.update {
                it.copy(
                    isVaultUnlocked = true,
                    isDecoyMode = false,
                    showPinDialog = false,
                    currentTab = MainTab.VAULT,
                    calcExpression = "",
                    calcResult = ""
                )
            }
            return true
        } else if (preferences.isDecoyPin(pin)) {
            _uiState.update {
                it.copy(
                    isVaultUnlocked = true,
                    isDecoyMode = true,
                    showPinDialog = false,
                    currentTab = MainTab.VAULT,
                    calcExpression = "",
                    calcResult = ""
                )
            }
            return true
        }
        return false
    }

    fun lockVault() {
        _uiState.update {
            it.copy(
                isVaultUnlocked = false,
                isDecoyMode = false,
                currentTab = if (it.isDisguiseModeEnabled) MainTab.DRAWER else MainTab.DRAWER
            )
        }
    }

    fun setDisguiseMode(enabled: Boolean) {
        preferences.isDisguiseModeEnabled = enabled
        _uiState.update { it.copy(isDisguiseModeEnabled = enabled) }
    }

    fun setQuickLock(enabled: Boolean) {
        preferences.isQuickLockEnabled = enabled
        _uiState.update { it.copy(isQuickLockEnabled = enabled) }
    }

    fun updatePin(newPin: String) {
        preferences.setPin(newPin)
        _uiState.update {
            it.copy(
                showChangePinDialog = false,
                statusMessage = "تم تحديث رمز PIN بنجاح"
            )
        }
    }

    fun updateDecoyPin(pin: String?) {
        preferences.setDecoyPin(pin)
        _uiState.update {
            it.copy(
                showDecoyPinDialog = false,
                statusMessage = if (pin.isNullOrBlank()) "تم تعطيل الرمز التمويهي" else "تم تعيين الرمز التمويهي بنجاح"
            )
        }
    }

    fun openPinDialog() {
        _uiState.update { it.copy(showPinDialog = true) }
    }

    fun closePinDialog() {
        _uiState.update { it.copy(showPinDialog = false) }
    }

    fun setShowChangePin(show: Boolean) {
        _uiState.update { it.copy(showChangePinDialog = show) }
    }

    fun setShowDecoyPin(show: Boolean) {
        _uiState.update { it.copy(showDecoyPinDialog = show) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // Calculator operations for disguise mode
    fun onCalcKey(key: String) {
        val currentExpr = _uiState.value.calcExpression
        when (key) {
            "C" -> {
                _uiState.update { it.copy(calcExpression = "", calcResult = "") }
            }
            "DEL" -> {
                if (currentExpr.isNotEmpty()) {
                    _uiState.update { it.copy(calcExpression = currentExpr.dropLast(1)) }
                }
            }
            "=" -> {
                // Check secret PIN!
                if (preferences.isPinCorrect(currentExpr)) {
                    _uiState.update {
                        it.copy(
                            isVaultUnlocked = true,
                            isDecoyMode = false,
                            currentTab = MainTab.VAULT,
                            calcExpression = "",
                            calcResult = ""
                        )
                    }
                    return
                } else if (preferences.isDecoyPin(currentExpr)) {
                    _uiState.update {
                        it.copy(
                            isVaultUnlocked = true,
                            isDecoyMode = true,
                            currentTab = MainTab.VAULT,
                            calcExpression = "",
                            calcResult = ""
                        )
                    }
                    return
                }

                // If not PIN, evaluate arithmetic
                val result = evaluateMath(currentExpr)
                _uiState.update { it.copy(calcResult = result) }
            }
            else -> {
                _uiState.update { it.copy(calcExpression = currentExpr + key) }
            }
        }
    }

    private fun evaluateMath(expr: String): String {
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/")
            if (sanitized.isBlank()) return ""

            // Simple safe evaluator for +, -, *, /
            val tokens = mutableListOf<String>()
            var currentNumber = StringBuilder()

            for (ch in sanitized) {
                if (ch in "+-*/%") {
                    if (currentNumber.isNotEmpty()) {
                        tokens.add(currentNumber.toString())
                        currentNumber = StringBuilder()
                    }
                    tokens.add(ch.toString())
                } else {
                    currentNumber.append(ch)
                }
            }
            if (currentNumber.isNotEmpty()) {
                tokens.add(currentNumber.toString())
            }

            if (tokens.isEmpty()) return ""

            // High precedence: * and /
            val afterMulDiv = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val t = tokens[i]
                if (t == "*" || t == "/" || t == "%") {
                    val prev = afterMulDiv.removeAt(afterMulDiv.size - 1).toDoubleOrNull() ?: 0.0
                    val next = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 1.0
                    val res = when (t) {
                        "*" -> prev * next
                        "/" -> if (next == 0.0) Double.NaN else prev / next
                        "%" -> prev % next
                        else -> 0.0
                    }
                    afterMulDiv.add(res.toString())
                    i += 2
                } else {
                    afterMulDiv.add(t)
                    i++
                }
            }

            // Low precedence: + and -
            var total = afterMulDiv.getOrNull(0)?.toDoubleOrNull() ?: 0.0
            var j = 1
            while (j < afterMulDiv.size) {
                val op = afterMulDiv[j]
                val next = afterMulDiv.getOrNull(j + 1)?.toDoubleOrNull() ?: 0.0
                when (op) {
                    "+" -> total += next
                    "-" -> total -= next
                }
                j += 2
            }

            if (total.isNaN()) "خطأ"
            else if (total == total.toLong().toDouble()) total.toLong().toString()
            else String.format(java.util.Locale.US, "%.4f", total).trimEnd('0').trimEnd('.')
        } catch (_: Exception) {
            "خطأ"
        }
    }
}
