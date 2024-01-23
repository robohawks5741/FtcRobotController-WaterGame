package org.firstinspires.ftc.teamcode.test

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.IMU
import org.firstinspires.ftc.ftccommon.internal.manualcontrol.parameters.ImuParameters
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES

@TeleOp
class GyroTest : OpMode() {
    private lateinit var imu: IMU
    override fun init() {
        imu = hardwareMap[IMU::class.java, "imu"]
    }

    override fun start() {

    }

    private var logo = RevHubOrientationOnRobot.LogoFacingDirection.UP
    private var usb = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD

    override fun loop() {

         logo = when {
            (-gamepad1.left_stick_y > 0.5)  -> RevHubOrientationOnRobot.LogoFacingDirection.FORWARD
            (-gamepad1.left_stick_y < -0.5) -> RevHubOrientationOnRobot.LogoFacingDirection.BACKWARD
            (-gamepad1.left_stick_x > 0.5)  -> RevHubOrientationOnRobot.LogoFacingDirection.LEFT
            (-gamepad1.left_stick_x < -0.5) -> RevHubOrientationOnRobot.LogoFacingDirection.RIGHT
            (gamepad1.left_stick_button)    -> RevHubOrientationOnRobot.LogoFacingDirection.UP
            (gamepad1.left_bumper)          -> RevHubOrientationOnRobot.LogoFacingDirection.DOWN
            else -> logo
        }

        usb = when {
            (-gamepad1.right_stick_y > 0.5)  -> RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
            (-gamepad1.right_stick_y < -0.5) -> RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
            (-gamepad1.right_stick_x > 0.5)  -> RevHubOrientationOnRobot.UsbFacingDirection.LEFT
            (-gamepad1.right_stick_x < -0.5) -> RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
            (gamepad1.right_stick_button)    -> RevHubOrientationOnRobot.UsbFacingDirection.UP
            (gamepad1.right_bumper)          -> RevHubOrientationOnRobot.UsbFacingDirection.DOWN
            else -> usb
        }

        if (gamepad1.a || gamepad1.circle) {
            imu.initialize(IMU.Parameters(RevHubOrientationOnRobot(logo, usb)))
            imu.resetYaw()
        }

        val yaw = imu.robotYawPitchRollAngles.getYaw(DEGREES)

        telemetry.addLine("LOGO DIRECTION: ${logo.name}")
        telemetry.addLine("USB DIRECTION: ${usb.name}")
        telemetry.addLine("YAW: $yaw deg")
        telemetry.update()
    }
}