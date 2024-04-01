package org.firstinspires.ftc.teamcode

import android.util.Log
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.opencv.core.Core
import org.opencv.core.Core.MinMaxLocResult
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvPipeline
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt


class CvTeamElementPipeline(var alliance: Alliance, private val opMode: OpMode) : OpenCvPipeline() {
    // * 360 / 256 = H 0-256 to to H 0-360
//    private var elementHue = 120

    var showDefault = false
    private lateinit var zone1: Mat
    private lateinit var zone2: Mat
    private lateinit var zone3: Mat
    private val channels = ArrayList<Mat>()
    private val hueHist = Mat()
    private val histRange = MatOfFloat(0f, 256f)
    private val subMatrices = arrayOf(Mat(), Mat(), Mat())
    private val mask = Mat()
    private val preview = Mat()

    /*
     * ~0 / ~180 = red
     * ~75-115 = green
     * ~120-160 = blue
     */
    var hue0: Int = -1
    var hue1: Int = -1
    var hue2: Int = -1
    var mml0: MinMaxLocResult = MinMaxLocResult()
    var mml1: MinMaxLocResult = MinMaxLocResult()
    var mml2: MinMaxLocResult = MinMaxLocResult()

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
        preview.create(720, 1280, CV_8UC3)
    }

    override fun processFrame(input: Mat): Mat {
        if (showDefault) return input
        Log.i("CV TEAM ELEMENT PIPELINE", "processing frame")

        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2HSV)
        Core.split(input, channels)
//        channels[0].copyTo(hueInput)//.mul(channels[1])
        val hueChannel = channels[0]

        val minMaxResults = arrayOf(MinMaxLocResult(), MinMaxLocResult(), MinMaxLocResult())

        var i = 0
        while (i < 3) {
            hueChannel.submat(rectangles[i]).copyTo(subMatrices[i])

            hueHist.setTo(Scalar(0.0))

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
                false
            )

            // the mode (max value of a histogram is the most frequent)
            val mml = Core.minMaxLoc(hueHist)

//            opMode.telemetry.addLine("(CV PIPELINE INJECT) maxVal: ${mml.maxVal} maxLoc: ${mml.maxLoc}")

            minMaxResults[i] = mml

            when (i) {
                // 0 to 180
                0 -> {
                    mml0 = mml
                    hue0 = mml.maxLoc.y.toInt()
                }
                1 -> {
                    mml1 = mml
                    hue1 = mml.maxLoc.y.toInt()
                }
                2 -> {
                    mml2 = mml
                    hue2 = mml.maxLoc.y.toInt()
                }
            }

            i++
        }

        //Putting averaged colors on zones (we can see on camera now)
//        zone1.setTo(Scalar(
//            minMaxResults[0].maxLoc.x * 255.0,
//            minMaxResults[0].maxLoc.y * 255.0,
//            0.2
//        ))
//        zone2.setTo(Scalar(
//            minMaxResults[1].maxLoc.x * 255.0,
//            minMaxResults[1].maxLoc.y * 255.0,
//            0.4
//        ))
//        zone3.setTo(Scalar(
//            minMaxResults[2].maxLoc.x * 255.0,
//            minMaxResults[2].maxLoc.y * 255.0,
//            0.6
//        ))

        Log.i("CV TEAM ELEMENT PIPELINE", "frame processed")
        // Allowing for the showing of the averages on the stream
        // just show the first one of them since I'm still testing this


//        Imgproc.rectangle(
//            input,
//            Point(
//                input.cols() / 4.0,
//                input.rows() / 4.0
//            ),
//            Point(
//                input.cols() * (3.0 / 4.0),
//                input.rows() * (3.0 / 4.0)
//            ),
//            Scalar(255.0, 255.0, 255.0), 16
//        )

        when (alliance) {
            Alliance.BLUE -> {
                val dLeft = abs(130 - hue0)
                val dCenter = abs(130 - hue1)
                val dRight = abs(130 - hue2)
                elementSpikeMark = when (min(min(dLeft, dCenter), dRight)) {
                    dLeft -> SpikeMark.LEFT
                    dCenter -> SpikeMark.CENTER
                    dRight -> SpikeMark.RIGHT
                    else -> SpikeMark.CENTER
                }
            }
            Alliance.RED -> {
                val dLeft = min(hue0, (180 - hue0 / 1.5).roundToInt())
                val dCenter = min(hue1, (180 - hue1 / 1.5).roundToInt())
                val dRight = min(hue2, (180 - hue2 / 1.5).roundToInt())

                elementSpikeMark = when (min(min(dLeft, dCenter), dRight)) {
                    dLeft -> SpikeMark.LEFT
                    dCenter -> SpikeMark.CENTER
                    dRight -> SpikeMark.RIGHT
                    else -> SpikeMark.CENTER
                }
            }
        }

//        preview.copySize(input)
//        preview.convertTo(preview, CV_8UC3)
//        preview.setTo(Scalar(0.0, 0.0, 0.0, 1.0))
//        preview.

//        Imgproc.putText(preview, "so sad that steve jobs died of ligma", Point(100.0, 100.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 255.0, 255.0, 255.0), 8)

//        hueHist.convertTo(preview, CV_8UC1)
        return hueChannel
    }
}