package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DistanceSensor
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo

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
    protected lateinit var distance: DistanceSensor
    protected var liftPos = 0
    abstract val alliance: Alliance
    abstract val side: AllianceSide

    override fun runOpMode() {
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
        distance =  hardwareMap[DistanceSensor::class.java, "distance"  ]
        imu =       hardwareMap[IMU::class.java,            "imu"       ]

        // claw
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
        waitForStart()
        runTaskA()
        runTaskB()
        runTaskC()
    }

    abstract fun runTaskA()
    open fun runTaskB() { }
    open fun runTaskC() { }
}