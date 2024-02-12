package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.ftc.runBlocking
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo

@Autonomous(name = "# Clay Red Right")
class AutoRedRight : LinearOpMode() {
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

    var placementZone: SpikeMark = SpikeMark.LEFT
    var teamAlliance = Alliance.RED
    var elementSpikeMark = SpikeMark.RIGHT

    protected lateinit var drive: MecanumDrive

    open val beginPose = Pose2d(0.0, 0.0, 0.0)
    protected var liftPos = 0

    override fun runOpMode() {
        drive = MecanumDrive(hardwareMap, beginPose)

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

        val autoSub = AutoSubsystem(this)
        autoSub.setAlliance(teamAlliance)

        /*   for (i in 0..100) {
               Thread.sleep(20)
               elementSpikeMark = autoSub.elementDetection()
               telemetry.addData("getMaxDistance", autoSub.pipeline.getMaxDistance())
               if (isStopRequested){
                   return
               }
           }*/

        while (!isStarted && !isStopRequested){
            elementSpikeMark = autoSub.elementDetection()
            autoSub.setAlliance(teamAlliance)
            telemetry.addLine("Select Alliance (Gamepad1 X = Blue, Gamepad1 B = Red)")
            telemetry.addData("Current Alliance Selected", teamAlliance.toString())
            telemetry.addData("Spike mark", autoSub.spikeMark == SpikeMark.LEFT)

            if (autoSub.spikeMark == SpikeMark.LEFT){
                placementZone = SpikeMark.LEFT
            } else if (autoSub.spikeMark == SpikeMark.CENTER){
                placementZone = SpikeMark.CENTER
            } else {
                placementZone = SpikeMark.RIGHT
            }
            telemetry.addData("Spike mark", elementSpikeMark)

            telemetry.update()
        }




        runBlocking(when (placementZone) {
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(21.34, 9.51), Math.toRadians(46.81))
                .splineToConstantHeading(Vector2d(3.75, -10.95), Math.toRadians(46.81))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(25.25, -32.85 + 2.5), Math.toRadians(90.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(24.33, 5.11), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(6.45, -1.47), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(27.16, -32.85), Math.toRadians(90.0))
                .build()
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(18.29, 0.823), Math.toRadians(-6.47))
                .splineToConstantHeading(Vector2d(5.49, 4.21), Math.toRadians(0.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(21.63, -32.85), Math.toRadians(90.0))
                .build()

        })
        liftPos = 600
        slideR.targetPosition = -liftPos
        slideL.targetPosition = liftPos
        slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideR.power = 1.0
        slideL.power = 1.0
        sleep(300)
        armL.position = 0.65
        armR.position = 0.35
        sleep(300)
        clawL.position = 0.0
        clawR.position = 0.36
        sleep(300)
        runBlocking(
            drive.actionBuilder(drive.pose)
                .lineToY(-23.8)
                .build()
        )
        armR.position = 0.05
        armL.position = 0.95
        sleep(100)
        liftPos = 0
        slideR.targetPosition = -liftPos
        slideL.targetPosition = liftPos
        slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideR.power = 1.0
        slideL.power = 1.0
        inlift.position = 0.0
        sleep(200)
        runBlocking(
            drive.actionBuilder(drive.pose)
                .strafeToConstantHeading(Vector2d(3.93, -29.9))
                .splineToConstantHeading(Vector2d(3.93, -45.99), Math.toRadians(90.0))
                .build()
        )
    }
}
