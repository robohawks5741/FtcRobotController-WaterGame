package org.firstinspires.ftc.teamcode.test

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import computer.living.gamepadyn.ActionBind
import computer.living.gamepadyn.ActionMap
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.RawInputDigital
import computer.living.gamepadyn.RawInputDigital.*
import computer.living.gamepadyn.RawInputAnalog1
import computer.living.gamepadyn.RawInputAnalog1.*
import computer.living.gamepadyn.RawInputAnalog2
import computer.living.gamepadyn.RawInputAnalog2.*
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.teamcode.ActionAnalog1
import org.firstinspires.ftc.teamcode.ActionAnalog2
import org.firstinspires.ftc.teamcode.ActionDigital
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.ActionDigital.*

@TeleOp
class GamepadynTest : OpMode() {
    private val gamepadyn = Gamepadyn(
        InputBackendFtc(this),
        strict = true,
        ActionMap(
            ActionDigital.entries,
            ActionAnalog1.entries,
            ActionAnalog2.entries,
        )
    )

    override fun init() {
        gamepadyn.players[0].configuration = Configuration(
            ActionBind(TOGGLE_DRIVER_RELATIVITY,    FACE_DOWN),
            ActionBind(MOVEMENT,                    STICK_LEFT)
        )
    }

    private var driverRelative = true

    override fun start() {

        gamepadyn.players[0].getEvent(TOGGLE_DRIVER_RELATIVITY) {
            if (it()) {
                driverRelative = !driverRelative
            }
        }

    }

    override fun loop() {
        gamepadyn.update()
        val movement = gamepadyn.players[0].getState(MOVEMENT)
        telemetry.addLine("driver relativity: ${if (driverRelative) "ENABLED" else "DISABLED" }")
        telemetry.addLine("state of movement: (${movement.x}, ${movement.y})")
        telemetry.update()
    }
}