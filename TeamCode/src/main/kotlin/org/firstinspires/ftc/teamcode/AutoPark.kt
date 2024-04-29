package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.abs
import kotlin.math.max

@Autonomous(name = "Drive forward for 2 seconds")
class AutoPark : LinearOpMode() {
    private val runtime = ElapsedTime()

    private lateinit var leftFrontDrive: DcMotor
    private lateinit var leftBackDrive: DcMotor
    private lateinit var rightFrontDrive: DcMotor
    private lateinit var rightBackDrive: DcMotor
    private lateinit var droneLaunch: Servo
    override fun runOpMode() {

        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        leftFrontDrive = hardwareMap.get(DcMotor::class.java, "left_front_drive")
        leftBackDrive = hardwareMap.get(DcMotor::class.java, "left_back_drive")
        rightFrontDrive = hardwareMap.get(DcMotor::class.java, "right_front_drive")
        rightBackDrive = hardwareMap.get(DcMotor::class.java, "right_back_drive")
        droneLaunch = hardwareMap.get(Servo::class.java, "drone")
        droneLaunch.position = 1.0

        leftFrontDrive.direction = DcMotorSimple.Direction.REVERSE
        leftBackDrive.direction = DcMotorSimple.Direction.FORWARD
        rightFrontDrive.direction = DcMotorSimple.Direction.FORWARD
        rightBackDrive.direction = DcMotorSimple.Direction.REVERSE

        // Wait for the game to start (driver presses PLAY)
        telemetry.addData("Status", "Initialized")
        telemetry.update()
        waitForStart()
        runtime.reset()

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            var max: Double

            // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
            val axial =
                if (runtime.seconds() < 4) 1.0 else 0.0 // Note: pushing stick forward gives negative value
            val lateral = 0.0
            val yaw = 0.0

            // Combine the joystick requests for each axis-motion to determine each wheel's power.
            // Set up a variable for each drive wheel to save the power level for telemetry.
            var leftFrontPower = axial + lateral + yaw
            var rightFrontPower = axial - lateral - yaw
            var leftBackPower = axial - lateral + yaw
            var rightBackPower = axial + lateral - yaw

            // Normalize the values so no wheel power exceeds 100%
            // This ensures that the robot maintains the desired motion.
            max = max(abs(leftFrontPower), abs(rightFrontPower))
            max = max(max, abs(leftBackPower))
            max = max(max, abs(rightBackPower))
            if (max > 1.0) {
                leftFrontPower /= max
                rightFrontPower /= max
                leftBackPower /= max
                rightBackPower /= max
            }

            // Send calculated power to wheels
            leftFrontDrive.power = leftFrontPower
            rightFrontDrive.power = rightFrontPower
            leftBackDrive.power = leftBackPower
            rightBackDrive.power = rightBackPower

            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: $runtime")
            telemetry.addData("Front left/Right", "%4.2f, %4.2f", leftFrontPower, rightFrontPower)
            telemetry.addData("Back  left/Right", "%4.2f, %4.2f", leftBackPower, rightBackPower)
            telemetry.update()
        }
    }
}
