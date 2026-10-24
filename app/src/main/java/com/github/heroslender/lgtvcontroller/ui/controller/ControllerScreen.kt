package com.github.heroslender.lgtvcontroller.ui.controller

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Mouse
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.heroslender.lgtvcontroller.R
import com.github.heroslender.lgtvcontroller.R.string
import com.github.heroslender.lgtvcontroller.device.DeviceControllerButton
import com.github.heroslender.lgtvcontroller.device.DeviceStatus
import com.github.heroslender.lgtvcontroller.device.impl.PreviewDevice
import com.github.heroslender.lgtvcontroller.ui.ConnectedDeviceScaffold
import com.github.heroslender.lgtvcontroller.ui.TvTextInputState
import com.github.heroslender.lgtvcontroller.ui.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 800)
@Composable
fun ControlPreview() {
    ControllerScreenPreview(isConnected = true)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 800)
@Composable
fun ControlPreviewDisconnected() {
    ControllerScreenPreview(isConnected = false)
}

@Composable
fun ControllerScreenPreview(
    isConnected: Boolean,
) {
    val device = if (isConnected) PreviewDevice else null
    val controllerUiState = ControllerUiState(
        deviceName = device?.friendlyName,
        deviceStatus = device?.let { runBlocking { it.state.first().status } }
            ?: DeviceStatus.DISCONNECTED,
        hasCapability = { isConnected },
        executeButton = {},
    )

    ControllerScreen(
        controllerUiState = controllerUiState,
        tvTextInputState = TvTextInputState(),
        errorFlow = emptyFlow(),
        navigateUp = {}
    )
}

@Composable
fun ControllerScreen(
    controllerViewModel: ControllerViewModel = hiltViewModel(),
    navigateUp: () -> Unit,
) {
    val controllerUiState by controllerViewModel.uiState.collectAsState()
    val tvTextInputState by controllerViewModel.tvTextInputState.collectAsState()

    ControllerScreen(
        controllerUiState,
        tvTextInputState,
        controllerViewModel.errors,
        navigateUp,
    )
}

@Composable
fun ControllerScreen(
    controllerUiState: ControllerUiState,
    tvTextInputState: TvTextInputState,
    errorFlow: Flow<Snackbar>,
    navigateUp: () -> Unit,
) {
    val has = controllerUiState.hasCapability
    val exec = controllerUiState.executeButton

    // Captura os botões físicos de volume do celular e manda para a TV.
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    ConnectedDeviceScaffold(
        errorFlow = errorFlow,
        textInputState = tvTextInputState,
        containerColor = RemoteColors.Background,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(RemoteColors.Background)
                .onPreviewKeyEvent { event ->
                    val button = when (event.key) {
                        Key.VolumeUp -> DeviceControllerButton.VOLUME_UP
                        Key.VolumeDown -> DeviceControllerButton.VOLUME_DOWN
                        else -> return@onPreviewKeyEvent false
                    }
                    if (event.type == KeyEventType.KeyDown && has(button)) exec(button)
                    true
                }
                .focusRequester(focusRequester)
                .focusable()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            RemoteHeader(
                deviceName = controllerUiState.deviceName
                    ?: stringResource(string.controller_device),
                deviceStatus = controllerUiState.deviceStatus,
                canMute = has(DeviceControllerButton.MUTE),
                canPower = has(DeviceControllerButton.POWER),
                onMute = { exec(DeviceControllerButton.MUTE) },
                onPower = { exec(DeviceControllerButton.POWER) },
                onDeviceClick = navigateUp,
            )

            Touchpad(
                controllerUiState = controllerUiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            SecondaryControls(has, exec)

            PrimaryButtons(has, exec, controllerUiState.openHomeApp)
        }
    }
}

@Composable
private fun RemoteHeader(
    deviceName: String,
    deviceStatus: DeviceStatus,
    canMute: Boolean,
    canPower: Boolean,
    onMute: () -> Unit,
    onPower: () -> Unit,
    onDeviceClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        RemoteCircleButton(
            onClick = onMute,
            size = 44.dp,
            enabled = canMute,
            containerColor = RemoteColors.Surface,
            contentDescription = stringResource(string.mute_button),
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.VolumeOff,
                stringResource(string.mute_button),
                tint = RemoteColors.Content,
                modifier = Modifier.size(20.dp),
            )
        }

        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            DevicePill(
                name = deviceName,
                status = deviceStatus,
                onClick = onDeviceClick,
            )
        }

        RemoteCircleButton(
            onClick = onPower,
            size = 44.dp,
            enabled = canPower,
            containerColor = RemoteColors.Surface,
            contentDescription = stringResource(string.power_button),
        ) {
            Icon(
                Icons.Rounded.PowerSettingsNew,
                stringResource(string.power_button),
                tint = RemoteColors.Content,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun Touchpad(
    controllerUiState: ControllerUiState,
    modifier: Modifier = Modifier,
) {
    val has = controllerUiState.hasCapability
    val exec = controllerUiState.executeButton
    val haptic = LocalHapticFeedback.current

    var pointerMode by rememberSaveable { mutableStateOf(false) }
    var touchPos by remember { mutableStateOf(Offset.Unspecified) }
    var touching by remember { mutableStateOf(false) }
    val glowAlpha by animateFloatAsState(
        targetValue = if (touching) 1f else 0f,
        animationSpec = tween(if (touching) 80 else 350),
        label = "glow",
    )
    val hintAlpha by animateFloatAsState(
        targetValue = if (touching) 0f else 1f,
        label = "hint",
    )

    // Estado sempre atualizado para os callbacks usados dentro do pointerInput.
    val current by rememberUpdatedState(controllerUiState)
    val onTap = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (pointerMode) current.clickMouse()
        else current.executeButton(DeviceControllerButton.OK)
    }
    val onSwipe = { btn: DeviceControllerButton ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        current.executeButton(btn)
    }
    val onMove = { d: Offset ->
        current.moveMouse(d.x.toDouble(), d.y.toDouble())
    }

    val enabled = has(DeviceControllerButton.OK)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(RemoteColors.Surface),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .drawBehind {
                    if (glowAlpha > 0f && touchPos != Offset.Unspecified) {
                        val r = 90.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.16f * glowAlpha),
                                    Color.Transparent,
                                ),
                                center = touchPos,
                                radius = r,
                            ),
                            radius = r,
                            center = touchPos,
                        )
                    }
                }
                .touchpadGestures(
                    enabled = enabled,
                    pointerMode = pointerMode,
                    onTouch = { pos ->
                        touching = pos != null
                        if (pos != null) touchPos = pos
                    },
                    onTap = onTap,
                    onSwipe = onSwipe,
                    onPointerMove = onMove,
                ),
        ) {
            Text(
                text = stringResource(
                    if (pointerMode) string.touchpad_hint_pointer
                    else string.touchpad_hint_swipe
                ),
                color = RemoteColors.ContentDisabled,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .alpha(hintAlpha * if (enabled) 1f else 0.5f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 4.dp),
        ) {
            RemoteGhostButton(
                icon = Icons.Rounded.Replay10,
                contentDescription = stringResource(string.rewind_button),
                enabled = has(DeviceControllerButton.REWIND),
                onClick = { exec(DeviceControllerButton.REWIND) },
            )
            RemoteGhostButton(
                icon = Icons.Outlined.Info,
                contentDescription = stringResource(string.info_button),
                enabled = has(DeviceControllerButton.INFO),
                onClick = { exec(DeviceControllerButton.INFO) },
            )
            RemoteGhostButton(
                icon = if (pointerMode) Icons.Rounded.OpenWith else Icons.Rounded.Mouse,
                contentDescription = stringResource(
                    if (pointerMode) string.swipe_mode_button else string.pointer_mode_button
                ),
                tint = if (pointerMode) RemoteColors.Connected else RemoteColors.Content,
                onClick = { pointerMode = !pointerMode },
            )
            RemoteGhostButton(
                icon = Icons.Rounded.Forward10,
                contentDescription = stringResource(string.fast_forward_button),
                enabled = has(DeviceControllerButton.FAST_FORWARD),
                onClick = { exec(DeviceControllerButton.FAST_FORWARD) },
            )
        }
    }
}

/** Volume, entrada, configurações e canal — botões extras que a TV LG precisa. */
@Composable
private fun SecondaryControls(
    has: (DeviceControllerButton) -> Boolean,
    exec: (DeviceControllerButton) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        RemoteCapsule(
            label = stringResource(string.volume_controls),
            decreaseIcon = Icons.Rounded.Remove,
            decreaseDescription = stringResource(string.volume_down_button),
            onDecrease = { exec(DeviceControllerButton.VOLUME_DOWN) },
            increaseIcon = Icons.Rounded.Add,
            increaseDescription = stringResource(string.volume_up_button),
            onIncrease = { exec(DeviceControllerButton.VOLUME_UP) },
            enabled = has(DeviceControllerButton.VOLUME_UP),
            modifier = Modifier.weight(1f),
        )

        RemoteCircleButton(
            onClick = { exec(DeviceControllerButton.SOURCE) },
            size = 48.dp,
            enabled = has(DeviceControllerButton.SOURCE),
            contentDescription = stringResource(string.source_button),
        ) {
            Icon(
                painterResource(R.drawable.baseline_input_24),
                stringResource(string.source_button),
                tint = RemoteColors.Content,
                modifier = Modifier.size(20.dp),
            )
        }

        RemoteCircleButton(
            onClick = { exec(DeviceControllerButton.QMENU) },
            size = 48.dp,
            enabled = has(DeviceControllerButton.QMENU),
            contentDescription = stringResource(string.settings_button),
        ) {
            Icon(
                Icons.Rounded.Settings,
                stringResource(string.settings_button),
                tint = RemoteColors.Content,
                modifier = Modifier.size(20.dp),
            )
        }

        RemoteCapsule(
            label = stringResource(string.channel_controls),
            decreaseIcon = Icons.Rounded.KeyboardArrowDown,
            decreaseDescription = stringResource(string.channel_down_button),
            onDecrease = { exec(DeviceControllerButton.CHANNEL_DOWN) },
            increaseIcon = Icons.Rounded.KeyboardArrowUp,
            increaseDescription = stringResource(string.channel_up_button),
            onIncrease = { exec(DeviceControllerButton.CHANNEL_UP) },
            enabled = has(DeviceControllerButton.CHANNEL_UP),
            modifier = Modifier.weight(1f),
        )
    }
}

/** Fileira inferior: ⏯  ‹ (grande)  📺. */
@Composable
private fun PrimaryButtons(
    has: (DeviceControllerButton) -> Boolean,
    exec: (DeviceControllerButton) -> Unit,
    openHomeApp: () -> Unit,
) {
    // A LG tem teclas separadas de PLAY e PAUSE, então alternamos localmente.
    var isPlaying by rememberSaveable { mutableStateOf(true) }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        RemoteCircleButton(
            onClick = {
                exec(if (isPlaying) DeviceControllerButton.PAUSE else DeviceControllerButton.PLAY)
                isPlaying = !isPlaying
            },
            size = 60.dp,
            enabled = has(DeviceControllerButton.PLAY),
            contentDescription = stringResource(string.play_pause_button),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.PlayArrow, null,
                    tint = RemoteColors.Content,
                    modifier = Modifier.size(20.dp),
                )
                Icon(
                    Icons.Rounded.Pause, null,
                    tint = RemoteColors.Content,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = (-3).dp),
                )
            }
        }

        // Toque = voltar · Segurar = sair do app (EXIT)
        RemoteCircleButton(
            onClick = { exec(DeviceControllerButton.BACK) },
            onLongClick = { exec(DeviceControllerButton.EXIT) },
            size = 80.dp,
            enabled = has(DeviceControllerButton.BACK),
            contentDescription = stringResource(string.back_button),
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                stringResource(string.back_button),
                tint = RemoteColors.Content,
                modifier = Modifier.size(40.dp),
            )
        }

        // Toque = abre o app "TV Sala" · Segurar = Home normal da TV
        RemoteCircleButton(
            onClick = openHomeApp,
            onLongClick = { exec(DeviceControllerButton.HOME) },
            size = 60.dp,
            enabled = has(DeviceControllerButton.HOME),
            contentDescription = stringResource(string.home_button),
        ) {
            Icon(
                Icons.Rounded.Tv,
                stringResource(string.home_button),
                tint = RemoteColors.Content,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
