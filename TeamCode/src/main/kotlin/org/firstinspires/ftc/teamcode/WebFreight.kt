package org.firstinspires.ftc.teamcode
//import android.annotation.SuppressLint
//import android.content.Context
//import com.qualcomm.ftccommon.FtcEventLoop
//import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
//import com.qualcomm.robotcore.util.WebHandlerManager
//import com.qualcomm.robotcore.util.WebServer
//import fi.iki.elonen.NanoHTTPD
//import fi.iki.elonen.NanoHTTPD.IHTTPSession
//import fi.iki.elonen.NanoHTTPD.Response
//import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
//import org.firstinspires.ftc.ftccommon.external.OnDestroy
//import org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar
//import org.firstinspires.ftc.robotcore.internal.webserver.websockets.FtcWebSocket
//import org.firstinspires.ftc.robotcore.internal.webserver.websockets.FtcWebSocketMessage
//import org.firstinspires.ftc.robotcore.internal.webserver.websockets.WebSocketNamespaceHandler
//
//// Sorta like the FTC-Dashboard library, but this is custom-made
//class WebFreight(private val server: WebServer) {
//    private var opModeManager: OpModeManagerImpl? = null
//    private var context: Context? = null
//    init {
////        server.webHandlerManager.register("/setdemo", ::setDemo)
//        server.webHandlerManager.register("/addie", ::dashResponse)
//        server.webSocketManager.registerNamespaceAsBroadcastOnly("addie-rc")
//        server.webSocketManager.registerNamespaceHandler(AddieRemoteControl)
//    }
//
//    public fun initOpMode(name: String) {
//        opModeManager?.initOpMode(name);
//    }
//
//    companion object {
//
//        object AddieRemoteControl : WebSocketNamespaceHandler("addie-rc" ) {
//            override fun onMessage(
//                message: FtcWebSocketMessage?,
//                webSocket: FtcWebSocket?
//            ): Boolean {
//                val sus = super.onMessage(message, webSocket)
//                return false
//            }
//        }
//
//        // Android calls this a memory leak. As long as the FTC SDK fulfills its promises, this is not the case.
//        @SuppressLint("StaticFieldLeak")
//        @JvmStatic public var instance: WebFreight? = null
//
//        @JvmStatic
//        fun dashResponse(session: IHTTPSession): Response {
//            return NanoHTTPD.newFixedLengthResponse(
//                Response.Status.OK,
//                NanoHTTPD.MIME_PLAINTEXT, "Hi!"
//            )
//        }
//
////        @JvmStatic
////        fun setDemo(session: IHTTPSession): Response {
////            val data: String? = session.queryParameterString
////            DemoSystem.inputFileName = data
////            DemoSystem.outputFileName = data
////            return NanoHTTPD.newFixedLengthResponse(
////                Response.Status.OK,
////                NanoHTTPD.MIME_PLAINTEXT, "set active demo file to \"${data}\""
////            )
////        }
////        @JvmStatic
////        val dashResponse = object: WebHandler {
////            override fun getResponse(session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
////                TODO("Not yet implemented")
////            }
////        }
//
//        @OnCreateEventLoop
//        @JvmStatic
//        fun onControllerCreateEventLoop(context: Context?, eventLoop: FtcEventLoop?) {
//            instance?.opModeManager = eventLoop?.opModeManager
//            instance?.context = context
//        }
//
//        @WebHandlerRegistrar
//        @JvmStatic
//        fun register(context: Context, manager: WebHandlerManager): Unit {
//            if (instance == null) instance = WebFreight(manager.webServer)
//        }
//
//        @OnDestroy
//        @JvmStatic
//        fun onControllerDestroy(context: Context?) { instance = null }
//    }
//}