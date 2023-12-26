package org.firstinspires.ftc.teamcode.botmodule

import com.acmerobotics.roadrunner.MecanumKinematics
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.PoseVelocity2dDual
import com.acmerobotics.roadrunner.Time
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorSimple
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.InputDataAnalog
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.Action
import org.firstinspires.ftc.teamcode.BotShared
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Drive(shared: BotShared, isTeleOp: Boolean) : BotModule(shared, isTeleOp) {
    private var useBotRelative = true

    override fun modInit() {
        // Drive motor directions **(DO NOT CHANGE THESE!!!)**
        shared.motorRightFront.direction = DcMotorSimple.Direction.FORWARD
        shared.motorLeftFront. direction = DcMotorSimple.Direction.REVERSE
        shared.motorRightBack. direction = DcMotorSimple.Direction.FORWARD
        shared.motorLeftBack.  direction = DcMotorSimple.Direction.REVERSE

        if (isTeleOp) {
            gamepadyn.players[0].getEventDigital(Action.TOGGLE_DRIVER_RELATIVITY)!!.addListener { if (it.digitalData) useBotRelative = !useBotRelative }
        }
    }

    override fun modStart() {}

    override fun modUpdate() {
        TODO("Not yet implemented")
    }

    private fun updateTeleOp(gamepadyn: Gamepadyn<Action>) {
        //        val drive = shared.drive!!

        // counter-clockwise
        val gyroYaw = shared.imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)

        val movement = gamepadyn.players[0].getState(Action.MOVEMENT) as InputDataAnalog
        val rotation = gamepadyn.players[0].getState(Action.ROTATION) as InputDataAnalog

        // +X = forward
        // +Y = left
        val inputVector = Vector2d(
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

        shared.motorLeftFront.power = wheelVels.leftFront[0] / powerModifier
        shared.motorLeftBack.power = wheelVels.leftBack[0] / powerModifier
        shared.motorRightBack.power = wheelVels.rightBack[0] / powerModifier
        shared.motorRightFront.power = wheelVels.rightFront[0] / powerModifier

//        Actions.run

        shared.opMode.telemetry.addLine("Gyro Yaw: " + shared.imu.robotYawPitchRollAngles.getYaw(AngleUnit.DEGREES))
        shared.opMode.telemetry.addLine("Input Yaw: " + if (inputVector.x > 0.05 && inputVector.y > 0.05) inputTheta * 180.0 / PI else 0.0)
//        telemetry.addLine("Yaw Difference (bot - input): " + )
    }

    override fun bindTeleOp(gamepadyn: Gamepadyn<Action>) {
        gamepadyn.players[0].getEventAnalog(Action.MOVEMENT)!!.addListener { updateTeleOp(gamepadyn) }
        gamepadyn.players[0].getEventAnalog(Action.ROTATION)!!.addListener { updateTeleOp(gamepadyn) }
    }


}