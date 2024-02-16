package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar
import com.qualcomm.robotcore.hardware.IMU
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName

// TODO: should this be a superclass? our codebase has grown so much, and we might want to reconsider
/**
 * Shared data.
 * Construct during the init phase. Contains HardwareMap definitions, as well as some other classes like the PixelPlacer and MecanumDrive.
 */
@Suppress("MemberVisibilityCanBePrivate", "RedundantSuppression")
class BotShared(opMode: OpMode) {
    val hardwareMap = opMode.hardwareMap!!

    /**
     * The control hub's internal Inertial Measurement Unit (IMU).
     */
    @JvmField val imu:      IMU         = hardwareMap[IMU::class.java, "imu" ]
    @JvmField val camera:   WebcamName? = hardwareMap.tryGet(WebcamName::class.java, "Webcam 1")
    @JvmField var rr:       MecanumDrive? = null

    init {
        // IMU orientation/calibration
        imu.initialize(IMU.Parameters(orientationOnRobot))
//        imu.resetYaw()
    }

    companion object {
        /**
         * RoadRunner Pose Storage
         */
        @JvmField var storedPose: Pose2d = Pose2d(0.0, 0.0, 0.0)
        @JvmField var wasLastOpModeAutonomous: Boolean = false
        @JvmField val orientationOnRobot = RevHubOrientationOnRobot(LogoFacingDirection.LEFT, UsbFacingDirection.FORWARD)
    }

}