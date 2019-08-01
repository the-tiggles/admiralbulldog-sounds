package com.github.mrbean355.dota2.integration.bytes

import com.github.mrbean355.dota2.integration.GameState
import com.github.mrbean355.dota2.integration.UNINITIALISED
import com.github.mrbean355.dota2.integration.assets.SoundFile
import com.github.mrbean355.dota2.integration.random
import java.util.concurrent.TimeUnit

class Periodically : SoundByte {
    override val choices = listOf(
            SoundFile.ADMIRALC,
            SoundFile.ALLIANCE,
            SoundFile.AYAWUTFACE,
            SoundFile.BRUH,
            SoundFile.CEB,
            SoundFile.EXPECT,
            SoundFile.FEELSGOODMAN,
            SoundFile.FOOD,
            SoundFile.HABIBI,
            SoundFile.HISHEAD,
            SoundFile.LIPS,
            SoundFile.MOTHERCOMES,
            SoundFile.NUTS,
            SoundFile.PERMABAN,
            SoundFile.PLEBSAREDISGUSTING,
            SoundFile.PRAISE,
            SoundFile.PSST,
            SoundFile.RONNIE,
            SoundFile.SAUSAGE,
            SoundFile.SLOWDOWN,
            SoundFile.SMART,
            SoundFile.STOP,
            SoundFile.TEAMPEPEGA,
            SoundFile.WATCHDOTA)
    private val minQuietTime = TimeUnit.MINUTES.toSeconds(5)
    private val maxQuietTime = TimeUnit.MINUTES.toSeconds(15)
    private var nextPlayClockTime = UNINITIALISED

    override fun shouldPlay(previous: GameState, current: GameState): Boolean {
        if (nextPlayClockTime == UNINITIALISED) {
            nextPlayClockTime = current.map!!.clock_time + random.nextLong(minQuietTime, maxQuietTime)
        } else if (current.map!!.clock_time >= nextPlayClockTime) {
            nextPlayClockTime += random.nextLong(minQuietTime, maxQuietTime)
            return true
        }
        return false
    }
}