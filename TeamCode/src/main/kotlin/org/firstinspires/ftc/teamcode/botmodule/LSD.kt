package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
import org.firstinspires.ftc.teamcode.idc

/**
 * Linear Slide Driver
 */
class LSD(cfg: ModuleConfig) : BotModule(cfg) {

    private val slideLeft:  DcMotorEx?  = idc { hardwareMap[DcMotorEx   ::class.java,   "slideL"     ] }
    private val slideRight: DcMotorEx?  = idc { hardwareMap[DcMotorEx   ::class.java,   "slideR"     ] }

    companion object {
        /**
         * The maximum position of the slide, in encoder ticks.
         */
        const val HEIGHT_MAX = 1550
        const val HEIGHT_MIN = 0
        const val HEIGHT_CLAW_SAFE = 500
        var POWER_MAX = 1.0
        var POWER_DOWNWARD_SCALE = 0.75
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
            slideLeft.mode =   STOP_AND_RESET_ENCODER
            slideRight.mode =  STOP_AND_RESET_ENCODER
            slideLeft.mode =   RUN_TO_POSITION
            slideRight.mode =  RUN_TO_POSITION

            slideLeft.zeroPowerBehavior =       BRAKE
            slideRight.zeroPowerBehavior =      BRAKE

            // Directions
            slideLeft.direction =               FORWARD
            slideLeft.direction =               FORWARD
//            slideRight.direction =              REVERSE
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var targetHeight: Int = 0
        set(height) {
            // half the power for downwards movement
            val desiredPower = if (height < field) (POWER_MAX * POWER_DOWNWARD_SCALE) else POWER_MAX
            slideLeft?. power = desiredPower
            slideRight?.power = desiredPower
            // evaluated height
            val evaluatedHeight = height.coerceIn(HEIGHT_MIN..HEIGHT_MAX)
            slideLeft?. targetPosition = evaluatedHeight
            slideRight?.targetPosition = -evaluatedHeight

            field = evaluatedHeight
        }

    val currentHeight: Float
        get() = if (status.status == StatusEnum.OK) (slideLeft!!.currentPosition + slideRight!!.currentPosition).toFloat() / 2f else Float.NaN

    override fun modStart() {
        slideLeft?.targetPosition = 0
        slideRight?.targetPosition = 0

        slideLeft?.mode  = RUN_TO_POSITION
        slideRight?.mode = RUN_TO_POSITION

    }

//    override fun modStartTeleOp() {
//        if (gamepadyn == null) {
//            return
//        }
//    }

    override fun modUpdate() {
        telemetry.addData("LSD target height:", targetHeight)
    }
}