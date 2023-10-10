package de.fhg.iais.roberta.exprEvaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.RuleNode;

import de.fhg.iais.roberta.exprly.generated.ExprlyBaseVisitor;
import de.fhg.iais.roberta.exprly.generated.ExprlyParser;
import de.fhg.iais.roberta.exprly.generated.ExprlyParser.ExpressionContext;
import de.fhg.iais.roberta.inter.mode.general.IMode;
import de.fhg.iais.roberta.mode.general.IndexLocation;
import de.fhg.iais.roberta.mode.general.ListElementOperations;
import de.fhg.iais.roberta.syntax.lang.expr.Binary;
import de.fhg.iais.roberta.syntax.lang.expr.BoolConst;
import de.fhg.iais.roberta.syntax.lang.expr.ColorConst;
import de.fhg.iais.roberta.syntax.lang.expr.ConnectConst;
import de.fhg.iais.roberta.syntax.lang.expr.EmptyExpr;
import de.fhg.iais.roberta.syntax.lang.expr.Expr;
import de.fhg.iais.roberta.syntax.lang.expr.ExprList;
import de.fhg.iais.roberta.syntax.lang.expr.FunctionExpr;
import de.fhg.iais.roberta.syntax.lang.expr.ListCreate;
import de.fhg.iais.roberta.syntax.lang.expr.MathConst;
import de.fhg.iais.roberta.syntax.lang.expr.MathConst.Const;
import de.fhg.iais.roberta.syntax.lang.expr.NullConst;
import de.fhg.iais.roberta.syntax.lang.expr.NumConst;
import de.fhg.iais.roberta.syntax.lang.expr.RgbColor;
import de.fhg.iais.roberta.syntax.lang.expr.StringConst;
import de.fhg.iais.roberta.syntax.lang.expr.Unary;
import de.fhg.iais.roberta.syntax.lang.expr.Var;
import de.fhg.iais.roberta.syntax.lang.functions.GetSubFunct;
import de.fhg.iais.roberta.syntax.lang.functions.IndexOfFunct;
import de.fhg.iais.roberta.syntax.lang.functions.IsListEmptyFunct;
import de.fhg.iais.roberta.syntax.lang.functions.LengthOfListFunct;
import de.fhg.iais.roberta.syntax.lang.functions.ListGetIndex;
import de.fhg.iais.roberta.syntax.lang.functions.ListRepeat;
import de.fhg.iais.roberta.syntax.lang.functions.ListSetIndex;
import de.fhg.iais.roberta.syntax.lang.functions.MathConstrainFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathNumPropFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathOnListFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathPowerFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathRandomFloatFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathRandomIntFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathSingleFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextJoinFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextPrintFunct;
import de.fhg.iais.roberta.syntax.lang.stmt.ExprStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtList;
import de.fhg.iais.roberta.syntax.lang.stmt.TernaryExpr;
import de.fhg.iais.roberta.transformer.AnnotationHelper;
import de.fhg.iais.roberta.typecheck.BlocklyType;
import de.fhg.iais.roberta.util.ast.BlocklyProperties;
import de.fhg.iais.roberta.util.dbc.DbcException;
import de.fhg.iais.roberta.util.syntax.FunctionNames;

public class ExprlyVisitor extends ExprlyBaseVisitor<Expr> {

    /**
     * @return AST instance for the whole expression
     */
    @Override
    public Expr visitExpression(ExpressionContext ctx) {
        return visit(ctx.expr());
    }

    /**
     * @return AST instance of null const
     */
    @Override
    public NullConst visitNullConst(ExprlyParser.NullConstContext ctx) {
        return new NullConst(mkPropertyFromClass(NullConst.class));
    }

    /**
     * @return AST instance of a color
     */
    @Override
    public ColorConst visitCol(ExprlyParser.ColContext ctx) {
        String colorText = ctx.COLOR().getText();
        String colorHex;
        switch ( colorText ) {
            case "#black":
                colorHex = "#000000";
                break;
            case "#blue":
                colorHex = "#0057A6";
                break;
            case "#green":
                colorHex = "#00642E";
                break;
            case "#yellow":
                colorHex = "#F7D117";
                break;
            case "#red":
                colorHex = "#B30006";
                break;
            case "#white":
                colorHex = "#FFFFFF";
                break;
            case "#brown":
                colorHex = "#532115";
                break;
            case "#NONW":
                colorHex = "#585858";
                break;
            default:
                colorHex = colorText;
        }
        return new ColorConst(mkInlineProperty("robColour_picker"), colorHex);
    }

    /**
     * @return AST instance of a binary boolean operation
     */
    @Override
    public Binary visitBinaryB(ExprlyParser.BinaryBContext ctx) throws UnsupportedOperationException {
        Expr p = visit(ctx.expr(0));
        Expr q = visit(ctx.expr(1));
        p.setReadOnly();
        q.setReadOnly();
        if ( ctx.op.getType() == ExprlyParser.AND ) {
            return new Binary(Binary.Op.AND, p, q, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.OR ) {
            return new Binary(Binary.Op.OR, p, q, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.EQUAL ) {
            return new Binary(Binary.Op.EQ, p, q, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.NEQUAL ) {
            return new Binary(Binary.Op.NEQ, p, q, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.GET ) {
            return new Binary(Binary.Op.GT, p, q, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.LET ) {
            return new Binary(Binary.Op.LT, p, q, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.GEQ ) {
            return new Binary(Binary.Op.GTE, p, q, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.LEQ ) {
            return new Binary(Binary.Op.LTE, p, q, "", mkInlineProperty("math_arithmetic"));
        }
        throw new UnsupportedOperationException("Invalid binary operation");

    }

    /**
     * @return AST instance of a binary number operation
     */
    @Override
    public Expr visitBinaryN(ExprlyParser.BinaryNContext ctx) throws UnsupportedOperationException {
        Expr n0 = visit(ctx.expr(0));
        Expr n1 = visit(ctx.expr(1));
        n0.setReadOnly();
        n1.setReadOnly();
        if ( ctx.op.getType() == ExprlyParser.POW ) {
            List<Expr> args = new LinkedList();
            args.add(n0);
            args.add(n1);
            return new MathPowerFunct(mkPropertyFromClass(MathPowerFunct.class), FunctionNames.POWER, args);
        }
        if ( ctx.op.getType() == ExprlyParser.ADD ) {
            return new Binary(Binary.Op.ADD, n0, n1, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.SUB ) {
            return new Binary(Binary.Op.MINUS, n0, n1, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.MUL ) {
            return new Binary(Binary.Op.MULTIPLY, n0, n1, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.DIV ) {
            return new Binary(Binary.Op.DIVIDE, n0, n1, "", mkInlineProperty("math_arithmetic"));
        }
        if ( ctx.op.getType() == ExprlyParser.MOD ) {
            return new Binary(Binary.Op.MOD, n0, n1, "", mkInlineProperty("math_arithmetic"));
        }
        throw new UnsupportedOperationException("Invalid binary operation");

    }

    /**
     * @return AST instance of a bool const
     */
    @Override
    public BoolConst visitBoolConstB(ExprlyParser.BoolConstBContext ctx) {
        return new BoolConst(mkPropertyFromClass(BoolConst.class), Boolean.parseBoolean(ctx.BOOL().getText().toLowerCase()));
    }

    /**
     * @return AST instance of a string const
     */
    @Override
    public StringConst visitConstStr(ExprlyParser.ConstStrContext ctx) {
        String s = "";
        int c = ctx.getChildCount();
        for ( int i = 1; i < c - 1; i++ ) {
            s = s + ctx.getChild(i).toString();
            if ( i != c - 2 ) {
                s += " ";
            }
        }
        return new StringConst(mkPropertyFromClass(StringConst.class), s);
    }

    /**
     * @return AST instance of a math const
     */
    @Override
    public MathConst visitMathConst(ExprlyParser.MathConstContext ctx) {
        String c = ctx.CONST().getText();
        if ( c.equals("phi") ) {
            c = "golden_ratio";
        }
        if ( c.equals("inf") ) {
            c = "infinity";
        }
        if ( c.equals("sqrt_1_2") ) {
            c = "sqrt1_2";
        }
        return new MathConst(mkPropertyFromClass(MathConst.class), Const.get(c));
    }

    /**
     * @return AST instance of a num const
     */
    @Override
    public NumConst visitIntConst(ExprlyParser.IntConstContext ctx) {
        return new NumConst(null, ctx.INT().getText());
    }

    /**
     * @return AST instance of a function
     */
    @Override
    public Expr visitFunc(ExprlyParser.FuncContext ctx) throws UnsupportedOperationException {

        String f = ctx.FNAME().getText();
        List<Expr> args = new LinkedList();

        for ( ExprlyParser.ExprContext expr : ctx.expr() ) {
            Expr ast = visit(expr);
            ast.setReadOnly();
            args.add(ast);
        }

        for ( int i = 0; i < args.size(); i++ ) {
            if ( args.get(i) instanceof ExprList ) {
                ExprList e = (ExprList) args.get(i);
                e.setReadOnly();
                args.set(i, new ListCreate(BlocklyType.ARRAY, e, mkPropertyFromClass(ListCreate.class)));
            }
        }

        // check the function name and return the corresponfing one math_random_float
        if ( f.equals("randInt") ) {
            return new FunctionExpr(new MathRandomIntFunct(mkProperty("math_random_int"), args.get(0), args.get(1)));
        }
        if ( f.equals("randFloat") ) {
            return new FunctionExpr(new MathRandomFloatFunct(mkProperty("math_random_float")));
        }
        if ( f.equals("sqrt") ) {
            f = "root";
        }
        if ( f.equals("isEven") || f.equals("isOdd") || f.equals("isPrime") || f.equals("isWhole") || f.equals("isPositive") || f.equals("isNegative") ) {
            f = f.substring(2);
            return new FunctionExpr(new MathNumPropFunct(FunctionNames.get(f), args, mkPropertyFromClass(MathNumPropFunct.class)));
        }
        if ( f.equals("isDivisibleBy") ) {
            return new FunctionExpr(new MathNumPropFunct(FunctionNames.DIVISIBLE_BY, args, mkPropertyFromClass(MathNumPropFunct.class)));
        }
        if ( f.equals("avg") ) {
            f = "average";
            return new FunctionExpr(new MathOnListFunct(mkPropertyFromClass(MathOnListFunct.class), null, FunctionNames.get(f), args.get(0)));
        }
        if ( f.equals("sd") ) {
            f = "std_dev";
            return new FunctionExpr(new MathOnListFunct(mkPropertyFromClass(MathOnListFunct.class), null, FunctionNames.get(f), args.get(0)));
        }
        if ( f.equals("randItem") ) {
            f = "random";
            return new FunctionExpr(new MathOnListFunct(mkPropertyFromClass(MathOnListFunct.class), null, FunctionNames.get(f), args.get(0)));
        }
        if ( f.equals("min") || f.equals("max") || f.equals("sum") || f.equals("median") ) {
            return new FunctionExpr(new MathOnListFunct(mkPropertyFromClass(MathOnListFunct.class), null, FunctionNames.get(f), args.get(0)));
        }
        if ( f.equals("lengthOf") ) {
            return new FunctionExpr(new LengthOfListFunct(mkPropertyFromClass(LengthOfListFunct.class), args.get(0)));
        }
        if ( f.equals("indexOfFirst") ) {
            return new FunctionExpr(new IndexOfFunct(mkPropertyFromClass(IndexOfFunct.class), IndexLocation.FIRST, args.get(0), args.get(1)));
        }
        if ( f.equals("indexOfLast") ) {
            return new FunctionExpr(new IndexOfFunct(mkPropertyFromClass(IndexOfFunct.class), IndexLocation.LAST, args.get(0), args.get(1)));
        }
        if ( f.contains("setIndex") ) {
            if ( f.equals("setIndex") ) {
                return new FunctionExpr(new ListSetIndex(ListElementOperations.SET, IndexLocation.FROM_START, args, mkPropertyFromClass(ListSetIndex.class)));
            }
            if ( f.equals("setIndexFromEnd") ) {
                return new FunctionExpr(new ListSetIndex(ListElementOperations.SET, IndexLocation.FROM_END, args, mkPropertyFromClass(ListSetIndex.class)));
            }
            if ( f.equals("setIndexFirst") ) {
                return new FunctionExpr(new ListSetIndex(ListElementOperations.SET, IndexLocation.FIRST, args, mkPropertyFromClass(ListSetIndex.class)));
            }
            if ( f.equals("setIndexLast") ) {
                return new FunctionExpr(new ListSetIndex(ListElementOperations.SET, IndexLocation.LAST, args, mkPropertyFromClass(ListSetIndex.class)));
            }
        }
        if ( f.contains("insertIndex") ) {
            if ( f.equals("insertIndex") ) {
                return new FunctionExpr(new ListSetIndex(ListElementOperations.INSERT, IndexLocation.FROM_START, args, mkPropertyFromClass(ListSetIndex.class)));
            }
            if ( f.equals("insertIndexFromEnd") ) {
                return new FunctionExpr(new ListSetIndex(ListElementOperations.INSERT, IndexLocation.FROM_END, args, mkPropertyFromClass(ListSetIndex.class)));
            }
            if ( f.equals("insertIndexFirst") ) {
                return new FunctionExpr(new ListSetIndex(ListElementOperations.INSERT, IndexLocation.FIRST, args, mkPropertyFromClass(ListSetIndex.class)));
            }
            if ( f.equals("insertIndexLast") ) {
                return new FunctionExpr(new ListSetIndex(ListElementOperations.INSERT, IndexLocation.LAST, args, mkPropertyFromClass(ListSetIndex.class)));
            }
        }
        if ( f.contains("getIndex") ) {
            if ( f.equals("getIndex") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.GET, IndexLocation.FROM_START, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("getIndexFromEnd") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.GET, IndexLocation.FROM_END, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("getIndexFirst") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.GET, IndexLocation.FIRST, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("getIndexLast") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.GET, IndexLocation.LAST, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
        }
        if ( f.contains("getAndRemoveIndex") ) {
            if ( f.equals("getAndRemoveIndex") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.GET_REMOVE, IndexLocation.FROM_START, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("getAndRemoveIndexFromEnd") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.GET_REMOVE, IndexLocation.FROM_END, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("getAndRemoveIndexFirst") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.GET_REMOVE, IndexLocation.FIRST, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("getAndRemoveIndexLast") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.GET_REMOVE, IndexLocation.LAST, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
        }
        if ( f.contains("removeIndex") ) {
            if ( f.equals("removeIndex") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.REMOVE, IndexLocation.FROM_START, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("removeIndexFromEnd") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.REMOVE, IndexLocation.FROM_END, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("removeIndexFirst") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.REMOVE, IndexLocation.FIRST, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
            if ( f.equals("removeIndexLast") ) {
                return new FunctionExpr(new ListGetIndex(ListElementOperations.REMOVE, IndexLocation.LAST, args, "VOID", mkPropertyFromClass(ListGetIndex.class)));
            }
        }
        if ( f.equals("repeatList") ) {
            return new FunctionExpr(new ListRepeat(BlocklyType.VOID, args, mkPropertyFromClass(ListRepeat.class)));
        }
        if ( f.contains("subList") ) {

            if ( f.equals("subList") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FROM_START, IndexLocation.FROM_START)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
            if ( f.equals("subListFromIndexToLast") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FROM_START, IndexLocation.LAST)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
            if ( f.equals("subListFromIndexToEnd") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FROM_START, IndexLocation.FROM_END)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
            if ( f.equals("subListFromFirstToIndex") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FIRST, IndexLocation.FROM_START)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
            if ( f.equals("subListFromFirstToLast") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FIRST, IndexLocation.LAST)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
            if ( f.equals("subListFromFirstToEnd") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FIRST, IndexLocation.FROM_END)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
            if ( f.equals("subListFromEndToIndex") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FROM_END, IndexLocation.FROM_START)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
            if ( f.equals("subListFromEndToLast") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FROM_END, IndexLocation.LAST)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
            if ( f.equals("subListFromEndToEnd") ) {
                return new FunctionExpr(new GetSubFunct(FunctionNames.GET_SUBLIST, new ArrayList<IMode>(Arrays.asList(IndexLocation.FROM_END, IndexLocation.FROM_END)), args, mkPropertyFromClass(GetSubFunct.class)));
            }
        }
        if ( f.equals("print") ) {
            return new FunctionExpr(new TextPrintFunct(args, mkPropertyFromClass(TextPrintFunct.class)));
        }
        if ( f.equals("createTextWith") ) {
            ExprList args0 = new ExprList();
            for ( Expr e : args ) {
                e.setReadOnly();
                args0.addExpr(e);
            }
            args0.setReadOnly();
            return new FunctionExpr(new TextJoinFunct(args0, mkPropertyFromClass(TextJoinFunct.class)));

        }
        if ( f.equals("constrain") ) {
            return new FunctionExpr(new MathConstrainFunct(mkPropertyFromClass(MathConstrainFunct.class), args.get(0), args.get(1), args.get(2)));
        }
        if ( f.equals("isEmpty") ) {
            return new FunctionExpr(new IsListEmptyFunct(mkPropertyFromClass(IsListEmptyFunct.class), args.get(0)));
        }
        if ( f.equals("getRGB") ) {
            if ( args.size() == 3 ) {
                return (Expr) (RgbColor) new RgbColor(mkPropertyFromClass(RgbColor.class), args.get(0), args.get(1), args.get(2), new EmptyExpr(BlocklyType.NUMBER_INT));
            } else if ( args.size() == 4 ) {
                return (Expr) (RgbColor) new RgbColor(mkPropertyFromClass(RgbColor.class), args.get(0), args.get(1), args.get(2), args.get(3));
            } else {
                Expr empty = new EmptyExpr(BlocklyType.NUMBER_INT);
                return (Expr) (RgbColor) new RgbColor(mkPropertyFromClass(RgbColor.class), empty, empty, empty, empty);
            }
        }
        try {
            return new FunctionExpr(new MathSingleFunct(FunctionNames.get(f), args, mkPropertyFromClass(MathSingleFunct.class)));
        } catch ( Exception e ) {
            throw new UnsupportedOperationException("Invalid function name: " + f);
        }
    }

    /**
     * @return AST instance of a float const
     */
    @Override
    public NumConst visitFloatConst(ExprlyParser.FloatConstContext ctx) {
        return new NumConst(null, ctx.FLOAT().getText());
    }

    /**
     * @return AST instance of a unary boolean operation
     */
    @Override
    public Unary visitUnaryB(ExprlyParser.UnaryBContext ctx) throws UnsupportedOperationException {
        Expr e = visit(ctx.expr());
        e.setReadOnly();
        if ( ctx.op.getType() == ExprlyParser.NOT ) {
            return new Unary(Unary.Op.NOT, e, mkPropertyFromClass((Unary.class)));
        }
        throw new UnsupportedOperationException("Invalid unary operation");
    }

    /**
     * @return AST instance of a unary number operation
     */
    @Override
    public Unary visitUnaryN(ExprlyParser.UnaryNContext ctx) throws UnsupportedOperationException {
        Expr e = visit(ctx.expr());
        e.setReadOnly();
        if ( ctx.op.getType() == ExprlyParser.ADD ) {
            return new Unary(Unary.Op.PLUS, e, mkPropertyFromClass(Unary.class));
        }
        if ( ctx.op.getType() == ExprlyParser.SUB ) {
            return new Unary(Unary.Op.NEG, e, mkPropertyFromClass(Unary.class));
        }
        throw new UnsupportedOperationException("Invalid unary operation");
    }

    /**
     * @return AST instance of a var
     */
    @Override
    public Var visitVarName(ExprlyParser.VarNameContext ctx) {
        // By default we use VOID for the types of the variables, the type can be
        // checked later when compiling the program with the typechecker
        return new Var(BlocklyType.VOID, ctx.VAR().getText(), mkPropertyFromClass(Var.class));
    }

    /**
     * @return AST instance of a list expression
     */
    @Override
    public ExprList visitListExpr(ExprlyParser.ListExprContext ctx) {
        ExprList list = new ExprList();
        for ( ExprlyParser.ExprContext expr : ctx.expr() ) {
            Expr e = visit(expr);
            e.setReadOnly();
            list.addExpr(e);
        }
        list.setReadOnly();
        return list;
    }

    /**
     * @return AST instance of the expression within a set of parentheses
     */
    @Override
    public Expr visitParenthese(ExprlyParser.ParentheseContext ctx) {
        return visit(ctx.expr());
    }

    /**
     * @return AST instance of a connection const
     */
    @Override
    public ConnectConst visitConn(ExprlyParser.ConnContext ctx) {
        return new ConnectConst(mkPropertyFromClass(ConnectConst.class), ctx.op1.getText(), ctx.op0.getText());
    }

    /**
     * @return AST instance of the ternary op
     */
    @Override
    public Expr visitIfElseOp(ExprlyParser.IfElseOpContext ctx) {
        Expr q = visit(ctx.expr(1));
        Expr r = visit(ctx.expr(2));
        if ( q instanceof ExprList ) {
            q.setReadOnly();
            q = new ListCreate(BlocklyType.VOID, (ExprList) q, mkPropertyFromClass(ListCreate.class));
        }
        if ( r instanceof ExprList ) {
            r.setReadOnly();
            r = new ListCreate(BlocklyType.VOID, (ExprList) r, mkPropertyFromClass(ListCreate.class));
        }
        q.setReadOnly();
        r.setReadOnly();
        StmtList thenList = new StmtList();
        StmtList elseList = new StmtList();
        thenList.addStmt(new ExprStmt(q));
        elseList.addStmt(new ExprStmt(r));
        thenList.setReadOnly();
        elseList.setReadOnly();
        TernaryExpr ternaryExpr = new TernaryExpr(mkPropertyFromClass(TernaryExpr.class), visit(ctx.expr(0)), visit(ctx.expr(1)), visit(ctx.expr(2)));
        return ternaryExpr;
    }

    @Override
    public Expr visitChildren(RuleNode node) {
        Expr result = super.visitChildren(node);
        result.setReadOnly();
        return result;
    }

    private static BlocklyProperties mkInlineProperty(String type) {
        return BlocklyProperties.make(type, "1", true);
    }

    private static BlocklyProperties mkProperty(String type) {
        return BlocklyProperties.make(type, "1", false);
    }

    private <T> BlocklyProperties mkPropertyFromClass(Class<T> clazz) {
        String[] blocklyNames = AnnotationHelper.getBlocklyNamesOfAstClass(clazz);
        if ( blocklyNames.length != 1 ) {
            throw new DbcException("rework that! Too many blockly names to generate an ast object, that can be regenerated as XML");
        }
        return mkProperty(blocklyNames[0]);
    }
}
