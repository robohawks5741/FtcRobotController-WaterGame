package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.teamcode.botmodule.Claw
import org.firstinspires.ftc.teamcode.botmodule.Drive
import org.firstinspires.ftc.teamcode.botmodule.Intake
import org.firstinspires.ftc.teamcode.botmodule.LSD
import org.firstinspires.ftc.teamcode.botmodule.March
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive

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

//    @JvmField val march               = camera?.    let {   March(opMode, it)   }
//    @JvmField val lsd                 = if (motorSlideLeft  != null && motorSlideRight != null) LSD(opMode, motorSlideLeft, motorSlideRight    )   else null
//    @JvmField val claw                = if (servoClawLeft   != null && servoClawRight  != null) Claw(opMode, servoClawLeft, servoClawRight     )   else null
//    @JvmField val intake              = if (motorIntakeLift != null || motorIntakeSpin != null) Intake(opMode, motorIntakeLift, motorIntakeSpin)   else null
//    @JvmField var drive               = Drive(this)
//    class SoftwareMap {
//
//        @JvmField val motorSlideLeft:   DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "liftLeft"  ] }
//        @JvmField val motorSlideRight:  DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "liftRight" ] }
//        @JvmField val motorIntakeSpin:  DcMotorEx?  =
//        @JvmField val motorIntakeLift:  DcMotorEx?  =
//        @JvmField val motorTrussPull:   DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "hang"      ] }
//        @JvmField val servoTrussLeft:   Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "trussl"    ] }
//        @JvmField val servoTrussRight:  Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "trussr"    ] }
//        @JvmField val servoArmLeft:     Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "armLeft"   ] }
//        @JvmField val servoArmRight:    Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "armRight"  ] }
//
//    }
    @JvmField var rr: MecanumDrive? = null

    init {
        // IMU orientation/calibration
        val logo = LogoFacingDirection.RIGHT
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

    /**
     * Should be called every update.
     * Place any reusable update functions here (i.e. for MecanumDrive)
     */
    fun update() {
        rr?.updatePoseEstimate()
    }

    companion object {
        /**
         * RoadRunner Pose Storage
         */
        @JvmStatic var storedPose: Pose2d = Pose2d(0.0, 0.0, 0.0)

    }

}