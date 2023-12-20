package org.firstinspires.ftc.teamcode.botmodule

import com.acmerobotics.roadrunner.MecanumKinematics
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.PoseVelocity2dDual
import com.acmerobotics.roadrunner.Time
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.IMU
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.Action
import org.firstinspires.ftc.teamcode.Action.MOVEMENT
import org.firstinspires.ftc.teamcode.Action.ROTATION
import org.firstinspires.ftc.teamcode.Action.TOGGLE_DRIVER_RELATIVITY
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Drive(
    private val frontRight: DcMotorEx,
    private val frontLeft: DcMotorEx,
    private val backRight: DcMotorEx,
    private val backLeft: DcMotorEx,
    private val imu: IMU,
    opMode: OpMode, isTeleOp: Boolean, gamepadyn: Gamepadyn<Action>?
) : BotModule(opMode, isTeleOp, gamepadyn) {

    @Suppress("MemberVisibilityCanBePrivate")
    var useBotRelative = true

    init {
        if (isTeleOp && gamepadyn != null) {
            // toggle driver-relative controls
            gamepadyn.players[0].getEventDigital(TOGGLE_DRIVER_RELATIVITY)!!.addListener { if (it.digitalData) useBotRelative = !useBotRelative }
        }
    }

    override fun modUpdate() {
        if (!isTeleOp || gamepadyn == null) return

        // counter-clockwise
        val gyroYaw = imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)

        val p0 = gamepadyn.players[0]
        val movement = p0.getStateAnalog(MOVEMENT)!!
        val rotation = p0.getStateAnalog(ROTATION)!!

        // +X = forward
        // +Y = left
        val inputVector = Vector2d(
            // up
            movement.analogData[1]!!.toDouble(),
            movement.analogData[0]!!.toDouble()
        )

        // angle of the stick
        val inputTheta = atan2(inputVector.y, inputVector.x)
        // evaluated theta
        val driveTheta = inputTheta - gyroYaw // + PI
        // magnitude of inputVector clamped to [0, 1]
        val inputPower = clamp(
            sqrt(
            (inputVector.x * inputVector.x) +
                    (inputVector.y * inputVector.y)
        ), 0.0, 1.0)

        val driveRelativeX = cos(driveTheta) * inputPower
        val driveRelativeY = sin(driveTheta) * inputPower

        // \frac{1}{1+\sqrt{2\left(1-\frac{\operatorname{abs}\left(\operatorname{mod}\left(a,90\right)-45\right)}{45}\right)\ }}
//        val powerModifier = 1.0 / (1.0 + sqrt(2.0 * (1.0 - abs((gyroYaw % (PI / 2)) - (PI / 4)) / (PI / 4))))

        val powerModifier = 1.0
        val pv = PoseVelocity2d(
            if (useBotRelative) Vector2d(
                driveRelativeX,
                driveRelativeY
            ) else inputVector,
            rotation.analogData[0]!!.toDouble()
        )
        // +X = forward, +Y = left
//        drive.setDrivePowers(pv)
        val wheelVels = MecanumKinematics(1.0).inverse<Time>(PoseVelocity2dDual.constant(pv, 1));

        frontLeft.power     = wheelVels.leftFront[0]    / powerModifier
        backLeft.power      = wheelVels.leftBack[0]     / powerModifier
        backRight.power     = wheelVels.rightBack[0]    / powerModifier
        frontRight.power    = wheelVels.rightFront[0]   / powerModifier

//        Actions.run

        opMode.telemetry.addLine("Gyro Yaw: " + imu.robotYawPitchRollAngles.getYaw(AngleUnit.DEGREES))
        opMode.telemetry.addLine("Input Yaw: " + if (inputVector.x > 0.05 && inputVector.y > 0.05) inputTheta * 180.0 / PI else 0.0)
//        telemetry.addLine("Yaw Difference (bot - input): " + )
    }
}