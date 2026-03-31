package com.matrix.multigpt.presentation.ui.autoreply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.multigpt.data.datastore.AutoReplyDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AutoReplyViewModel @Inject constructor(
    private val autoReplyDataSource: AutoReplyDataSource
) : ViewModel() {

    val masterEnabled: StateFlow<Boolean> = autoReplyDataSource.masterEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val whatsappEnabled: StateFlow<Boolean> = autoReplyDataSource.whatsappEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val telegramEnabled: StateFlow<Boolean> = autoReplyDataSource.telegramEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val instagramEnabled: StateFlow<Boolean> = autoReplyDataSource.instagramEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val replyDelay: StateFlow<Int> = autoReplyDataSource.replyDelaySeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    fun setMaster(enabled: Boolean) {
        viewModelScope.launch {
            autoReplyDataSource.setMasterEnabled(enabled)
        }
    }

    fun setWhatsapp(enabled: Boolean) {
        viewModelScope.launch {
            autoReplyDataSource.setWhatsapp(enabled)
        }
    }

    fun setTelegram(enabled: Boolean) {
        viewModelScope.launch {
            autoReplyDataSource.setTelegram(enabled)
        }
    }

    fun setInstagram(enabled: Boolean) {
        viewModelScope.launch {
            autoReplyDataSource.setInstagram(enabled)
        }
    }

    fun setReplyDelay(seconds: Int) {
        viewModelScope.launch {
            autoReplyDataSource.setReplyDelay(seconds)
        }
    }
}
