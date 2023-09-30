// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.frcteam3255.utils.SN_Math;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.constElevator;
import frc.robot.Constants.DesiredHeight;
import frc.robot.RobotMap.mapElevator;
import frc.robot.RobotPreferences.prefElevator;

public class Elevator extends SubsystemBase {

  TalonFX leftMotor;
  TalonFX rightMotor;

  TalonFXConfiguration config;
  StatorCurrentLimitConfiguration statorLimit;

  DesiredHeight desiredHeight;
  double desiredPosition;
  boolean isPrepped;

  public Elevator() {
    leftMotor = new TalonFX(mapElevator.LEFT_MOTOR_CAN);
    rightMotor = new TalonFX(mapElevator.RIGHT_MOTOR_CAN);
    config = new TalonFXConfiguration();

    desiredHeight = DesiredHeight.NONE;

    configure();
  }

  public void configure() {
    leftMotor.configFactoryDefault();
    rightMotor.configFactoryDefault();

    config.slot0.kP = prefElevator.elevatorP.getValue();
    config.slot0.kI = prefElevator.elevatorI.getValue();
    config.slot0.kD = prefElevator.elevatorD.getValue();

    config.slot0.allowableClosedloopError = SN_Math.metersToFalcon(prefElevator.elevatorPIDTolerance.getValue(),
        constElevator.CIRCUMFRENCE, constElevator.GEAR_RATIO);
    config.motionCruiseVelocity = SN_Math.metersToFalcon(prefElevator.elevatorMaxVelocity.getValue(),
        constElevator.CIRCUMFRENCE, constElevator.GEAR_RATIO);
    config.motionAcceleration = SN_Math.metersToFalcon(prefElevator.elevatorMaxAccel.getValue(),
        constElevator.CIRCUMFRENCE, constElevator.GEAR_RATIO);

    leftMotor.setInverted(constElevator.INVERT_LEFT_MOTOR);
    rightMotor.setInverted(!constElevator.INVERT_LEFT_MOTOR);

    leftMotor.setNeutralMode(NeutralMode.Brake);
    rightMotor.setNeutralMode(NeutralMode.Brake);

    config.forwardSoftLimitThreshold = SN_Math.metersToFalcon(prefElevator.elevatorMaxPos.getValue(),
        constElevator.CIRCUMFRENCE,
        constElevator.GEAR_RATIO);
    config.reverseSoftLimitThreshold = SN_Math.metersToFalcon(prefElevator.elevatorMinPos.getValue(),
        constElevator.CIRCUMFRENCE,
        constElevator.GEAR_RATIO);

    config.forwardSoftLimitEnable = true;
    config.reverseSoftLimitEnable = true;

    // https://v5.docs.ctr-electronics.com/en/stable/ch13_MC.html?highlight=Current%20limit#new-api-in-2020
    // statorLimit = new StatorCurrentLimitConfiguration(true,
    // constElevator.CURRENT_LIMIT_FLOOR_AMPS,
    // 1000, constElevator.CURRENT_LIMIT_AFTER_SEC);

    leftMotor.configAllSettings(config);
    rightMotor.configAllSettings(config);

    // rightMotor.configStatorCurrentLimit(statorLimit);
    // leftMotor.configStatorCurrentLimit(statorLimit);

    leftMotor.follow(rightMotor);
  }

  /**
   * Set the speed of the Elevator. Includes safeties/soft stops.
   * 
   * @param speed Desired speed to set both of the motors to, as a PercentOutput
   *              (-1.0 to 1.0)
   * 
   */
  public void setElevatorSpeed(double speed) {
    leftMotor.set(ControlMode.PercentOutput, speed);
    rightMotor.set(ControlMode.PercentOutput, speed);
  }

  /**
   * Set the position of the Elevator. Includes safeties/soft stops.
   * 
   * @param position Desired position to set both of the motors to, in meters
   * 
   */
  public void setElevatorPosition(double position) {
    position = SN_Math.metersToFalcon(MathUtil.clamp(position,
        prefElevator.elevatorMinPos.getValue(),
        prefElevator.elevatorMaxPos.getValue()), constElevator.CIRCUMFRENCE, constElevator.GEAR_RATIO);

    leftMotor.set(ControlMode.MotionMagic, position);
    rightMotor.set(ControlMode.MotionMagic, position);
  }

  /**
   * Returns if the elevator is within its positional tolerance.
   * 
   * @return If it is at that position
   * 
   */
  public boolean isElevatorAtPosition() {
    return SN_Math.metersToFalcon(prefElevator.elevatorPositionTolerance.getValue(), constElevator.CIRCUMFRENCE,
        constElevator.GEAR_RATIO) >= Math.abs(rightMotor.getClosedLoopError());
  }

  /**
   * Returns the encoder counts of one motor on the elevator. (They should have
   * the same reading)
   * 
   * @return Elevator encoder counts
   * 
   */
  public double getElevatorEncoderCounts() {
    return rightMotor.getSelectedSensorPosition();
  }

  /**
   * Returns the position of the elevator, relative to itself, in meters.
   * 
   * @return Elevator position
   * 
   */
  public double getElevatorPositionMeters() {
    return getElevatorEncoderCounts() / prefElevator.elevatorEncoderCountsPerMeter.getValue();
  }

  public void neutralElevatorOutputs() {
    leftMotor.neutralOutput();
    rightMotor.neutralOutput();
  }

  public void setDesiredHeight(DesiredHeight height) {
    desiredHeight = height;
  }

  public DesiredHeight getDesiredHeight() {
    return desiredHeight;
  }

  public void setIsPrepped(boolean prepped) {
    prepped = isPrepped;
  }

  public boolean isPrepped() {
    return isPrepped;
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Elevator Encoder Counts", getElevatorEncoderCounts());
    SmartDashboard.putNumber("Elevator Position Meters", getElevatorPositionMeters());
    SmartDashboard.putNumber("Elevator Velocity", SN_Math.falconToMeters(rightMotor.getSelectedSensorVelocity(0),
        constElevator.CIRCUMFRENCE, constElevator.GEAR_RATIO));
    SmartDashboard.putNumber("Elevator Sator Amps", rightMotor.getStatorCurrent());
    SmartDashboard.putString("TEST Elevator desired height",
        getDesiredHeight().toString());
  }
}
