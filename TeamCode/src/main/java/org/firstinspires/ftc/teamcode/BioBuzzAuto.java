
package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;

@Autonomous(name = "BioBuzz Shoot Intake Load", group = "BioBuzz")
public final class BioBuzzAuto extends LinearOpMode {

    static final double START_X = 12;
    static final double START_Y = 65;
    static final double START_HEADING = Math.toRadians(270);

    static final double CLEAR_X = 34;
    static final double CLEAR_Y = 60;

    static final double PARK_X = 70;
    static final double PARK_Y = -79;

    static final double SHOOTER_SPINUP_SECONDS = 1.5;
    static final double INTAKE_SECONDS = 2.0;
    static final double INTAKE_POWER = 0.5;

    static final int SHOTS_BEFORE_INTAKE = 2;
    static final int SHOTS_AFTER_INTAKE = 3;
    static final int TOTAL_SHOTS =
            SHOTS_BEFORE_INTAKE + SHOTS_AFTER_INTAKE;

    @Override
    public void runOpMode() {

        Pose2d startPose =
                new Pose2d(START_X, START_Y, START_HEADING);

        MecanumDrive drive =
                new MecanumDrive(hardwareMap, startPose);

        ShooterSubsystem shooter =
                new ShooterSubsystem(hardwareMap);

        Action shooting = new ShootAndIntakeAction(shooter);

        Action travelToLoadingZone =
                drive.actionBuilder(startPose)
                        // Clear the hive first
                        .strafeTo(new Vector2d(CLEAR_X, CLEAR_Y))

                        // Then head to the loading zone
                        .strafeTo(new Vector2d(PARK_X, PARK_Y))
                        .build();

        telemetry.addLine("BioBuzz: Shoot, Intake, Load");
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        Actions.runBlocking(
                new SequentialAction(
                        shooting,
                        travelToLoadingZone
                )
        );
    }

    private class ShootAndIntakeAction implements Action {

        private final ShooterSubsystem shooter;
        private final ElapsedTime timer = new ElapsedTime();

        private int shotsFired = 0;

        private boolean started = false;
        private boolean triggerSent = false;
        private boolean waitingForIndexer = false;

        private boolean intakeStarted = false;
        private boolean intakeFinished = false;

        ShootAndIntakeAction(ShooterSubsystem shooter) {
            this.shooter = shooter;
        }

        @Override
        public boolean run(TelemetryPacket packet) {

            if (!started) {
                started = true;
                timer.reset();
            }

            shooter.setShooterEnabled(true);

            // Spin up before the first shot.
            if (shotsFired == 0 &&
                    timer.seconds() < SHOOTER_SPINUP_SECONDS) {

                shooter.updateIndexer(false);
                shooter.updateIntake(0, 0);
                return true;
            }

            // After two shots, run the intake for two seconds.
            if (shotsFired >= SHOTS_BEFORE_INTAKE
                    && !intakeFinished) {

                // Keep the indexer idle during intake.
                shooter.updateIndexer(false);

                if (!intakeStarted) {
                    intakeStarted = true;
                    timer.reset();
                }

                if (timer.seconds() < INTAKE_SECONDS) {
                    shooter.updateIntake(INTAKE_POWER, 0);
                } else {
                    shooter.updateIntake(0, 0);
                    intakeFinished = true;
                }

                packet.put("Shots fired", shotsFired);
                packet.put("Intake", intakeFinished
                        ? "Finished" : "Running");
                packet.put("Intake seconds",
                        timer.seconds());
                return true;
            }

            // Trigger one indexer cycle.
            if (!triggerSent && !waitingForIndexer) {
                shooter.updateIndexer(true);

                triggerSent = true;
                waitingForIndexer = true;
                return true;
            }

            // Continue updating until the indexer returns to rest.
            shooter.updateIndexer(false);
            shooter.updateIntake(0, 0);

            if (waitingForIndexer &&
                    shooter.getStateName().equals("IDLE")) {

                shotsFired++;
                waitingForIndexer = false;
                triggerSent = false;
            }

            packet.put("Shots fired", shotsFired);
            packet.put("Target shots", TOTAL_SHOTS);
            packet.put("Indexer", shooter.getStateName());

            if (shotsFired >= TOTAL_SHOTS) {
                shooter.updateIntake(0, 0);
                shooter.setShooterEnabled(false);
                return false;
            }

            return true;
        }
    }
}
