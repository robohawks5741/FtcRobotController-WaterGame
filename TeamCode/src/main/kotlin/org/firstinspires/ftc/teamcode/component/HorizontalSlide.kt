package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.MainDriverControl

class HorizontalSlide(manager: ComponentManager) : Component(manager) {
    private val hSlideRight: DcMotorEx?     = getHardware("hslideRight")
    private val hSlideLeft: DcMotorEx?      = getHardware("hslideLeft")

    override val status: Status

    override fun start() {
        if (hSlideLeft != null && hSlideRight != null) {
//            hSlideLeft.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//            hSlideRight.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

            hSlideLeft.power = 1.0
            hSlideRight.power = 1.0

            hSlideLeft.direction = DcMotorSimple.Direction.FORWARD
            hSlideRight.direction = DcMotorSimple.Direction.REVERSE

            hSlideLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            hSlideRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

            hSlideLeft.targetPositionTolerance = 8
            hSlideRight.targetPositionTolerance = 8

            hSlideRight.targetPosition = 0
            hSlideLeft.targetPosition = 0

            hSlideLeft.mode = DcMotor.RunMode.RUN_TO_POSITION
            hSlideRight.mode = DcMotor.RunMode.RUN_TO_POSITION

        }
    }

    override fun loop() {
        if (status.functionality != Functionality.NONE) {
            telemetry.addLine("H. Slide Left Current Pos: ${hSlideLeft?.currentPosition}")
            telemetry.addLine("H. Slide Left Target Pos: ${hSlideLeft?.targetPosition}")
            telemetry.addLine("H. Slide Right Current Pos: ${hSlideRight?.currentPosition}")
            telemetry.addLine("H. Slide Right Target Pos: ${hSlideRight?.targetPosition}")
        }
    }

    fun retract() { position = 0 }

    fun extend() { position = H_SLIDE_MAX }

    var position: Int = 0
        set(targetPos) {
            field = targetPos.coerceIn(0..H_SLIDE_MAX)
            hSlideLeft?.targetPosition = field
            hSlideRight?.targetPosition = field
        }

    init {
        val functionality = when {
            hSlideLeft == null || hSlideRight == null -> Functionality.NONE
            else -> Functionality.FULL
        }
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("hslideRight", Servo::class, hSlideRight != null),
            HardwareUsage("hslideLeft", Servo::class, hSlideLeft != null)
        )
        status = Status(
            functionality,
            hardwareSet
        )
    }

    companion object {
        const val H_SLIDE_MAX: Int = 288 * 4
    }
}