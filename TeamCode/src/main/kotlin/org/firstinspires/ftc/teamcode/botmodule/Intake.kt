package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo

/**
 * Intake controls
 */
@Suppress("unused")
class Intake(opMode: OpMode, private val servoLift: Servo?, private val motorSpin: DcMotorEx?) : BotModule(opMode) {

    // pos
    // 1    - pixel off ground
    // 1-5  - pixel off stack
    // 6    - starting pos within 18 inches
    var intakePos = 0
        set(pos) {
            field = pos
            when(pos){
                1 -> servoLift?.position = 1.0 //These numbers are placeholders and will be changed later
                2 -> servoLift?.position = 1.0
                3 -> servoLift?.position = 1.0
                4 -> servoLift?.position = 1.0
                5 -> servoLift?.position = 1.0
                6 -> servoLift?.position = 1.0 // This should be all the way up
                else -> return
            }
        }


    // 0.0 is off, 1.0 is inwards, -1.0 is outwards

    var spinPower: Double = 0.0
        set(power) {
            field = when {
                (power > 1.0) -> 1.0
                (power < -1.0) -> -1.0
                else -> power
            }
            motorSpin?.power = field
        }
}
