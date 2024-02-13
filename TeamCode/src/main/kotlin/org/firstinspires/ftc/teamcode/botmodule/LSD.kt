package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataDigital
import org.firstinspires.ftc.teamcode.ActionDigital
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.idc
import kotlin.math.roundToInt

/**
 * Linear Slide Driver
 */
// TODO: make sure the slides are zeroed out and that the zero-position is KEPT across autonomous and TeleOp boundaries
// TODO:
//  - make sure that the slides won't break if they get stuck on the truss
//  - collision avoidance in both TeleOp and Auto
//  - Beam breaks to the claws so we know when the pixels are in and maybe to spin the intake
class LSD(cfg: ModuleConfig) : BotModule(cfg) {

    private val slideLeft:  DcMotorEx?  = idc { hardwareMap[DcMotorEx   ::class.java,   "slideL"     ] }
    private val slideRight: DcMotorEx?  = idc { hardwareMap[DcMotorEx   ::class.java,   "slideR"     ] }
    private val armLeft: Servo    = hardwareMap[Servo::class.java, "armL"]
    private val armRight: Servo    = hardwareMap[Servo::class.java, "armR"]

    companion object {
        /**
         * The maximum position of the slide, in encoder ticks.
         */
        const val HEIGHT_MAX = 1500
        const val HEIGHT_MIN = 0
        const val HEIGHT_ARM_SAFE = 502
        const val ARM_UP_LEFT = 0.65
        const val ARM_UP_RIGHT = 0.35
        const val ARM_DOWN_LEFT = 0.98
        const val ARM_DOWN_RIGHT = 0.05
        var POWER_MAX = 1.0
        // scale when moving downwards
        var POWER_DOWNWARD_SCALE = 1.0 // 0.75
    }

    init {
        if (slideLeft == null || slideRight == null) {
            val missing = mutableSetOf<String>()
            if (slideLeft == null) missing.add("slideL")
            if (slideRight == null) missing.add("slideR")

            status = Status(StatusEnum.MISSING_HARDWARE, hardwareMissing = missing)
        } else {
            slideLeft.targetPosition = 0
            slideRight.targetPosition = 0

            if (!BotShared.wasLastOpModeAutonomous) {
                slideLeft.mode = STOP_AND_RESET_ENCODER
                slideRight.mode = STOP_AND_RESET_ENCODER
            }

            slideLeft.mode = RUN_TO_POSITION
            slideRight.mode = RUN_TO_POSITION

            slideLeft.zeroPowerBehavior = BRAKE
            slideRight.zeroPowerBehavior = BRAKE

            // Directions
            slideLeft.direction = FORWARD
            slideLeft.direction = FORWARD
//            slideRight.direction =              REVERSE
        }
    }

    var isArmDown = true
        private set(status) {
            field = status
            //                  if             [down pos]     else [up pos]
            armRight.position = if (isArmDown) ARM_DOWN_RIGHT else ARM_UP_RIGHT
            armLeft.position =  if (isArmDown) ARM_DOWN_LEFT  else ARM_UP_LEFT
        }

    private var isMovingSlides = false

    var targetHeight: Int = 0
        set(height) {
            // half the power for downwards movement
            val desiredPower = if (height < currentHeight) POWER_MAX * POWER_DOWNWARD_SCALE else POWER_MAX

            slideLeft?. power = desiredPower
            slideRight?.power = desiredPower
            // evaluated height
            field = height.coerceIn(HEIGHT_MIN..HEIGHT_MAX)
            slideLeft?. targetPosition = field
            slideRight?.targetPosition = -field
        }

    var teleOpTargetHeight = 0

    /**
     * Whether or not the slide's **target height** is up.
     */
    val isSlideUp: Boolean
        get() = targetHeight > HEIGHT_ARM_SAFE

    /**
     * The average value (in ticks) of the two slides.
     */
    val currentHeight: Int
        // pay attention to the negative sign here, the right slide is negative.
        get() = if (status.status == StatusEnum.OK) ((slideLeft!!.currentPosition + -slideRight!!.currentPosition).toFloat() / 2f).roundToInt()
                else 0

    override fun modStart() {
        slideLeft?.targetPosition = 0
        slideRight?.targetPosition = 0

        slideLeft?.mode  = RUN_TO_POSITION
        slideRight?.mode = RUN_TO_POSITION
    }

    private fun teleOpSlideUpdate() {
        teleOpTargetHeight = teleOpTargetHeight.coerceIn(0..6)
        if (isSlideUp) targetHeight = teleOpTargetHeight * 200 + 300
    }

    override fun modStartTeleOp() {
        if (gamepadyn == null) {
            TODO("cry about it")
        }
        val macroSlideAdjustUp = fun(it: InputDataDigital) {
            // D-Pad left -> raise slides
            // pos = target * 200 + 300
            if (it() && teleOpTargetHeight < 6) {
                teleOpTargetHeight++
                teleOpSlideUpdate()
            }
        }

        val macroSlideAdjustDown = fun(it: InputDataDigital) {
            if (it() && teleOpTargetHeight > 0) {
                teleOpTargetHeight--
                teleOpSlideUpdate()
            }
        }

        gamepadyn.players[0].getEvent(SLIDE_ADJUST_UP, macroSlideAdjustUp)
        gamepadyn.players[1].getEvent(SLIDE_ADJUST_UP, macroSlideAdjustUp)
        gamepadyn.players[0].getEvent(SLIDE_ADJUST_DOWN, macroSlideAdjustDown)
        gamepadyn.players[1].getEvent(SLIDE_ADJUST_DOWN, macroSlideAdjustDown)
    }

    override fun modUpdate() {
        // if our target & current heights are above the safe height for the arm, extend the arm.
        if (targetHeight > HEIGHT_ARM_SAFE && currentHeight > HEIGHT_ARM_SAFE) {
            isArmDown = false
        }
        telemetry.addData("LSD target height:", targetHeight)
        telemetry.addData("LSD left slide height", slideLeft?.currentPosition)
        telemetry.addData("LSD right slide height", slideRight?.currentPosition)
        telemetry.addData("LSD current avg. height", currentHeight)
    }
}