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

@Autonomous(name = "# ClayAutoBlueLeftTest")
class AutoBlueLeftTesting : AutoSuper() {
    var placementZone = 1
    override fun runTaskA() {
        runBlocking(
            drive.actionBuilder(beginPose)
                .splineTo(Vector2d(26.386, 0.0), 0.0)
                .splineToConstantHeading(Vector2d(23.386, 0.0), 0.0)
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(25.682, 30.831), Math.toRadians(270.0))
                .build()
        )
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
                .splineTo(Vector2d(53.80, 20.0), Math.toRadians(270.0))
                .turnTo(Math.toRadians(270.0))
                .build()
        )
        armR.position = 0.05
        armL.position = 0.95
        liftPos = 0
        slideR.targetPosition = -liftPos
        slideL.targetPosition = liftPos
        slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideR.power = 1.0
        slideL.power = 1.0
        runBlocking(
            drive.actionBuilder(drive.pose)
                .splineTo(Vector2d(53.80, -71.15), Math.toRadians(270.0))
                .build()
        )
    }
}
