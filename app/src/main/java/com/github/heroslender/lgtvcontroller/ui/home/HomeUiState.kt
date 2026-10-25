package com.github.heroslender.lgtvcontroller.ui.home

import com.github.heroslender.lgtvcontroller.device.DeviceControllerButton
import com.github.heroslender.lgtvcontroller.device.DeviceStatus
import com.github.heroslender.lgtvcontroller.domain.model.App
import com.github.heroslender.lgtvcontroller.domain.model.Input

data class HomeUiState(
    val deviceID: String? = null,
    val deviceName: String? = null,
    val deviceStatus: DeviceStatus = DeviceStatus.DISCONNECTED,
    /** Apps visíveis na tela inicial, já na ordem escolhida. */
    val apps: List<App> = emptyList(),
    /** Todos os apps na ordem escolhida, inclusive os escondidos (para o organizador). */
    val allApps: List<App> = emptyList(),
    val hiddenApps: Set<String> = emptySet(),
    val inputs: List<Input> = emptyList(),
    val runningApp: String = "",
    val hasCapability: (DeviceControllerButton) -> Boolean = { false },
    val executeButton: (DeviceControllerButton) -> Unit = {},
    val launchApp: (String) -> Unit = {},
    /** Botão Home: abre o app "TV Sala" na TV (cai no Home normal se não achar). */
    val openHomeApp: () -> Unit = {},
    /** Salva a nova ordem (IDs de todos os apps) e os IDs escondidos. */
    val saveAppLayout: (order: List<String>, hidden: Set<String>) -> Unit = { _, _ -> },
    val resetAppLayout: () -> Unit = {},
)