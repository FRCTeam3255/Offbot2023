// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Auto;

import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.RobotPreferences.prefArm;
import frc.robot.RobotPreferences.prefIntake;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class CubeThenDock extends SequentialCommandGroup {

  Drivetrain subDrivetrain;
  Intake subIntake;
  Arm subArm;

  public CubeThenDock(Drivetrain subDrivetrain, Intake subIntake, Arm subArm) {
    this.subDrivetrain = subDrivetrain;
    this.subIntake = subIntake;
    this.subArm = subArm;

    addCommands(
        Commands.waitSeconds(1),
        subDrivetrain.swerveAutoBuilder.resetPose(subDrivetrain.cubeThenDockPath),
        Commands
            .run(() -> subArm.setGoalAngles(prefArm.armShootCubeHighShoulderAngle, prefArm.armShootCubeHighElbowAngle))
            .until(() -> subArm.areJointsInTolerance()),
        Commands.waitSeconds(0.5),

        Commands.run(() -> subIntake.setMotorSpeed(prefIntake.intakeShootSpeedHigh), subIntake)
            .until(() -> !subIntake.isGamePieceCollected()),
        Commands.waitSeconds(prefIntake.intakeReleaseDelay.getValue()),
        Commands.runOnce(() -> subIntake.setMotorSpeed(prefIntake.intakeHoldSpeed), subIntake),

        subDrivetrain.swerveAutoBuilder.fullAuto(subDrivetrain.cubeThenDockPath)
            .andThen(Commands.runOnce(() -> subDrivetrain.setDefenseMode(), subDrivetrain)));

  }
}