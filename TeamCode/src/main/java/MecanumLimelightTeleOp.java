import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.MecanumDriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LimelightSubsystem;

@TeleOp(name = "Mecanum + Advanced Limelight Assist")
public class MecanumLimelightTeleOp extends LinearOpMode {

    @Override
    public void runOpMode() {
        // Initialize independent subsystems
        MecanumDriveSubsystem drive = new MecanumDriveSubsystem(hardwareMap);
        LimelightSubsystem limelight = new LimelightSubsystem(hardwareMap);
        ShooterSubsystem shooterSystem = new ShooterSubsystem(hardwareMap);

        telemetry.addData("Status", "Ready for Game Elements");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Drivetrain translation
            double forward = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;

            // Strategy 1: Driver Assist "Snap-to-Target" (Hold A)
            if (gamepad1.a && limelight.hasTarget()) {
                double tx = limelight.getTargetXOffset();
                turn = tx * 0.03;
            }

             //Strategy 2: Distance Approach Assist (Hold B)
            if (gamepad1.b && limelight.hasTarget()) {
                double ty = limelight.getTargetYOffset();
                double targetTy = 5.0;
                double error = ty - targetTy;
                forward = error * 0.05;
            }

            // Handle the shooter, indexer, and intake macros via the subsystem loop
            shooterSystem.updateShooter(gamepad1.b);          // press A to toggle flywheel on/off
            shooterSystem.updateIndexer(gamepad1.a); // press bumper to fire
//            shooterSystem.updateIntake(gamepad1.right_trigger, gamepad1.left_trigger);
            shooterSystem.updateIntake(gamepad1.left_trigger,gamepad1.right_trigger);
            shooterSystem.updateIntake2(gamepad1.x);
            // Execute hardware movement for drivetrain
            drive.drive(forward, strafe, turn);

            // Diagnostics telemetry
            telemetry.addData("Shooter Macro State", shooterSystem.getStateName());
            telemetry.addData("Has Target", limelight.hasTarget());
            telemetry.addData("Target X (tx)", limelight.getTargetXOffset());
            telemetry.addData("Target Y (ty)", limelight.getTargetYOffset());
            telemetry.update();
        }
    }
}
