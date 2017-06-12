# Deployment
The folder contains the deployment files for the second part of the [MiniJava compiler](https://github.com/aperkaz/mini-java-symbol-table).

## Contents
- `SyntaxTypeCheck.jar` deployed jar containing the code for executing the analysis on the java files on `test programs`
- `test programs` contains the test programs

## Execution
The `.jar` file can be run from command line and is configurable by 2 arguments.
```
java -jar arg1 [arg2]
```
- `arg1` = (String) absolute path of `test programs`
- `arg2` = (boolean) toggle visibility of the generated symbol table for every program
