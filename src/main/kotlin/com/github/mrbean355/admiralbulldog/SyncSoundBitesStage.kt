package com.github.mrbean355.admiralbulldog

import com.github.mrbean355.admiralbulldog.assets.SoundBites
import com.github.mrbean355.admiralbulldog.persistence.ConfigPersistence
import com.github.mrbean355.admiralbulldog.ui.finalise
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Stage

class SyncSoundBitesStage : Stage() {
    private val progress = SimpleDoubleProperty()
    private val log = SimpleStringProperty(MSG_SYNC_WELCOME)
    private val complete = SimpleBooleanProperty(false)

    init {
        val root = VBox(PADDING_MEDIUM).apply {
            padding = Insets(PADDING_MEDIUM)
        }
        root.children += ProgressBar().apply {
            prefWidthProperty().bind(root.widthProperty())
            progressProperty().bind(this@SyncSoundBitesStage.progress)
        }
        root.children += TextArea().apply {
            isEditable = false
            textProperty().bind(log)
            log.addListener { _, _, _ ->
                // Using setScrollTop() doesn't work reliably
                selectPositionCaret(Int.MAX_VALUE)
                deselect()
            }
        }
        root.children += Button(ACTION_DONE).apply {
            disableProperty().bind(complete.not())
            setOnAction { close() }
        }

        SoundBites.synchronise(action = {
            Platform.runLater {
                log.value = "${log.value}\n$it"
            }
        }, progress = {
            progress.set(it)
        }, complete = { successful ->
            if (successful) {
                ConfigPersistence.markLastSync()
            }
            complete.set(true)
        })
        finalise(title = TITLE_SYNC_SOUND_BITES, root = root)
        width = WINDOW_WIDTH_LARGE
    }
}
