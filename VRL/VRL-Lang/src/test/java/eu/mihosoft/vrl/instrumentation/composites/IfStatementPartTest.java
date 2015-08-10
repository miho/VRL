package eu.mihosoft.vrl.instrumentation.composites;

import static org.junit.Assert.*;

import org.codehaus.groovy.ast.stmt.IfStatement;
import org.junit.Test;

import eu.mihosoft.vrl.lang.model.Argument;
import eu.mihosoft.vrl.lang.model.ConstantValueFactory;
import eu.mihosoft.vrl.lang.model.ElseIfDeclaration;
import eu.mihosoft.vrl.lang.model.IArgument;
import eu.mihosoft.vrl.lang.model.IfDeclaration;
import eu.mihosoft.vrl.lang.model.Type;

public class IfStatementPartTest extends CompositeTestUtil<IfStatement, IfStatementPart> {
	
	@Test public void testIfStatementPartComplete() throws Exception
	{
		createHarness("if (1<2) then { println(\"if\"); } else if (1>2) { println(\"else if\"); } else { print(\"else\"); }", IfStatement.class, IfStatementPart.class);
		part.transform(statement, scope, context);
	}
	
	@Test public void testIfStatementPartSimpleIf() throws Exception
	{
		createHarness("if (1<2) then { println(\"if\"); }", IfStatement.class, IfStatementPart.class);
		IArgument arg = Argument.constArg(ConstantValueFactory.createConstantValue("test", Type.STRING));
		resolveAs("IfStatement.condition", arg);
		IfDeclaration decl = part.transform(statement, scope, context);
		
		assertEquals(arg, decl.getCheck());
	}
	
	@Test public void testIfStatementPartElseIf() throws Exception
	{
		createHarness("if (1<2) then { println(\"if\"); }", IfStatement.class, IfStatementPart.class);
		scope = fixture(IfDeclaration.class);
		IArgument arg = Argument.constArg(ConstantValueFactory.createConstantValue("test", Type.STRING));
		resolveAs("IfStatement.condition", arg);
		IfDeclaration decl = part.transform(statement, scope, context);
		assertTrue(decl instanceof ElseIfDeclaration);
		assertEquals(arg, decl.getCheck());
	}

}