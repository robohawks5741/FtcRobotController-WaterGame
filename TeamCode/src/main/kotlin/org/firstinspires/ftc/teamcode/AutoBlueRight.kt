package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.ftc.runBlocking
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.hardware.DcMotor

@Autonomous(name = "# Clay Blue Right")
class AutoBlueRight : AutoSuper() {
    override val alliance = Alliance.BLUE
    override val side = AllianceSide.AUDIENCE_SIDE

    override fun runSpecialized() {
        runBlocking(when (placementZone) {
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(20.32, 9.60), Math.toRadians(44.51))
                .splineToConstantHeading(Vector2d(2.98, -7.4), Math.toRadians(44.51))
                .turnTo(Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(50.4, -10.67), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(49.41, 73.98), Math.toRadians(90.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(19.88, 88.94), Math.toRadians(270.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(18.39, -0.21), 0.0)
                .splineToConstantHeading(Vector2d(4.96, -20.0), 0.0)
                .splineToConstantHeading(Vector2d(49.75, -18.46), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(50.0, 74.94), Math.toRadians(90.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(25.42, 90.69), Math.toRadians(270.0))
                .build()
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(16.61, 0.66), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(3.72, -18.46), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(50.21, -16.11), Math.toRadians(0.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(49.55, 73.64), Math.toRadians(90.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(30.44, 88.65), Math.toRadians(270.0))
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
