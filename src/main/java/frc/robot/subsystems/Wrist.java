// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotMap.mapWrist;
import frc.robot.RobotPreferences.prefWrist;

public class Wrist extends SubsystemBase {

  TalonFX wristMotor;

  TalonFXConfiguration config;

  public Wrist() {
    wristMotor = new TalonFX(mapWrist.WRIST_MOTOR_CAN);
    config = new TalonFXConfiguration();

    configure();
  }

  public void configure() {
    wristMotor.configFactoryDefault();
    wristMotor.configAllSettings(config);

    config.slot0.kF = prefWrist.wristF.getValue();
    config.slot0.kP = prefWrist.wristP.getValue();
    config.slot0.kI = prefWrist.wristI.getValue();
    config.slot0.kD = prefWrist.wristD.getValue();
  }

  public void setWristSpeed(double speed) {
    wristMotor.set(ControlMode.PercentOutput, speed);
  }

  /**
   * Set the angle of the wrist. Includes safeties/soft stops.
   * 
   * @param position Desired angle to set the motor to, in Encoder
   *                 ticks
   * 
   */
  public void setWristAngle(double angle) {
    angle = MathUtil.clamp(angle, prefWrist.wristMinPos.getValue(),
        prefWrist.wristMaxPos.getValue());

    wristMotor.set(ControlMode.Position, angle);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
