package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.Axis
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.RawInputAnalog1.*
import computer.living.gamepadyn.RawInputAnalog2.*
import computer.living.gamepadyn.RawInputDigital.*
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.component.*

class DriverControlNouveau(host: OpModeHost<DriverControlNouveau>) : UltraOpMode(host) {

    private val gamepadyn = Gamepadyn.create(
        ActionDigital::class,
        ActionAnalog1::class,
        ActionAnalog2::class,
        InputBackendFtc(this.host),
        strict = true
    )

    // Truss component
    private val truss = Truss(componentManager)
    private val drive = Drive(componentManager)
    private val intake = Intake(componentManager)

    private val p0 = gamepadyn.getPlayer(0)!!
    private val p1 = gamepadyn.getPlayer(1)!!

    private var teleOpTargetHeight: Int = 0

    private fun teleOpSlideUpdate() {
        teleOpTargetHeight = teleOpTargetHeight.coerceIn(0..6)
        if (intake.isSlideUp) intake.targetHeight = teleOpTargetHeight * 200 + 300
    }

    override fun start() {
        p0.configuration = Configuration {

            // Left stick -> Movement
            actionAnalog2(MOVEMENT)         { input(STICK_LEFT) }
            // Right stick X -> Rotation
            actionAnalog1(ROTATION)         { split(input(STICK_RIGHT), Axis.X) }
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

        // Truss pull
        gamepadyn.addListener(TRUSS_PULL) {
            truss.hangPower = it.data.x.toDouble()
        }

        // Truss cycle
        gamepadyn.addListener(TRUSS_CYCLE) {
            truss.position = when (truss.position) {
                TrussPosition.DOWN -> TrussPosition.UP
                TrussPosition.UP -> TrussPosition.DOWN
            }
        }

        // Driver relativity toggle
        gamepadyn.addListener(TOGGLE_DRIVER_RELATIVITY) { if (it.data()) drive::useDriverRelative.flip() }

        gamepadyn.addListener(SLIDE_ADJUST_UP) {
            // D-Pad left -> raise slides
            // pos = target * 200 + 300
            if (it.data() && teleOpTargetHeight < 6) {
                teleOpTargetHeight++
                teleOpSlideUpdate()
            }
        }

        gamepadyn.addListener(SLIDE_ADJUST_DOWN) {
            if (it.data() && teleOpTargetHeight > 0) {
                teleOpTargetHeight--
                teleOpSlideUpdate()
            }
        }
    }

    override fun update() {
        componentManager.update()
        telemetry.addLine("LSD (TeleOp) target height: $teleOpTargetHeight")
    }

    override fun stop() {

    }
}
