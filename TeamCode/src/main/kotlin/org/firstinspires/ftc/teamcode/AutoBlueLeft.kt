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

@Autonomous(name = "# Clay Blue Left")
class AutoBlueLeft : AutoSuper() {
    var placementZone: SpikeMark = SpikeMark.LEFT
    override val alliance: Alliance = Alliance.BLUE
    override val side: AllianceSide = AllianceSide.BACKDROP_SIDE

    override fun runTaskA() {
        runBlocking(when (placementZone) {
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(19.75, 2.07), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(4.29, -18.15), Math.toRadians(0.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(20.18, 32.56), Math.toRadians(270.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(25.22, -3.60), 0.0)
                .splineToConstantHeading(Vector2d(23.386, 0.0), 0.0)
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(25.682, 30.831), Math.toRadians(270.0))
                .build()
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(22.56, -8.72), Math.toRadians(-40.26))
                .splineToConstantHeading(Vector2d(11.85, 5.24), Math.toRadians(-40.26))
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
                .lineToY(29.0)
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
                .strafeToConstantHeading(Vector2d(1.81, 34.19))
                .splineToConstantHeading(Vector2d(1.38, 45.79), Math.toRadians(270.0))
                .build()
        )
    }
}
