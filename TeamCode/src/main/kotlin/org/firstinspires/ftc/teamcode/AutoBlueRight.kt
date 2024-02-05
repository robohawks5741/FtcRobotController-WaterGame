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

@Autonomous(name = "# Clay Blue Right")
class AutoBlueRight : AutoSuper() {
    var placementZone: SpikeMark = SpikeMark.CENTER
    override fun runTaskA() {
        runBlocking(when (placementZone) {
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(20.12, -6.98), Math.toRadians(-31.68))
                .splineToConstantHeading(Vector2d(7.0, 3.0), Math.toRadians(31.68))
                .turnTo(Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(49.28, 4.45), Math.toRadians(0.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(50.11, 76.62), Math.toRadians(90.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(19.38, 87.4), Math.toRadians(270.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(26.32, 2.04), 0.0)
                .splineToConstantHeading(Vector2d(16.8, -7.89), 0.0)
                .splineToConstantHeading(Vector2d(48.94, -8.83), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(52.55, 77.19), Math.toRadians(90.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(28.21, 89.29), Math.toRadians(90.0))
                .build()
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(21.83, -5.68), Math.toRadians(-25.0))
                .splineToConstantHeading(Vector2d(25.96, 4.070), Math.toRadians(157.81))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(31.3, 31.0), Math.toRadians(270.0))
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
                .lineToY(83.0)
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
                .strafeToConstantHeading(Vector2d(50.0, 84.72))
                .splineToConstantHeading(Vector2d(50.0, 101.92), Math.toRadians(270.0))
                .build()
        )
    }
}
