package com.github.mrbean355.admiralbulldog

import com.github.mrbean355.admiralbulldog.events.SoundEvent
import com.github.mrbean355.admiralbulldog.persistence.ConfigPersistence
import com.github.mrbean355.admiralbulldog.ui.SoundBiteTracker
import com.github.mrbean355.admiralbulldog.ui.finalise
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlin.reflect.KClass

class ChooseSoundFilesStage(private val type: KClass<out SoundEvent>) : Stage() {
    private val tracker = SoundBiteTracker(ConfigPersistence.getSoundsForType(type))

    init {
        val root = VBox(PADDING_SMALL)
        root.padding = Insets(PADDING_MEDIUM)
        root.children += tracker.createSearchField()
        root.children += tracker.createListView()
        root.children += Button(ACTION_SAVE).apply {
            setOnAction { saveToggles() }
        }
        finalise(title = type.friendlyName, root = root)
    }

    private fun saveToggles() {
        ConfigPersistence.saveSoundsForType(type, tracker.getSelection())
        close()
    }
}