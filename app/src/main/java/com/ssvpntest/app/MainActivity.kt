package com.ssvpntest.app

import android.os.Bundle
import android.os.RemoteException
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.preference.PreferenceDataStore
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import com.ssvpntest.app.ui.MainScreen
import com.ssvpntest.app.ui.MainScreenState
import com.ssvpntest.app.ui.theme.SsvpntestTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class MainActivity : ComponentActivity(), ShadowsocksConnection.Callback, OnPreferenceDataStoreChangeListener {

    companion object {
        var stateListener: ((BaseService.State) -> Unit)? = null
    }

    private val connection = ShadowsocksConnection(true)
    private var connectionState = BaseService.State.Idle
    private val isEnabled get() = connectionState.let { it.canStop || it == BaseService.State.Stopped }
    private val connect = registerForActivityResult(StartService()) {
        if (it) toast(R.string.vpn_permission_denied)
    }

    private val screenState = MutableStateFlow(
        MainScreenState(
            profiles = ProfileManager.getActiveProfiles()?.toMutableList() ?: mutableListOf()
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SsvpntestTheme {
                val state by screenState.collectAsState()
                MainScreen(
                    onConnectionButtonClick = ::toggle,
                    onKeyValueChange = ::onKeyValueChange,
                    onKeyDoneClick = ::acceptKey,
                    onProfileClick = ::onProfileClick,
                    state = state,
                )
            }
        }

        connection.connect(this, this)
        DataStore.publicStore.registerChangeListener(this)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) = changeState(state, msg)

    private fun changeState(state: BaseService.State, msg: String? = null, animate: Boolean = true) {
        if (msg != null) toast(msg)
        connectionState = state
        stateListener?.invoke(state)
    }

    override fun onServiceConnected(service: IShadowsocksService) = changeState(try {
        BaseService.State.values()[service.state]
    } catch (_: RemoteException) {
        BaseService.State.Idle
    })

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                connection.disconnect(this)
                connection.connect(this, this)
            }
        }
    }

    private fun toggle() = if (connectionState.canStop) Core.stopService() else connect.launch(null)

    private fun onKeyValueChange(value: String) {
        screenState.update { it.copy(keyField = value) }
    }

    private fun acceptKey() {
        try {
            val profiles = Profile.findAllUrls(
                screenState.value.keyField.trim(),
                Core.currentProfile?.main
            ).toList()
            if (profiles.isNotEmpty()) {
                profiles.forEach {
                    val new = ProfileManager.createProfile(it)
                    screenState.update { it.copy(profiles = it.profiles + new, keyField = "") }
                }
                toast(R.string.action_import_msg)
                return
            }
        } catch (exc: Exception) {
            Timber.d(exc)
        }
        toast(R.string.action_import_err)
    }

    private fun onProfileClick(id: Long) {
        if (isEnabled) {
            Core.switchProfile(id)
            if (connectionState.canStop) Core.reloadService()
        }
    }

    private fun toast(textResId: Int) {
        Toast.makeText(this, resources.getString(textResId), Toast.LENGTH_SHORT).show()
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}