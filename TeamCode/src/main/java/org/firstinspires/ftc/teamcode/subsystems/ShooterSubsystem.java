package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.Servo; // Changed from DcMotor to Servo
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class ShooterSubsystem {
    private final DcMotor shooter;
    private final Servo indexer; // Changed to Servo
    private final DcMotor intake;

    // --- SERVO CALIBRATION POSITIONS ---
    // Modify these values (between 0.0 and 1.0) to match your physical robot angles
    private static final double SERVO_UP_POSITION = 0.70;   // Position that pushes ball into flywheel
    private static final double SERVO_DOWN_POSITION = 0.20; // Position that tilts down to let balls roll in

    // Smooth speed factor (Lower = slower, smoother glide. Higher = faster movement)
    private static final double SERVO_SPEED_FACTOR = 0.05;
    private double targetServoPosition = SERVO_DOWN_POSITION;
    private double currentServoPosition = SERVO_DOWN_POSITION;

    // Macro State Definitions
    private enum MacroState {
        IDLE,
        START_SHOOTER,
        SEESAW_LOWER,  // Tilt down to accept rolling ball
        SEESAW_PUSH    // Rock up to feed ball to flywheel
    }

    private MacroState currentMacroState = MacroState.IDLE;
    private final ElapsedTime macroTimer = new ElapsedTime();

    public ShooterSubsystem(HardwareMap hardwareMap) {
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        indexer = hardwareMap.get(Servo.class, "indexer"); // Fetching as a Servo
        intake = hardwareMap.get(DcMotor.class, "intake");

        // Initialize the servo to its resting down position
        indexer.setPosition(SERVO_DOWN_POSITION);
    }

    public void update(boolean runMacro, double manualIntakePower, double manualOuttakePower) {

        if (runMacro) {
            switch (currentMacroState) {

                case IDLE:
                    // Step 1: Start flywheel, lock servo down, clear intake
                    shooter.setPower(1.0);
                    intake.setPower(0.0);
                    targetServoPosition = SERVO_DOWN_POSITION;
                    macroTimer.reset();
                    currentMacroState = MacroState.START_SHOOTER;
                    break;

                case START_SHOOTER:
                    // Step 2: Wait 0.5s for flywheel spin-up, then proceed to the cycle
                    if (macroTimer.seconds() >= 0.5) {
                        macroTimer.reset();
                        currentMacroState = MacroState.SEESAW_LOWER;
                    }
                    break;

                case SEESAW_LOWER:
                    // Step 3: Tell servo to go down so the ball can roll onto the seesaw
                    targetServoPosition = SERVO_DOWN_POSITION;

                    // Wait for the ball to roll in and settle (e.g., 0.6 seconds)
                    if (macroTimer.seconds() >= 0.4) {
                        macroTimer.reset();
                        currentMacroState = MacroState.SEESAW_PUSH; // Time to score
                    }
                    break;

                case SEESAW_PUSH:
                    // Step 4: Tilt the servo up to feed the ball into the flywheel
                    targetServoPosition = SERVO_UP_POSITION;

                    // Hold the up position long enough for the flywheel to grab the ball (e.g., 0.4 seconds)
                    if (macroTimer.seconds() >= 0.4) {
                        macroTimer.reset();
                        currentMacroState = MacroState.SEESAW_LOWER; // Cycle back down for the next ball
                    }
                    break;
            }
        }
        else {
            // Reset when the driver lets go of the button
            shooter.setPower(0.0);
            targetServoPosition = SERVO_DOWN_POSITION;
            currentMacroState = MacroState.IDLE;

            // Manual intake overrides when macro is inactive
            if (manualIntakePower > 0.1) {
                intake.setPower(manualIntakePower);
            } else if (manualOuttakePower > 0.1) {
                intake.setPower(-manualOuttakePower);
            } else {
                intake.setPower(0.0);
            }
        }

        // --- THE SMOOTH MOVING TRICK ---
        // Instead of jumping instantly to target position, smoothly slide towards it over multiple loops
        double positionDelta = targetServoPosition - currentServoPosition;
        currentServoPosition += positionDelta * SERVO_SPEED_FACTOR;

        // Safety constraint to keep the servo values strictly between 0 and 1
        currentServoPosition = Range.clip(currentServoPosition, 0.0, 1.0);

        // Write the calculated smooth position directly to hardware [1]
        indexer.setPosition(currentServoPosition);
    }

    public String getStateName() {
        return currentMacroState.toString();
    }
}
