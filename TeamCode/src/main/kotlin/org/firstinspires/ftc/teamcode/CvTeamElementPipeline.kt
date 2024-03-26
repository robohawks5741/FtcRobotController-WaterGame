package org.firstinspires.ftc.teamcode

import android.R.id
import android.util.Log
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.opencv.core.Core
import org.opencv.core.Core.MinMaxLocResult
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvPipeline


class CvTeamElementPipeline(private val opMode: OpMode) : OpenCvPipeline() {
    // * 360 / 256 = H 0-256 to to H 0-360
    private var elementHue = 120

    var showDefault = false
    private lateinit var zone1: Mat
    private lateinit var zone2: Mat
    private lateinit var zone3: Mat
    private val hueHist = Mat()
    private val histRange = MatOfFloat(0f, 256f)
    private val subMatrices = arrayOf(Mat(), Mat(), Mat())
    private val hueInput: Mat = Mat()
    private val mask = Mat()

    var maxPos0: Point = Point(0.0, 0.0)
    var maxPos1: Point = Point(0.0, 0.0)
    var maxPos2: Point = Point(0.0, 0.0)

    private val rectangles = arrayOf(
        Rect(0, 0, 640, 600),
        Rect(640, 0, 640, 600),
        Rect(1280, 0, 640, 600)
    )
    var elementSpikeMark = SpikeMark.RIGHT

    override fun init(mat: Mat) {
        //Defining Zones
        //Rect(top left x, top left y, bottom right x, bottom right y)
        zone1 = mat.submat(rectangles[0])
        zone2 = mat.submat(rectangles[1])
        zone3 = mat.submat(rectangles[2])
//        // Creating duplicate of original frame with no edits
//        hsvMat.create(input.size(), input.type())
    }

    override fun processFrame(input: Mat): Mat {
        if (showDefault) return input
        Log.i("CV TEAM ELEMENT PIPELINE", "processing frame")

        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2HSV)
        val channels = ArrayList<Mat>()
        Core.split(input, channels)
        channels[0].copyTo(hueInput)//.mul(channels[1])

        val minMaxResults = arrayOf(MinMaxLocResult(), MinMaxLocResult(), MinMaxLocResult())

        var i = 0
        while (i < 3) {
            hueInput.submat(rectangles[i]).copyTo(subMatrices[i])

            val planes = arrayListOf(subMatrices[i])

            // TODO: new algorithm:
            //      - convert to HSV and posterize
            //      - most frequent instead of average
            //      - weighted?

            // create histogram
            // range of values
            Imgproc.calcHist(
                planes,
                MatOfInt(0),
                mask,
                hueHist,
                MatOfInt(256),
                histRange,
                true
            )

            // the mode (max value of a histogram is the most frequent)
            val mml = Core.minMaxLoc(hueHist)

//            opMode.telemetry.addLine("(CV PIPELINE INJECT) maxVal: ${mml.maxVal} maxLoc: ${mml.maxLoc}")

            minMaxResults[i] = mml

            when (i) {
                0 -> maxPos0 = mml.maxLoc
                1 -> maxPos1 = mml.maxLoc
                2 -> maxPos2 = mml.maxLoc
            }

            i++
        }

        val targetHue = elementHue * 256f / 360f
        // TODO: set elementSpikeMark based on the distance to the desired hue

        //Putting averaged colors on zones (we can see on camera now)
        zone1.setTo(Scalar(
            minMaxResults[0].maxLoc.x * 255.0,
            minMaxResults[0].maxLoc.y * 255.0,
            0.2
        ))
        zone2.setTo(Scalar(
            minMaxResults[1].maxLoc.x * 255.0,
            minMaxResults[1].maxLoc.y * 255.0,
            0.4
        ))
        zone3.setTo(Scalar(
            minMaxResults[2].maxLoc.x * 255.0,
            minMaxResults[2].maxLoc.y * 255.0,
            0.6
        ))

        Log.i("CV TEAM ELEMENT PIPELINE", "frame processed")
        // Allowing for the showing of the averages on the stream
        // just show the first one of them since I'm still testing this

        Imgproc.rectangle(
            input,
            Point(
                input.cols() / 4.0,
                input.rows() / 4.0
            ),
            Point(
                input.cols() * (3.0 / 4.0),
                input.rows() * (3.0 / 4.0)
            ),
            Scalar(0.0, 255.0, 0.0), 4
        )

        return input
    }

    fun setAlliancePipe(alliance: Alliance) = when (alliance) {
        Alliance.RED -> elementHue = 355 // approx. red on a 0-360 color picker
        Alliance.BLUE -> elementHue = 220 // approx. blue on a 0-360 color picker
    }
}