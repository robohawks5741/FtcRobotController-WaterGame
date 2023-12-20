package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotorEx
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.Action

/**
 * Linear Slide Driver
 */
class LSD(
    private val slideLeft: DcMotorEx,
    private val slideRight: DcMotorEx,
    opMode: OpMode, isTeleOp: Boolean, gamepadyn: Gamepadyn<Action>?
) : BotModule(opMode, isTeleOp, gamepadyn) {

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
        slideLeft.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideRight.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
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
            slideLeft.power = desiredPower
            slideRight.power = desiredPower
            slideLeft.targetPosition = height.coerceIn(SLIDE_HEIGHT_MIN..SLIDE_HEIGHT_MAX)
            slideRight.targetPosition = height.coerceIn(SLIDE_HEIGHT_MIN..SLIDE_HEIGHT_MAX)
            field = height
        }

    override fun modStart() {
        slideLeft.targetPosition = 0
        slideRight.targetPosition = 0
        slideLeft.mode = RUN_TO_POSITION
        slideRight.mode = RUN_TO_POSITION
    }

    override fun modUpdate() {
        targetHeight += 10 * ((if (opMode.gamepad1.left_trigger > 0.5) 1 else 0) + (if (opMode.gamepad1.right_trigger > 0.5) -1 else 0))
//    //        shared.motorSlide!!.mode = RUN_WITHOUT_ENCODER
//    //        shared.motorSlide!!.power = (gamepad1.left_trigger - gamepad2.right_trigger).toDouble().coerceAtLeast(0.0).coerceAtMost(1.0)
//        shared.motorSlide!!.targetPosition = (shared.motorSlide!!.targetPosition + (10 * ((if (gamepad1.left_trigger > 0.5) 1 else 0) + (if (gamepad1.right_trigger > 0.5) -1 else 0)))).coerceAtLeast(0).coerceAtMost(1086)
//        shared.motorSlide!!.mode = RUN_TO_POSITION
//        shared.motorSlide!!.power = 1.0
//        telemetry.addLine(
//            """
//        |==================================================
//        |Slide target position: ${shared.motorSlide!!.targetPosition}
//        |Slide current position: ${shared.motorSlide!!.currentPosition}
//        |Slide mode: ${shared.motorSlide!!.mode}
//        |Slide ZPB: ${shared.motorSlide!!.zeroPowerBehavior}"
//        |Slide is enabled: ${shared.motorSlide!!.isMotorEnabled}"
//        |Slide power: ${shared.motorSlide!!.power}"
//        |Slide current: ${shared.motorSlide!!.getCurrent(CurrentUnit.AMPS)}"
//        |Slide velocity: ${shared.motorSlide!!.velocity}
//        |==================================================
//        """.trimMargin()
//        )
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