import java.io.IOException;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cc1.ap222ze.examples.Assignment1_ap222zeLexer;
import cc1.ap222ze.examples.Assignment1_ap222zeParser;
import symbolTable.SymbolTable;
import symbolTable.TypeCheckVisitor;

public class SintaxTreeTraverser {

	public static void traverseSintaxTree(){
		
		//file containing the program
		String myPath = "test_program.java";
		System.out.println("Reading program from: " + myPath);

		// parse the imput program
		Assignment1_ap222zeLexer lexer = null;
		try {
			lexer = new Assignment1_ap222zeLexer(new ANTLRFileStream(myPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assignment1_ap222zeParser parser = new Assignment1_ap222zeParser(new BufferedTokenStream(lexer));
		Assignment1_ap222zeParser.ProgContext tree = parser.prog();
	
		// display the generated tree
		Trees.inspect(tree, parser);
		
		SymbolTable symbolTable = new SymbolTable();
		
		// symbol table construction		
		ParseTreeWalker walker = new ParseTreeWalker();
		SymbolListener symbolListener= new SymbolListener(symbolTable);
		walker.walk(symbolListener, tree);
	
		// print the symbol table
		symbolTable.printTable();
				
		// debug the scope tree V2
		//symbolTable.printScopeTree();
		
		// DEBUG THE SCOPE TREE		
		//symbolTable.debugScopeTree();
		
		//type checking visitor				
		TypeCheckVisitor typeChecker = new TypeCheckVisitor();
		symbolTable.resetTable();
		typeChecker.setSymbolTable(symbolTable);
		typeChecker.visit(tree);
		
		
	}
	
}
