import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.firstinspires.ftc.teamcode.*
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.hardware.Gamepad
import org.json.JSONArray
import org.json.JSONStringer
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.attribute.FileAttribute
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

object DemoSystem {

    val PLAYBACK_OPMODE: KClass<*> = DriverControl::class
    const val TICK_RATE: Double = 30.0

    var frames1: ArrayList<ByteArray?> = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())
    var frames2: ArrayList<ByteArray?> = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())

    fun readFrames(anyOpMode: OpMode) {
        val context = anyOpMode.hardwareMap.appContext
         val file = File(context.filesDir, "0.replay")
         val reader = FileReader(file)
    }

    @Autonomous(name = "Play Recorded Demo", group = "DemoSystem")
    class DemoPlayback : OpMode() {

        val emulatedOpMode: OpMode = PLAYBACK_OPMODE.createInstance() as OpMode
//         var lastTickTime: Double = 0.0

        override fun init() {
//            if (frames1[0] == null) {
//                val dir = File(hardwareMap.appContext.filesDir, "demoReplays")
//                if (!dir.exists()) {
//                    dir.mkdir()
//                }
//
//                val file = File(dir, "0.replay")
//                val reader = FileWriter(file)
//                var totalSize = 0
//                for (e in frames1) {
//
//                }
//            }

            emulatedOpMode.gamepad1 = Gamepad()
            emulatedOpMode.gamepad2 = Gamepad()
            emulatedOpMode.telemetry = this.telemetry
            emulatedOpMode.hardwareMap = this.hardwareMap
//            emulatedOpMode.internalOpModeServices = this.internalOpModeServices
            emulatedOpMode.time = this.time
            emulatedOpMode.init()
        }

        override fun start() {
            emulatedOpMode.time = this.time
            emulatedOpMode.start()
        }

        override fun loop() {
            emulatedOpMode.time = this.time

            val index = floor(this.time * TICK_RATE).toInt()
            
            if (index < frames1.size && frames1[index] != null) {
                emulatedOpMode.gamepad1.fromByteArray(frames1[index])
                emulatedOpMode.gamepad2.fromByteArray(frames2[index])
                telemetry.addLine("frame #${index}")
                Log.i("DEMOREPLAY", "playing frame #$index")
            } else {
                Log.i("DEMOREPLAY", "skipping frame read #$index (max frames = ${frames1.size}, frame@index = ${frames1[index]})")
            }

            emulatedOpMode.loop()
        }
    }

    @TeleOp(name = "Record Demo", group = "DemoSystem")
    class DemoRecorder : OpMode() {

        val emulatedOpMode: OpMode = PLAYBACK_OPMODE.createInstance() as OpMode
//         var lastTickTime: Double = 0.0

        override fun init() {
//            val dir = File(hardwareMap.appContext.filesDir, "demoReplays")
//            if (!dir.exists()) {
//                dir.mkdir()
//            }

            // TODO: only overwrite if we can load from memory
            frames1 = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())
            frames2 = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())

            emulatedOpMode.gamepad1 = Gamepad()
            emulatedOpMode.gamepad2 = Gamepad()
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)
            frames1[0] = emulatedOpMode.gamepad1.toByteArray()
            frames2[0] = emulatedOpMode.gamepad2.toByteArray()

            emulatedOpMode.telemetry = this.telemetry
            emulatedOpMode.hardwareMap = this.hardwareMap
//            emulatedOpMode.internalOpModeServices = this.internalOpModeServices
            emulatedOpMode.time = this.time
            emulatedOpMode.init()
        }

        override fun start() {
            emulatedOpMode.time = this.time
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)
            emulatedOpMode.start()
            Log.i("DEMOREPLAY", "Started recording.")
        }

        override fun loop() {
            emulatedOpMode.time = this.time
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)

            val index = floor(this.time * TICK_RATE).toInt()
            
            if (index < frames1.size && frames1[index] == null) {
                frames1[index] = emulatedOpMode.gamepad1.toByteArray()
                frames2[index] = emulatedOpMode.gamepad2.toByteArray()
                Log.i("DEMOREPLAY", "recorded frame #$index")
            } else {
                Log.i("DEMOREPLAY", "skipping frame write #$index (max frames = ${frames1.size}, frame@index = ${frames1[index]})")
            }

            emulatedOpMode.loop()
        }

        override fun stop() {
            Log.i("DEMOREPLAY", "Demo recorded, frames:")
            for ((i, e) in frames1.withIndex()) {
                if (e == null) continue
                Log.i("DEMOREPLAY", "#$i: $e")
            }
            Log.i("DEMOREPLAY", "No more frames.")

//            val context = hardwareMap.appContext
//            val dir = File(context.filesDir, "demoReplays")
//            if (!dir.exists()) {
//                dir.mkdir()
//            }
//            val file = File(dir, "0.replay")
//            var jsonFrames = ArrayList<JSONArray>(frames1.size)
//            val writer = FileWriter(file)
//            var totalSize = 0
//            for (e in frames1) {
//                jsonFrames.add(JSONArray(e))
//            }

        }
    }
}
