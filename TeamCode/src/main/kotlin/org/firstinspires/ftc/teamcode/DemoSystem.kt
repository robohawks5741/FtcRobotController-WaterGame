import org.firstinspires.ftc.teamcode.*
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.eventloop.opmode.Autonomous

object DemoSystem {

    @JvmStatic @JvmField
    const val PLAYBACK_OPMODE: KClass<OpMode> = DriverControl::class
    @JvmStatic @JvmField
    const val TICK_RATE: Double = 30 

    @JvmStatic @JvmField
    var frames1: ArrayList<ByteArray?> = arrayOfNulls(math.ceil(TICK_RATE * 30)).toCollection(ArrayList())
    @JvmStatic @JvmField  
    var frames2: ArrayList<ByteArray?> = arrayOfNulls(math.ceil(TICK_RATE * 30)).toCollection(ArrayList())

    fun writeFrames() {
        // TODO: find the context !!!
        // val file = File(context.filesDir, "addieDemoReplay0")
        // val writer = FileWriter(file)
        // writer.write
    }

    fun readFrames() {
        // TODO: find the context !!!
        // val file = File(context.filesDir, "addieDemoReplay0")
        // val reader = FileReader(file)
    }

    @Autonomous
    class DemoPlayback : OpMode {

        val emulatedOpMode: OpMode = PLAYBACK_OPMODE.createInstance()
//         var lastTickTime: Double = 0.0

        override fun init() {
            emulatedOpMode.gamepad1 = Gamepad()
            emulatedOpMode.gamepad2 = Gamepad()
            emulatedOpMode.gamepad1.fromByteArray(frames1[0])
            emulatedOpMode.gamepad2.fromByteArray(frames2[0])
            emulatedOpMode.telemetry = this.telemetry
            emulatedOpMode.hardwareMap = this.hardwareMap
            emulatedOpMode.internalOpModeServices = this.internalOpModeServices
            emulatedOpMode.msStuckDetectInit = this.msStuckDetectInit 
            emulatedOpMode.msStuckDetectInitLoop = this.msStuckDetectInitLoop 
            emulatedOpMode.msStuckDetectLoop = this.msStuckDetectLoop 
            emulatedOpMode.msStuckDetectStart = this.msStuckDetectStart 
            emulatedOpMode.msStuckDetectStop = this.msStuckDetectStop 
            emulatedOpMode.time = this.time
            emulatedOpMode.init()
        }

        override fun start() {
            emulatedOpMode.msStuckDetectInit = this.msStuckDetectInit 
            emulatedOpMode.msStuckDetectInitLoop = this.msStuckDetectInitLoop 
            emulatedOpMode.msStuckDetectLoop = this.msStuckDetectLoop 
            emulatedOpMode.msStuckDetectStart = this.msStuckDetectStart 
            emulatedOpMode.msStuckDetectStop = this.msStuckDetectStop 
            emulatedOpMode.time = this.time
            emulatedOpMode.gamepad1.fromByteArray(frames1[0])
            emulatedOpMode.gamepad2.fromByteArray(frames2[0])
            emulatedOpMode.start()
        }

        override fun loop() {
            emulatedOpMode.msStuckDetectInit = this.msStuckDetectInit 
            emulatedOpMode.msStuckDetectInitLoop = this.msStuckDetectInitLoop 
            emulatedOpMode.msStuckDetectLoop = this.msStuckDetectLoop 
            emulatedOpMode.msStuckDetectStart = this.msStuckDetectStart 
            emulatedOpMode.msStuckDetectStop = this.msStuckDetectStop 
            emulatedOpMode.time = this.time

            val index = math.floor(this.time / 30.0)
            
            if (index < frames1.size && frames1[index] != null) {
                emulatedOpMode.gamepad1.fromByteArray(frames1[0])
                emulatedOpMode.gamepad2.fromByteArray(frames2[0])
            }

            emulatedOpMode.loop()
        }
    }

    @TeleOp
    class DemoRecorder : OpMode() {

        val emulatedOpMode: OpMode = PLAYBACK_OPMODE.createInstance()
//         var lastTickTime: Double = 0.0

        override fun init() {
            emulatedOpMode.gamepad1 = Gamepad()
            emulatedOpMode.gamepad2 = Gamepad()
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)
            emulatedOpMode.telemetry = this.telemetry
            emulatedOpMode.hardwareMap = this.hardwareMap
            emulatedOpMode.internalOpModeServices = this.internalOpModeServices
            emulatedOpMode.msStuckDetectInit = this.msStuckDetectInit 
            emulatedOpMode.msStuckDetectInitLoop = this.msStuckDetectInitLoop 
            emulatedOpMode.msStuckDetectLoop = this.msStuckDetectLoop 
            emulatedOpMode.msStuckDetectStart = this.msStuckDetectStart 
            emulatedOpMode.msStuckDetectStop = this.msStuckDetectStop 
            emulatedOpMode.time = this.time
            emulatedOpMode.init()
        }

        override fun start() {
            emulatedOpMode.msStuckDetectInit = this.msStuckDetectInit 
            emulatedOpMode.msStuckDetectInitLoop = this.msStuckDetectInitLoop 
            emulatedOpMode.msStuckDetectLoop = this.msStuckDetectLoop 
            emulatedOpMode.msStuckDetectStart = this.msStuckDetectStart 
            emulatedOpMode.msStuckDetectStop = this.msStuckDetectStop 
            emulatedOpMode.time = this.time
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)
            emulatedOpMode.start()
        }

        override fun loop() {
            emulatedOpMode.msStuckDetectInit = this.msStuckDetectInit 
            emulatedOpMode.msStuckDetectInitLoop = this.msStuckDetectInitLoop 
            emulatedOpMode.msStuckDetectLoop = this.msStuckDetectLoop 
            emulatedOpMode.msStuckDetectStart = this.msStuckDetectStart 
            emulatedOpMode.msStuckDetectStop = this.msStuckDetectStop 
            emulatedOpMode.time = this.time
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)

            val index = math.floor(this.time / 30.0)
            
            if (index < frames1.size && frames1[index] != null) {
                frames1[index] = emulatedOpMode.gamepad1.toByteArray()
                frames2[index] = emulatedOpMode.gamepad2.toByteArray()
            }

            emulatedOpMode.loop()
        }
    }
}
