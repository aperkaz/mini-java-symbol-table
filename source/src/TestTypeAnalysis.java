

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cc1.ap222ze.examples.Assignment1_ap222zeLexer;
import cc1.ap222ze.examples.Assignment1_ap222zeParser;
import symbolTable.SymbolTable;
import symbolTable.TypeCheckVisitor;


/*
 * Helper class that execute the type analysis of programs in a folder
 * The folder relative path is specified in .config
 */

public class TestTypeAnalysis {

	final static int INTERVAL = 0;
	
	static String spoolDir = "";
	static String printSymbolTables = "false";
	
	public static void main(String[] args) {
		
		// presentation
		System.out.println("\n** CC1 - PA2 - ap222ze **");
		System.out.println("\nThe following program does: \n\t ->Syntax Tree build \n\t ->Symbol Table construction \n\t ->Type checking ");
		System.out.println("Over all the java programs inside a given folder");
		System.out.println("\n\t[ ERRORS found are displayed on screen ]\n");
					
		// get spool directory
		try{
			spoolDir = args[0];
			//spoolDir = "C:\\Users\\alain\\Dropbox\\ALAIN\\Master Software Development - LNU\\_LNU_\\COURSES\\First Semester\\Compiler Construction 1\\Assignments\\ap222ze_workspace\\PracticalAssignment2\\test_program.java";
		}catch(Exception e){
			System.err.println("\nThe folder that contains the programs,\n should be introduced as args[0]");
			System.exit(0);
		}
		
		// print symbol tables	
		try{
			printSymbolTables = args[1];
			if(printSymbolTables != null){
				if(printSymbolTables.equals("true") || printSymbolTables.equals("false")){
					System.out.println("\n +Symbol tables displayed: "+printSymbolTables+"\n");
				}else{
					printSymbolTables = "false";
					System.out.println("\n +Symbol tables displayed: "+printSymbolTables+"\n");
				}
			}	
		}catch(Exception e){}
							
		
		if(spoolDir == null){
			try {
				throw new Exception("Put a valid path");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Reading files from: "+spoolDir);
		
		System.out.println("\n***TESTING PROGRAMS***\n");
		// parse every correct program
		try(Stream<Path> paths = Files.walk(Paths.get(spoolDir))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {		        		       
		        	// execute process
		            testTypeCheckFile(filePath);
		            try {
						Thread.sleep(INTERVAL);
					} catch (Exception e) {					
						e.printStackTrace();
					}
		        }
		    });
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
		
	}

	private static void testTypeCheckFile(Path filePath){

			System.out.println("Testing FILE:  " + filePath.getFileName());
			String filePathString = filePath.toString();
			
			// java file(?)
			if(!validJavaFile(filePath.getFileName().toString())){
				System.err.println("\tOnly .java files valid \n");
				return;
			}
						
			// Parse input program
			Assignment1_ap222zeLexer lexer = null;
			try {
				lexer = new Assignment1_ap222zeLexer(new ANTLRFileStream(filePathString));
			} catch (IOException e) {
				e.printStackTrace();
			}
			Assignment1_ap222zeParser parser = new Assignment1_ap222zeParser(new BufferedTokenStream(lexer));
			Assignment1_ap222zeParser.ProgContext tree = parser.prog();
			
			SymbolTable symbolTable = new SymbolTable();
			
			// symbol table construction		
			ParseTreeWalker walker = new ParseTreeWalker();
			SymbolListener symbolListener= new SymbolListener(symbolTable);
			walker.walk(symbolListener, tree);
		
			// print the symbol table
			if(printSymbolTables.equals("true"))
				symbolTable.printTable();
			
			//type checking visitor				
			TypeCheckVisitor typeChecker = new TypeCheckVisitor();
			symbolTable.resetTable();
			typeChecker.setSymbolTable(symbolTable);
			typeChecker.visit(tree);			
	}
	
	private static boolean validJavaFile(String fileName){		
		return fileName.toLowerCase().endsWith(".java");		
	}
	
}
