package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
// Import your specific Limelight helper class provided by the vendor library
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;

public class LimelightSubsystem {
    // Assuming you use the Limelight3A or similar vendor class wrapper
     private Limelight3A limelight; // Replace with your actual Limelight object type

    public LimelightSubsystem(HardwareMap hardwareMap) {
         limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0); // Set default pipeline
    }

    public boolean hasTarget() {
        // Return true if a target is currently visible
         return limelight.getLatestResult().isValid();

    }

    public double getTargetXOffset() {
        // Returns the horizontal offset from the crosshair to the target (-29.8 to 29.8 degrees)
         return limelight.getLatestResult().getTx();

    }

    public double getTargetYOffset() {
        // Returns the vertical offset
        return limelight.getLatestResult().getTy();

    }
}