package com.github.heroslender.lgtvcontroller.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.model.GlideUrl
import com.github.heroslender.lgtvcontroller.ControllerTopAppBar
import com.github.heroslender.lgtvcontroller.R.string
import com.github.heroslender.lgtvcontroller.TopAppBarAction
import com.github.heroslender.lgtvcontroller.device.DeviceControllerButton
import com.github.heroslender.lgtvcontroller.device.DeviceStatus
import com.github.heroslender.lgtvcontroller.device.impl.PreviewDevice
import com.github.heroslender.lgtvcontroller.domain.model.App
import com.github.heroslender.lgtvcontroller.domain.model.Input
import com.github.heroslender.lgtvcontroller.ui.ConnectedDeviceScaffold
import com.github.heroslender.lgtvcontroller.ui.TvTextInputState
import com.github.heroslender.lgtvcontroller.ui.controller.RemoteCapsule
import com.github.heroslender.lgtvcontroller.ui.controller.RemoteCircleButton
import com.github.heroslender.lgtvcontroller.ui.icons.MyIconPack
import com.github.heroslender.lgtvcontroller.ui.icons.myiconpack.TvRemote
import com.github.heroslender.lgtvcontroller.ui.snackbar.Snackbar
import com.github.heroslender.lgtvcontroller.ui.theme.AppColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 800)
@Composable
fun HomePreview(
    isConnected: Boolean = true,
) {
    val device = if (isConnected) PreviewDevice else null
    val uiState = HomeUiState(
        device?.id,
        deviceName = device?.friendlyName,
        deviceStatus = device?.let { runBlocking { it.state.first().status } }
            ?: DeviceStatus.DISCONNECTED,
        runningApp = "netflix",
        apps = listOf(
            App(id = "youtube", name = "YouTube", icon = "a"),
            App(id = "netflix", name = "Netflix", icon = "a"),
        ),
        inputs = listOf(
            Input(id = "hdmi1", name = "HDMI1", icon = "a"),
            Input(id = "hdmi2", name = "HDMI2", icon = "a", connected = true),
            Input(id = "scart", name = "SCART", icon = "a"),
        ),
        hasCapability = { true },
    )

    HomeScreen(
        uiState = uiState,
        textInputState = TvTextInputState(isKeyboardOpen = true),
        errorFlow = flow {},
        navigateToDeviceList = {},
        navigateToController = {},
        navigateToEditDevice = {},
    )
}

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    navigateToDeviceList: () -> Unit,
    navigateToController: () -> Unit,
    navigateToEditDevice: (String) -> Unit,
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val textInputState by homeViewModel.tvTextInputState.collectAsState()

    HomeScreen(
        uiState = uiState,
        textInputState = textInputState,
        errorFlow = homeViewModel.errors,
        navigateToDeviceList = navigateToDeviceList,
        navigateToController = navigateToController,
        navigateToEditDevice = navigateToEditDevice,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    textInputState: TvTextInputState,
    errorFlow: Flow<Snackbar>,
    navigateToDeviceList: () -> Unit,
    navigateToController: () -> Unit,
    navigateToEditDevice: (String) -> Unit,
) {
    ConnectedDeviceScaffold(
        errorFlow = errorFlow,
        textInputState = textInputState,
        containerColor = AppColors.Background,
        topBar = {
            ControllerTopAppBar(
                title = uiState.deviceName ?: "",
                subtitle = buildAnnotatedString {
                    val dot = when (uiState.deviceStatus) {
                        DeviceStatus.CONNECTED -> AppColors.Green
                        DeviceStatus.CONNECTING, DeviceStatus.PAIRING -> AppColors.Yellow
                        DeviceStatus.DISCONNECTED -> AppColors.ContentTertiary
                    }
                    withStyle(SpanStyle(color = dot)) { append("● ") }
                    append(stringResource(uiState.deviceStatus.nameResId))
                },
                navigateUp = navigateToDeviceList,
                actions = {
                    TopAppBarAction(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(string.edit_button),
                        enabled = uiState.deviceID != null,
                    ) {
                        uiState.deviceID?.also { navigateToEditDevice(it) }
                    }
                },
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 32.dp)
        ) {
            ControllerShortcutsCard(
                isConnected = uiState.deviceID != null,
                hasCapability = uiState.hasCapability,
                executeButton = uiState.executeButton,
                openHomeApp = uiState.openHomeApp,
                navigateToController = navigateToController,
            )

            AppsCard(
                apps = uiState.apps,
                runningApp = uiState.runningApp,
                openApp = { app -> uiState.launchApp(app.id) }
            )

            InputsCard(
                inputs = uiState.inputs,
                runningApp = uiState.runningApp,
                switchInput = { input -> uiState.launchApp(input.id) }
            )
        }
    }
}

/** Cabeçalho de seção: texto pequeno, cinza e em maiúsculas. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = AppColors.ContentSecondary,
        modifier = modifier.padding(start = 36.dp, end = 36.dp, bottom = 8.dp),
    )
}

@Composable
fun ControllerShortcutsCard(
    isConnected: Boolean,
    hasCapability: (DeviceControllerButton) -> Boolean,
    executeButton: (DeviceControllerButton) -> Unit,
    openHomeApp: () -> Unit,
    navigateToController: () -> Unit,
) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        // Cartão grande que abre o controle completo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(AppColors.Surface)
                .clickable(enabled = isConnected, onClick = navigateToController)
                .padding(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.SurfaceHigh),
            ) {
                Icon(
                    imageVector = MyIconPack.TvRemote,
                    contentDescription = null,
                    tint = AppColors.Content,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = stringResource(string.controller_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Content,
                )
                Text(
                    text = stringResource(string.open_remote),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.ContentSecondary,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.ContentTertiary,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Atalhos rápidos
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ShortcutButton(
                icon = Icons.Rounded.PowerSettingsNew,
                description = stringResource(string.power_button),
                enabled = hasCapability(DeviceControllerButton.POWER),
            ) { executeButton(DeviceControllerButton.POWER) }

            ShortcutButton(
                icon = Icons.AutoMirrored.Rounded.VolumeOff,
                description = stringResource(string.mute_button),
                enabled = hasCapability(DeviceControllerButton.MUTE),
            ) { executeButton(DeviceControllerButton.MUTE) }

            ShortcutButton(
                icon = Icons.Rounded.Home,
                description = stringResource(string.home_button),
                enabled = hasCapability(DeviceControllerButton.HOME),
                // Toque = abre o app "TV Sala" · Segurar = Home normal da TV
                onLongClick = { executeButton(DeviceControllerButton.HOME) },
                onClick = openHomeApp,
            )

            RemoteCapsule(
                label = stringResource(string.volume_controls),
                decreaseIcon = Icons.Rounded.Remove,
                decreaseDescription = stringResource(string.volume_down_button),
                onDecrease = { executeButton(DeviceControllerButton.VOLUME_DOWN) },
                increaseIcon = Icons.Rounded.Add,
                increaseDescription = stringResource(string.volume_up_button),
                onIncrease = { executeButton(DeviceControllerButton.VOLUME_UP) },
                enabled = hasCapability(DeviceControllerButton.VOLUME_UP),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ShortcutButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    RemoteCircleButton(
        onClick = onClick,
        onLongClick = onLongClick,
        size = 48.dp,
        enabled = enabled,
        containerColor = AppColors.Surface,
        contentDescription = description,
    ) {
        Icon(icon, description, tint = AppColors.Content, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AppsCard(
    apps: List<App>,
    runningApp: String,
    openApp: (App) -> Unit,
) {
    if (apps.isEmpty()) return
    Column {
        SectionHeader(stringResource(string.apps_card_title))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            items(apps) { app ->
                Tile(
                    label = app.name,
                    selected = app.id == runningApp,
                    onClick = { openApp(app) },
                ) {
                    GlideImage(
                        model = CustomGlideUrl(app.icon, app.name),
                        contentDescription = app.name,
                        modifier = Modifier.fillTile(),
                        loading = placeholder { TilePlaceholder() },
                        failure = placeholder { TilePlaceholder() },
                    )
                }
            }
        }
    }
}

class CustomGlideUrl(
    url: String,
    val cacheName: String,
) : GlideUrl(url.ifEmpty { "Not Found" }) {
    override fun getCacheKey(): String {
        return cacheName
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun InputsCard(
    inputs: List<Input>,
    runningApp: String,
    switchInput: (Input) -> Unit,
) {
    if (inputs.isEmpty()) return
    Column {
        SectionHeader(stringResource(string.inputs_card_title))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            items(inputs) { input ->
                Tile(
                    label = input.name,
                    selected = input.id == runningApp,
                    showDot = input.connected,
                    onClick = { switchInput(input) },
                ) {
                    GlideImage(
                        model = CustomGlideUrl(input.icon, input.name),
                        contentDescription = input.name,
                        colorFilter = ColorFilter.tint(AppColors.Content),
                        modifier = Modifier
                            .fillTile()
                            .padding(18.dp),
                        loading = placeholder { TilePlaceholder() },
                        failure = placeholder { TilePlaceholder() },
                    )
                }
            }
        }
    }
}

private fun Modifier.fillTile(): Modifier = this.size(TileSize)

private val TileSize = 76.dp

@Composable
private fun TilePlaceholder() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(TileSize)) {
        CircularProgressIndicator(
            color = AppColors.ContentSecondary,
            strokeWidth = 2.dp,
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * Bloco de app/entrada: ícone num quadrado arredondado,
 * nome embaixo e um contorno branco no que está aberto na TV.
 */
@Composable
private fun Tile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    showDot: Boolean = false,
    icon: @Composable () -> Unit,
) {
    val borderColor by animateColorAsState(
        if (selected) AppColors.Content else AppColors.Surface,
        label = "tileBorder",
    )
    val shape = remember { RoundedCornerShape(20.dp) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(TileSize + 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(TileSize)
                .clip(shape)
                .background(AppColors.Surface)
                .border(2.dp, borderColor, shape),
        ) {
            icon()
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                if (showDot) withStyle(SpanStyle(color = AppColors.Green)) { append("● ") }
                append(label)
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) AppColors.Content else AppColors.ContentSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
