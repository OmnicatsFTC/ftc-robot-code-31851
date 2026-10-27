
package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class ShooterSubsystem {

    private final DcMotorEx shooter;
    private final Servo indexer;
    private final DcMotor intake;

    // SHOOTER SETTINGS
    // Initial test value only. Tune on your actual robot.
    private static final double TARGET_RPM = 3000.0;
    private static final double READY_TOLERANCE_RPM = 75.0;
    private static final double READY_SETTLE_SECONDS = 0.20;

    private double targetRpm = TARGET_RPM;
    private boolean shooterOn = false;
    private boolean lastShooterToggleState = false;
    private boolean lastShooterToggleState2 = false;
    private boolean shooterOn2 = false;

    private final double ticksPerMotorRev;

    private final ElapsedTime readyTimer = new ElapsedTime();
    private boolean speedInRange = false;

    // INDEXER SETTINGS
    private static final double REST_POSITION = 0.60;
    private static final double TURN_POSITION = 0.10;
    private static final double HOLD_SECONDS = 0.6;
    private static final double SERVO_SPEED_FACTOR = 0.5;

    private double targetServoPosition = REST_POSITION;
    private double currentServoPosition = REST_POSITION;

    private enum IndexerState {
        IDLE, TURNING, RETURNING
    }

    private IndexerState indexerState = IndexerState.IDLE;
    private final ElapsedTime indexerTimer = new ElapsedTime();
    private boolean lastIndexerButtonState = false;

    public ShooterSubsystem(HardwareMap hardwareMap) {
        shooter = hardwareMap.get(
                DcMotorEx.class, "shooter");

        indexer = hardwareMap.get(
                Servo.class, "indexer");

        intake = hardwareMap.get(
                DcMotor.class, "intake");

        // Uses the encoder resolution in the configured motor type.
        ticksPerMotorRev =
                shooter.getMotorType().getTicksPerRev();

        shooter.setMode(
                DcMotor.RunMode.RUN_USING_ENCODER);

        indexer.setPosition(REST_POSITION);
    }

    // SHOOTER CONTROL

    public void updateShooter(boolean toggleButtonPressed) {
        boolean justPressed =
                toggleButtonPressed && !lastShooterToggleState;

        lastShooterToggleState = toggleButtonPressed;

        if (justPressed) {
            setShooterEnabled(!shooterOn);
        }
    }

    public void setShooterEnabled(boolean enabled) {
        shooterOn = enabled;

        if (enabled) {
            applyTargetVelocity();
        } else {
            shooter.setVelocity(0);
            speedInRange = false;
        }
    }

    public void setTargetRpm(double rpm) {
        targetRpm = Math.max(0, rpm);

        if (shooterOn) {
            applyTargetVelocity();
        }

        speedInRange = false;
    }

    private void applyTargetVelocity() {
        double ticksPerSecond =
                targetRpm * ticksPerMotorRev / 60.0;

        shooter.setVelocity(ticksPerSecond);
    }

    public double getTargetRpm() {
        return targetRpm;
    }

    public double getActualRpm() {
        if (ticksPerMotorRev <= 0) {
            return 0;
        }

        return shooter.getVelocity()
                * 60.0 / ticksPerMotorRev;
    }

    public boolean isShooterOn() {
        return shooterOn;
    }

    public boolean isShooterReady() {
        if (!shooterOn || targetRpm <= 0) {
            speedInRange = false;
            return false;
        }

        double error =
                Math.abs(getActualRpm() - targetRpm);

        if (error > READY_TOLERANCE_RPM) {
            speedInRange = false;
            return false;
        }

        if (!speedInRange) {
            speedInRange = true;
            readyTimer.reset();
        }

        return readyTimer.seconds()
                >= READY_SETTLE_SECONDS;
    }

    // INDEXER CONTROL

    public void updateIndexer(boolean buttonPressed) {
        boolean justPressed =
                buttonPressed && !lastIndexerButtonState;

        lastIndexerButtonState = buttonPressed;

        if (!shooterOn) {
            return;
        }

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

        double delta =
                targetServoPosition - currentServoPosition;

        currentServoPosition +=
                delta * SERVO_SPEED_FACTOR;

        currentServoPosition =
                Range.clip(currentServoPosition, 0.0, 1.0);

        indexer.setPosition(currentServoPosition);
    }

    public String getStateName() {
        return indexerState.toString();
    }

    // INTAKE CONTROL

    public void updateIntake(
            double manualIntakePower,
            double manualOuttakePower) {

        if (manualIntakePower > 0.1) {
            intake.setPower(
                    Range.clip(manualIntakePower, 0.0, 1.0));
        } else if (manualOuttakePower > 0.1) {
            intake.setPower(
                    -Range.clip(manualOuttakePower, 0.0, 1.0));
        } else {
            intake.setPower(0.0);
        }
    }

    public void updateIntake2(boolean toggleButtonPressed) {
        boolean justPressed =
                toggleButtonPressed && !lastShooterToggleState2;

        lastShooterToggleState2 = toggleButtonPressed;

        if (justPressed) {
            shooterOn2 = !shooterOn2;
        }

        intake.setPower(shooterOn2 ? 1.0 : 0.0);
    }
}
