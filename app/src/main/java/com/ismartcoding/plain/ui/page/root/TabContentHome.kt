package com.ismartcoding.plain.ui.page.root

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ismartcoding.lib.channel.receiveEventHandler
import com.ismartcoding.lib.channel.sendEvent
import com.ismartcoding.lib.helpers.CoroutinesHelper.coMain
import com.ismartcoding.lib.helpers.NetworkHelper
import com.ismartcoding.plain.R
import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.features.Permission
import com.ismartcoding.plain.features.PermissionsResultEvent
import com.ismartcoding.plain.features.RequestPermissionsEvent
import com.ismartcoding.plain.features.WindowFocusChangedEvent
import com.ismartcoding.plain.helpers.AppHelper
import com.ismartcoding.plain.preference.HttpPortPreference
import com.ismartcoding.plain.preference.HttpsPortPreference
import com.ismartcoding.plain.preference.LocalWeb
import com.ismartcoding.plain.ui.base.AlertType
import com.ismartcoding.plain.ui.base.BottomSpace
import com.ismartcoding.plain.ui.base.PAlert
import com.ismartcoding.plain.ui.base.PMiniOutlineButton
import com.ismartcoding.plain.ui.base.TopSpace
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.components.home.HomeFeatures
import com.ismartcoding.plain.ui.components.home.HomeWeb
import com.ismartcoding.plain.ui.models.MainViewModel
import com.ismartcoding.plain.web.HttpServerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabContentHome(
    navController: NavHostController,
    viewModel: MainViewModel,
    paddingValues: PaddingValues
) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val itemWidth = (configuration.screenWidthDp.dp - 97.dp) / 3
    val webEnabled = LocalWeb.current
    val context = LocalContext.current
    var systemAlertWindow by remember { mutableStateOf(Permission.SYSTEM_ALERT_WINDOW.can(context)) }
    var isVPNConnected by remember { mutableStateOf(NetworkHelper.isVPNConnected(context)) }
    val events = remember { mutableStateListOf<Job>() }
    LaunchedEffect(Unit) {
        events.add(
            receiveEventHandler<PermissionsResultEvent> {
                systemAlertWindow = Permission.SYSTEM_ALERT_WINDOW.can(context)
            }
        )
        events.add(
            receiveEventHandler<WindowFocusChangedEvent> {
                isVPNConnected = NetworkHelper.isVPNConnected(context)
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            events.forEach { it.cancel() }
            events.clear()
        }
    }

    LazyColumn {
        item {
            TopSpace()
        }
        item {
            if (webEnabled) {
                if (viewModel.httpServerError.isNotEmpty()) {
                    PAlert(title = stringResource(id = R.string.error), description = viewModel.httpServerError, AlertType.ERROR) {
                        if (HttpServerManager.portsInUse.isNotEmpty()) {
                            PMiniOutlineButton(
                                text = stringResource(R.string.change_port),
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        if (HttpServerManager.portsInUse.contains(TempData.httpPort)) {
                                            HttpPortPreference.putAsync(context, HttpServerManager.httpPorts.filter { it != TempData.httpPort }.random())
                                        }
                                        if (HttpServerManager.portsInUse.contains(TempData.httpsPort)) {
                                            HttpsPortPreference.putAsync(context, HttpServerManager.httpsPorts.filter { it != TempData.httpsPort }.random())
                                        }
                                        coMain {
                                            MaterialAlertDialogBuilder(context)
                                                .setTitle(R.string.restart_app_title)
                                                .setMessage(R.string.restart_app_message)
                                                .setPositiveButton(R.string.relaunch_app) { _, _ ->
                                                    AppHelper.relaunch(context)
                                                }
                                                .setCancelable(false)
                                                .create()
                                                .show()
                                        }
                                    }
                                },
                            )
                        }
                        PMiniOutlineButton(
                            text = stringResource(R.string.relaunch_app),
                            modifier = Modifier.padding(start = 16.dp),
                            onClick = {
                                AppHelper.relaunch(context)
                            },
                        )
                    }
                } else {
                    if (isVPNConnected) {
                        PAlert(title = stringResource(id = R.string.attention), description = stringResource(id = R.string.vpn_web_conflict_warning), AlertType.WARNING)
                    }
                    if (!systemAlertWindow) {
                        PAlert(title = stringResource(id = R.string.attention), description = stringResource(id = R.string.system_alert_window_warning), AlertType.WARNING) {
                            PMiniOutlineButton(
                                text = stringResource(R.string.grant_permission),
                                onClick = {
                                    sendEvent(RequestPermissionsEvent(Permission.SYSTEM_ALERT_WINDOW))
                                },
                            )
                        }
                    }
                }
            }
        }
        item {
            HomeWeb(context, navController, viewModel, webEnabled)
            VerticalSpace(dp = 16.dp)
        }
        item {
            HomeFeatures(navController, itemWidth)
        }
        item {
            BottomSpace(paddingValues)
        }
    }
}