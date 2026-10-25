package com.github.heroslender.lgtvcontroller.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.github.heroslender.lgtvcontroller.R.string
import com.github.heroslender.lgtvcontroller.domain.model.App
import com.github.heroslender.lgtvcontroller.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * Painel que sobe de baixo para organizar os apps da tela inicial:
 * setas para mudar a ordem e um interruptor para mostrar ou esconder cada app.
 * Cada mudança é salva na hora.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun AppOrganizerSheet(
    apps: List<App>,
    hidden: Set<String>,
    onChange: (order: List<String>, hidden: Set<String>) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun move(index: Int, delta: Int) {
        val target = index + delta
        if (target !in apps.indices) return
        val ids = apps.map { it.id }.toMutableList()
        ids.add(target, ids.removeAt(index))
        onChange(ids, hidden)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Surface,
        contentColor = AppColors.Content,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.ContentTertiary) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp),
        ) {
            Text(
                text = stringResource(string.organize_apps_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Content,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onReset) {
                Text(stringResource(string.organize_apps_reset), color = AppColors.ContentSecondary)
            }
            TextButton(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            }) {
                Text(
                    stringResource(string.organize_apps_done),
                    color = AppColors.Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Text(
            text = stringResource(string.organize_apps_hint),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.ContentSecondary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(apps, key = { _, app -> app.id }) { index, app ->
                val visible = app.id !in hidden
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    GlideImage(
                        model = CustomGlideUrl(app.icon, app.name),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.SurfaceHigh)
                            .alpha(if (visible) 1f else 0.35f),
                    )
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (visible) AppColors.Content else AppColors.ContentSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    )
                    val arrowColors = IconButtonDefaults.iconButtonColors(
                        contentColor = AppColors.Content,
                        disabledContentColor = AppColors.ContentTertiary,
                    )
                    IconButton(
                        onClick = { move(index, -1) },
                        enabled = index > 0,
                        colors = arrowColors,
                    ) {
                        Icon(Icons.Rounded.KeyboardArrowUp, stringResource(string.move_up))
                    }
                    IconButton(
                        onClick = { move(index, 1) },
                        enabled = index < apps.lastIndex,
                        colors = arrowColors,
                    ) {
                        Icon(Icons.Rounded.KeyboardArrowDown, stringResource(string.move_down))
                    }
                    Switch(
                        checked = visible,
                        onCheckedChange = { show ->
                            onChange(
                                apps.map { it.id },
                                if (show) hidden - app.id else hidden + app.id,
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppColors.Green,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = AppColors.SurfaceHighest,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
    }
}
