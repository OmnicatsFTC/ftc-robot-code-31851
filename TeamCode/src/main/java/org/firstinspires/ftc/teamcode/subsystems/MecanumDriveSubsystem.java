package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class MecanumDriveSubsystem {
    private DcMotor frontLeft, frontRight, backLeft, backRight;

    public MecanumDriveSubsystem(HardwareMap hardwareMap) {
        // Initialize hardware mapping
        frontLeft = hardwareMap.get(DcMotor.class, "front_left");
        frontRight = hardwareMap.get(DcMotor.class, "front_right");
        backLeft = hardwareMap.get(DcMotor.class, "back_left");
        backRight = hardwareMap.get(DcMotor.class, "back_right");

        // Set motor directions (adjust based on your robot's build)
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        // Set zero power behavior
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void drive(double forward, double strafe, double turn) {
        // Calculate power for each wheel
        double flPower = forward + strafe + turn;
        double frPower = forward - strafe - turn;
        double blPower = forward - strafe + turn;
        double brPower = forward + strafe - turn;

        // Normalize powers if any exceed 1.0
        double max = Math.max(1.0, Math.max(Math.abs(flPower), Math.max(Math.abs(frPower),
                Math.max(Math.abs(blPower), Math.abs(brPower)))));

        frontLeft.setPower(flPower / max);
        frontRight.setPower(frPower / max);
        backLeft.setPower(blPower / max);
        backRight.setPower(brPower / max);
    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        frontLeft.setZeroPowerBehavior(behavior);
        frontRight.setZeroPowerBehavior(behavior);
        backLeft.setZeroPowerBehavior(behavior);
        backRight.setZeroPowerBehavior(behavior);
    }
}