package com.github.heroslender.lgtvcontroller.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.heroslender.lgtvcontroller.ControllerTopAppBar
import com.github.heroslender.lgtvcontroller.R.string
import com.github.heroslender.lgtvcontroller.ui.theme.LGTVControllerTheme
import com.github.heroslender.lgtvcontroller.ui.theme.AppColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    LGTVControllerTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ControllerTopAppBar(
                    title = stringResource(string.edit_tv_title),
                    navigateUp = {},
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(
                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                        top = innerPadding.calculateTopPadding()
                    )
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(15.dp, 10.dp)
            ) {
                PreferenceCategory("General")

                TextPrefence(
                    title = "ID",
                    value = "c93dbf94-d50d-480b-90d3-009c1a2b21c1",
                )

                TextPrefence(
                    title = "Name",
                    value = "LG TV WebOs - Some Version",
                )

                TextPrefence(
                    title = "Display name",
                    value = "Living Room TV",
                    onValueChange = { newValue ->
                        println("New value: $newValue")
                    }
                )

                SwitchPrefence(
                    title = "Auto Connect",
                    subtitle = "Automatically connect to this device when the app starts",
                    value = true,
                    onValueChange = { }
                )

                PreferenceSeparator()
                PreferenceCategory("Toggles")

                SwitchPrefence(
                    title = "Favorite",
                    value = true,
                    onValueChange = { }
                )

                var fav by remember { mutableStateOf(false) }
                SwitchPrefence(
                    title = "Favorite",
                    value = fav,
                    onValueChange = { fav = it }
                )
            }
        }
    }
}

@Composable
fun PreferenceCategory(
    text: String,
) {
    Text(
        text = text,
        color = AppColors.ContentSecondary,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun PreferenceSeparator() {
    HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(bottom = 24.dp))
}

@Composable
fun SwitchPrefence(
    title: String,
    subtitle: String? = null,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = subtitle ?: if (value) "On" else "Off",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Switch(
            checked = value,
            onCheckedChange = { onValueChange(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.Green,
                checkedBorderColor = AppColors.Green,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = AppColors.SurfaceHighest,
                uncheckedBorderColor = AppColors.SurfaceHighest,
            ),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

/** Grupo de preferências em um bloco arredondado. */
@Composable
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface),
        content = content,
    )
}

@Composable
fun PreferenceDivider() {
    HorizontalDivider(
        color = AppColors.Separator,
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 16.dp),
    )
}
