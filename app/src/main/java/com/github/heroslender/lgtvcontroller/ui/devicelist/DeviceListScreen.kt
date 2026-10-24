@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.github.heroslender.lgtvcontroller.ui.devicelist

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.heroslender.lgtvcontroller.R
import com.github.heroslender.lgtvcontroller.R.string
import com.github.heroslender.lgtvcontroller.device.DeviceStatus
import com.github.heroslender.lgtvcontroller.ui.ConnectedDeviceScaffold
import com.github.heroslender.lgtvcontroller.ui.theme.AppColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.github.heroslender.lgtvcontroller.ui.theme.LGTVControllerTheme
import kotlin.math.min
import kotlin.math.pow

@Preview(
    group = "no devices",
    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewDeviceList() {
    LGTVControllerTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SharedTransitionLayout(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(true, label = "found devices transition") { _ ->
                    NoDevices(
                        animatedVisibilityScope = this@AnimatedContent,
                        sharedTransitionScope = this@SharedTransitionLayout
                    )
                }
            }
        }
    }
}

@Preview(
    group = "has devices",
    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
@SuppressLint("UnrememberedMutableState")
fun PreviewDeviceListFound() {
    val devices = listOf(
        DeviceItemData(
            displayName = "Your Awesome TV 1",
            status = mutableStateOf(DeviceStatus.DISCONNECTED),
            isPoweredOn = true,
            connect = {}
        ),
        DeviceItemData(
            displayName = "Your Awesome TV 2",
            status = mutableStateOf(DeviceStatus.CONNECTED),
            isPoweredOn = true,
            connect = {}
        ),
        DeviceItemData(
            displayName = "Your Awesome TV 3",
            status = mutableStateOf(DeviceStatus.DISCONNECTED),
            isPoweredOn = false,
            connect = {}
        )
    )

    LGTVControllerTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SharedTransitionLayout(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(true, label = "found devices transition") { _ ->
                    HasDevices(
                        devices,
                        navigateToController = {},
                        animatedVisibilityScope = this@AnimatedContent,
                        sharedTransitionScope = this@SharedTransitionLayout
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceListScreen(
    deviceListViewModel: DeviceListViewModel = hiltViewModel(),
    navigateToController: () -> Unit,
) {
    val devices by deviceListViewModel.uiState.collectAsState()
    val textInputState by deviceListViewModel.tvTextInputState.collectAsState()

    deviceListViewModel.onDeviceConnected {
        navigateToController()
    }

    ConnectedDeviceScaffold(
        errorFlow = deviceListViewModel.errors,
        textInputState = textInputState,
    ) {
        SharedTransitionLayout() {
            AnimatedContent(
                devices.devices.isNotEmpty(),
                label = "found devices transition"
            ) { hasDevices ->
                if (!hasDevices) {
                    NoDevices(
                        animatedVisibilityScope = this@AnimatedContent,
                        sharedTransitionScope = this@SharedTransitionLayout
                    )
                } else {
                    HasDevices(
                        devices.devices,
                        navigateToController,
                        animatedVisibilityScope = this@AnimatedContent,
                        sharedTransitionScope = this@SharedTransitionLayout
                    )
                }
            }
        }
    }
}

@Composable
fun NoDevices(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        with(sharedTransitionScope) {
            Sonar(
                Modifier
                    .padding(horizontal = 48.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "sonar"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(string.searching_devices),
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.Content,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "txt_h1"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(string.check_network),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.ContentSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "txt_h2"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )
        }
    }
}

@Composable
fun HasDevices(
    devices: List<DeviceItemData>,
    navigateToController: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Título grande
        Text(
            text = stringResource(string.device_list_title),
            style = MaterialTheme.typography.headlineLarge,
            color = AppColors.Content,
            modifier = Modifier.padding(top = 24.dp, bottom = 20.dp),
        )

        Row(
            Modifier
                .padding(bottom = 12.dp, start = 4.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            with(sharedTransitionScope) {
                Sonar(
                    Modifier
                        .size(20.dp)
                        .sharedElement(
                            rememberSharedContentState(key = "sonar"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                )
                Column(
                    Modifier
                        .weight(1F)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = stringResource(string.searching_devices).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.ContentSecondary,
                        modifier = Modifier
                            .sharedBounds(
                                rememberSharedContentState(key = "txt_h1"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    )
                    Text(
                        text = stringResource(string.check_network),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.ContentTertiary,
                        modifier = Modifier
                            .sharedBounds(
                                rememberSharedContentState(key = "txt_h2"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    )
                }
            }
        }

        // Lista agrupada em um único bloco arredondado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface)
        ) {
            val sorted = devices.sortedWith { device1, device2 ->
                return@sortedWith when {
                    !device1.isPoweredOn -> 1
                    !device2.isPoweredOn -> -1
                    device1.status.value == DeviceStatus.CONNECTED -> -1
                    device2.status.value == DeviceStatus.CONNECTED -> 1
                    else -> 0
                }
            }
            sorted.forEachIndexed { index, device ->
                DeviceItem(device, navigateToController)
                if (index < sorted.lastIndex) {
                    HorizontalDivider(
                        color = AppColors.Separator,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun onlineTVColor() = AppColors.Green

@Composable
fun offlineTVColor() = AppColors.ContentTertiary

@Composable
fun DeviceItem(
    deviceData: DeviceItemData,
    navigateToController: () -> Unit,
) {
    val status by deviceData.status
    val (statusText, statusColor) = when {
        !deviceData.isPoweredOn ->
            stringResource(string.device_list_offline) to AppColors.ContentTertiary
        status == DeviceStatus.CONNECTED ->
            stringResource(string.device_list_connected) to AppColors.Green
        status == DeviceStatus.CONNECTING || status == DeviceStatus.PAIRING ->
            stringResource(status.nameResId) to AppColors.Yellow
        else ->
            stringResource(string.device_list_disconnected) to AppColors.ContentSecondary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (status == DeviceStatus.DISCONNECTED) {
                    deviceData.connect()
                }
                navigateToController()
            }
            .alpha(if (deviceData.isPoweredOn) 1f else 0.55f)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.SurfaceHigh),
        ) {
            Icon(
                painterResource(
                    if (status != DeviceStatus.DISCONNECTED) R.drawable.baseline_connected_tv_24
                    else R.drawable.baseline_tv_24
                ),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = AppColors.Content,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = deviceData.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.Content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.ContentSecondary,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = AppColors.ContentTertiary,
        )
    }
}

data class DeviceItemData(
    val displayName: String,
    val status: State<DeviceStatus>,
    val isPoweredOn: Boolean,
    val connect: () -> Unit,
)

@Composable
fun Sonar(
    modifier: Modifier = Modifier,
) {
    val color = AppColors.Accent
    val colorStart = color.copy(alpha = .05F)
    val colorEnd = color.copy(alpha = .45F)
    val brush = remember(colorStart, colorEnd) {
        Brush.radialGradient(
            colors = listOf(colorStart, colorEnd)
        )
    }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
            .aspectRatio(1F),
        contentAlignment = Alignment.Center
    ) {
        val transition = rememberInfiniteTransition()
        val animation = transition.animateFloat(
            initialValue = 0F,
            targetValue = 1F,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing)
            ),
            label = "Sonar Animation"
        )

        Ripple(animation.value, brush)
        Ripple((animation.value + .33F) % 1, brush)
        Ripple((animation.value + .66F) % 1, brush)
    }
}

@Composable
fun Ripple(animationValue: Float, brush: Brush) {
    Box(
        Modifier
            .scale(animationValue)
            .clip(shape = CircleShape)
            .fillMaxSize()
            .alpha(min(1F, (4F * (1F - animationValue)).pow(2)))
            .background(
                brush = brush
            )
    )
}