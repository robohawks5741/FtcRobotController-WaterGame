package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler
import org.firstinspires.ftc.teamcode.test.ModuleCheckTeleop
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import java.lang.Thread.sleep

@Autonomous(name = "# Auto (Blue Backdrop-Most)", group = "# Sub-Mode")
class AutoBlueBackdrop :    AddieAutoSuper(Alliance.BLUE,    AllianceSide.BACKDROP_SIDE, Pose2d(0.0, 0.0, 0.0))
@Autonomous(name = "# Auto (Blue Audience-Most)", group = "# Sub-Mode")
class AutoBlueAudience :    AddieAutoSuper(Alliance.BLUE,    AllianceSide.AUDIENCE_SIDE, Pose2d(0.0, 0.0, 0.0))
@Autonomous(name = "# Auto (Red Backdrop-Most)", group = "# Sub-Mode")
class AutoRedBackdrop :     AddieAutoSuper(Alliance.RED,     AllianceSide.BACKDROP_SIDE, Pose2d(0.0, 0.0, 0.0))
@Autonomous(name = "# Auto (Red Audience-Most)", group = "# Sub-Mode")
class AutoRedAudience :     AddieAutoSuper(Alliance.RED,     AllianceSide.AUDIENCE_SIDE, Pose2d(0.0, 0.0, 0.0))


//@Disabled
// TODO: can replace super constructor with open and override
open class AddieAutoSuper(
    private val alliance: Alliance? = null,
    private val side: AllianceSide? = null,
    private val initialPose: Pose2d
) : OpMode() {

    private lateinit var shared: BotShared
    @Suppress("LeakingThis")
    private val moduleHandler = ModuleHandler(this, shared, false, alliance, side, gamepadyn = null)

    var elementSpikeMark = SpikeMark.RIGHT
    var togglePreview = true

    override fun init() {
        shared = BotShared(this)
        shared.rr = MecanumDrive(hardwareMap, initialPose)

        moduleHandler.init()
    }

    private var targetDetection: AprilTagDetection? = null

    override fun loop() {
        moduleHandler.update()
    }

    override fun start() {
        moduleHandler.start()
        // Game Plan:
        // - Place a purple pixel on the spike mark
        // - Do other stuff (we're working on it!)

        // alias using JVM references
        var drive = shared.rr!!
    }

    override fun stop() {
        BotShared.storedPose = shared.rr?.pose!!
        moduleHandler.stop()
    }

    companion object {
        //  Set the GAIN constants to control the relationship between the measured position error, and how much power is
        //  applied to the drive motors to correct the error.
        //  Drive = Error * Gain    Make these values smaller for smoother control, or larger for a more aggressive response.
        @JvmStatic private val SPEED_GAIN = 0.02 //  Forward Speed Control "Gain". eg: Ramp up to 50% power at a 25 inch error.   (0.50 / 25.0)
        @JvmStatic private val STRAFE_GAIN = 0.015 //  Strafe Speed Control "Gain".  eg: Ramp up to 25% power at a 25 degree Yaw error.   (0.25 / 25.0)
        @JvmStatic private val TURN_GAIN = 0.01 //  Turn Control "Gain".  eg: Ramp up to 25% power at a 25 degree error. (0.25 / 25.0)

        @JvmStatic private val MAX_AUTO_SPEED = 0.5 //  Clip the approach speed to this max value (adjust for your robot)
        @JvmStatic private val MAX_AUTO_STRAFE = 0.5 //  Clip the approach speed to this max value (adjust for your robot)
        @JvmStatic private val MAX_AUTO_TURN = 0.3 //  Clip the turn speed to this max value (adjust for your robot)

    }
}