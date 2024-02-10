package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.Opticon

abstract class AutoSuper : LinearOpMode() {
    protected lateinit var intake: DcMotorEx
    protected lateinit var slideR: DcMotorEx
    protected lateinit var slideL: DcMotorEx
    protected lateinit var trussL: Servo
    protected lateinit var trussR: Servo
    protected lateinit var armR: Servo
    protected lateinit var armL: Servo
    protected lateinit var clawR: Servo
    protected lateinit var clawL: Servo
    protected lateinit var drone: Servo
    protected lateinit var inlift: Servo
    protected lateinit var imu: IMU

    protected lateinit var shared: BotShared
    protected lateinit var opticon: Opticon

    protected lateinit var drive: MecanumDrive
    protected lateinit var autoSub: AutoSubsystem

    open val beginPose = Pose2d(0.0, 0.0, 0.0)
    protected var placementZone: SpikeMark = SpikeMark.RIGHT
    protected var liftPos = 0
    abstract val alliance: Alliance
    abstract val side: AllianceSide

    final override fun runOpMode() {
        intake =    hardwareMap[DcMotorEx::class.java,      "intake"    ]
        slideR =    hardwareMap[DcMotorEx::class.java,      "slideR"    ]
        slideL =    hardwareMap[DcMotorEx::class.java,      "slideL"    ]
        drone =     hardwareMap[Servo::class.java,          "drone"     ]
        trussR =    hardwareMap[Servo::class.java,          "trussR"    ]
        trussL =    hardwareMap[Servo::class.java,          "trussL"    ]
        armR =      hardwareMap[Servo::class.java,          "armR"      ]
        armL =      hardwareMap[Servo::class.java,          "armL"      ]
        clawR =     hardwareMap[Servo::class.java,          "clawR"     ]
        clawL =     hardwareMap[Servo::class.java,          "clawL"     ]
        inlift =    hardwareMap[Servo::class.java,          "inlift"    ]
        imu =       hardwareMap[IMU::class.java,            "imu"       ]

        clawR.position = 0.07
        clawL.position = 0.29
        inlift.position = 0.34

        // arm
        armR.position = 0.05
        armL.position = 0.95

        // mode settings
        slideR.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideR.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        slideL.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideL.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        autoSub = AutoSubsystem(this)
        shared = BotShared(this)
        opticon = Opticon(ModuleConfig(this, shared, false))
        drive = MecanumDrive(hardwareMap, beginPose)
        autoSub.setAlliance(alliance)

        /* for (i in 0..100) {
           Thread.sleep(20)
           elementSpikeMark = autoSub.elementDetection()
           telemetry.addData("getMaxDistance", autoSub.pipeline.getMaxDistance())
           if (isStopRequested){
               return
           }
        } */

        while (!isStarted && !isStopRequested){
            placementZone = autoSub.elementDetection()
            telemetry.addData("Current Alliance Selected", alliance.toString())
            telemetry.addData("Spike mark", autoSub.spikeMark.name)
            telemetry.update()
            Thread.yield()
        }

        placementZone = autoSub.spikeMark

        waitForStart()
        runSpecialized()
    }

    abstract fun runSpecialized()
}