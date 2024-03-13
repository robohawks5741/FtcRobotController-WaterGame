package org.firstinspires.ftc.teamcode

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvPipeline
import kotlin.math.pow
import kotlin.math.sqrt


class CvTeamElementPipeline : OpenCvPipeline() {
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
        zone1 = mat
        zone2 = mat
        zone3 = mat
    }

    override fun processFrame(input: Mat): Mat {

        //Creating duplicate of original frame with no edits
        val hsvMat = Mat()
        hsvMat.create(input.size(), input.type())
        Imgproc.cvtColor(input, hsvMat, Imgproc.COLOR_RGB2HSV);
        val channels = ArrayList<Mat>()
        Core.split(hsvMat, channels)
        val hs = channels[0].mul(channels[1])


//            .submat(Rect(0, 0, 639, 600))
//            .submat(Rect(641, 0, 639, 600))
//            .submat(Rect(1281, 0, 639, 600))

        val planes = arrayListOf<Mat>(hs)

        // TODO: new algorithm:
        //      - convert to HSV and posterize
        //      - mode instead of mean? (i mixed them up T_T)
        //      - weighted average

        //Averaging the colors in the zones
//        val zone1Cols = Core.()
        val hsHist = Mat()
        val histRange = MatOfFloat(0f, 256f)
        Imgproc.calcHist(
            planes,
            MatOfInt(0),
            Mat(),
            hsHist,
            MatOfInt(256),
            histRange,
            true
        )

        // the mode
        val mml = Core.minMaxLoc(hsHist)

//        val avgColor1 = Core.mean(zone1)
//        val avgColor2 = Core.mean(zone2)
//        val avgColor3 = Core.mean(zone3)

        //Putting averaged colors on zones (we can see on camera now)
//        zone1.setTo(avgColor1)
//        zone2.setTo(avgColor2)
//        zone3.setTo(avgColor3)
//
//        val distance1 = colorDistance(avgColor1, elementColor)
//        val distance2 = colorDistance(avgColor2, elementColor)
//        val distance3 = colorDistance(avgColor3, elementColor)
//        maxDistance = maxOf(distance1, distance2, distance3)
//
//        elementSpikeMark = when (maxDistance) {
//            distance1 -> SpikeMark.LEFT
//            distance2 -> SpikeMark.CENTER
//            distance3 -> SpikeMark.RIGHT
//            else -> SpikeMark.CENTER
//        }

        // Allowing for the showing of the averages on the stream
        return if (toggleShow) hsHist else input
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