package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.Axis
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.RawInputAnalog1
import computer.living.gamepadyn.RawInputAnalog2
import computer.living.gamepadyn.RawInputDigital
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.teamcode.component.Arm
import org.firstinspires.ftc.teamcode.component.Claw
import org.firstinspires.ftc.teamcode.component.Drive
import org.firstinspires.ftc.teamcode.component.VerticalSlide
import org.firstinspires.ftc.teamcode.component.Wrist


class MainDriverControl(host: OpModeHost<MainDriverControl>) : UltraOpMode(host) {
    @TeleOp(name = "MainDriverControl")
    class Host : OpModeHost<MainDriverControl>(MainDriverControl::class)

    private val gamepadyn = Gamepadyn.create(
        ActionDigital::class,
        ActionAnalog1::class,
        ActionAnalog2::class,
        InputBackendFtc(host),
        strict = true
    )

    private val p0 = gamepadyn.getPlayer(0)!!
    private val p1 = gamepadyn.getPlayer(1)!!

    private val claw = Claw(componentManager)
    private val arm = Arm(componentManager)
    private val drive = Drive(componentManager)
    // TODO: wrist component
    // private val wrist = Wrist(componentManager)
    // private val horizontalSlide = HorizontalSlide(componentManager)
    private val verticalSlide = VerticalSlide(componentManager)

    private val droneLaunch: Servo? = getHardware("drone") //looking for a servo with name "drone"

    private var hasLaunchedDrone = false

    //Initializing code below
    override fun start() {

        droneLaunch?.position = 1.0

        p0.configuration = Configuration {
            actionDigital(ActionDigital.TOGGLE_DRIVER_RELATIVITY) { input(RawInputDigital.SPECIAL_BACK) } //toggle driver relative controls
            actionAnalog2(ActionAnalog2.MOVEMENT) { input(RawInputAnalog2.STICK_LEFT) } //lateral movement, left stick
            actionAnalog1(ActionAnalog1.ROTATION) {
                split(
                    input(RawInputAnalog2.STICK_RIGHT), //rotation controlled by right stick left/right
                    Axis.X
                )
            }
            actionDigital(ActionDigital.H_SLIDE_EXTEND) {// horizontal slide extend, right trigger
                gt(
                    input(RawInputAnalog1.TRIGGER_RIGHT),
                    constant(0.5f)
                )
            }
            actionDigital(ActionDigital.H_SLIDE_RETRACT) {// retracts slide, left trigger
                gt(
                    input(RawInputAnalog1.TRIGGER_LEFT),
                    constant(0.5f)
                )
            }
        }

        p1.configuration = Configuration {
            actionDigital(ActionDigital.LAUNCH_DRONE) { input(RawInputDigital.STICK_RIGHT_BUTTON) } //drone launch (not currently working by the looks of it)

//            actionDigital(ActionDigital.H_SLIDE_EXTEND) {
//                    gt(
//                        input(RawInputAnalog1.TRIGGER_RIGHT),
//                        constant(0.5f)
//                    )
//            }
//            actionDigital(ActionDigital.H_SLIDE_RETRACT) {
//                    gt(
//                        input(RawInputAnalog1.TRIGGER_LEFT),
//                        constant(0.5f)
//                    )
//            }

            actionDigital(ActionDigital.V_SLIDE_EXTEND) {//vertical slide extend, right stick up
                gt(
                    split(
                        input(RawInputAnalog2.STICK_RIGHT),
                        Axis.Y
                    ),
                    constant(0.5f)
                )
            }
            actionDigital(ActionDigital.V_SLIDE_RETRACT) {//vertical slide extend, right stick down
                lt(
                    split(
                        input(RawInputAnalog2.STICK_RIGHT),
                        Axis.Y
                    ),
                    constant(-0.5f)
                )
            }

            actionDigital(ActionDigital.CLAW_CLOSE) { input(RawInputDigital.BUMPER_LEFT) } //Claw open and close, right and left bumper respectively
            actionDigital(ActionDigital.CLAW_OPEN) { input(RawInputDigital.BUMPER_RIGHT) }

            actionAnalog1(ActionAnalog1.ARM_MOVE_MANUAL) {
                subtract(
                    input(RawInputAnalog1.TRIGGER_RIGHT), // extend
                    input(RawInputAnalog1.TRIGGER_LEFT) // retract
                )
            }
            actionDigital(ActionDigital.ARM_EXTEND) { input(RawInputDigital.FACE_DOWN) }
            actionDigital(ActionDigital.ARM_RETRACT) { input(RawInputDigital.FACE_RIGHT) }

            actionAnalog1(ActionAnalog1.WRIST_MOVE_MANUAL) {
                subtract(
                    branch(input(RawInputDigital.FACE_UP), constant(1f), constant(0f)),
                    branch(input(RawInputDigital.FACE_LEFT), constant(1f), constant(0f))
                )
            }
        }

//        gamepadyn.addListener(ActionDigital.H_SLIDE_EXTEND) { if (it.data()) horizontalSlide.extend() }
//        gamepadyn.addListener(ActionDigital.H_SLIDE_RETRACT) {if (it.data()) horizontalSlide.retract() } // Horizontal slide not currently working

        gamepadyn.addListener(ActionDigital.V_SLIDE_EXTEND) { if (it.data()) verticalSlide.extend() }
        gamepadyn.addListener(ActionDigital.V_SLIDE_RETRACT) { if (it.data()) verticalSlide.retract() }

        gamepadyn.addListener(ActionDigital.LAUNCH_DRONE) {
            if (it.data() && !hasLaunchedDrone) {
//        if (runtime > 90) {
//
//        }
                droneLaunch?.position = 0.0
            }
        }

        gamepadyn.addListener(ActionDigital.TOGGLE_DRIVER_RELATIVITY) {
            if (it.data()) drive.useDriverRelative = !drive.useDriverRelative
        }

        gamepadyn.addListener(ActionDigital.CLAW_OPEN) { if (it.data()) claw.isOpen = true }
        gamepadyn.addListener(ActionDigital.CLAW_CLOSE) { if (it.data()) claw.isOpen = false }

        gamepadyn.addListener(ActionDigital.ARM_EXTEND) { if (it.data()) arm.extend() }
        gamepadyn.addListener(ActionDigital.ARM_RETRACT) { if (it.data()) arm.retract() }
    }

    //Main loop below
    override fun loop() {
        gamepadyn.update()

        arm.position = (arm.position + p1.getState(ActionAnalog1.ARM_MOVE_MANUAL).x * 0.0016f).coerceIn(0.0..1.0)
        telemetry.addLine("Drone launch servo pos: ${droneLaunch?.position}")

        telemetry.update()
    }

}