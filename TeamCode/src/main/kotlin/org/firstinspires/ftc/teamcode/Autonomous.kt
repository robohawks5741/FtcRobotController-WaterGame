package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.MecanumKinematics
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.PoseVelocity2dDual
import com.acmerobotics.roadrunner.Time
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.util.Range
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import java.lang.Thread.sleep

@Autonomous(name = "# Auto (Blue Left)", group = "# Sub-Mode")
class AutoBlueLeft : AutoSuper( Alliance.BLUE,    AllianceSide.LEFT,    Pose2d(0.0, 0.0, 0.0))
@Autonomous(name = "# Auto (Blue Right)", group = "# Sub-Mode")
class AutoBlueRight : AutoSuper(Alliance.BLUE,    AllianceSide.RIGHT,   Pose2d(0.0, 0.0, 0.0))
@Autonomous(name = "# Auto (Red Left)", group = "# Sub-Mode")
class AutoRedLeft : AutoSuper(  Alliance.RED,     AllianceSide.LEFT,    Pose2d(0.0, 0.0, 0.0))
@Autonomous(name = "# Auto (Red Right)", group = "# Sub-Mode")
class AutoRedRight : AutoSuper( Alliance.RED,     AllianceSide.RIGHT,   Pose2d(0.0, 0.0, 0.0))

//@Disabled
// TODO: can replace super constructor with open and override
open class AutoSuper(
    private val alliance: Alliance? = null,
    private val side: AllianceSide? = null,
    private val initialPose: Pose2d
) : OpMode() {
    enum class Alliance {
        RED,
        BLUE
    }
    enum class AllianceSide {
        LEFT,
        RIGHT
    }

    private lateinit var shared: BotShared

    override fun init() {
        shared = BotShared(this)
//        shared.drive = MecanumDrive(hardwareMap, initialPose)
    }

    var targetDetection: AprilTagDetection? = null

    override fun loop() {
        shared.update()

        val detection = targetDetection
        if (detection != null) {
            // Determine heading, range and Yaw (tag image rotation) error so we can use them to control the robot automatically.
            val rangeError: Double = detection.ftcPose.range - /*DESIRED_DISTANCE*/ 12.0
            val headingError: Double = detection.ftcPose.bearing
            val yawError: Double = detection.ftcPose.yaw


            //  Set the GAIN constants to control the relationship between the measured position error, and how much power is
            //  applied to the drive motors to correct the error.
            //  Drive = Error * Gain    Make these values smaller for smoother control, or larger for a more aggressive response.
            val SPEED_GAIN = 0.02 //  Forward Speed Control "Gain". eg: Ramp up to 50% power at a 25 inch error.   (0.50 / 25.0)
            val STRAFE_GAIN = 0.015 //  Strafe Speed Control "Gain".  eg: Ramp up to 25% power at a 25 degree Yaw error.   (0.25 / 25.0)
            val TURN_GAIN = 0.01 //  Turn Control "Gain".  eg: Ramp up to 25% power at a 25 degree error. (0.25 / 25.0)


            val MAX_AUTO_SPEED = 0.5 //  Clip the approach speed to this max value (adjust for your robot)
            val MAX_AUTO_STRAFE = 0.5 //  Clip the approach speed to this max value (adjust for your robot)
            val MAX_AUTO_TURN = 0.3 //  Clip the turn speed to this max value (adjust for your robot)

            // Use the speed and turn "gains" to calculate how we want the robot to move.
            val fwDist: Double = Range.clip(rangeError * SPEED_GAIN, -MAX_AUTO_SPEED, MAX_AUTO_SPEED)
            val turn: Double = Range.clip(headingError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN)
            val hzDist: Double = Range.clip(-yawError * STRAFE_GAIN, -MAX_AUTO_STRAFE, MAX_AUTO_STRAFE)

            val pv = PoseVelocity2d(
                Vector2d(
                    fwDist,
                    hzDist
                ),
                turn
            )

            val wheelVels = MecanumKinematics(1.0).inverse<Time>(PoseVelocity2dDual.constant(pv, 1));

            shared.motorFrontLeft.power = wheelVels.leftFront[0]
            shared.motorBackLeft.power = wheelVels.leftBack[0]
            shared.motorBackRight.power = wheelVels.rightBack[0]
            shared.motorFrontRight.power = wheelVels.rightFront[0]
        }
    }

    override fun start() {
        // Game Plan:
        // - Place a purple pixel on the spike mark
        // - Do other stuff (we're working on it!)

        // alias using JVM references
//        var drive = shared.drive!!
        val march = shared.march!!

        while (march.detections.isEmpty()) try {
            sleep(20)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        targetDetection = march.detections[0]

//        for (detection in march.detections) {
//
//        }

        /*
////        val lsd = shared.lsd!!
//
////        while (march.detections.isEmpty()) sleep(march.aprilTag.perTagAvgPoseSolveTime.toLong())
////        val detectionPose = march.detections[0].ftcPose
////        val a = drive.pose.position + ((drive.pose.heading + detectionPose.yaw) * Vector2d(detectionPose.x, detectionPose.y))
////        val testAction = drive.actionBuilder(drive.pose).splineToConstantHeading(a, 0.0).build()
//        val testAction = drive.actionBuilder(drive.pose).splineToConstantHeading(drive.pose.position + Vector2d(10.0, 0.0), 0.0).build()
//        val packet = TelemetryPacket()
//        var res = testAction.run(packet)
//        res = testAction.run(packet)
////        drive.FollowTrajectoryAction(TimeTrajectory())
//
////        drive.FollowTrajectoryAction()
//        // +X = forward, +Y = left
////        drive.setDrivePowers(
////            PoseVelocity2d(
////                Vector2d(
////                    1.0,
////                    0.0
////                ),
////                0.0
////            )
////        )
////        sleep(1000)
////        drive.setDrivePowers(
////            PoseVelocity2d(
////                Vector2d(
////                    0.0,
////                    0.0
////                ),
////                0.0
////            )
////        )
////        sleep(250)
////        drive.setDrivePowers(
////            PoseVelocity2d(
////                Vector2d(
////                    0.0,
////                    0.0
////                ),
////                1.0
////            )
////        )
////        sleep(500)
////        drive.setDrivePowers(
////            PoseVelocity2d(
////                Vector2d(
////                    0.0,
////                    0.0
////                ),
////                0.0
////            )
////        )
//
//
////        drive.actionBuilder(drive.pose)
//
////        sleep(5000)
////        lsd.setHeight(LSD.SlideHeight.TOP.height)
////        march
////        TODO("Not yet implemented")
         */
    }

    override fun stop() {
//        BotShared.storedPose = shared.drive?.pose!!
    }

}