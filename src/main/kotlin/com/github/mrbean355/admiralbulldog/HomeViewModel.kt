package com.github.mrbean355.admiralbulldog

import com.github.mrbean355.admiralbulldog.assets.SoundBytes
import com.github.mrbean355.admiralbulldog.game.monitorGameStateUpdates
import com.github.mrbean355.admiralbulldog.persistence.ConfigPersistence
import com.github.mrbean355.admiralbulldog.service.ReleaseInfo
import com.github.mrbean355.admiralbulldog.service.UpdateChecker
import com.github.mrbean355.admiralbulldog.service.logAnalyticsEvent
import com.github.mrbean355.admiralbulldog.ui.Alert
import com.github.mrbean355.admiralbulldog.ui.DotaPath
import com.github.mrbean355.admiralbulldog.ui.Installer
import com.github.mrbean355.admiralbulldog.ui.getString
import com.github.mrbean355.admiralbulldog.ui.prepareTrayIcon
import com.github.mrbean355.admiralbulldog.ui.removeVersionPrefix
import com.github.mrbean355.admiralbulldog.ui.showModal
import com.github.mrbean355.admiralbulldog.ui.toNullable
import com.vdurmont.semver4j.Semver
import javafx.application.HostServices
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import kotlin.system.exitProcess

class HomeViewModel(private val stage: Stage, private val hostServices: HostServices) {
    private val logger = LoggerFactory.getLogger(HomeViewModel::class.java)
    private val hasHeardFromDota = SimpleBooleanProperty(false)
    val heading: ObservableValue<String> = Bindings.createStringBinding(Callable {
        if (hasHeardFromDota.get()) getString("msg_connected") else getString("msg_not_connected")
    }, hasHeardFromDota)
    val progressBarVisible: ObservableValue<Boolean> = hasHeardFromDota.not()
    val infoMessage: ObservableValue<String> = Bindings.createStringBinding(Callable {
        if (hasHeardFromDota.get()) getString("dsc_connected") else getString("dsc_not_connected")
    }, hasHeardFromDota)
    val version = SimpleStringProperty(getString("lbl_app_version", APP_VERSION))

    fun init() {
        if (SoundBytes.shouldSync()) {
            SyncSoundBytesStage().showModal(owner = stage, wait = true)
        }
        val dotaPath = loadDotaPath()
        prepareTrayIcon(stage)
        Installer.installIfNecessary(dotaPath)
        checkForInvalidSounds()
        GlobalScope.launch {
            checkForAppUpdate()
        }
        stage.show()
        monitorGameStateUpdates {
            Platform.runLater {
                hasHeardFromDota.set(true)
            }
        }
        logAnalyticsEvent(eventType = "app_start", eventData = APP_VERSION)
    }

    fun onChangeSoundsClicked() {
        ToggleSoundEventsStage().showModal(owner = stage)
    }

    fun onDiscordBotClicked() {
        DiscordBotStage(hostServices).showModal(owner = stage)
    }

    fun onDiscordCommunityClicked() {
        logAnalyticsEvent(eventType = "button_click", eventData = "discord_community")
        hostServices.showDocument(URL_DISCORD_INVITE)
    }

    fun onProjectWebsiteClicked() {
        logAnalyticsEvent(eventType = "button_click", eventData = "project_website")
        hostServices.showDocument(URL_PROJECT_WEBSITE)
    }

    private fun loadDotaPath(): String {
        val result = runCatching {
            DotaPath.loadPath(stage)
        }
        if (result.isSuccess) {
            return result.getOrThrow()
        }
        Alert(type = Alert.AlertType.ERROR,
                header = getString("header_installer"),
                content = getString("msg_no_dota_path"),
                buttons = arrayOf(ButtonType.CLOSE),
                owner = stage
        ).showAndWait()
        exitProcess(-1)
    }

    private fun checkForInvalidSounds() {
        val invalidSounds = ConfigPersistence.getInvalidSounds()
        if (invalidSounds.isNotEmpty()) {
            Alert(type = Alert.AlertType.WARNING,
                    header = getString("header_sounds_removed"),
                    content = getString("msg_sounds_removed", invalidSounds.joinToString(separator = "\n")),
                    buttons = arrayOf(ButtonType.OK),
                    owner = stage
            ).showAndWait()
            ConfigPersistence.clearInvalidSounds()
        }
    }

    private suspend fun checkForAppUpdate() {
        logger.info("Checking for app update...")
        val releaseInfo = UpdateChecker.getLatestReleaseInfo()
        if (releaseInfo == null) {
            logger.warn("Null app release info, giving up")
            return
        }
        val currentVersion = Semver(APP_VERSION)
        val latestVersion = Semver(releaseInfo.tagName.removeVersionPrefix())
        if (latestVersion > currentVersion) {
            logger.info("Later app version available: $latestVersion")
            withContext(Main) {
                if (doesUserWantToUpdate(getString("header_app_update_available"), currentVersion, releaseInfo)) {
                    logAnalyticsEvent(eventType = "button_click", eventData = "download_app_update")
                    downloadAppUpdate(releaseInfo)
                }
            }
        } else {
            logger.info("Already at latest app version: $currentVersion")
        }
    }

    private fun downloadAppUpdate(releaseInfo: ReleaseInfo) {
        DownloadUpdateStage(releaseInfo.getJarAssetInfo()!!, ".")
                .setOnComplete {
                    Alert(type = Alert.AlertType.INFORMATION,
                            header = getString("header_app_update_downloaded"),
                            content = getString("msg_app_update_downloaded", it),
                            buttons = arrayOf(ButtonType.FINISH),
                            owner = stage
                    ).showAndWait()
                    exitProcess(0)
                }.show()
    }

    private fun doesUserWantToUpdate(header: String, currentVersion: Semver, releaseInfo: ReleaseInfo): Boolean {
        val whatsNewButton = ButtonType(getString("btn_whats_new"), ButtonBar.ButtonData.HELP_2)
        val downloadButton = ButtonType(getString("btn_download"), ButtonBar.ButtonData.NEXT_FORWARD)
        val action = Alert(
                type = Alert.AlertType.INFORMATION,
                header = header,
                content = getString("msg_app_update_available", currentVersion, releaseInfo.name, releaseInfo.publishedAt),
                buttons = arrayOf(whatsNewButton, downloadButton, ButtonType.CANCEL),
                owner = stage
        ).showAndWait().toNullable()

        if (action === whatsNewButton) {
            hostServices.showDocument(releaseInfo.htmlUrl)
            return doesUserWantToUpdate(header, currentVersion, releaseInfo)
        }
        return action === downloadButton
    }
}