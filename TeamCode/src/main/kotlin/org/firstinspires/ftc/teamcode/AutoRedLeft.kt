package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.ftc.runBlocking
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DistanceSensor
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo

@Autonomous(name = "# Clay Red Left")
class AutoRedLeft : AutoSuper() {
    //Start 0, 7.32, 0
    var placementZone: SpikeMark = SpikeMark.RIGHT
    var beginPose = Pose2d(0.0, 0.0, 0.0)
    override val alliance: Alliance = Alliance.BLUE
    override val side: AllianceSide = AllianceSide.BACKDROP_SIDE

    protected lateinit var drive: MecanumDrive

    var xmult = 1;
    var ymult = -1;
    var dheading = -1;
    override fun runTaskA() {
        drive = MecanumDrive(hardwareMap, beginPose)

        runBlocking(when (placementZone) {
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(18.29, 0.823), Math.toRadians(-6.47))
                .splineToConstantHeading(Vector2d(5.49, 4.21), Math.toRadians(0.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(20.95, -33.32), Math.toRadians(90.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(26.50*xmult, -1.24*ymult), dheading*0.0)
                .splineToConstantHeading(Vector2d(19.16*xmult, -9.99*ymult), dheading*0.0)
                .splineToConstantHeading(Vector2d(52.87*xmult, -7.67*ymult), dheading*0.0)
                .turnTo(Math.toRadians(dheading*90.0))
                .splineToConstantHeading(Vector2d(50.0*xmult, 74.94*ymult), Math.toRadians(dheading*90.0))
                .turnTo(Math.toRadians(dheading*270.0))
                .splineToConstantHeading(Vector2d(25.42*xmult, 90.69*ymult), Math.toRadians(dheading*270.0))
                .build()
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(19.08*xmult, 6.84*ymult), Math.toRadians(dheading*15.69))
                .splineToConstantHeading(Vector2d(6.73*xmult, 0.3*ymult), Math.toRadians(dheading*15.69))
                .turnTo(Math.toRadians(dheading*0.0))
                .splineToConstantHeading(Vector2d(50.14*xmult, 2.13*ymult), dheading*0.0)
                .turnTo(Math.toRadians(dheading* 90.0))
                .splineToConstantHeading(Vector2d(49.41*xmult, 73.98*ymult), Math.toRadians(dheading*90.0))
                .turnTo(Math.toRadians(dheading*270.0))
                .splineToConstantHeading(Vector2d(19.88*xmult, 88.94*ymult), Math.toRadians(dheading*270.0))
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
                .lineToY(-27.45)
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
                .strafeToConstantHeading(Vector2d(3.92, -27.45))
                .splineToConstantHeading(Vector2d(3.16, -45.42), Math.toRadians(90.0))
                .build()
        )
    }
}
