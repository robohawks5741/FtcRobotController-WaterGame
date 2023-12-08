package org.firstinspires.ftc.teamcode

//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonArray
//import kotlinx.serialization.json.JsonNull
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.teamcode.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Suppress("unused")
object DemoSystem {

    val PLAYBACK_OPMODE: KClass<*> = DriverControl::class
    const val TICK_RATE: Double = 30.0

    var frames1: ArrayList<ByteArray?> = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())
    var frames2: ArrayList<ByteArray?> = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())

    fun ByteArray.toBase64(): String = String(Base64.encode(this, Base64.DEFAULT or Base64.NO_WRAP))

    @Autonomous(name = "Play Recorded Demo", group = "DemoSystem")
    class DemoPlayback : OpMode() {

        private val emulatedOpMode: OpMode = PLAYBACK_OPMODE.createInstance() as OpMode
//         var lastTickTime: Double = 0.0

        override fun init() {
            val context = hardwareMap.appContext
            val dir = File(context.filesDir, "demoReplays")
            if (!dir.exists()) {
                dir.mkdir()
            }
            val file = File(dir, "0.replay")
            val fileReader = FileReader(file)
            val reader = BufferedReader(fileReader)
            val lines = StringBuilder(file.length().toInt())
            while (true) {
                val line = reader.readLine()
                if (line == null) break; else lines.append(line)
            }
            val gson = Gson()
            val jsonAllFrames: Array<Array<String?>> = gson.fromJson(lines.toString(), Array<Array<String?>>::class.java)

            var totalSize = 0
            val jsonFrames1 = jsonAllFrames[0]
            val jsonFrames2 = jsonAllFrames[1]
            for ((i, e) in jsonFrames1.withIndex()) frames1[i] = if (e is String) Base64.decode(e, Base64.DEFAULT or Base64.NO_WRAP) else null
            for ((i, e) in jsonFrames2.withIndex()) frames2[i] = if (e is String) Base64.decode(e, Base64.DEFAULT or Base64.NO_WRAP) else null

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

        private val emulatedOpMode: OpMode = PLAYBACK_OPMODE.createInstance() as OpMode
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

            val context = hardwareMap.appContext
            val dir = File(context.filesDir, "demoReplays")
            if (!dir.exists()) {
                dir.mkdir()
            }
            val file = File(dir, "0.replay")
            val jsonAllFrames = JsonArray()
            val jsonFrames1 = JsonArray()
            val jsonFrames2 = JsonArray()
            val writer = FileWriter(file)
            var totalSize = 0
            for (e in frames1) if (e == null) jsonFrames1.add(JsonNull.INSTANCE); else jsonFrames1.add(e.toBase64())
            for (e in frames2) if (e == null) jsonFrames2.add(JsonNull.INSTANCE); else jsonFrames2.add(e.toBase64())
            jsonAllFrames.add(jsonFrames1)
            jsonAllFrames.add(jsonFrames2)
            writer.write(jsonAllFrames.toString())
            writer.close()
        }
    }
}
