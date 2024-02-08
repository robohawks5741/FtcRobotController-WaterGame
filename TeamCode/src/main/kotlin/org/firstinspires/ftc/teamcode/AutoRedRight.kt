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

@Autonomous(name = "# Clay Red Right")
class AutoRedRight : AutoSuper() {
    //Start 0,-7.18, 0
    var placementZone: SpikeMark = SpikeMark.RIGHT
    var xmult = 1;
    var ymult = -1;
    var dheading = -1;
    override fun runTaskA() {
        runBlocking(when (placementZone) {
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(20.49*xmult, 7.18*ymult), Math.toRadians(dheading*37.30))
                .splineToConstantHeading(Vector2d(16.307*xmult, -0.1312*ymult), Math.toRadians(dheading*270.0))
                .turnTo(Math.toRadians(dheading*270.0))
                .splineToConstantHeading(Vector2d(19.36*xmult, 32.8*ymult), Math.toRadians(dheading*270.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(26.386*xmult, 0.0), dheading-0.0)
                .splineToConstantHeading(Vector2d(23.386*xmult, 0.0*ymult), dheading*0.0)
                .turnTo(Math.toRadians(dheading*270.0))
                .splineToConstantHeading(Vector2d(25.682*xmult, 30.831*ymult), Math.toRadians(dheading*270.0))
                .build()
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(21.83*xmult, -5.68), Math.toRadians(dheading* -25.0))
                .splineToConstantHeading(Vector2d(25.96*xmult, 4.070*ymult), Math.toRadians(dheading*157.81))
                .turnTo(Math.toRadians(dheading*270.0))
                .splineToConstantHeading(Vector2d(31.3*xmult, 31.0*ymult), Math.toRadians(dheading*270.0))
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
                .lineToY(29.0*ymult)
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
                .strafeToConstantHeading(Vector2d(1.81*xmult, 34.19*ymult))
                .splineToConstantHeading(Vector2d(1.38*xmult, 45.79*ymult), Math.toRadians(dheading*270.0))
                .build()
        )
    }
}
