package org.firstinspires.ftc.teamcode.test

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.botmodule.BotModule.StatusEnum.OK
import kotlin.math.roundToInt

@TeleOp
class SlideCalibrator : OpMode() {
    private lateinit var slideLeft: DcMotorEx
    private lateinit var slideRight: DcMotorEx

    override fun init() {

        slideLeft = hardwareMap[DcMotorEx::class.java, "slideL"]
        slideRight = hardwareMap[DcMotorEx::class.java, "slideR"]

        slideLeft.targetPosition = 0
        slideRight.targetPosition = 0
        slideLeft.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideRight.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideLeft.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideRight.mode = DcMotor.RunMode.RUN_TO_POSITION

        slideLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        slideRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        // Directions
        slideLeft.direction = DcMotorSimple.Direction.FORWARD
        slideRight.direction = DcMotorSimple.Direction.REVERSE
    }

    private var targetHeightLeft: Float = 0f
    private var targetHeightRight: Float = 0f

    override fun loop() {
        targetHeightLeft = (targetHeightLeft - gamepad1.left_stick_y).coerceAtLeast(0f)
        targetHeightRight = (targetHeightRight - gamepad1.right_stick_y).coerceAtLeast(0f)

        if (gamepad1.a) {
            slideLeft.power = 1.0
            slideRight.power = 1.0

            slideLeft.targetPosition = targetHeightLeft.roundToInt().coerceAtLeast(0)
            slideRight.targetPosition = targetHeightRight.roundToInt().coerceAtLeast(0)

            slideLeft.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideRight.mode = DcMotor.RunMode.RUN_TO_POSITION

            slideLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            slideRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        } else {
            slideLeft.power = 0.0
            slideRight.power = 0.0

            slideLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            slideRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        }
        telemetry.addData("Target Height L (var)", targetHeightLeft)
        telemetry.addData("Target Height R (var)", targetHeightRight)
        telemetry.addData("Target Height L", slideLeft.targetPosition)
        telemetry.addData("Target Height R", slideRight.targetPosition)
        telemetry.addData("Actual Height L", slideLeft.currentPosition)
        telemetry.addData("Actual Height R", slideRight.currentPosition)

        telemetry.update()
    }
}