package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.IMU
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName

// TODO: should this be a superclass? our codebase has grown so much, and we might want to reconsider
/**
 * Shared data.
 * Construct during the init phase. Contains HardwareMap definitions, as well as some other classes like the PixelPlacer and MecanumDrive.
 */
@Suppress("MemberVisibilityCanBePrivate", "RedundantSuppression")
class BotShared(opMode: OpMode) {
    // NOTE: i wish the hardware would stop changing so that I could keep the code stable for 5 minutes
    //       i also wish the bot existed

    // Get stuff from the hardware map (HardwareMap.get() can be HardwareMap[] in kt)
    val hardwareMap = opMode.hardwareMap!!

    @JvmField val imu:              IMU         =           hardwareMap[IMU          ::class.java,   "imu"       ]
    @JvmField val camera:           WebcamName? =   idc {   hardwareMap[WebcamName   ::class.java,   "Webcam 1"  ] }
    @JvmField var rr: MecanumDrive? = null

    init {
        // IMU orientation/calibration
        val logo = LogoFacingDirection.LEFT
        val usb = UsbFacingDirection.FORWARD
        val orientationOnRobot = RevHubOrientationOnRobot(logo, usb)
        imu.initialize(IMU.Parameters(orientationOnRobot))
        imu.resetYaw()

//        // Directions
//        motorIntakeSpin?.   direction =         REVERSE
//        motorSlideLeft?.    direction =         REVERSE
//        motorSlideRight?.   direction =         REVERSE
//        // Modes
//        motorTrussPull?.    mode =              RUN_WITHOUT_ENCODER
//        motorIntakeSpin?.   mode =              RUN_WITHOUT_ENCODER
//
//        motorSlideLeft?.    zeroPowerBehavior = BRAKE
//        motorSlideRight?.   zeroPowerBehavior = BRAKE
    }

    companion object {
        /**
         * RoadRunner Pose Storage
         */
        @JvmStatic var storedPose: Pose2d = Pose2d(0.0, 0.0, 0.0)

    }

}