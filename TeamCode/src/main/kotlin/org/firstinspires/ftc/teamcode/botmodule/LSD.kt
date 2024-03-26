package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.ftccommon.SoundPlayer
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.FLOAT
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.R
import org.firstinspires.ftc.teamcode.botmodule.LSD.EmergencyStep.BACKPEDAL
import org.firstinspires.ftc.teamcode.botmodule.LSD.EmergencyStep.LIMP
import org.firstinspires.ftc.teamcode.botmodule.LSD.EmergencyStep.PANIC
import org.firstinspires.ftc.teamcode.search
import java.lang.Thread.sleep
import kotlin.math.max
import kotlin.math.min
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
class LSD(cfg: ModuleConfig) : BotModule(cfg) {

    private val slideLeft: DcMotorEx?                       = hardwareMap.search("slideL")
    private val slideRight: DcMotorEx?                      = hardwareMap.search("slideR")
    private val armLeft: Servo?                             = hardwareMap.search("armL")
    private val armRight: Servo?                            = hardwareMap.search("armR")

    private enum class EmergencyStep {
        BACKPEDAL,
        LIMP,
        PANIC
    }
    private var emergencyStep = BACKPEDAL
    private var lastEmergencyStartTime: Long = -2000
    private var lastEmergencyFlagTime: Long = -2000
    private val emergencyDuration: Long = 2000

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

        const val CURRENT_THRESHOLD = 18.0
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

    override fun modUpdate() {
        if (slideLeft == null || slideRight == null || armLeft == null || armRight == null) return

        // if our target & current heights are above the safe height for the arm, always extend the arm.
        if (targetHeight >= HEIGHT_ARM_SAFE && currentHeight >= HEIGHT_ARM_SAFE) {
            armRight.position = ARM_DOWN_RIGHT
            armLeft.position =  ARM_DOWN_LEFT
        } else {
            armRight.position = ARM_UP_RIGHT
            armLeft.position =  ARM_UP_LEFT
        }

        telemetry.addData("LSD target height:", targetHeight)
        telemetry.addData("LSD current avg. height", currentHeight)
        telemetry.addData("LSD slide L height", slideLeft.currentPosition)
        telemetry.addData("LSD slide R height", slideRight.currentPosition)
        telemetry.addData("LSD arm L pos", armLeft.position)
        telemetry.addData("LSD arm R pos", armRight.position)

        val leftAmps = slideLeft.getCurrent(CurrentUnit.AMPS)
        val rightAmps = slideRight.getCurrent(CurrentUnit.AMPS)
        telemetry.addData("LSD slide L amps", slideLeft.getCurrent(CurrentUnit.AMPS))
        telemetry.addData("LSD slide R amps", slideRight.getCurrent(CurrentUnit.AMPS))

        val minAmps = min(leftAmps, rightAmps)
        val maxAmps = max(leftAmps, rightAmps)

        if (maxAmps > CURRENT_THRESHOLD) {
            if (lastEmergencyFlagTime + emergencyDuration >= System.currentTimeMillis()) throw Exception("Repeatedly over max current, panicking")
            when (emergencyStep) {
                // step 1: try and move backwards
                BACKPEDAL -> {
                    // prime step 2
                    emergencyStep = LIMP

                    lastEmergencyStartTime = System.currentTimeMillis()
                    val movingUp = (targetHeight - currentHeight) >= 0
                    targetHeight = if (movingUp) HEIGHT_MIN else HEIGHT_MAX
                }
                // step 2: adjust the ZPB and stop power to the motor
                LIMP -> {
                    // prime step 3
                    emergencyStep = PANIC

                    slideLeft.zeroPowerBehavior = FLOAT
                    slideRight.zeroPowerBehavior = FLOAT
                    slideLeft.powerFloat
                    playPanic()
                }
                PANIC -> {
                    // reset to step 1
                    emergencyStep = BACKPEDAL

                    // trip the flag
                    lastEmergencyFlagTime = System.currentTimeMillis()
                }
            }
            sleep(1000)
        } else if (lastEmergencyStartTime + emergencyDuration >= System.currentTimeMillis()) {
            if (emergencyStep == LIMP || emergencyStep == PANIC) {
                slideLeft.zeroPowerBehavior = BRAKE
                slideRight.zeroPowerBehavior = BRAKE
            }
            // we're fine :3
            emergencyStep = BACKPEDAL
        }

    }

    private var isPlayingPanic = false

    private fun playPanic() {
        if (isPlayingPanic) return
        val ctx = hardwareMap.appContext
        val params = SoundPlayer.PlaySoundParams()
        params.loopControl = 0
        isPlayingPanic = true
        SoundPlayer.getInstance().startPlaying(ctx, R.raw.system_failure, params, null) {
            isPlayingPanic = false
        }
//        ctx.resources.getIdentifier("system_failure", "raw", ctx.packageName)
    }
}