package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.teamcode.botmodule.BotModule
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
class BotShared(opMode: OpMode, isTeleOp: Boolean, gamepadyn: Gamepadyn<Action>? = null) {
    // NOTE: i wish the hardware would stop changing so that I could keep the code stable for 5 minutes
    //       i also wish the bot existed

    // Get stuff from the hardware map (HardwareMap.get() can be HardwareMap[] in kt)
    val hardwareMap = opMode.hardwareMap!!
    @JvmField val imu:              IMU         =           hardwareMap[IMU          ::class.java,   "imu"       ]
    @JvmField val motorFrontRight:  DcMotorEx   =           hardwareMap[DcMotorEx    ::class.java,   "fr"        ]
    @JvmField val motorFrontLeft:   DcMotorEx   =           hardwareMap[DcMotorEx    ::class.java,   "fl"        ]
    @JvmField val motorBackRight:   DcMotorEx   =           hardwareMap[DcMotorEx    ::class.java,   "br"        ]
    @JvmField val motorBackLeft:    DcMotorEx   =           hardwareMap[DcMotorEx    ::class.java,   "bl"        ]
    @JvmField val camera:           WebcamName? =   idc {   hardwareMap[WebcamName   ::class.java,   "Webcam 1"  ] }
    @JvmField val motorSlideLeft:   DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "liftLeft"  ] }
    @JvmField val motorSlideRight:  DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "liftRight" ] }
    @JvmField val motorIntakeSpin:  DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "inspin"    ] }
    @JvmField val motorIntakeLift:  DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "inlift"    ] }
    @JvmField val motorTrussPull:   DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "hang"      ] }
    @JvmField val servoTrussLeft:   Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "trussl"    ] }
    @JvmField val servoTrussRight:  Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "trussr"    ] }
    @JvmField val servoArmLeft:     Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "armLeft"   ] }
//    @JvmField val servoArmRight:         Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "armRight"  ] }
    @JvmField val servoClawLeft:    Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "clawl"     ] }
    @JvmField val servoClawRight:   Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "clawr"     ] }

    @JvmField val march                 = camera?.    let {   March(it, opMode, isTeleOp)   }
    @JvmField val lsd                   = if (motorSlideLeft  != null && motorSlideRight != null)   LSD(motorSlideLeft, motorSlideRight, opMode, isTeleOp, gamepadyn)       else null
    @JvmField val claw                  = if (servoClawLeft   != null && servoClawRight  != null)   Claw(servoClawLeft, servoClawRight, opMode, isTeleOp, gamepadyn)        else null
    @JvmField val intake                = if (motorIntakeLift != null || motorIntakeSpin != null)   Intake(motorIntakeLift, motorIntakeSpin, opMode, isTeleOp, gamepadyn)   else null
    @JvmField var drive: Drive = Drive(
        motorFrontRight,
        motorFrontLeft,
        motorBackRight,
        motorBackLeft,
        imu,
        opMode, isTeleOp, gamepadyn
    )

    @JvmField val modules: Set<BotModule?> = setOf(march, lsd, claw, intake)
    @JvmField var rr: MecanumDrive? = null

    /**
     * Should be called once on start.
     */
    fun start() {
        for (module in modules) module?.modStart()
    }

    /**
     * Should be called every update.
     */
    fun update() {
//        drive?.updatePoseEstimate()
        for (module in modules) module?.modUpdate()
    }

    init {
        // IMU orientation/calibration
        val logo = LogoFacingDirection.RIGHT
        val usb = UsbFacingDirection.FORWARD
        val orientationOnRobot = RevHubOrientationOnRobot(logo, usb)
        imu.initialize(IMU.Parameters(orientationOnRobot))
        imu.resetYaw()

        // Drive motor directions **(DO NOT CHANGE THESE!!!)**
        motorFrontRight.    direction =         FORWARD
        motorFrontLeft.     direction =         REVERSE
        motorBackRight.     direction =         FORWARD
        motorBackLeft.      direction =         REVERSE

        // Directions
        motorIntakeSpin?.   direction =         REVERSE
        motorSlideLeft?.    direction =         REVERSE
        motorSlideRight?.   direction =         REVERSE
        // Modes
        motorTrussPull?.    mode =              RUN_WITHOUT_ENCODER
        motorIntakeSpin?.   mode =              RUN_WITHOUT_ENCODER
        // Zero-power behavior
        motorFrontLeft.     zeroPowerBehavior = BRAKE
        motorBackLeft.      zeroPowerBehavior = BRAKE
        motorFrontRight.    zeroPowerBehavior = BRAKE
        motorBackRight.     zeroPowerBehavior = BRAKE
        motorSlideLeft?.    zeroPowerBehavior = BRAKE
        motorSlideRight?.   zeroPowerBehavior = BRAKE
    }

    companion object {
        /**
         * RoadRunner Pose Storage
         */
        @JvmStatic var storedPose: Pose2d = Pose2d(0.0, 0.0, 0.0)

    }

}