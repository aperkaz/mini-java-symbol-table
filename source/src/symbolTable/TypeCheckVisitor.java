package symbolTable;

import org.antlr.v4.runtime.tree.ParseTree;

import cc1.ap222ze.examples.Assignment1_ap222zeBaseVisitor;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.ArrayCreateExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.ArrayLengthExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.ArraySelectExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.AssignArrayStatementContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.AssignStatementContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.BlockStatementContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.BoolExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.BraquetedExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.Call_a_methodContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.CharExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.Class_declarationContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.ComparisonExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.DivisionExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.DoWhileStatementContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.EqualExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.Field_declarationContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.IdentifierContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.IfStatementContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.IntegerExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.LessThanExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.Main_classContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.Main_methodContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.MethodCallExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.Method_declarationContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.MinusExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.MultiplicationExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.NewCallExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.PrintStatementContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.Return_statementContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.StringCharAtExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.StringExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.StringLengthExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.SubstractionExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.SumExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.ThisExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.VariableCallExpressionContext;
import cc1.ap222ze.examples.Assignment1_ap222zeParser.WhileStatementContext;
import misc.Utils;

/*
 * Type Checking for Mini Java.
 * 
 * 1- Check undeclared identifiers
 * 2-Type analysis 
 * 		2.1-Expression
 * 		2.2-Statement
 * 		2.3-Method		
 * 		2.4-Calls
 * 		2.5-[] and length on int arrays
 * 		2.6-....
 */

public class TypeCheckVisitor extends Assignment1_ap222zeBaseVisitor<Record> {

	SymbolTable symbolTable;

	boolean debug = false;

	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	private enum VarTypes {
		INT("int"), INT_ARRAY("int_array"), CHAR("char"), BOOLEAN("boolean"), STRING("String");

		private final String text;

		private VarTypes(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	/* Required Scope changes */

	@Override
	public Record visitMain_class(Main_classContext ctx) {
		symbolTable.enterScope();
		if (debug)
			System.out.println(symbolTable.getCurrentClassName());
		// enter inside the class
		Record rec = super.visitMain_class(ctx);
		// visit the main class
		symbolTable.exitScope();
		if (debug)
			System.out.println(symbolTable.getCurrentClassName());
		if (debug)
			System.out.println("-");
		return rec;
	}

	@Override
	public Record visitMain_method(Main_methodContext ctx) {
		symbolTable.enterScope();
		if (debug)
			System.out.println(symbolTable.getCurrentClassName());
		Record rec = super.visitMain_method(ctx);
		symbolTable.exitScope();
		if (debug)
			System.out.println(symbolTable.getCurrentClassName());
		if (debug)
			System.out.println("-");
		return rec;
	}

	@Override
	public Record visitClass_declaration(Class_declarationContext ctx) {
		symbolTable.enterScope();
		if (debug)
			System.out.println(symbolTable.getCurrentClassName());
		// enter inside the class
		Record rec = super.visitClass_declaration(ctx);
		symbolTable.exitScope();
		if (debug)
			System.out.println(symbolTable.getCurrentClassName());
		if (debug)
			System.out.println("-");
		return rec;
	}

	@Override
	public Record visitMethod_declaration(Method_declarationContext ctx) {
		// enter scope
		symbolTable.enterScope();
		if (debug)
			System.out.println(symbolTable.getCurrentClassName());
		/* Method wise operations */

		// check method return type - 6th children
		ParseTree innerBody = ctx.getChild(6); // method inner body
		// return -> last child
		Record returnStatement = visit(innerBody.getChild(innerBody.getChildCount() - 1));
		String methodType = ctx.getChild(0).getChild(0).getChild(0).toString();
		if (returnStatement == null || methodType == null) {
			Utils.errorMessage(ctx, "Error on return statement");
			return null;
		}
		if (!returnStatement.getType().equals(methodType)) {
			Utils.errorMessage(ctx, "Method type and return mismatch");
			return null;
		}
		Record rec = super.visitMethod_declaration(ctx);

		// exit scope
		symbolTable.exitScope();
		if (debug)
			System.out.println(symbolTable.getCurrentClassName());
		if (debug)
			System.out.println("-");
		return rec;
	}

	@Override
	public Record visitField_declaration(Field_declarationContext ctx) {
		// TODO
		String identifier = visit(ctx.getChild(1)).toString();
		// test duplicate identifiers on declaration
		if (symbolTable.lookup(identifier) != null) {
			Utils.errorMessage(ctx, "The identifier [ " + identifier + "] already exists");
			return null;
		}
		return super.visitField_declaration(ctx);
	}

	@Override
	public Record visitReturn_statement(Return_statementContext ctx) {
		// return expression type
		Record returnType = visit(ctx.getChild(1));
		return returnType;
	}

	/* check identifier existence */

	@Override
	public Record visitIdentifier(IdentifierContext ctx) {
		String identifier = ctx.getChild(0).toString();
		if (debug)
			System.out.println("\nIdentifier to visit: " + identifier);
		// lookUp the identifier directly
		Record identifierRecord = symbolTable.lookup(identifier);
		if (identifierRecord == null) {
			Utils.errorMessage(ctx, "The identifier [ " + identifier + " ] does NOT exist");
			return null;
		}
		return identifierRecord;
	}

	@Override
	public Record visitMethodCallExpression(MethodCallExpressionContext ctx) {
		Record classRec = visit(ctx.getChild(0));
		String methodName = ctx.getChild(2).getChild(0).getChild(0).toString();
		if (debug)
			System.out.println("Visit Method-> " + classRec.getId() + ": " + methodName);
		if (methodName == null) {
			Utils.errorMessage(ctx, "Method record not found");
			return null;
		}
		if (classRec == null) {
			Utils.errorMessage(ctx, "Class id not not found");
			return null;
		}
		// extract class record
		ClassRecord classRecord;
		// EXCEPTION class variables OR methods that return objects
		if (classRec instanceof VarRecord) {
			classRecord = (ClassRecord) symbolTable.lookup(classRec.getType());
		} else if (classRec instanceof MethodRecord) {
			classRecord = (ClassRecord) symbolTable.lookup(classRec.getType());
		} else {
			classRecord = (ClassRecord) classRec;
		}

		if (classRecord == null) {
			Utils.errorMessage(ctx, "ClassRecord not found for " + classRec.getId());
			return null;
		}
		// method contained in class
		MethodRecord methodRecord = (MethodRecord) classRecord.getMethod(methodName);
		if (methodRecord == null) {
			Utils.errorMessage(ctx, "MethodRecord not found for " + classRec.getId() + " - " + methodName);
			return null;
		}

		// parameter checking
		int allChildren = ctx.getChild(2).getChildCount(); // id ( PARAMS )
		int separators = 0, paramNumber = 0;
		for (int i = 2; i < allChildren - 1; i++) { // last children ')'
			String temp = ctx.getChild(2).getChild(i).toString();
			if (!temp.equals(",")) { // avoid ','
				Record paramIden = visit(ctx.getChild(2).getChild(i));
				if (!methodRecord.containParameter(paramNumber, paramIden)) {
					Utils.errorMessage(ctx, "Invalid parameter '" + paramIden.getId() + "'");
					return null;
				}
				paramNumber++;
			} else {
				separators++;
			}
		}
		// parameter count
		int parameterCount = allChildren - separators - 3;
		if (parameterCount != methodRecord.numberOfParameters()) {
			Utils.errorMessage(ctx, "Unmatched number of parameters on method " + methodRecord.getId());
			return null;
		}

		return methodRecord;
	}

	@Override
	public Record visitVariableCallExpression(VariableCallExpressionContext ctx) {
		Record classRec = visit(ctx.getChild(0));
		String varName = ctx.getChild(2).toString();
		if (debug)
			System.out.println("Visit variable-> " + classRec.getId() + ": " + varName);
		if (varName == null) {
			Utils.errorMessage(ctx, "Variable record not found");
			return null;
		}
		if (classRec == null) {
			Utils.errorMessage(ctx, "Class id not not found");
			return null;
		}
		// extract class record
		ClassRecord classRecord;
		// EXCEPTION class variables OR methods that return objects
		if (classRec instanceof VarRecord) {
			classRecord = (ClassRecord) symbolTable.lookup(classRec.getType());
		} else if (classRec instanceof MethodRecord) {
			classRecord = (ClassRecord) symbolTable.lookup(classRec.getType());
		} else {
			classRecord = (ClassRecord) classRec;
		}

		if (classRecord == null) {
			Utils.errorMessage(ctx, "ClassRecord not found for " + classRec.getId());
			return null;
		}
		// variable contained in class
		VarRecord varRecord = (VarRecord) classRecord.getField(varName);
		if (varRecord == null) {
			Utils.errorMessage(ctx, "VarRecord not found for " + classRec.getId() + " - " + varName);
			return null;
		}
		return varRecord;
	}

	@Override
	public Record visitThisExpression(ThisExpressionContext ctx) {
		return symbolTable.lookup("this");
	}

	@Override
	public Record visitCall_a_method(Call_a_methodContext ctx) {
		Record methodIdentifier = visit(ctx.getChild(0));
		return methodIdentifier;
	}

	@Override
	public Record visitNewCallExpression(NewCallExpressionContext ctx) {
		// lookup class
		Record classRecord = visit(ctx.getChild(1));
		return classRecord;
	}

	/* Statement type correctness */

	@Override
	public Record visitPrintStatement(PrintStatementContext ctx) {
		// assure only STRING, INT or CHAR inside
		Record printType = visit(ctx.getChild(2));
		if (printType != null) {
			if (printType.getType().equals(VarTypes.INT.toString())
					|| printType.getType().equals(VarTypes.STRING.toString())
					|| printType.getType().equals(VarTypes.CHAR.toString())) {
				return printType;
			}
		}
		Utils.errorMessage(ctx, "System.out.println can only print string or ints");
		return null;
	}

	@Override
	public Record visitBlockStatement(BlockStatementContext ctx) {
		return visit(ctx.getChild(1));
	}

	@Override
	public Record visitWhileStatement(WhileStatementContext ctx) {
		Record whileType = visit(ctx.getChild(2));
		if (whileType == null) {
			Utils.errorMessage(ctx, "NULL on while expression ");
			return null;
		} else if (!whileType.getType().equals(VarTypes.BOOLEAN.toString())) {
			Utils.errorMessage(ctx, "Whiles need to be boolean ");
			return null;
		}

		// visit inside
		visit(ctx.getChild(4));
		return new Record("while", VarTypes.BOOLEAN.toString());
	}

	@Override
	public Record visitDoWhileStatement(DoWhileStatementContext ctx) {
		Record doWhileType = visit(ctx.getChild(4));
		if (!doWhileType.getType().equals(VarTypes.BOOLEAN.toString())) {
			Utils.errorMessage(ctx, "Do-Whiles need to be boolean ");
			return null;
		}
		// visit inside
		visit(ctx.getChild(1));
		return new Record("do_while", VarTypes.BOOLEAN.toString());
	}

	@Override
	public Record visitIfStatement(IfStatementContext ctx) {
		Record ifType = visit(ctx.getChild(2));
		if (ifType == null) {
			Utils.errorMessage(ctx, "Error on if statement");
			return null;
		}
		if (!ifType.getType().equals(VarTypes.BOOLEAN.toString())) {
			Utils.errorMessage(ctx, "Ifs need to be boolean ");
			return null;
		}
		// visit inside
		visit(ctx.getChild(4));

		return new Record("if", VarTypes.BOOLEAN.toString());
	}

	@Override
	public Record visitAssignStatement(AssignStatementContext ctx) {
		Record leftSide = visit(ctx.getChild(0));
		Record rightSide = visit(ctx.getChild(2));
		if (leftSide == null || rightSide == null) {
			Utils.errorMessage(ctx, "Error on assign");
			return null;
		}
		if (leftSide.getType().equals(rightSide.getId())) { // class assign
			return null;
		}
		if (!leftSide.getType().equals(rightSide.getType())) {
			Utils.errorMessage(ctx, "Types must be same in assingments");
		}
		return null;
	}

	@Override
	public Record visitAssignArrayStatement(AssignArrayStatementContext ctx) {
		// check that the ID type is array and right side int
		Record leftSide = visit(ctx.getChild(0));
		Record rightSide = visit(ctx.getChild(2));
		if (leftSide == null || rightSide == null) {
			Utils.errorMessage(ctx, "Error on assign");
			return null;
		}
		if(leftSide.getType() != VarTypes.INT_ARRAY.toString()){
			Utils.errorMessage(ctx, "Array assigns only on arrays");
			return null;
		}
		if(rightSide.getType() != VarTypes.INT.toString()){
			Utils.errorMessage(ctx, "Only assign ints to int_array");
			return null;
		}
		return null;	
	}
	
	/* Expression type correctness */

	@Override
	public Record visitBraquetedExpression(BraquetedExpressionContext ctx) {
		return visit(ctx.getChild(1));
	}

	@Override
	public Record visitMultiplicationExpression(MultiplicationExpressionContext ctx) {
		Record leftType = visit(ctx.getChild(0));
		String simbol = ctx.getChild(1).toString();
		Record rightType = visit(ctx.getChild(2));

		if (leftType == null || rightType == null || simbol == null) {
			Utils.errorMessage(ctx, "NULL on Multiplication Expression");
			return null;
		}
		// int operation
		if (leftType.getType().equals(VarTypes.INT.toString()) && rightType.getType().equals(VarTypes.INT.toString()))
			return new Record("int arimetic", VarTypes.INT.toString());
		Utils.errorMessage(ctx, "Type mismatch on Multiplication Expression");
		return null;
	}

	@Override
	public Record visitDivisionExpression(DivisionExpressionContext ctx) {
		Record leftType = visit(ctx.getChild(0));
		String simbol = ctx.getChild(1).toString();
		Record rightType = visit(ctx.getChild(2));

		if (leftType == null || rightType == null || simbol == null) {
			Utils.errorMessage(ctx, "NULL on Division Expression");
			return null;
		}
		// int operation
		if (leftType.getType().equals(VarTypes.INT.toString()) && rightType.getType().equals(VarTypes.INT.toString()))
			return new Record("int arimetic", VarTypes.INT.toString());
		Utils.errorMessage(ctx, "Type mismatch on Division Expression");
		return null;
	}

	@Override
	public Record visitSumExpression(SumExpressionContext ctx) {
		Record leftType = visit(ctx.getChild(0));
		String simbol = ctx.getChild(1).toString();
		Record rightType = visit(ctx.getChild(2));

		if (leftType == null || rightType == null || simbol == null) { // null?
			Utils.errorMessage(ctx, "NULL on Sum Expression");
			return null;
		}
		// string operation
		if (leftType.getType().equals(VarTypes.STRING.toString())
				|| leftType.getType().equals(VarTypes.CHAR.toString())) {
			if (rightType.getType().equals(VarTypes.STRING.toString())
					|| rightType.getType().equals(VarTypes.CHAR.toString())) {
				return new Record("StringAdd", VarTypes.STRING.toString());
			}
		} else { // integer operation
			if (leftType.getType().equals(VarTypes.INT.toString())
					&& rightType.getType().equals(VarTypes.INT.toString())) {
				Record temp = new Record("intAdd", VarTypes.INT.toString());
				return temp;
			}
		}
		Utils.errorMessage(ctx, "Type mismatch on Sum Expression");
		return null;
	}

	@Override
	public Record visitSubstractionExpression(SubstractionExpressionContext ctx) {
		Record leftType = visit(ctx.getChild(0));
		String simbol = ctx.getChild(1).toString();
		Record rightType = visit(ctx.getChild(2));

		if (leftType == null || rightType == null || simbol == null) {
			Utils.errorMessage(ctx, "NULL on Substraction Expression");
			return null;
		}
		// int operation
		if (leftType.getType().equals(VarTypes.INT.toString()) && rightType.getType().equals(VarTypes.INT.toString()))
			return new Record("int arimetic", VarTypes.INT.toString());

		Utils.errorMessage(ctx, "Type mismatch on Substraction Expression");
		return null;
	}

	@Override
	public Record visitLessThanExpression(LessThanExpressionContext ctx) {
		Record leftType = visit(ctx.getChild(0));
		Record rightType = visit(ctx.getChild(2));

		if (leftType == null || rightType == null) {
			Utils.errorMessage(ctx, "NULL on less_than Expression");
			return null;
		}
		if (leftType.getType().equals(VarTypes.INT.toString())) {
			if (rightType.getType().equals(VarTypes.INT.toString())) {
				return new Record("less_than", VarTypes.BOOLEAN.toString());
			}
			Utils.errorMessage(ctx, "Evaluate INT or CHARS");
			return null;
		} else if (leftType.getType().equals(VarTypes.CHAR.toString())) {
			if (rightType.getType().equals(VarTypes.CHAR.toString())) {
				return new Record("less_than", VarTypes.BOOLEAN.toString());
			}
			Utils.errorMessage(ctx, "Evaluate INT or CHARS");
			return null;
		}
		Utils.errorMessage(ctx, "Error on less_than operation");
		return null;
	}

	@Override
	public Record visitEqualExpression(EqualExpressionContext ctx) {
		Record leftType = visit(ctx.getChild(0));
		Record rightType = visit(ctx.getChild(2));

		if (leftType == null || rightType == null) {
			Utils.errorMessage(ctx, "NULL on Equal Expression");
			return null;
		}
		if (leftType.getType().equals(VarTypes.INT.toString())) {
			if (rightType.getType().equals(VarTypes.INT.toString())) {
				return new Record("equals", VarTypes.BOOLEAN.toString());
			}
			Utils.errorMessage(ctx, "Compare same types");
			return null;
		} else if (leftType.getType().equals(VarTypes.CHAR.toString())) {
			if (rightType.getType().equals(VarTypes.CHAR.toString())) {
				return new Record("equals", VarTypes.BOOLEAN.toString());
			}
			Utils.errorMessage(ctx, "Compare same types");
		}
		Utils.errorMessage(ctx, "Error on equals operation");
		return null;
	}

	@Override
	public Record visitComparisonExpression(ComparisonExpressionContext ctx) {
		Record leftType = visit(ctx.getChild(0));
		Record rightType = visit(ctx.getChild(2));

		if (leftType == null || rightType == null) {
			Utils.errorMessage(ctx, "NULL on comparison Expression");
			return null;
		}
		if (leftType.getType().equals(VarTypes.BOOLEAN.toString())) {
			if (rightType.getType().equals(VarTypes.BOOLEAN.toString())) {
				return new Record("comparison", VarTypes.BOOLEAN.toString());
			}
			Utils.errorMessage(ctx, "Compare INTS");
			return null;
		}
		Utils.errorMessage(ctx, "Error on comparison operation");
		return null;
	}

	@Override
	public Record visitIntegerExpression(IntegerExpressionContext ctx) {
		return new Record("int", VarTypes.INT.toString());
	}

	@Override
	public Record visitBoolExpression(BoolExpressionContext ctx) {
		return new Record("boolean", VarTypes.BOOLEAN.toString());
	}

	@Override
	public Record visitCharExpression(CharExpressionContext ctx) {
		return new Record("char", VarTypes.CHAR.toString());
	}

	@Override
	public Record visitStringExpression(StringExpressionContext ctx) {
		return new Record("String", VarTypes.STRING.toString());
	}

	/* stringLengthExpression */

	@Override
	public Record visitStringLengthExpression(StringLengthExpressionContext ctx) {
		// get type of calling expression
		Record typeRec = visit(ctx.getChild(0));
		// check if type is string
		if (typeRec == null) {
			Utils.errorMessage(ctx, "Length() can only be called on Strings");
			return null;
		} else if (!typeRec.getType().equals(VarTypes.STRING.toString())) {
			Utils.errorMessage(ctx, "Length() can only be called on Strings");
			return null;
		}

		return new Record("stringLength", VarTypes.INT.toString());
	}

	/* stringCharAtExpression */

	@Override
	public Record visitStringCharAtExpression(StringCharAtExpressionContext ctx) {
		// get left expression type
		Record leftRec = visit(ctx.getChild(0));
		// assure its string
		if (!leftRec.getType().equals(VarTypes.STRING.toString())) {
			Utils.errorMessage(ctx, "charAt() can only be called on Strings");
			return null;
		}

		Record rightRec = visit(ctx.getChild(4));
		if (rightRec == null) { // null
			Utils.errorMessage(ctx, "CharAt() have to specify the position with int");
			return null;
		}
		// assure right expression INT IDENTIFIER or INTEGER
		if (!rightRec.getType().equals(VarTypes.INT.toString())) {
			Utils.errorMessage(ctx, "charAt() should contain int");
			return null;
		}

		return new Record("charAt", VarTypes.CHAR.toString());
	}

	/* minusExpression */

	@Override
	public Record visitMinusExpression(MinusExpressionContext ctx) {
		// expression type
		Record typeRec = visit(ctx.getChild(1));
		if (!typeRec.getType().equals(VarTypes.INT.toString())) {
			System.out.println("Minus operator can only precede an int");
			return null;
		}
		return new Record("minusInt", VarTypes.INT.toString());
	}

	/* Array calls */

	@Override
	public Record visitArrayCreateExpression(ArrayCreateExpressionContext ctx) {
		Record typeRec = visit(ctx.getChild(3));
		if (!typeRec.getType().equals(VarTypes.INT.toString())) {
			Utils.errorMessage(ctx, "The array size should be an int");
			return null;
		}
		return new Record("int_array", VarTypes.INT_ARRAY.toString());
	}

	@Override
	public Record visitArraySelectExpression(ArraySelectExpressionContext ctx) {
		Record leftType = visit(ctx.getChild(0)); // expect int_array
		Record rightType = visit(ctx.getChild(2)); // expect int
		if (!leftType.getType().equals(VarTypes.INT_ARRAY.toString())) {
			Utils.errorMessage(ctx, "Array visiting only on int_arrays");
			return null;
		}
		if (rightType == null) {
			Utils.errorMessage(ctx, "Array[X], X => can not be null");
			return null;
		}
		if (!rightType.getType().equals(VarTypes.INT.toString())) {
			Utils.errorMessage(ctx, "Array[X], X => int");
			return null;
		}
		return new Record("int_array", VarTypes.INT.toString());
	}

	@Override
	public Record visitArrayLengthExpression(ArrayLengthExpressionContext ctx) {
		// get the type of the calling expression
		Record typeRec = visit(ctx.getChild(0));
		// check if type is int_array_type
		if (!typeRec.getType().equals(VarTypes.INT_ARRAY.toString())) {
			Utils.errorMessage(ctx, "Length can only be called on int_arrays");
			return null;
		}

		return new Record("arrayLength", "int");
	}
}
