package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.subsystems.MecanumDriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LimelightSubsystem;

@TeleOp(name = "Mecanum + Advanced Limelight Assist")
public class MecanumLimelightTeleOp extends LinearOpMode {

    @Override
    public void runOpMode() {
        MecanumDriveSubsystem drive = new MecanumDriveSubsystem(hardwareMap);
        LimelightSubsystem limelight = new LimelightSubsystem(hardwareMap);

        telemetry.addData("Status", "Ready for Game Elements");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Standard driver controls
            //can change later to tune
            double forward = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;

            // Strategy 1: Driver Assist "Snap-to-Target" (Hold A)
            if (gamepad1.a && limelight.hasTarget()) {
                double tx = limelight.getTargetXOffset();
                // Proportional loop to steer toward the target horizontally
                turn = tx * 0.03;
            }

            // Strategy 2: Distance Approach Assist (Hold B)
            // Useful if you want the robot to automatically inch closer or back up to a target height
            if (gamepad1.b && limelight.hasTarget()) {
                double ty = limelight.getTargetYOffset();

                // Let's say your ideal target vertical offset is 5.0 degrees.
                // If ty is less than 5, drive forward; if more, back up.
                double targetTy = 5.0;
                double error = ty - targetTy;

                // Override the forward stick input with our vision correction
                forward = error * 0.05;
            }

            // Send calculated inputs to the drive subsystem
            drive.drive(forward, strafe, turn);

            // Telemetry to help you tune your multipliers on the fly
            telemetry.addData("Has Target", limelight.hasTarget());
            telemetry.addData("Target X (tx)", limelight.getTargetXOffset());
            telemetry.addData("Target Y (ty)", limelight.getTargetYOffset());
            telemetry.update();
        }
    }
}