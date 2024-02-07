package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE
import computer.living.gamepadyn.InputDataDigital
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.idc

/**
 * Linear Slide Driver
 */
class LSD(cfg: ModuleConfig) : BotModule(cfg) {

    private val slideLeft:  DcMotorEx?  = idc { hardwareMap[DcMotorEx   ::class.java,   "slideL"     ] }
    private val slideRight: DcMotorEx?  = idc { hardwareMap[DcMotorEx   ::class.java,   "slideR"     ] }

    companion object {
        /**
         * The maximum position of the slide, in encoder ticks.
         */
        var SLIDE_HEIGHT_MAX = 1565
        var SLIDE_HEIGHT_MIN = 0
        var POWER_MAX = 1.0
        var POWER_DOWNWARD_SCALE = 0.75
    }

    enum class SlideStop(@JvmField public val height: Int) {
        BOTTOM(SLIDE_HEIGHT_MIN),
        TOP(SLIDE_HEIGHT_MAX)
    }

    init {
        if (slideLeft == null || slideRight == null) {
            val missing = mutableSetOf<String>()
            if (slideLeft == null) missing.add("slideL")
            if (slideRight == null) missing.add("slideR")

            status = Status(StatusEnum.MISSING_HARDWARE, hardwareMissing = missing)
        } else {
            slideLeft.targetPosition = 0
            slideRight.targetPosition = 0
            slideLeft.mode =   STOP_AND_RESET_ENCODER
            slideRight.mode =  STOP_AND_RESET_ENCODER
            slideLeft.mode =   RUN_TO_POSITION
            slideRight.mode =  RUN_TO_POSITION

            slideLeft.zeroPowerBehavior =       BRAKE
            slideRight.zeroPowerBehavior =      BRAKE

            // Directions
            slideLeft.direction =               FORWARD
            slideRight.direction =              REVERSE
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var targetHeight: Int = 0
        set(height) {
            // half the power for downwards movement
            val desiredPower = if (height < field) (POWER_MAX * POWER_DOWNWARD_SCALE) else POWER_MAX
            slideLeft?. power = desiredPower
            slideRight?.power = desiredPower
            // evaluated height
            val evaluatedHeight = height.coerceIn(SLIDE_HEIGHT_MIN..SLIDE_HEIGHT_MAX)
            slideLeft?. targetPosition = evaluatedHeight
            slideRight?.targetPosition = evaluatedHeight

            field = evaluatedHeight
        }

    override fun modStart() {
        slideLeft?.targetPosition = 0
        slideRight?.targetPosition = 0

        slideLeft?.mode  = RUN_TO_POSITION
        slideRight?.mode = RUN_TO_POSITION

        if (isTeleOp) {
            if (gamepadyn == null) {
                return
            }
            val moveUp: (InputDataDigital) -> Unit = {
                if (it()) targetHeight += 200
            }
            val moveDown: (InputDataDigital) -> Unit = {
                if (it()) targetHeight -= 200
            }

            val p0 = gamepadyn.players[0]
            val p1 = gamepadyn.players[1]

            p0.getEvent(PIXEL_MOVE_UP,      moveUp)
            p1.getEvent(PIXEL_MOVE_UP,      moveUp)
            p0.getEvent(PIXEL_MOVE_DOWN,    moveDown)
            p1.getEvent(PIXEL_MOVE_DOWN,    moveDown)
        }
    }

    override fun modUpdate() {
        telemetry.addData("LSD target height:", targetHeight)
    }
}