package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
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
        const val SLIDE_HEIGHT_MAX = 1086
        const val SLIDE_HEIGHT_MIN = 0
    }

//    private val coefficients = PIDFCoefficients(
//        0.0,
//        0.0,
//        0.0,
//        0.0,
//        PIDF
//    )

    init {
        if (slideLeft == null || slideRight == null) {
            val missing = mutableSetOf<String>()
            if (slideLeft != null) missing.add("slideL")
            if (slideRight != null) missing.add("slideR")

            status = Status(StatusEnum.MISSING_HARDWARE, hardwareMissing = missing)
        } else {
            slideLeft?.targetPosition = 0
            slideRight?.targetPosition = 0
            slideLeft?.mode =   RUN_TO_POSITION
            slideRight?.mode =  RUN_TO_POSITION
        }
    }

    init {
//        slideLeft.targetPosition = 0
//        slideLeft.mode = RUN_TO_POSITION
//        slideLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        slideLeft.power = 0.0
//        slideLeft.power = 1.0

//        slide.targetPositionTolerance = 1
//        slide.setPIDFCoefficients(RUN_TO_POSITION, coefficients)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    private var useManual: Boolean = true

    var targetHeight: Int = 0
        set(height) {
            useManual = true

            val desiredPower = if (height < field) 0.5 else 1.0
            slideLeft?. power = desiredPower
            slideRight?.power = desiredPower
            slideLeft?. targetPosition = height.coerceIn(SLIDE_HEIGHT_MIN..SLIDE_HEIGHT_MAX)
            slideRight?.targetPosition = height.coerceIn(SLIDE_HEIGHT_MIN..SLIDE_HEIGHT_MAX)
            field = height
        }
//
//    fun addHeight(offset: Double): Unit {
////        TODO()
//    }
//
//    fun setRow(row: Int): Unit {
//        useManual = false
//        TODO("automatically set the slide to be aligned with a row (0 to 10, -1 for retracted)")
//    }
}