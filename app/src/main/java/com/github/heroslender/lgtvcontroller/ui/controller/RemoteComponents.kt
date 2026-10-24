package com.github.heroslender.lgtvcontroller.ui.controller

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.heroslender.lgtvcontroller.device.DeviceControllerButton
import com.github.heroslender.lgtvcontroller.device.DeviceStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Paleta fixa do controle (sempre escuro). */
object RemoteColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF1C1C1E)
    val Button = Color(0xFF2C2C2E)
    val ButtonPressed = Color(0xFF3A3A3C)
    val Content = Color(0xFFFFFFFF)
    val ContentSecondary = Color(0xFF8E8E93)
    val ContentDisabled = Color(0xFF48484A)
    val Connected = Color(0xFF30D158)
    val Connecting = Color(0xFFFFD60A)
}

/**
 * Botão circular com feedback de pressão (escala + cor) e vibração,
 * sem o ripple do Material, para um visual mais limpo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    enabled: Boolean = true,
    contentDescription: String? = null,
    containerColor: Color = RemoteColors.Button,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 900f),
        label = "buttonScale",
    )
    val bg by animateColorAsState(
        targetValue = if (pressed) RemoteColors.ButtonPressed else containerColor,
        label = "buttonColor",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(scale)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(bg)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClickLabel = contentDescription,
                onLongClick = onLongClick?.let {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        it()
                    }
                },
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            ),
        content = content,
    )
}

/** Ícone de ação sem fundo (usado na faixa inferior do touchpad). */
@Composable
fun RemoteGhostButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = RemoteColors.Content,
) {
    RemoteCircleButton(
        onClick = onClick,
        size = 44.dp,
        enabled = enabled,
        contentDescription = contentDescription,
        containerColor = Color.Transparent,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/** "Pílula" central do cabeçalho com o nome da TV. */
@Composable
fun DevicePill(
    name: String,
    status: DeviceStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dotColor = when (status) {
        DeviceStatus.CONNECTED -> RemoteColors.Connected
        DeviceStatus.CONNECTING, DeviceStatus.PAIRING -> RemoteColors.Connecting
        DeviceStatus.DISCONNECTED -> RemoteColors.ContentSecondary
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val bg by animateColorAsState(
        if (pressed) RemoteColors.ButtonPressed else RemoteColors.Surface,
        label = "pillColor",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(bg)
            .combinedClickableNoIndication(interaction, onClick)
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.Tv,
            contentDescription = null,
            tint = RemoteColors.Content,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = name,
            color = RemoteColors.Content,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = RemoteColors.ContentSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableNoIndication(
    interaction: MutableInteractionSource,
    onClick: () -> Unit,
) = this.combinedClickable(
    interactionSource = interaction,
    indication = null,
    onClick = onClick,
)

/**
 * Cápsula com dois botões (ex.: − VOL +). Segurar o botão repete o comando,
 * como num controle físico.
 */
@Composable
fun RemoteCapsule(
    label: String,
    decreaseIcon: ImageVector,
    decreaseDescription: String,
    onDecrease: () -> Unit,
    increaseIcon: ImageVector,
    increaseDescription: String,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(RemoteColors.Button),
    ) {
        RepeatingHalf(decreaseIcon, decreaseDescription, onDecrease, enabled, Modifier.weight(1f))
        Text(
            text = label,
            color = RemoteColors.ContentSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
        )
        RepeatingHalf(increaseIcon, increaseDescription, onIncrease, enabled, Modifier.weight(1f))
    }
}

@Composable
private fun RepeatingHalf(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val currentOnClick by rememberUpdatedState(onClick)
    val scope = rememberCoroutineScope()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val bg by animateColorAsState(
        if (pressed) RemoteColors.ButtonPressed else Color.Transparent,
        label = "halfColor",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .background(bg)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    interaction.emit(press)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    currentOnClick()
                    val repeatJob = scope.launch {
                        delay(450)
                        while (isActive) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentOnClick()
                            delay(110)
                        }
                    }
                    val released = tryAwaitRelease()
                    repeatJob.cancel()
                    interaction.emit(
                        if (released) PressInteraction.Release(press)
                        else PressInteraction.Cancel(press)
                    )
                })
            },
    ) {
        Icon(icon, contentDescription, tint = RemoteColors.Content, modifier = Modifier.size(22.dp))
    }
}

/**
 * Gestos do touchpad.
 *
 * - Modo deslizar: cada ~56dp arrastados viram um passo direcional (arrastar
 *   longo = vários passos); um "flick" curto também conta como um passo.
 *   Toque = OK.
 * - Modo ponteiro: arrastar move o cursor do Magic Remote; toque = clique.
 */
fun Modifier.touchpadGestures(
    enabled: Boolean,
    pointerMode: Boolean,
    onTouch: (Offset?) -> Unit,
    onTap: () -> Unit,
    onSwipe: (DeviceControllerButton) -> Unit,
    onPointerMove: (Offset) -> Unit,
): Modifier = this.pointerInput(enabled, pointerMode) {
    if (!enabled) return@pointerInput

    val stepPx = 56.dp.toPx()
    val flickPx = 24.dp.toPx()
    val slop = viewConfiguration.touchSlop

    fun direction(v: Offset): DeviceControllerButton =
        if (abs(v.x) > abs(v.y)) {
            if (v.x > 0) DeviceControllerButton.RIGHT else DeviceControllerButton.LEFT
        } else {
            if (v.y > 0) DeviceControllerButton.DOWN else DeviceControllerButton.UP
        }

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        onTouch(down.position)

        var total = Offset.Zero
        var acc = Offset.Zero
        var dragging = false
        var emittedStep = false

        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break

            val delta = change.positionChange()
            total += delta
            onTouch(change.position)

            if (!dragging && total.getDistance() > slop) dragging = true
            if (dragging) {
                if (pointerMode) {
                    onPointerMove(delta)
                } else {
                    acc += delta
                    if (abs(acc.x) >= stepPx || abs(acc.y) >= stepPx) {
                        onSwipe(direction(acc))
                        emittedStep = true
                        acc = Offset.Zero
                    }
                }
                change.consume()
            }
        }

        onTouch(null)

        when {
            !dragging -> onTap()
            !pointerMode && !emittedStep && total.getDistance() >= flickPx ->
                onSwipe(direction(total))
        }
    }
}
