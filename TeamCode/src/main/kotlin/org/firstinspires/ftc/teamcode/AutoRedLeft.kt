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
    var placementZone: SpikeMark = SpikeMark.LEFT
    override val alliance: Alliance = Alliance.BLUE
    override val side: AllianceSide = AllianceSide.BACKDROP_SIDE

    override fun runTaskA() {
        runBlocking(when (placementZone) {
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(21.87, -7.97), Math.toRadians(-39.75))
                .splineToConstantHeading(Vector2d(5.28, 8.41), Math.toRadians(-39.75))
                .turnTo(Math.toRadians(0.0))
                .splineTo(Vector2d(49.74, 12.76), Math.toRadians(0.0))
                .turnTo(Math.toRadians(-90.0))
                .splineToConstantHeading(Vector2d(58.23, -70.92), Math.toRadians(-90.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(28.03, -83.24), Math.toRadians(90.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(24.0, -3.94), 0.0)
                .splineToConstantHeading(Vector2d(8.07, 17.29), 0.0)
                .splineToConstantHeading(Vector2d(54.83, 20.73), 0.0)
                .turnTo(Math.toRadians(-90.0))
                .splineToConstantHeading(Vector2d(60.88, -67.68), Math.toRadians(-90.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(38.62, -83.33), Math.toRadians(90.0))
                .build()
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(17.42, 3.7), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(12.88, -2.59), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(50.0, -3.09), Math.toRadians(0.0))
                .turnTo(Math.toRadians(-90.0))
                .splineToConstantHeading(Vector2d(55.16, -69.26), Math.toRadians(-90.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(39.35, -84.85), Math.toRadians(90.0))
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
                .lineToY(-74.34)
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
                .strafeToConstantHeading(Vector2d(60.0, -74.34))
                .splineToConstantHeading(Vector2d(60.0, -98.15), Math.toRadians(90.0))
                .build()
        )
    }
}
