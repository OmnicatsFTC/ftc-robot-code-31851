package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class ShooterSubsystem {
    private final DcMotor shooter;
    private final Servo indexer;
    private final DcMotor intake;

    // --- SHOOTER ---
    private static final double SHOOTER_POWER = 0.71;
    private boolean shooterOn = false;
    private boolean lastShooterToggleState = false;

    // --- INDEXER (SERVO) CALIBRATION POSITIONS ---
    private static final double REST_POSITION = 0.60;
    private static final double TURN_POSITION = 0.10; // "180 degrees" - adjust to match your servo's real range
    private static final double HOLD_SECONDS = 0.3;
    private static final double SERVO_SPEED_FACTOR = 1;
    private double targetServoPosition = REST_POSITION;
    private double currentServoPosition = REST_POSITION;

    private enum IndexerState {
        IDLE, TURNING, RETURNING
    }

    private IndexerState indexerState = IndexerState.IDLE;
    private final ElapsedTime indexerTimer = new ElapsedTime();
    private boolean lastIndexerButtonState = false;

    public ShooterSubsystem(HardwareMap hardwareMap) {
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        indexer = hardwareMap.get(Servo.class, "indexer");
        intake = hardwareMap.get(DcMotor.class, "intake");

        indexer.setPosition(REST_POSITION);
    }

    /**
     * Call every loop. Toggles the flywheel on/off each time the button transitions
     * from not-pressed to pressed. Holding the button does nothing extra.
     */
    public void updateShooter(boolean toggleButtonPressed) {
        boolean justPressed = toggleButtonPressed && !lastShooterToggleState;
        lastShooterToggleState = toggleButtonPressed;

        if (justPressed) {
            shooterOn = !shooterOn;
        }

        shooter.setPower(shooterOn ? SHOOTER_POWER : 0.0);
    }

    public boolean isShooterOn() {
        return shooterOn;
    }

    /** Call every loop. Runs the one-shot "turn 180 and return" cycle on a button press. */
    public void updateIndexer(boolean buttonPressed) {
        boolean justPressed = buttonPressed && !lastIndexerButtonState;
        lastIndexerButtonState = buttonPressed;

        if (justPressed && indexerState == IndexerState.IDLE) {
            targetServoPosition = TURN_POSITION;
            indexerTimer.reset();
            indexerState = IndexerState.TURNING;
        }

        switch (indexerState) {
            case IDLE:
                break;
            case TURNING:
                if (indexerTimer.seconds() >= HOLD_SECONDS) {
                    targetServoPosition = REST_POSITION;
                    indexerTimer.reset();
                    indexerState = IndexerState.RETURNING;
                }
                break;
            case RETURNING:
                if (indexerTimer.seconds() >= HOLD_SECONDS) {
                    indexerState = IndexerState.IDLE;
                }
                break;
        }

        double positionDelta = targetServoPosition - currentServoPosition;
        currentServoPosition += positionDelta * SERVO_SPEED_FACTOR;
        currentServoPosition = Range.clip(currentServoPosition, 0.0, 1.0);
        indexer.setPosition(currentServoPosition);
    }

    /** Call every loop. Runs intake independently of shooter/indexer state. */
    public void updateIntake(double manualIntakePower, double manualOuttakePower) {
        if (manualIntakePower > 0.1) {
            intake.setPower(2*manualIntakePower);
        } else if (manualOuttakePower > 0.1) {
            intake.setPower(-2*manualOuttakePower);
        } else {
            intake.setPower(0.0);
        }
    }

    public String getStateName() {
        return indexerState.toString();
    }
}