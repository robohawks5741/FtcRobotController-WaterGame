package org.firstinspires.ftc.teamcode.botmodule

import com.acmerobotics.roadrunner.DualNum
import com.acmerobotics.roadrunner.MecanumKinematics
import com.acmerobotics.roadrunner.MecanumKinematics.WheelVelocities
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.PoseVelocity2dDual
import com.acmerobotics.roadrunner.Time
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.BotShared
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Drive(config: ModuleConfig) : BotModule(config) {
    @JvmField val motorRightFront: DcMotorEx    = hardwareMap[DcMotorEx::class.java,  "fr"    ]
    @JvmField val motorLeftFront: DcMotorEx     = hardwareMap[DcMotorEx::class.java,  "fl"    ]
    @JvmField val motorRightBack: DcMotorEx     = hardwareMap[DcMotorEx::class.java,  "br"    ]
    @JvmField val motorLeftBack: DcMotorEx      = hardwareMap[DcMotorEx::class.java,  "bl"    ]

    private var status: Status = Status(StatusEnum.OK)

    private var wheelVels: WheelVelocities<Time> = WheelVelocities(
        DualNum.constant(0.0, 0),
        DualNum.constant(0.0, 0),
        DualNum.constant(0.0, 0),
        DualNum.constant(0.0, 0)
    )
    var powerModifier = 1.0
    private var useBotRelative = true
    override fun getStatus() = status

    override fun modInit() {
        // Drive motor directions **(DO NOT CHANGE THESE!!!)**
        motorRightFront.direction = DcMotorSimple.Direction.FORWARD
        motorLeftFront. direction = DcMotorSimple.Direction.REVERSE
        motorRightBack. direction = DcMotorSimple.Direction.FORWARD
        motorLeftBack.  direction = DcMotorSimple.Direction.REVERSE

        // Zero-power behavior
        motorLeftFront.     zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        motorLeftBack.      zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        motorRightFront.    zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        motorRightBack.     zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        if (isTeleOp) {
            if (gamepadyn == null) {
                telemetry.addLine("(Drive Module) TeleOp was enabled but Gamepadyn was null!")
                return
            }
            gamepadyn.players[0].getEvent(TOGGLE_DRIVER_RELATIVITY)!! { if (it()) useBotRelative = !useBotRelative }
        }
    }

    override fun modUpdate() {
        if (isTeleOp) {
            updateTeleOp()
            motorLeftFront.power = wheelVels.leftFront[0] / powerModifier
            motorLeftBack.power = wheelVels.leftBack[0] / powerModifier
            motorRightBack.power = wheelVels.rightBack[0] / powerModifier
            motorRightFront.power = wheelVels.rightFront[0] / powerModifier
        }

    }

    private fun updateTeleOp() {

        if (gamepadyn == null) {
            telemetry.addLine("(Drive Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }
        //        val drive = shared.drive!!

        // counter-clockwise
        val gyroYaw = imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)

        val movement = gamepadyn.players[0].getState(MOVEMENT)!!
        val rotation = gamepadyn.players[0].getState(ROTATION)!!

        // +X = forward
        // +Y = left
        val inputVector = Vector2d(
            movement.y.toDouble(),
            movement.x.toDouble()
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
//        powerModifier = 1.0 / (1.0 + sqrt(2.0 * (1.0 - abs((gyroYaw % (PI / 2)) - (PI / 4)) / (PI / 4))))

        val pv = PoseVelocity2d(
            if (useBotRelative) Vector2d(
                driveRelativeX,
                driveRelativeY
            ) else inputVector,
            rotation.x.toDouble()
        )
        // +X = forward, +Y = left
//        drive.setDrivePowers(pv)
        wheelVels = MecanumKinematics(1.0).inverse(PoseVelocity2dDual.constant(pv, 1))

//        Actions.run

        telemetry.addLine("Gyro Yaw: " + shared.imu.robotYawPitchRollAngles.getYaw(AngleUnit.DEGREES))
        telemetry.addLine("Input Yaw: " + if (inputVector.x > 0.05 && inputVector.y > 0.05) inputTheta * 180.0 / PI else 0.0)
//        telemetry.addLine("Yaw Difference (bot - input): " + )
    }

}