package com.github.mrbean355.admiralbulldog.events

import com.github.mrbean355.admiralbulldog.game.GameState
import com.github.mrbean355.admiralbulldog.game.Item
import com.github.mrbean355.admiralbulldog.game.Items

class OnMidasReady : SoundEvent {

    override fun shouldPlay(previous: GameState, current: GameState): Boolean {
        return previous.items.isMidasOnCooldown() && current.items.isMidasOffCooldown()
    }

    private fun Items?.isMidasOnCooldown(): Boolean {
        if (this == null) return false
        return slot0.isMidasOnCooldown() || slot1.isMidasOnCooldown() || slot2.isMidasOnCooldown() ||
                slot3.isMidasOnCooldown() || slot4.isMidasOnCooldown() || slot5.isMidasOnCooldown()
    }

    private fun Items?.isMidasOffCooldown(): Boolean {
        if (this == null) return false
        return slot0.isMidasOffCooldown() || slot1.isMidasOffCooldown() || slot2.isMidasOffCooldown() ||
                slot3.isMidasOffCooldown() || slot4.isMidasOffCooldown() || slot5.isMidasOffCooldown()
    }

    private fun Item?.isMidasOffCooldown(): Boolean {
        return this != null && name == "item_hand_of_midas" && cooldown.compareTo(0f) == 0
    }

    private fun Item?.isMidasOnCooldown(): Boolean {
        return this != null && name == "item_hand_of_midas" && cooldown.compareTo(0f) > 0
    }
}