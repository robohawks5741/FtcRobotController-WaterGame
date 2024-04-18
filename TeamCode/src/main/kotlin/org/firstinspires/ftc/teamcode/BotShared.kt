package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode

class BotShared(val opMode: OpMode) {
    companion object {
        var wasLastOpModeAutonomous: Boolean = false
    }
}