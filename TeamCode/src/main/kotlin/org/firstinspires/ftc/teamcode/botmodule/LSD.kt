package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.MotorControlAlgorithm
import com.qualcomm.robotcore.hardware.PIDFCoefficients

/**
 * Linear Slide Driver
 */
class LSD(opMode: OpMode, private val slide: DcMotorEx) : BotModule(opMode) {

    companion object {
        /**
         * The maximum position of the slide, in encoder ticks.
         */
        const val SLIDE_HEIGHT_MAX = 1086
        const val SLIDE_HEIGHT_MIN = 0
    }

    private val coefficients = PIDFCoefficients(
        0.0,
        0.0,
        0.0,
        0.0,
        MotorControlAlgorithm.PIDF
    )

    init {
        slide.targetPosition = 0
        slide.mode = RUN_TO_POSITION
        slide.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        slide.power = 0.0
        slide.power = 1.0
//        slide.targetPositionTolerance = 1
        slide.setPIDFCoefficients(RUN_TO_POSITION, coefficients)
    }

    private var targetHeight: Double = 0.0
    // some constant, test later
    private val maxPosition: Int = 1

    @Suppress("MemberVisibilityCanBePrivate")
    var useManual: Boolean = true

    fun setHeight(pos: Double): Unit {
        useManual = true
        TODO()
//        targetHeight = pos
    }

    fun addHeight(offset: Double): Unit {
        TODO()
    }

    fun setRow(row: Int): Unit {
        useManual = false
        TODO("automatically set the slide to be aligned with a row (0 to 10, -1 for retracted)")
    }
}