package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName

class SoftwareMap {

    @JvmField val motorSlideLeft:   DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "liftLeft"  ] }
    @JvmField val motorSlideRight:  DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "liftRight" ] }
    @JvmField val motorIntakeSpin:  DcMotorEx?  =
    @JvmField val motorIntakeLift:  DcMotorEx?  =
    @JvmField val motorTrussPull:   DcMotorEx?  =   idc {   hardwareMap[DcMotorEx    ::class.java,   "hang"      ] }
    @JvmField val servoTrussLeft:   Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "trussl"    ] }
    @JvmField val servoTrussRight:  Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "trussr"    ] }
    @JvmField val servoArmLeft:     Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "armLeft"   ] }
    @JvmField val servoArmRight:    Servo?      =   idc {   hardwareMap[Servo        ::class.java,   "armRight"  ] }

}