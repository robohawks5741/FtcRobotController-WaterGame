package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.search
import kotlin.math.roundToInt

/**
 * Linear Slide Driver
 * Also controls the arm, maybe the claw in the future
 */
// TODO: make sure the slides are zeroed out and that the zero-position is KEPT across autonomous and TeleOp boundaries
// TODO:
//  - make sure that the slides won't break if they get stuck on the truss
//  - collision avoidance in both TeleOp and Auto
//  - Beam breaks to the claws so we know when the pixels are in and maybe to spin the intake
class Slide(manager: ComponentManager) : Component(manager) {
    
//    private val slideLeftController: DcMotorControllerEx?   = slideLeft?.controller as? DcMotorControllerEx
//    private val slideRightController: DcMotorControllerEx?  = slideRight?.controller as? DcMotorControllerEx
    private val slideLeft: DcMotorEx?                       = hardwareMap.search("slideL")
    private val slideRight: DcMotorEx?                      = hardwareMap.search("slideR")
    private val armLeft: Servo?                             = hardwareMap.search("armL")
    private val armRight: Servo?                            = hardwareMap.search("armR")
//    private val armLeftController: ServoControllerEx?       = armLeft?.controller as? ServoControllerEx // idc { hardwareMap[ServoControllerEx::class.java, "armL"] }
//    private val armRightController: ServoControllerEx?      = armRight?.controller as? ServoControllerEx // idc { hardwareMap[ServoControllerEx::class.java, "armR"] }

    // it takes ~900ms to move the slides up from HEIGHT_ARM_SAFE to HEIGHT_MAX
    // it takes ~600ms to move the slides down to HEIGHT_MIN from HEIGHT_MAX
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
        if (slideLeft == null || slideRight == null || armLeft == null || armRight == null) {
            val missing = mutableSetOf<String>()
            if (slideLeft == null)  missing.add("slideL")
            if (slideRight == null) missing.add("slideR")
            if (armLeft == null)    missing.add("armL")
            if (armRight == null)   missing.add("armR")

            status = Status(StatusEnum.BAD, hardwareMissing = missing)
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

    private var isMovingSlides = false
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
            telemetry.addLine("(LSD Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }
        
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

    override fun modUpdateTeleOp() {
        telemetry.addData("LSD (TeleOp) target height:", teleOpTargetHeight)
    }

    override fun modUpdate() {
        // if our target & current heights are above the safe height for the arm, always extend the arm.
        if (targetHeight >= HEIGHT_ARM_SAFE && currentHeight >= HEIGHT_ARM_SAFE) {
            armRight?.position = ARM_DOWN_RIGHT
            armLeft?.position =  ARM_DOWN_LEFT
        } else {
            armRight?.position = ARM_UP_RIGHT
            armLeft?.position =  ARM_UP_LEFT
        }

        telemetry.addData("LSD target height:", targetHeight)
        telemetry.addData("LSD current avg. height", currentHeight)
        telemetry.addData("LSD slide L height", slideLeft?.currentPosition)
        telemetry.addData("LSD slide R height", slideRight?.currentPosition)
        telemetry.addData("LSD arm L pos", armLeft?.position)
        telemetry.addData("LSD arm R pos", armRight?.position)

        telemetry.addData("LSD slide L amps", slideLeft?.getCurrent(CurrentUnit.AMPS))
        telemetry.addData("LSD slide R amps", slideRight?.getCurrent(CurrentUnit.AMPS))

//        if (slideLeft?.getCurrent(CurrentUnit.AMPS) >)
    }
}