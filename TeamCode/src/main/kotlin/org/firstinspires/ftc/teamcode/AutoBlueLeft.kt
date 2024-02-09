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
    var placementZone: SpikeMark = SpikeMark.RIGHT

    override val beginPose = Pose2d(0.0, 0.0, 0.0)
    override val alliance: Alliance = Alliance.BLUE
    override val side: AllianceSide = AllianceSide.BACKDROP_SIDE

    override fun runTaskA() {
        runBlocking(when (placementZone) {
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(20.49, 7.18), Math.toRadians(37.30))
                .splineToConstantHeading(Vector2d(16.307, -0.1312), Math.toRadians(270.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(19.36, 32.8), Math.toRadians(270.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(26.386, 0.0), 0.0)
                .splineToConstantHeading(Vector2d(23.386, 0.0), 0.0)
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(25.682, 30.831), Math.toRadians(270.0))
                .build()
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
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
