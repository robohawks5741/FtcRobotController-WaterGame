package org.firstinspires.ftc.teamcode

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.IMU

/**
 * Various parts of the robot that shouldn't need to be re-instantiated everywhere they're used.
 * This isn't a singleton because of garbage collection worries; you create an instance of it when
 * you initialize your OpMode, and
 */
class BotShared(val opMode: OpMode) {

    /**
     * The Control Hub's built-in Inertial Measurement Unit, used to get
     */
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
        /**
         * Whether or not the last known OpMode was running as Autonomous.
         * This is useful for keeping the IMU orientation across OpMode boundaries.
         */
        @JvmStatic var wasLastOpModeAutonomous: Boolean = false

        /**
         * The "canonical" orientation of the control hub on the robot.
         */
        @JvmStatic val orientationOnRobot = RevHubOrientationOnRobot(
            RevHubOrientationOnRobot.LogoFacingDirection.UP,
            RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
        )
    }
}