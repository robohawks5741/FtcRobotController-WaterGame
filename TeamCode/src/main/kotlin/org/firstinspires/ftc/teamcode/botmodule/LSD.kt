package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataDigital
import org.firstinspires.ftc.teamcode.ActionAnalog1
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionDigital
import org.firstinspires.ftc.teamcode.ActionDigital.*
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
        const val POWER_MAX = 0.1
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
            slideRight.direction =              REVERSE
        }
    }

//        slideLeft.targetPosition = 0
//        slideLeft.mode = RUN_TO_POSITION
//        slideLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        slideLeft.power = 0.0
//        slideLeft.power = 1.0
//
//        slide.targetPositionTolerance = 1
//        slide.setPIDFCoefficients(RUN_TO_POSITION, coefficients)


    @Suppress("MemberVisibilityCanBePrivate")
    private var useManual: Boolean = true

    var targetHeight: Int = 0
        set(height) {
            useManual = true

            // half the power for downwards movement
            val desiredPower = if (height < field) (POWER_MAX / 2.0) else POWER_MAX
            slideLeft?. power = desiredPower
            slideRight?.power = desiredPower
            val evh = height.coerceIn(SLIDE_HEIGHT_MIN..SLIDE_HEIGHT_MAX)
            slideLeft?. targetPosition = evh
            slideRight?.targetPosition = evh

            field = height
        }

    override fun modStart() {
        slideLeft?.targetPosition = 0
        slideRight?.targetPosition = 0

        slideLeft?.mode  = RUN_TO_POSITION
        slideRight?.mode = RUN_TO_POSITION

        if (isTeleOp) {
            if (gamepadyn == null) {
                return
            }
            val moveUp: (InputDataDigital) -> Unit = {
                if (it()) targetHeight += 100
            }
            val moveDown: (InputDataDigital) -> Unit = {
                if (it()) targetHeight -= 100
            }

            val p0 = gamepadyn.players[0]
            val p1 = gamepadyn.players[1]

            p0.getEvent(PIXEL_MOVE_UP,      moveUp)
            p1.getEvent(PIXEL_MOVE_UP,      moveUp)
            p0.getEvent(PIXEL_MOVE_DOWN,    moveDown)
            p1.getEvent(PIXEL_MOVE_DOWN,    moveDown)
        }
    }

    override fun modUpdate() {


//        targetHeight += 10 * ((if (opMode.gamepad1.left_trigger > 0.5) 1 else 0) + (if (opMode.gamepad1.right_trigger > 0.5) -1 else 0))
//
//        if (isTeleOp) {
//            if (gamepadyn == null) {
//                telemetry.addLine("(LSD) TeleOp was enabled but Gamepadyn was null!")
//                return
//            }
//            val player = gamepadyn.players[1]
//            slideLeft?. mode = RUN_USING_ENCODER
//            slideRight?.mode = RUN_USING_ENCODER
//            val p = player.getState(SLIDE_MANUAL).x.toDouble().coerceAtMost(1.0) * POWER_MAX
//            slideLeft?. power = p
//            slideRight?.power = p
//        }

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