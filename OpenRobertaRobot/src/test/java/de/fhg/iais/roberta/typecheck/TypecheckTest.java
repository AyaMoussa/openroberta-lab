package de.fhg.iais.roberta;

import org.junit.Assert;
import org.junit.Test;

import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.typecheck.TypecheckCommonLanguageVisitor;
import de.fhg.iais.roberta.util.test.UnitTestHelper;

public class TypecheckTest extends AstTest {

    @Test
    public void test0ok() throws Exception {
        Phrase ast = UnitTestHelper.getGenericAstOfFirstBlock(testFactory, "/ast/expressions/expr_typecorrect0.xml");
        TypecheckCommonLanguageVisitor typechecker = TypecheckCommonLanguageVisitor.makeVisitorAndTypecheck(ast);
        Assert.assertEquals(0, typechecker.getErrorCount());
    }

    @Test
    public void testAssignOk() throws Exception {
        Phrase ast = UnitTestHelper.getGenericAstOfFirstBlock(testFactory, "/ast/expressions/expr_assign_ok.xml");
        TypecheckCommonLanguageVisitor typechecker = TypecheckCommonLanguageVisitor.makeVisitorAndTypecheck(ast);
        Assert.assertEquals(0, typechecker.getErrorCount());
    }

    @Test
    public void testAssignFail() throws Exception {
        Phrase ast = UnitTestHelper.getGenericAstOfFirstBlock(testFactory, "/ast/expressions/expr_assign_fail.xml");
        TypecheckCommonLanguageVisitor typechecker = TypecheckCommonLanguageVisitor.makeVisitorAndTypecheck(ast);
        Assert.assertEquals(1, typechecker.getErrorCount());
    }

    @Test
    public void test1ok() throws Exception {
        Phrase ast = UnitTestHelper.getGenericAstOfFirstBlock(testFactory, "/ast/expressions/expr_typecorrect1.xml");
        TypecheckCommonLanguageVisitor typechecker = TypecheckCommonLanguageVisitor.makeVisitorAndTypecheck(ast);
        Assert.assertEquals(1, typechecker.getErrorCount());
    }

    @Test
    public void test2ok() throws Exception {
        Phrase ast = UnitTestHelper.getGenericAstOfFirstBlock(testFactory, "/ast/expressions/expr_typecorrect2.xml");
        TypecheckCommonLanguageVisitor typechecker = TypecheckCommonLanguageVisitor.makeVisitorAndTypecheck(ast);
        Assert.assertEquals(0, typechecker.getErrorCount());
    }

    @Test
    public void test0error() throws Exception {
        Phrase ast = UnitTestHelper.getGenericAstOfFirstBlock(testFactory, "/ast/expressions/expr_typeerror0.xml");
        TypecheckCommonLanguageVisitor typechecker = TypecheckCommonLanguageVisitor.makeVisitorAndTypecheck(ast);
        Assert.assertTrue(typechecker.getErrorCount() > 0);
    }

    @Test
    public void test1error() throws Exception {
        Phrase ast = UnitTestHelper.getGenericAstOfFirstBlock(testFactory, "/ast/expressions/expr_typeerror1.xml");
        TypecheckCommonLanguageVisitor typechecker = TypecheckCommonLanguageVisitor.makeVisitorAndTypecheck(ast);
        Assert.assertTrue(typechecker.getErrorCount() > 0);
    }
}
