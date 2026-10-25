package com.github.heroslender.lgtvcontroller.ui.home

import androidx.lifecycle.viewModelScope
import com.github.heroslender.lgtvcontroller.DeviceManager
import com.github.heroslender.lgtvcontroller.device.DeviceControllerButton
import com.github.heroslender.lgtvcontroller.storage.AppLayout
import com.github.heroslender.lgtvcontroller.storage.AppLayoutStore
import com.github.heroslender.lgtvcontroller.ui.controller.HOME_APP_NAME
import com.github.heroslender.lgtvcontroller.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    deviceManager: DeviceManager,
    private val appLayoutStore: AppLayoutStore,
) : BaseViewModel(deviceManager) {
    val uiState: StateFlow<HomeUiState> =
        deviceManager.connectedDevice.flatMapLatest { device ->
            if (device == null) {
                return@flatMapLatest flowOf(HomeUiState())
            }

            combine(device.state, appLayoutStore.layout(device.id)) { deviceState, layout ->
                val sortedApps = layout.sort(deviceState.apps)
                HomeUiState(
                    deviceID = device.id,
                    deviceName = if (deviceState.displayName.isNullOrEmpty()) device.friendlyName else deviceState.displayName,
                    deviceStatus = deviceState.status,
                    runningApp = deviceState.runningApp,
                    apps = sortedApps.filter { it.id !in layout.hidden },
                    allApps = sortedApps,
                    hiddenApps = layout.hidden,
                    inputs = deviceState.inputs,
                    hasCapability = device::hasCapability,
                    executeButton = device::executeControllerButton,
                    launchApp = device::launchApp,
                    saveAppLayout = { order, hidden ->
                        appLayoutStore.save(device.id, AppLayout(order, hidden))
                    },
                    resetAppLayout = { appLayoutStore.reset(device.id) },
                    openHomeApp = {
                        device.launchAppByName(HOME_APP_NAME) {
                            device.executeControllerButton(DeviceControllerButton.HOME)
                        }
                    },
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())
}