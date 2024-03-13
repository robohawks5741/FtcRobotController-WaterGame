package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DigitalChannel
import org.firstinspires.ftc.teamcode.addSome
import org.firstinspires.ftc.teamcode.search
import java.util.ArrayList

class SamFin(config: ModuleConfig) : BotModule(config) {

    @JvmField val indicatorRed: DigitalChannel?     = hardwareMap.search("led1")
    @JvmField val indicatorGreen: DigitalChannel?   = hardwareMap.search("led0")

    enum class Pulse {
        SPACE,
        LETTER_SPACE,
        DOT,
        DASH;

        companion object {
            fun fromString(string: String): ArrayList<Pulse> {
                val str = string.uppercase()
                val out = ArrayList<Pulse>()
                var i = 0
                while (i < str.length) {
                    val char = str[i]
                    when (char) {
                        'A' -> out.addSome(DOT, DASH, LETTER_SPACE)
                        'B' -> out.addSome(DASH, DOT, DOT, DOT, LETTER_SPACE)
                        'C' -> out.addSome(DASH, DOT, DASH, DOT, LETTER_SPACE)
                        'D' -> out.addSome(DASH, DOT, DOT, LETTER_SPACE)
                        'E' -> out.addSome(DOT, LETTER_SPACE)
                        'F' -> out.addSome(DOT, DOT, DASH, DOT, LETTER_SPACE)
                        'G' -> out.addSome(DASH, DASH, DOT, LETTER_SPACE)
                        'H' -> out.addSome(DOT, DOT, DOT, DOT, LETTER_SPACE)
                        'I' -> out.addSome(DOT, DOT, LETTER_SPACE)
                        'J' -> out.addSome(DOT, DASH, DASH, DASH, LETTER_SPACE)
                        'K' -> out.addSome(DASH, DOT, DASH, LETTER_SPACE)
                        'L' -> out.addSome(DOT, DASH, DOT, DOT, LETTER_SPACE)
                        'M' -> out.addSome(DASH, DASH, LETTER_SPACE)
                        'N' -> out.addSome(DASH, DOT, LETTER_SPACE)
                        'O' -> out.addSome(DASH, DASH, DASH, LETTER_SPACE)
                        'P' -> out.addSome(DOT, DASH, DASH, DOT, LETTER_SPACE)
                        'Q' -> out.addSome(DASH, DASH, DOT, DASH, LETTER_SPACE)
                        'R' -> out.addSome(DOT, DASH, DOT, LETTER_SPACE)
                        'S' -> out.addSome(DOT, DOT, DOT, LETTER_SPACE)
                        'T' -> out.addSome(DASH, LETTER_SPACE)
                        'U' -> out.addSome(DOT, DOT, DASH, LETTER_SPACE)
                        'V' -> out.addSome(DOT, DOT, DOT, DASH, LETTER_SPACE)
                        'W' -> out.addSome(DOT, DASH, DASH, LETTER_SPACE)
                        'X' -> out.addSome(DASH, DOT, DOT, DASH, LETTER_SPACE)
                        'Y' -> out.addSome(DASH, DOT, DASH, DASH, LETTER_SPACE)
                        'Z' -> out.addSome(DASH, DASH, DOT, DOT, LETTER_SPACE)
                        ' ' -> out.addSome(SPACE)
                    }
                    i++
                }
                return out
            }
        }
    }

    private var jerma: Thread? = null

    private fun ping(state: Boolean) {
        indicatorRed?.mode = DigitalChannel.Mode.OUTPUT
        indicatorGreen?.mode = DigitalChannel.Mode.OUTPUT
        indicatorRed?.state = state
        indicatorGreen?.state = state
    }

    override fun modStart() {
        jerma = Thread {
            val wtf = Pulse.fromString(WHAT_TO_FLASH)
            var prev = Pulse.SPACE

            for (e in wtf) {
                // 1. set light state
                // 2. sleep
                Thread.sleep(
                    when (e) {
                        Pulse.DOT -> { ping(true); TIME_UNIT_MS * 1 }
                        Pulse.DASH -> { ping(true); TIME_UNIT_MS * 3 }
                        Pulse.LETTER_SPACE -> { ping(false); TIME_UNIT_MS * (3 - 1) }
                        Pulse.SPACE -> {
                            ping(false)
                            // don't make spaces longer after an existing space
                            if (prev == Pulse.LETTER_SPACE) {
                                TIME_UNIT_MS * ((7 - 1) - (3 - 1))
                            } else TIME_UNIT_MS * (7 - 1)
                        }
                    }
                )
                // set previous
                prev = e

                // 3. turn light off
                ping(false)
                // 4. sleep for 1 time unit
                Thread.sleep(TIME_UNIT_MS * 1)
            }
        }
    }

    override fun modUpdate() {

    }

    override fun modStop() {
        jerma?.interrupt()
    }

    companion object {
        const val WHAT_TO_FLASH = "matthew please give us money"
        const val TIME_UNIT_MS: Long = 200
    }
}