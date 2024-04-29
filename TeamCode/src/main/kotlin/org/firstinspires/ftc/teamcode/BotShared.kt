package org.firstinspires.ftc.teamcode

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.IMU

class BotShared(val opMode: OpMode) {

    val imu: IMU? = opMode.hardwareMap.search("imu")

    init {
        imu?.initialize(IMU.Parameters(orientationOnRobot))
        if (wasLastOpModeAutonomous) {
            addieLog("KEEPING IMU yaw (last OpMode was Autonomous)")
        } else {
            addieLog("RESETTING IMU yaw (last known OpMode was TeleOp)")
            imu?.resetYaw()
        }
    }

    companion object {
        var wasLastOpModeAutonomous: Boolean = false
        val orientationOnRobot = RevHubOrientationOnRobot(
            RevHubOrientationOnRobot.LogoFacingDirection.UP,
            RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
        )
    }
}