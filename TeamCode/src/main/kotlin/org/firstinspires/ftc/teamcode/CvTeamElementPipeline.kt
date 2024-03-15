package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
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


class CvTeamElementPipeline(private val opMode: OpMode) : OpenCvPipeline() {
    // * 360 / 256 = H 0-256 to to H 0-360
    private var elementHue = 120

    // TODO: see if this needs to be atomic
    var toggleShow = true
    lateinit var zone1: Mat
    lateinit var zone2: Mat
    lateinit var zone3: Mat

    var elementSpikeMark = SpikeMark.RIGHT

    override fun init(mat: Mat) {
        //Defining Zones
        //Rect(top left x, top left y, bottom right x, bottom right y)
        zone1 = mat
        zone2 = mat
        zone3 = mat
    }

    override fun processFrame(input: Mat): Mat {

        // Creating duplicate of original frame with no edits
        val hsvMat = Mat()
        hsvMat.create(input.size(), input.type())
        Imgproc.cvtColor(input, hsvMat, Imgproc.COLOR_RGB2HSV);
        val channels = ArrayList<Mat>()
        Core.split(hsvMat, channels)
        val hueInput = channels[0]//.mul(channels[1])

        val rectangles = arrayOf(
            Rect(0, 0, 639, 600),
            Rect(641, 0, 639, 600),
            Rect(1281, 0, 639, 600)
        )
        val dxs = arrayOf(Double.NaN, Double.NaN, Double.NaN)
        val subMatrices = ArrayList<Mat>()
        var i = 0
        while (i < 3) {
            val submat = hueInput.submat(rectangles[i])

            val planes = arrayListOf(hueInput)

            // TODO: new algorithm:
            //      - convert to HSV and posterize
            //      - most frequent instead of average
            //      - weighted?

            // create histogram
            val hsHist = Mat()
            // range of values
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

            // the mode (max value of a histogram is the most frequent)
            val mml = Core.minMaxLoc(hsHist)

            opMode.telemetry.addLine("(CV PIPELINE INJECT) maxVal: ${mml.maxVal} maxLoc: ${mml.maxLoc}")

            dxs[i] = mml.maxLoc.x

            //Putting averaged colors on zones (we can see on camera now)
//        zone1.setTo(avgColor1)
//        zone2.setTo(avgColor2)
//        zone3.setTo(avgColor3)

            subMatrices.add(submat)

            i++
        }

        val targetHue = elementHue * 256f / 360f
//        dxs
        // TODO: set this based on the distance to the desired hue
//        elementSpikeMark

        // Allowing for the showing of the averages on the stream
        // just show the first one of them since I'm still testing this
        return if (toggleShow) subMatrices[0] else hueInput
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
        Alliance.RED -> elementHue = 355 // approx. red on a 0-360 color picker
        Alliance.BLUE -> elementHue = 220 // approx. blue on a 0-360 color picker
    }

    fun toggleShowAverageZone() {
        toggleShow = !toggleShow
    }
}