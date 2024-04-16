package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.botmodule.Claw
import org.firstinspires.ftc.teamcode.search
import kotlin.math.roundToInt

// TODO: make sure the slides are zeroed out and that the zero-position is KEPT across autonomous and TeleOp boundaries
// TODO:
//  - make sure that the slides won't break if they get stuck on the truss
//  - collision avoidance in both TeleOp and Auto
//  - Beam breaks to the claws so we know when the pixels are in and maybe to spin the intake
class Intake(manager: ComponentManager) : Component(manager) {

//    private val slideLeftController: DcMotorControllerEx?   = slideLeft?.controller as? DcMotorControllerEx
//    private val slideRightController: DcMotorControllerEx?  = slideRight?.controller as? DcMotorControllerEx
    private val slideRight: DcMotorEx?                      = hardwareMap.search("slideR")
    private val slideLeft: DcMotorEx?                       = hardwareMap.search("slideL")
    private val armRight: Servo?                            = hardwareMap.search("armR")
    private val armLeft: Servo?                             = hardwareMap.search("armL")
    private val clawRight: Servo?                           = hardwareMap.search("clawR")
    private val clawLeft: Servo?                            = hardwareMap.search("clawL")
//    private val armLeftController: ServoControllerEx?       = armLeft?.controller as? ServoControllerEx // idc { hardwareMap[ServoControllerEx::class.java, "armL"] }
//    private val armRightController: ServoControllerEx?      = armRight?.controller as? ServoControllerEx // idc { hardwareMap[ServoControllerEx::class.java, "armR"] }

    // it takes ~900ms to move the slides up from HEIGHT_ARM_SAFE to HEIGHT_MAX
    // it takes ~600ms to move the slides down to HEIGHT_MIN from HEIGHT_MAX
    companion object {
        /**
         * The max position of the slide, in encoder ticks.
         */
        const val HEIGHT_MAX = 1500

        /**
         * The min position of the slide, in encoder ticks.
         */
        const val HEIGHT_MIN = 0

        /**
         * The lowest position of the slide where the arm can still freely rotate, in encoder ticks.
         */
        const val HEIGHT_ARM_SAFE = 502

        const val ARM_UP_LEFT = 0.65
        const val ARM_UP_RIGHT = 0.35
        const val ARM_DOWN_LEFT = 0.98
        const val ARM_DOWN_RIGHT = 0.05

        const val CLAW_LEFT_POS_OPEN: Double       = 0.0
        const val CLAW_LEFT_POS_CLOSED: Double     = 0.29
        const val CLAW_RIGHT_POS_OPEN: Double      = 0.36
        const val CLAW_RIGHT_POS_CLOSED: Double    = 0.07

        var POWER_MAX = 1.0
        // scale when moving downwards
        var POWER_DOWNWARD_SCALE = 1.0 // 0.75
    }

    override val status: Status

    init {
        val functionality = when {
            slideLeft == null || slideRight == null -> Functionality.NONE
            armRight == null || armLeft == null || clawRight == null || clawLeft == null -> Functionality.PARTIAL
            else -> Functionality.FULL
        }
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("slideR", DcMotorEx::class, slideRight != null),
            HardwareUsage("slideL", DcMotorEx::class, slideLeft != null),
            HardwareUsage("armR", Servo::class, armRight != null, false),
            HardwareUsage("armL", Servo::class, armLeft != null, false),
            HardwareUsage("clawR", Servo::class, clawRight != null, false),
            HardwareUsage("clawL", Servo::class, clawLeft != null, false),
        )
        status = Status(
            functionality,
            hardwareSet
        )
        if (slideLeft != null && slideRight != null) {
            slideLeft.targetPosition = 0
            slideRight.targetPosition = 0

            /**
             * This is really important!!!
             * This WILL NOT RESET THE SLIDES if the previous OpMode said that it was
             */
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
            // WHY ARE THERE TWO SEPARATE DIRECTION ENUMS?!?!
            clawLeft?.direction = Servo.Direction.FORWARD
            clawRight?.direction = Servo.Direction.FORWARD

            //            slideRight.direction =              REVERSE
        }
    }

    var targetHeight: Int = 0
        set(height) {
            if (slideLeft == null || slideRight == null) {
                field = 0
                return
            }

            // half the power for downwards movement
            val desiredPower = if (height < currentHeight) POWER_MAX * POWER_DOWNWARD_SCALE else POWER_MAX

            slideLeft. power = desiredPower
            slideRight.power = desiredPower
            // evaluated height
            field = height.coerceIn(HEIGHT_MIN..HEIGHT_MAX)
            slideLeft. targetPosition = field
            slideRight.targetPosition = -field
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
        get() = if (slideLeft != null && slideRight != null) ((slideLeft.currentPosition + -slideRight.currentPosition).toFloat() / 2f).roundToInt() else 0

    /**
     * Whether the left claw is open. The setter moves the servos.
     */
    var leftOpen: Boolean = true
        set(state) {
            field = state
            // TODO: make sure that you can't open the claws while the slide is moving
            clawLeft?.position = if (state) Claw.CLAW_LEFT_POS_OPEN else Claw.CLAW_LEFT_POS_CLOSED
        }
    /**
     * Whether the right claw is open. The setter moves the servos.
     */
    var rightOpen: Boolean = true
        set(state) {
            field = state
            // TODO: make sure that you can't open the claws while the slide is moving
            clawRight?.position = if (state) Claw.CLAW_RIGHT_POS_OPEN else Claw.CLAW_RIGHT_POS_CLOSED
        }

    override fun start() {
        slideLeft?.targetPosition = 0
        slideRight?.targetPosition = 0

        slideLeft?.mode  = RUN_TO_POSITION
        slideRight?.mode = RUN_TO_POSITION
    }

    override fun update() {
        // if our target & current heights are above the safe height for the arm, always extend the arm.
        if (targetHeight >= HEIGHT_ARM_SAFE && currentHeight >= HEIGHT_ARM_SAFE) {
            armRight?.position = ARM_DOWN_RIGHT
            armLeft?.position =  ARM_DOWN_LEFT
        } else {
            armRight?.position = ARM_UP_RIGHT
            armLeft?.position =  ARM_UP_LEFT
        }

        log("target height: $targetHeight")
        log("current avg. height: $currentHeight")
        log("slide L height: ${slideLeft?.currentPosition}")
        log("slide R height: ${slideRight?.currentPosition}")
        log("arm L pos: ${armLeft?.position}")
        log("arm R pos: ${armRight?.position}")
        log("slide L amps: ${slideLeft?.getCurrent(CurrentUnit.AMPS)}")
        log("slide R amps: ${slideRight?.getCurrent(CurrentUnit.AMPS)}")

//        if (slideLeft?.getCurrent(CurrentUnit.AMPS) >)
    }
}