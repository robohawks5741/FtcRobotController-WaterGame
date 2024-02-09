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

//TODO FIX LEFT SIDE
@Autonomous(name = "# Clay Red Right")
class AutoRedRight : AutoSuper() {
    //Start 0,-7.18, 0
    var placementZone: SpikeMark = SpikeMark.LEFT
    override val alliance: Alliance = Alliance.BLUE
    override val side: AllianceSide = AllianceSide.BACKDROP_SIDE

    override fun runTaskA() {
        runBlocking(when (placementZone) {
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(21.34, 9.51), Math.toRadians(46.81))
                .splineToConstantHeading(Vector2d(3.75, -10.95), Math.toRadians(46.81))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(32.52, -33.01), Math.toRadians(90.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(24.33, 5.11), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(6.45, -1.47), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(26.56, -31.05), Math.toRadians(90.0))
                .build()
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(18.29, 0.823), Math.toRadians(-6.47))
                .splineToConstantHeading(Vector2d(5.49, 4.21), Math.toRadians(0.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(20.95, -33.32), Math.toRadians(90.0))
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
                .strafeToConstantHeading(Vector2d(48.511, -23.11))
                .splineToConstantHeading(Vector2d(48.511, -45.44), Math.toRadians(90.0))
                .build()
        )
    }
}
