package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import android.graphics.Camera;

import com.qualcomm.robotcore.hardware.HardwareMap;


    public class AutoSubsystem {
        OpenCvCamera camera;
        CameraPipeline pipeline;
        int camW = 1280;
        int camH = 720;

        int zone = 1;

        public AutoSubsystem(HardwareMap hardwareMap){
            camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"));
            pipeline = new CameraPipeline();

            camera.setPipeline(pipeline);
            camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
            {
                @Override
                public void onOpened()
                {
                    camera.startStreaming(camW, camH, OpenCvCameraRotation.UPRIGHT);
                }

                @Override
                public void onError(int errorCode)
                {

                }
            });
        }

        public void setAlliance(String alliance){
            pipeline.setAlliancePipe(alliance);
        }

        public int elementDetection(Telemetry telemetry) {
            zone = pipeline.get_element_zone();
            telemetry.addData("Element Zone", zone);
            return zone;
        }

        public void toggleAverageZone(){
            pipeline.toggleAverageZonePipe();
        }

        public double getMaxDistance(){
            return pipeline.getMaxDistance();
        }
    }

