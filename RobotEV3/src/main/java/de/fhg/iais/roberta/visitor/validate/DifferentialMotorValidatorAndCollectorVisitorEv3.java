package de.fhg.iais.roberta.visitor.validate;

import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ClassToInstanceMap;

import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.components.UsedActor;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.Action;
import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.configuration.ConfigurationComponent;
import de.fhg.iais.roberta.syntax.lang.expr.Expr;
import de.fhg.iais.roberta.syntax.lang.expr.NumConst;
import de.fhg.iais.roberta.util.syntax.SC;
import de.fhg.iais.roberta.visitor.EV3DevMethods;
import de.fhg.iais.roberta.visitor.hardware.actor.IDifferentialMotorVisitor;

public abstract class DifferentialMotorValidatorAndCollectorVisitorEv3 extends MotorValidatorAndCollectorVisitor implements IDifferentialMotorVisitor<Void> {

    public DifferentialMotorValidatorAndCollectorVisitorEv3(
        ConfigurationAst robotConfiguration,
        ClassToInstanceMap<IProjectBean.IBuilder> beanBuilders) {
        super(robotConfiguration, beanBuilders);
    }

    @Override
    public Void visitCurveAction(CurveAction curveAction) {
        if ( Objects.equals(robotConfiguration.getRobotName(), "ev3dev") ) {
            /*
             Only add the helper methods for "ev3dev". Following methods are needed for visitCurveAction:
             -  EV3DevMethods.BUSY_WAIT, EV3DevMethods.CLAMP, EV3DevMethods.DRIVE_IN_CURVE, EV3DevMethods.SCALE_SPEED
            */
            usedMethodBuilder.addUsedMethod(EV3DevMethods.BUSY_WAIT);
            usedMethodBuilder.addUsedMethod(EV3DevMethods.CLAMP);
            usedMethodBuilder.addUsedMethod(EV3DevMethods.DRIVE_IN_CURVE);
            usedMethodBuilder.addUsedMethod(EV3DevMethods.SCALE_SPEED);
        }
        requiredComponentVisited(curveAction, curveAction.paramLeft.getSpeed(), curveAction.paramRight.getSpeed());
        Optional.ofNullable(curveAction.paramLeft.getDuration())
            .ifPresent(duration -> requiredComponentVisited(curveAction, duration.getValue()));
        Optional.ofNullable(curveAction.paramRight.getDuration())
            .ifPresent(duration -> requiredComponentVisited(curveAction, duration.getValue()));
        checkForZeroSpeedInCurve(curveAction.paramLeft.getSpeed(), curveAction.paramRight.getSpeed(), curveAction);
        checkLeftRightMotorPort(curveAction);
        addLeftAndRightMotorToUsedActors();
        return null;
    }

    @Override
    public Void visitTurnAction(TurnAction turnAction) {
        checkAndVisitMotionParam(turnAction, turnAction.param);
        checkLeftRightMotorPort(turnAction);
        addLeftAndRightMotorToUsedActors();
        return null;
    }

    @Override
    public Void visitDriveAction(DriveAction driveAction) {
        checkAndVisitMotionParam(driveAction, driveAction.param);
        checkLeftRightMotorPort(driveAction);
        addLeftAndRightMotorToUsedActors();
        return null;
    }

    public Void visitCurveActionForDiff(CurveAction curveAction) {
        requiredComponentVisited(curveAction, curveAction.paramLeft.getSpeed(), curveAction.paramRight.getSpeed());
        Optional.ofNullable(curveAction.paramLeft.getDuration())
            .ifPresent(duration -> requiredComponentVisited(curveAction, duration.getValue()));
        Optional.ofNullable(curveAction.paramRight.getDuration())
            .ifPresent(duration -> requiredComponentVisited(curveAction, duration.getValue()));
        checkForZeroSpeedInCurve(curveAction.paramLeft.getSpeed(), curveAction.paramRight.getSpeed(), curveAction);
        checkAndAddLeftRightMotorPortForDiff(curveAction);
        return null;
    }

    public Void visitTurnActionForDiff(TurnAction turnAction){
        checkAndVisitMotionParam(turnAction, turnAction.param);
        checkAndAddLeftRightMotorPortForDiff(turnAction);
        return null;
    }

    public Void visitDriveActionForDiff(DriveAction driveAction){
        checkAndVisitMotionParam(driveAction, driveAction.param);
        checkAndAddLeftRightMotorPortForDiff(driveAction);
        return null;
    }

    private boolean checkPortsForDiff(Phrase driveAction){
        String leftMotor = robotConfiguration.getConfigurationComponent("Diff").getOptProperty("MOTOR_L");
        String rightMotor = robotConfiguration.getConfigurationComponent("Diff").getOptProperty("MOTOR_R");
        if (rightMotor.equals(leftMotor)){
            addErrorToPhrase(driveAction, "CONFIGURATION_ERROR_MULTIPLE_RIGHT_MOTORS");
            return true;
        }
        int numLeftMotors = 0;
        int numRightMotors = 0;
        for ( ConfigurationComponent component : robotConfiguration.getConfigurationComponentsValues() ){
            if ( component.componentType.equals("MOTOR") ){
                String motorPort =  component.getOptProperty("MOTOR");
                if (motorPort.equals(leftMotor)){
                    numRightMotors++;
                }else if ( motorPort.equals(rightMotor) ){
                    numLeftMotors++;
                }
                if ( numRightMotors > 1 || numLeftMotors > 1 ){
                    addErrorToPhrase(driveAction, "");
                    return true;
                }
            }
        }
        if ( numRightMotors != 1 || numLeftMotors != 1 ){
            addErrorToPhrase(driveAction, "CONFIGURATION_ERROR_MOTOR_MISSING");
            return true;
        }
            return false;
    }

    private void checkAndAddLeftRightMotorPortForDiff(Phrase driveAction){
        ConfigurationComponent differentialDrive = robotConfiguration.getConfigurationComponent("Diff");
        if (differentialDrive == null){
            addErrorToPhrase(driveAction, "CONFIGURATION_ERROR_MOTOR_LEFT_MISSING");
        }
        if ( checkPortsForDiff(driveAction) ) {
            return;
        }
        usedHardwareBuilder.addUsedActor(new UsedActor(robotConfiguration.getConfigurationComponent("Diff").getOptProperty("MOTOR_L"), SC.LARGE));//TODO: nicht über "Diff" zugreifen
        usedHardwareBuilder.addUsedActor(new UsedActor(robotConfiguration.getConfigurationComponent("Diff").getOptProperty("MOTOR_R"), SC.LARGE));
    }

    @Override
    public Void visitMotorDriveStopAction(MotorDriveStopAction stopAction) {
        checkLeftRightMotorPort(stopAction);
        addLeftAndRightMotorToUsedActors();
        return null;
    }

    private void checkLeftRightMotorPort(Phrase driveAction) {
        if ( hasTooManyMotors(driveAction) ) {
            return;
        }

        ConfigurationComponent leftMotor = this.robotConfiguration.getFirstMotor(SC.LEFT);
        ConfigurationComponent rightMotor = this.robotConfiguration.getFirstMotor(SC.RIGHT);
        checkLeftMotorPresenceAndRegulation(driveAction, leftMotor);
        checkRightMotorPresenceAndRegulation(driveAction, rightMotor);
        checkMotorRotationDirection(driveAction, leftMotor, rightMotor);
    }

    protected boolean hasTooManyMotors(Phrase driveAction) {
        if ( this.robotConfiguration.getMotors(SC.RIGHT).size() > 1 ) {
            addErrorToPhrase(driveAction, "CONFIGURATION_ERROR_MULTIPLE_RIGHT_MOTORS");
            return true;
        }
        if ( this.robotConfiguration.getMotors(SC.LEFT).size() > 1 ) {
            addErrorToPhrase(driveAction, "CONFIGURATION_ERROR_MULTIPLE_LEFT_MOTORS");
            return true;
        }
        return false;
    }

    private void checkRightMotorPresenceAndRegulation(Phrase driveAction, ConfigurationComponent rightMotor) {
        if ( rightMotor == null ) {
            addErrorToPhrase(driveAction, "CONFIGURATION_ERROR_MOTOR_RIGHT_MISSING");
        } else {
            checkIfMotorRegulated(driveAction, rightMotor, "CONFIGURATION_ERROR_MOTOR_RIGHT_UNREGULATED");
        }
    }

    private void checkLeftMotorPresenceAndRegulation(Phrase driveAction, ConfigurationComponent leftMotor) {
        if ( leftMotor == null ) {
            addErrorToPhrase(driveAction, "CONFIGURATION_ERROR_MOTOR_LEFT_MISSING");
        } else {
            checkIfMotorRegulated(driveAction, leftMotor, "CONFIGURATION_ERROR_MOTOR_LEFT_UNREGULATED");
        }
    }

    private void checkIfMotorRegulated(Phrase driveAction, ConfigurationComponent motor, String errorMsg) {
        if ( !motor.getProperty(SC.MOTOR_REGULATION).equals(SC.TRUE) ) {
            addErrorToPhrase(driveAction, errorMsg);
        }
    }

    private void checkMotorRotationDirection(Phrase driveAction, ConfigurationComponent leftMotor, ConfigurationComponent rightMotor) {
        if ( (leftMotor == null) || (rightMotor == null) ) {
            return;
        }

        boolean rotationDirectionsEqual = leftMotor.getProperty(SC.MOTOR_REVERSE).equals(rightMotor.getProperty(SC.MOTOR_REVERSE));
        if ( !rotationDirectionsEqual ) {
            addErrorToPhrase(driveAction, "CONFIGURATION_ERROR_MOTORS_ROTATION_DIRECTION");
        }
    }

    private void checkForZeroSpeedInCurve(Expr speedLeft, Expr speedRight, Action action) {
        if ( speedLeft.getKind().hasName("NUM_CONST") && speedRight.getKind().hasName("NUM_CONST") ) {
            double speedLeftNumConst = Double.parseDouble(((NumConst) speedLeft).value);
            double speedRightNumConst = Double.parseDouble(((NumConst) speedRight).value);

            boolean bothMotorsHaveZeroSpeed = (Math.abs(speedLeftNumConst) < DOUBLE_EPS) && (Math.abs(speedRightNumConst) < DOUBLE_EPS);
            if ( bothMotorsHaveZeroSpeed ) {
                addWarningToPhrase(action, "MOTOR_SPEED_0");
            }
        }
    }

    private void addLeftAndRightMotorToUsedActors() {
        Optional<String> optionalLeftPort = Optional.ofNullable(robotConfiguration.getFirstMotor(SC.LEFT))
            .map(configurationComponent1 -> configurationComponent1.userDefinedPortName);

        Optional<String> optionalRightPort = Optional.ofNullable(robotConfiguration.getFirstMotor(SC.RIGHT))
            .map(configurationComponent -> configurationComponent.userDefinedPortName);

        if ( optionalLeftPort.isPresent() && optionalRightPort.isPresent() ) {
            usedHardwareBuilder.addUsedActor(new UsedActor(optionalLeftPort.get(), SC.LARGE));
            usedHardwareBuilder.addUsedActor(new UsedActor(optionalRightPort.get(), SC.LARGE));
        }
    }
}