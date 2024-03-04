package org.firstinspires.ftc.teamcode

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.openftc.easyopencv.OpenCvPipeline
import kotlin.math.pow
import kotlin.math.sqrt

class TeamElementPipeline : OpenCvPipeline() {
    private var elementColor: List<Int> = mutableListOf(0, 0, 255) //(red, green, blue)

    // TODO: see if this needs to be atomic
    var toggleShow = true
    lateinit var zone1: Mat
    lateinit var zone2: Mat
    lateinit var zone3: Mat
    @JvmField
    var maxDistance = Double.NaN

    var elementSpikeMark = SpikeMark.RIGHT

    override fun init(mat: Mat) {
        //Defining Zones
        //Rect(top left x, top left y, bottom right x, bottom right y)
        zone1 = mat.submat(Rect(0, 0, 639, 600))
        zone2 = mat.submat(Rect(641, 0, 639, 600))
        zone3 = mat.submat(Rect(1281, 0, 639, 600))
    }

    override fun processFrame(input: Mat): Mat {

        //Creating duplicate of original frame with no edits
        val original = input.clone()

//        input = input.submat(new Rect(0));
//        Imgproc.cvtColor(input, grey, Imgproc.COLOR_RGBALab);

        // TODO: new algorithm:
        //      - convert to HSV and posterize
        //      - median instead of mode?

        //Averaging the colors in the zones
        val avgColor1 = Core.mean(zone1)
        val avgColor2 = Core.mean(zone2)
        val avgColor3 = Core.mean(zone3)

        //Putting averaged colors on zones (we can see on camera now)
        zone1.setTo(avgColor1)
        zone2.setTo(avgColor2)
        zone3.setTo(avgColor3)

        val distance1 = colorDistance(avgColor1, elementColor)
        val distance2 = colorDistance(avgColor2, elementColor)
        val distance3 = colorDistance(avgColor3, elementColor)
        maxDistance = maxOf(distance1, distance2, distance3)

        elementSpikeMark = when (maxDistance) {
            distance1 -> SpikeMark.LEFT
            distance2 -> SpikeMark.CENTER
            distance3 -> SpikeMark.RIGHT
            else -> SpikeMark.CENTER
        }

        // Allowing for the showing of the averages on the stream
        return if (toggleShow) input else original
    }

    private fun colorDistance(color1: Scalar, color2: List<Int>): Double {
        val r1 = color1.`val`[0]
        val g1 = color1.`val`[1]
        val b1 = color1.`val`[2]
        val r2 = color2[0]
        val g2 = color2[1]
        val b2 = color2[2]

//        deltaL = L1 - L2
//        C1 = √(a1² + b1²)
//        C2 = √(a2² + b2²)
//        ΔC = C1 - C2
//        Δa = a1 - a2
//        Δb = b1 - b2
//        ΔH = √(Δa² + Δb² - ΔC²)
//        ΔEOK = √(ΔL² + ΔC² + ΔH²)
        return sqrt(
            (r1 - r2).pow(2.0) +
            (g1 - g2).pow(2.0) +
            (b1 - b2).pow(2.0)
        )
    }

    fun setAlliancePipe(alliance: Alliance) = when (alliance) {
        Alliance.RED -> elementColor = mutableListOf(255, 0, 0)
        Alliance.BLUE -> elementColor = mutableListOf(0, 0, 255)
    }

    fun toggleShowAverageZone() {
        toggleShow = !toggleShow
    }
}