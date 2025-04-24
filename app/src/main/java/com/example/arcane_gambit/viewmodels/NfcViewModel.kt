package com.example.arcane_gambit.viewmodels

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.arcane_gambit.nfc.NfcManager
import com.example.arcane_gambit.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * ViewModel to handle NFC/RFID related operations
 */
class NfcViewModel(application: Application) : AndroidViewModel(application) {
    // These will be initialized later when an Activity is available
    private var nfcManager: NfcManager? = null
    private val sessionManager = SessionManager(application)

    // LiveData for RFID tag
    private val _currentRfidTag = MutableLiveData<String?>(null)
    val currentRfidTag: LiveData<String?> = _currentRfidTag

    // LiveData for NFC availability
    private val _isNfcAvailable = MutableLiveData(false)
    val isNfcAvailable: LiveData<Boolean> = _isNfcAvailable

    // LiveData for NFC enabled state
    private val _isNfcEnabled = MutableLiveData(false)
    val isNfcEnabled: LiveData<Boolean> = _isNfcEnabled

    // LiveData for auto-login result
    private val _autoLoginSuccess = MutableLiveData<Boolean>(false)
    val autoLoginSuccess: LiveData<Boolean> = _autoLoginSuccess

    /**
     * Initialize the NfcManager with an Activity
     */
    fun initializeNfcManager(activity: androidx.activity.ComponentActivity) {
        if (nfcManager == null) {
            nfcManager = NfcManager(activity)
            refreshNfcState()
        }
    }

    /**
     * Process NFC intent to extract RFID tag ID
     */
    fun processNfcIntent(intent: Intent) {
        val manager = nfcManager ?: return

        val tagId = manager.processIntent(intent)
        if (tagId != null) {
            // Update current RFID tag
            _currentRfidTag.value = tagId

            // Check if this RFID tag is associated with a user for auto-login
            val canAutoLogin = sessionManager.checkRfidTagForLogin(tagId)
            _autoLoginSuccess.value = canAutoLogin
        }
    }

    /**
     * Enable NFC foreground dispatch
     */
    fun enableNfcForegroundDispatch() {
        nfcManager?.enableForegroundDispatch()
        refreshNfcState()
    }

    /**
     * Disable NFC foreground dispatch
     */
    fun disableNfcForegroundDispatch() {
        nfcManager?.disableForegroundDispatch()
    }

    /**
     * Refresh NFC state (availability and enabled)
     */
    private fun refreshNfcState() {
        _isNfcAvailable.value = nfcManager?.isNfcAvailable() ?: false
        _isNfcEnabled.value = nfcManager?.isNfcEnabled() ?: false
    }

    /**
     * Clear the current RFID tag
     */
    fun clearRfidTag() {
        _currentRfidTag.value = null
        _autoLoginSuccess.value = false
    }
}