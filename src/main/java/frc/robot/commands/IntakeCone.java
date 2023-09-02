// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.RobotPreferences.prefElevator;
import frc.robot.RobotPreferences.prefIntake;
import frc.robot.RobotPreferences.prefWrist;
import frc.robot.subsystems.Elevator;
import frc.robot.subsystems.NewIntake;
import frc.robot.subsystems.Wrist;

public class IntakeCone extends SequentialCommandGroup {

  Wrist subWrist;
  NewIntake subIntake;
  Elevator subElevator;

  public IntakeCone(Wrist subWrist, NewIntake subIntake, Elevator subElevator) {

    this.subWrist = subWrist;
    this.subIntake = subIntake;

    addCommands(
        Commands.parallel(
            Commands.runOnce(() -> subWrist.setWristAngle(prefWrist.wristIntakingAngle.getValue())),
            Commands.runOnce(() -> subElevator.setElevatorPosition(prefElevator.elevatorIntakingPos.getValue()))),

        Commands.parallel(
            Commands.runOnce(() -> subIntake.setInsideMotorSpeed(prefIntake.intakeIntakeSpeed.getValue())),
            Commands.runOnce(() -> subIntake.setOutsideMotorSpeed(prefIntake.intakeIntakeSpeed.getValue())))
            .until(() -> subIntake.isGamePieceCollected()),

        Commands.runOnce(() -> subWrist.setWristAngle(prefWrist.wristStowAngle.getValue())));
  }
}
