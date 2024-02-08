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
    var placementZone: SpikeMark = SpikeMark.RIGHT
    override fun runTaskA() {
        runBlocking(when (placementZone) {
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(19.81, -6.57), Math.toRadians(-27.82))
                .splineToConstantHeading(Vector2d(3.83, 1.91), Math.toRadians(-27.82))
                .turnTo(Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(49.55, 6.43), Math.toRadians(0.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(49.55, 73.64), Math.toRadians(90.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(30.44, 88.65), Math.toRadians(270.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(26.50, -1.24), 0.0)
                .splineToConstantHeading(Vector2d(19.16, -9.99), 0.0)
                .splineToConstantHeading(Vector2d(52.87, -7.67), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(50.0, 74.94), Math.toRadians(90.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(25.42, 90.69), Math.toRadians(270.0))
                .build()
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(19.08, 6.84), Math.toRadians(15.69))
                .splineToConstantHeading(Vector2d(6.73, 0.3), Math.toRadians(15.69))
                .turnTo(Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(50.14, 2.13), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(49.41, 73.98), Math.toRadians(90.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(19.88, 88.94), Math.toRadians(270.0))
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
