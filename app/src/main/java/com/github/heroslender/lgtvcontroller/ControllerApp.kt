package com.github.heroslender.lgtvcontroller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.heroslender.lgtvcontroller.R.string
import com.github.heroslender.lgtvcontroller.ui.controller.RemoteCircleButton
import com.github.heroslender.lgtvcontroller.ui.navigation.ControllerNavHost
import com.github.heroslender.lgtvcontroller.ui.theme.AppColors

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun ControllerApp(navController: NavHostController = rememberNavController()) {
    ControllerNavHost(navController)
}

/**
 * Barra superior minimalista: título centralizado, botões circulares
 * de voltar e de ações, fundo preto sem sombra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerTopAppBar(
    title: String,
    subtitle: AnnotatedString? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.ContentSecondary,
                        maxLines = 1,
                    )
                }
            }
        },
        navigationIcon = {
            if (navigateUp != null) {
                Row(Modifier.padding(start = 12.dp)) {
                    RemoteCircleButton(
                        onClick = navigateUp,
                        size = 40.dp,
                        containerColor = AppColors.Surface,
                        contentDescription = stringResource(string.back_button),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = stringResource(string.back_button),
                            tint = AppColors.Content,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 12.dp),
                content = actions,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppColors.Background,
            scrolledContainerColor = AppColors.Background,
            titleContentColor = AppColors.Content,
        ),
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun TopAppBarAction(
    imageVector: ImageVector,
    contentDescription: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    RemoteCircleButton(
        onClick = onClick,
        size = 40.dp,
        enabled = enabled,
        containerColor = AppColors.Surface,
        contentDescription = contentDescription,
        modifier = modifier,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = AppColors.Content,
            modifier = Modifier.size(20.dp),
        )
    }
}
