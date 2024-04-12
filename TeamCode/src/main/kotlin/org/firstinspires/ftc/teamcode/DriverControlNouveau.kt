package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.RawInputAnalog1.*
import computer.living.gamepadyn.RawInputAnalog2.*
import computer.living.gamepadyn.RawInputDigital.*
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.component.Component.ComponentManager
import org.firstinspires.ftc.teamcode.component.Truss

class DriverControlNouveau : LinearOpMode() {

    private val gamepadyn = Gamepadyn.create(
        ActionDigital::class,
        ActionAnalog1::class,
        ActionAnalog2::class,
        InputBackendFtc(this),
        strict = true
    )

    override fun runOpMode() {

        val compMan = ComponentManager(this)
        val truss = Truss(compMan)

        val p0 = gamepadyn.getPlayer(0)!!
        val p1 = gamepadyn.getPlayer(1)!!

        p0.configuration = Configuration {

            // Left stick -> Movement
            actionAnalog2(MOVEMENT)         { input(STICK_LEFT) }
            // Left trigger -> Spin intake inwards
            actionAnalog1(INTAKE_SPIN)      { input(TRIGGER_LEFT) }
            // Right bumper -> Open left claw
            actionDigital(CLAW_LEFT_OPEN)   { input(BUMPER_RIGHT) }
            // Left bumper -> Open right claw
            actionDigital(CLAW_RIGHT_OPEN)  { input(BUMPER_LEFT) }

            // D-Pad up -> Close claws, move slides up to a safe height, rotate arm up, move slide up fully (or to runToHeight)
            actionDigital(MACRO_SLIDE_UP)   { input(DPAD_UP) }

            // Face down / A -> Pull in truss hang
            actionAnalog1(TRUSS_PULL) { branch(
                    /* if   */  input(FACE_DOWN),
                    /* then */  constant(1.0f),
                    /* else */  constant(0.0f)
            )}

            // Face down / A -> Pull in truss hang
            actionDigital(CLAW_LEFT_CLOSE)  { input(FACE_LEFT) }
            actionDigital(CLAW_RIGHT_CLOSE) { input(FACE_LEFT) }
        }

        p1.configuration = Configuration { }

        gamepadyn.addListener(TRUSS_PULL) {
            truss.hangPower = it.data.x.toDouble()
        }
        gamepadyn.addListener(TRUSS_CYCLE) {
            truss.position = when (truss.position) {
                TrussPosition.DOWN -> TrussPosition.UP
                TrussPosition.UP -> TrussPosition.DOWN
            }
        }

    }
}