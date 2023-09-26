package de.fhg.iais.roberta.typecheck;

import java.util.ArrayList;
import java.util.List;

import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.serial.SerialWriteAction;
import de.fhg.iais.roberta.syntax.lang.blocksequence.Location;
import de.fhg.iais.roberta.syntax.lang.blocksequence.MainTask;
import de.fhg.iais.roberta.syntax.lang.expr.ActionExpr;
import de.fhg.iais.roberta.syntax.lang.expr.Binary;
import de.fhg.iais.roberta.syntax.lang.expr.BoolConst;
import de.fhg.iais.roberta.syntax.lang.expr.ColorConst;
import de.fhg.iais.roberta.syntax.lang.expr.EmptyExpr;
import de.fhg.iais.roberta.syntax.lang.expr.EmptyList;
import de.fhg.iais.roberta.syntax.lang.expr.ExprList;
import de.fhg.iais.roberta.syntax.lang.expr.FunctionExpr;
import de.fhg.iais.roberta.syntax.lang.expr.ListCreate;
import de.fhg.iais.roberta.syntax.lang.expr.MathConst;
import de.fhg.iais.roberta.syntax.lang.expr.MethodExpr;
import de.fhg.iais.roberta.syntax.lang.expr.NNGetBias;
import de.fhg.iais.roberta.syntax.lang.expr.NNGetOutputNeuronVal;
import de.fhg.iais.roberta.syntax.lang.expr.NNGetWeight;
import de.fhg.iais.roberta.syntax.lang.expr.NullConst;
import de.fhg.iais.roberta.syntax.lang.expr.NumConst;
import de.fhg.iais.roberta.syntax.lang.expr.RgbColor;
import de.fhg.iais.roberta.syntax.lang.expr.SensorExpr;
import de.fhg.iais.roberta.syntax.lang.expr.StmtExpr;
import de.fhg.iais.roberta.syntax.lang.expr.StringConst;
import de.fhg.iais.roberta.syntax.lang.expr.Unary;
import de.fhg.iais.roberta.syntax.lang.expr.Var;
import de.fhg.iais.roberta.syntax.lang.expr.VarDeclaration;
import de.fhg.iais.roberta.syntax.lang.functions.GetSubFunct;
import de.fhg.iais.roberta.syntax.lang.functions.IndexOfFunct;
import de.fhg.iais.roberta.syntax.lang.functions.IsListEmptyFunct;
import de.fhg.iais.roberta.syntax.lang.functions.LengthOfListFunct;
import de.fhg.iais.roberta.syntax.lang.functions.ListGetIndex;
import de.fhg.iais.roberta.syntax.lang.functions.ListRepeat;
import de.fhg.iais.roberta.syntax.lang.functions.ListSetIndex;
import de.fhg.iais.roberta.syntax.lang.functions.MathCastCharFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathCastStringFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathConstrainFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathModuloFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathNumPropFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathOnListFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathPowerFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathRandomFloatFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathRandomIntFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathSingleFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextCharCastNumberFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextJoinFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextPrintFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextStringCastNumberFunct;
import de.fhg.iais.roberta.syntax.lang.methods.MethodCall;
import de.fhg.iais.roberta.syntax.lang.methods.MethodIfReturn;
import de.fhg.iais.roberta.syntax.lang.methods.MethodReturn;
import de.fhg.iais.roberta.syntax.lang.methods.MethodVoid;
import de.fhg.iais.roberta.syntax.lang.stmt.ActionStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.AssertStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.AssignStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.DebugAction;
import de.fhg.iais.roberta.syntax.lang.stmt.ExprStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.FunctionStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.IfStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.MathChangeStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.MethodStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.NNSetBiasStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.NNSetInputNeuronVal;
import de.fhg.iais.roberta.syntax.lang.stmt.NNSetWeightStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.NNStepStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.RepeatStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.SensorStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtFlowCon;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtList;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtTextComment;
import de.fhg.iais.roberta.syntax.lang.stmt.TernaryExpr;
import de.fhg.iais.roberta.syntax.lang.stmt.TextAppendStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitTimeStmt;
import de.fhg.iais.roberta.syntax.sensor.generic.GetSampleSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerReset;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.visitor.BaseVisitor;
import de.fhg.iais.roberta.visitor.IVisitor;
import de.fhg.iais.roberta.visitor.lang.ILanguageVisitor;

/**
 * This class is implementing {@link IVisitor}. All methods are implemented and they append a human-readable JAVA code representation of a phrase to a
 * StringBuilder. <b>This representation is correct JAVA code.</b> <br>
 */
public class InfoCollector<Void> extends BaseVisitor<Void> implements ILanguageVisitor<Void> {

    private final List<NepoInfo> infos = new ArrayList<>();

    /**
     * initialize the info collector visitor.
     */
    private InfoCollector() {
    }

    /**
     * collects the infos generated during typechecking for an AST. This is done by a visitor, which is an instance of this class<br>
     *
     * @param phrase whose infos should be collected
     * @return list of collected infos
     */
    public static List<NepoInfo> collectInfos(Phrase phrase) //
    {
        InfoCollector astVisitor = new InfoCollector<>();
        phrase.accept(astVisitor);
        return astVisitor.infos;
    }

    @Override
    public Void visitNumConst(NumConst numConst) {
        extractInfos(numConst);
        return null;
    }

    @Override
    public Void visitMathConst(MathConst mathConst) {
        extractInfos(mathConst);
        return null;
    }

    @Override
    public Void visitBoolConst(BoolConst boolConst) {
        extractInfos(boolConst);
        return null;
    }

    @Override
    public Void visitStringConst(StringConst stringConst) {
        extractInfos(stringConst);
        return null;
    }

    @Override
    public Void visitTextAppendStmt(TextAppendStmt textAppendStmt) {
        return null;
    }

    @Override
    public Void visitNullConst(NullConst nullConst) {
        extractInfos(nullConst);
        return null;
    }

    @Override
    public Void visitColorConst(ColorConst colorConst) {
        extractInfos(colorConst);
        return null;
    }

    @Override
    public Void visitVar(Var var) {
        extractInfos(var);
        return null;
    }

    @Override
    public Void visitUnary(Unary unary) {
        extractInfos(unary);
        return null;
    }

    @Override
    public Void visitBinary(Binary binary) {
        binary.left.accept(this);
        binary.right.accept(this);
        extractInfos(binary);
        return null;
    }

    @Override
    public Void visitMathPowerFunct(MathPowerFunct func) {
        // func.extractInfos(func);
        return null;
    }

    @Override
    public Void visitActionExpr(ActionExpr actionExpr) {
        extractInfos(actionExpr);
        return null;
    }

    @Override
    public Void visitSensorExpr(SensorExpr sensorExpr) {
        extractInfos(sensorExpr);
        return null;
    }

    @Override
    public Void visitEmptyExpr(EmptyExpr emptyExpr) {
        extractInfos(emptyExpr);
        return null;
    }

    @Override
    public Void visitExprList(ExprList exprList) {
        extractInfos(exprList);
        return null;
    }

    @Override
    public Void visitActionStmt(ActionStmt actionStmt) {
        extractInfos(actionStmt);
        return null;
    }

    @Override
    public Void visitAssignStmt(AssignStmt assignStmt) {
        assignStmt.expr.accept(this);
        extractInfos(assignStmt);
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        extractInfos(exprStmt);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        extractInfos(ifStmt);
        return null;
    }

    @Override
    public Void visitTernaryExpr(TernaryExpr ternaryExpr) {
        extractInfos(ternaryExpr);
        return null;
    }

    @Override
    public Void visitNNStepStmt(NNStepStmt nnStepStmt) {
        extractInfos(nnStepStmt);
        return null;
    }

    @Override
    public Void visitNNSetInputNeuronVal(NNSetInputNeuronVal val) {
        extractInfos(val);
        return null;
    }

    @Override
    public Void visitNNGetOutputNeuronVal(NNGetOutputNeuronVal nnGetOutputNeuronVal) {
        extractInfos(nnGetOutputNeuronVal);
        return null;
    }

    @Override
    public Void visitNNSetWeightStmt(NNSetWeightStmt nnSetWeightStmt) {
        return null;
    }

    @Override
    public Void visitNNSetBiasStmt(NNSetBiasStmt nnSetBiasStmt) {
        return null;
    }

    @Override
    public Void visitNNGetWeight(NNGetWeight nnGetWeight) {
        return null;
    }

    @Override
    public Void visitNNGetBias(NNGetBias nnGetBias) {
        return null;
    }

    @Override
    public Void visitRepeatStmt(RepeatStmt repeatStmt) {
        extractInfos(repeatStmt);
        return null;
    }

    @Override
    public Void visitSensorStmt(SensorStmt sensorStmt) {
        extractInfos(sensorStmt);
        return null;
    }

    @Override
    public Void visitStmtFlowCon(StmtFlowCon stmtFlowCon) {
        extractInfos(stmtFlowCon);
        return null;
    }

    @Override
    public Void visitStmtList(StmtList stmtList) {
        extractInfos(stmtList);
        return null;
    }

    @Override
    public Void visitRgbColor(RgbColor rgbColor) {
        extractInfos(rgbColor);
        return null;
    }

    @Override
    public Void visitTimerSensor(TimerSensor timerSensor) {
        extractInfos(timerSensor);
        return null;
    }

    @Override
    public Void visitTimerReset(TimerReset timerReset) {
        return null;
    }

    @Override
    public Void visitMainTask(MainTask mainTask) {
        extractInfos(mainTask);
        return null;
    }

    @Override
    public Void visitWaitStmt(WaitStmt waitStmt) {
        extractInfos(waitStmt);
        return null;
    }

    private void extractInfos(Phrase phrase) {
        this.infos.addAll(phrase.getInfos().getInfos());
    }

    @Override
    public Void visitLocation(Location location) {
        this.infos.addAll(location.getInfos().getInfos());
        return null;
    }

    @Override
    public Void visitEmptyList(EmptyList emptyList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGetSampleSensor(GetSampleSensor sensorGetSample) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTextPrintFunct(TextPrintFunct textPrintFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFunctionExpr(FunctionExpr functionExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGetSubFunct(GetSubFunct getSubFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIndexOfFunct(IndexOfFunct indexOfFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLengthOfListFunct(LengthOfListFunct lengthOfListFunct) {
        return null;
    }

    @Override
    public Void visitIsListEmptyFunct(IsListEmptyFunct isListEmptyFunct) {
        return null;
    }

    @Override
    public Void visitListCreate(ListCreate listCreate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitListGetIndex(ListGetIndex listGetIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitListRepeat(ListRepeat listRepeat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitListSetIndex(ListSetIndex listSetIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMathConstrainFunct(MathConstrainFunct mathConstrainFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMathModuloFunct(MathModuloFunct mathModuloFunct) {
        return null;
    }

    @Override
    public Void visitMathNumPropFunct(MathNumPropFunct mathNumPropFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMathOnListFunct(MathOnListFunct mathOnListFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMathRandomFloatFunct(MathRandomFloatFunct mathRandomFloatFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMathRandomIntFunct(MathRandomIntFunct mathRandomIntFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMathSingleFunct(MathSingleFunct mathSingleFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMathCastStringFunct(MathCastStringFunct mathCastStringFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMathChangeStmt(MathChangeStmt mathChangeStmt) {
        return null;
    }

    @Override
    public Void visitMathCastCharFunct(MathCastCharFunct mathCastCharFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTextCharCastNumberFunct(TextCharCastNumberFunct textCharCastNumberFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTextStringCastNumberFunct(TextStringCastNumberFunct textStringCastNumberFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTextJoinFunct(TextJoinFunct textJoinFunct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitWaitTimeStmt(WaitTimeStmt waitTimeStmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclaration var) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMethodVoid(MethodVoid methodVoid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMethodReturn(MethodReturn methodReturn) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMethodIfReturn(MethodIfReturn methodIfReturn) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMethodStmt(MethodStmt methodStmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMethodCall(MethodCall methodCall) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMethodExpr(MethodExpr methodExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitStmtExpr(StmtExpr stmtExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitStmtTextComment(StmtTextComment stmtTextComment) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSerialWriteAction(SerialWriteAction serialWriteAction) {
        return null;
    }

    @Override
    public Void visitAssertStmt(AssertStmt assertStmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDebugAction(DebugAction debugAction) {
        // TODO Auto-generated method stub
        return null;
    }
}
