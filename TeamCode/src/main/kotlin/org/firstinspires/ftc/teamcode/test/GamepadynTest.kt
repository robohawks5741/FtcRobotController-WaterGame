package org.firstinspires.ftc.teamcode.test

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
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
    private val gamepadyn = Gamepadyn.create(
        ActionDigital::class,
        ActionAnalog1::class,
        ActionAnalog2::class,
        InputBackendFtc(this),
        strict = true
    )

    override fun init() {
        gamepadyn.getPlayer(0)!!.configuration = Configuration {
            actionDigital(TOGGLE_DRIVER_RELATIVITY) { input(FACE_DOWN) }
            actionAnalog2(MOVEMENT)                 { input(STICK_LEFT) }
        }
    }

    private var driverRelative = true

    override fun start() {

        gamepadyn.getPlayer(0)!!.addListener(TOGGLE_DRIVER_RELATIVITY) {
            if (it.data()) driverRelative = !driverRelative
        }

    }

    override fun loop() {
        gamepadyn.update()
        val movement = gamepadyn.getPlayer(0)!!.getState(MOVEMENT)
        telemetry.addLine("driver relativity: ${if (driverRelative) "ENABLED" else "DISABLED" }")
        telemetry.addLine("state of movement: (${movement.x}, ${movement.y})")
        telemetry.update()
    }
}