package main.visitor.codeGenerator;


import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.tools.javac.jvm.Code;
import main.ast.Type.ArrayType.ArrayType;
import main.ast.Type.NoType;
import main.ast.Type.PrimitiveType.BooleanType;
import main.ast.Type.PrimitiveType.StringType;
import main.ast.Type.PrimitiveType.IntType;
import main.ast.Type.Type;
import main.ast.Type.UserDefinedType.UserDefinedType;
import main.ast.node.Node;
import main.ast.node.Program;
import main.ast.node.declaration.ClassDeclaration;
import main.ast.node.declaration.MainMethodDeclaration;
import main.ast.node.declaration.MethodDeclaration;
import main.ast.node.declaration.VarDeclaration;
import main.ast.node.expression.*;
import main.ast.node.expression.BinOp.BinaryOperator;
import main.ast.node.expression.Value.BooleanValue;
import main.ast.node.expression.Value.IntValue;
import main.ast.node.expression.Value.StringValue;
import main.ast.node.statement.*;
import main.symbolTable.ClassSymbolTableItem;
import main.symbolTable.SymbolTable;
import main.symbolTable.SymbolTableMethodItem;
import main.symbolTable.itemException.ItemNotFoundException;
import main.symbolTable.symbolTableVariable.SymbolTableFieldVariableItem;
import main.symbolTable.symbolTableVariable.SymbolTableLocalVariableItem;
import main.symbolTable.symbolTableVariable.SymbolTableMethodArgumentItem;
import main.symbolTable.symbolTableVariable.SymbolTableVariableItemBase;
import main.visitor.VisitorImpl;
import oracle.jrockit.jfr.StringConstantPool;

import main.visitor.typeChecker.TypeChecker;
import sun.jvm.hotspot.debugger.cdbg.Sym;


import javax.swing.text.StyledEditorKit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CodeGenerator extends VisitorImpl {

    private static final String DIR="output/";
    private static final String FORMAT=".j";
    private static final String NL="\n";
    private static final String T="\t";
    private static final int load = 0;
    private static final int store = 1;
    private ArrayList<String> classCode;
    private String currentClass;
    private int currentLabel;
    private int mode;
    private Program programNode;
    private ClassDeclaration classOfMethodCall;
    private MethodDeclaration methodOfMethodCall;
    private int isfield;

    public CodeGenerator() {
        classCode = new ArrayList<>();
        currentClass = "object";
        currentLabel = 0;
        mode = -1;
    }

//    private classHeaderGenerator(){}

    private Type findIdentifierType(Identifier identifier){
        String key = SymbolTableVariableItemBase.VARIABLE + identifier.getName();
        try {
            return ((SymbolTableVariableItemBase) SymbolTable.top.get(key)).getType();
        }
        catch(ItemNotFoundException itemNotFound){

        }
        return null;
    }

    private Type findExpressionType(Expression expression){
        if(expression instanceof BooleanValue){
            return new BooleanType();
        }
        else if(expression instanceof IntValue){
            return new IntType();
        }
        else if(expression instanceof StringValue){
            return new StringType();
        }
        else if(expression instanceof ArrayCall){
            return new IntType();
        }
        else if(expression instanceof UnaryExpression){
            if(((UnaryExpression) expression).getUnaryOperator().equals(UnaryOperator.minus)){
                return new IntType();
            }
            else{
                return new BooleanType();
            }
        }
        else if(expression instanceof This){
            UserDefinedType ut = new UserDefinedType();
            ut.setName(new Identifier(currentClass));
            return ut;
        }
        else if(expression instanceof NewArray){
            return new ArrayType();
        }
        else if(expression instanceof NewClass){
            UserDefinedType udt = new UserDefinedType();
            udt.setName(((NewClass)expression).getClassName());
            return udt;
        }
        else if(expression instanceof Length){
            return new IntType();
        }
        else if(expression instanceof MethodCall){
            findMethodDeclaration((MethodCall)expression);
            return methodOfMethodCall.getActualReturnType();
        }
        else if(expression instanceof Identifier){
            return findIdentifierType((Identifier)expression);
        }
        else if(expression instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression)expression;
            if (be.getBinaryOperator().equals(BinaryOperator.assign)) {
                return findExpressionType(be.getLeft());
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.add)) {
                return new IntType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.mult)) {
                return new IntType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.sub)) {
                return new IntType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.div)) {
                return new IntType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.eq)) {
                return new BooleanType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.neq)) {
                return new BooleanType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.and)) {
                return new BooleanType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.or)) {
                return new BooleanType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.gt)) {
                return new BooleanType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.gte)) {
                return new BooleanType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.lt)) {
                return new BooleanType();
            }
            else if (be.getBinaryOperator().equals(BinaryOperator.lte)) {
                return new BooleanType();
            }

        }
        return null;
    }

    private void findMethodDeclaration(MethodCall methodCall){
        String className = ((UserDefinedType)findExpressionType(methodCall.getInstance())).getName().getName();
        for(ClassDeclaration classDeclaration : programNode.getClasses()){
            if(classDeclaration.getName().getName().equals(className)){
                for(MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()){
                    if(methodDeclaration.getName().getName().equals(methodCall.getMethodName().getName())){
                        classOfMethodCall = classDeclaration;
                        methodOfMethodCall = methodDeclaration;
                    }
                }
            }
        }
    }

    private Type findFieldType(Identifier identifier){
        String key = SymbolTableVariableItemBase.VARIABLE + identifier.getName();
        SymbolTable classSymTable = SymbolTable.top.getPreSymbolTable();
        try {
            SymbolTableFieldVariableItem field = (SymbolTableFieldVariableItem)SymbolTable.top.get(key);

            return field.getType();
        }
        catch (ItemNotFoundException itemNotFound){
            System.out.println("not found");
        }
        return null;
    }

    private void modifyIndexes(MethodDeclaration methodDeclaration){
        int counter = 1;
        for(VarDeclaration argument : methodDeclaration.getArgs()){
            String key = SymbolTableMethodArgumentItem.VARIABLE + argument.getIdentifier().getName();
            try {
                SymbolTableMethodArgumentItem argItem = (SymbolTableMethodArgumentItem) SymbolTable.top.getInCurrentScope(key);
                argItem.setIndex(counter);
                counter++;
            }catch(ItemNotFoundException itemNotFound){
                return;
            }
        }

        for(VarDeclaration localVar : methodDeclaration.getLocalVars()){
            String key = SymbolTableLocalVariableItem.VARIABLE + localVar.getIdentifier().getName();
            try {
                SymbolTableLocalVariableItem localVarItem = (SymbolTableLocalVariableItem) SymbolTable.top.getInCurrentScope(key);
                localVarItem.setIndex(counter);
                counter++;
            }catch(ItemNotFoundException itemNotFound){
                return;
            }
        }
    }

    private void enterScope(ClassDeclaration classDeclaration) {
        String name = classDeclaration.getName().getName();
        try {
            ClassSymbolTableItem classItem = (ClassSymbolTableItem) SymbolTable.root.getInCurrentScope(ClassSymbolTableItem.CLASS + name);
            SymbolTable next = classItem.getClassSym();
            SymbolTable.push(next);
        } catch (ItemNotFoundException itemNotFound) {
            System.out.println("there is an error in pushing class symbol table");
        }
    }

    private void enterScope(MethodDeclaration methodDeclaration) {
        String name = methodDeclaration.getName().getName();
        try {
            SymbolTableMethodItem methodItem = (SymbolTableMethodItem) SymbolTable.top.getInCurrentScope(SymbolTableMethodItem.METHOD + name);
            SymbolTable next = methodItem.getMethodSymbolTable();
            SymbolTable.push(next);
        } catch (ItemNotFoundException itemNotFound) {
            System.out.println("there is an error in pushing method symbol table");
        }
    }

    private String classFieldGen(VarDeclaration varDeclaration){
        String gen = ".field private ";
        gen += varDeclaration.getIdentifier().getName();
        Type type = varDeclaration.getType();

        gen += " " + typeStringGenerator(type);

        return gen;
    }

    private String typeStringGenerator(Type type){
        if(type.toString().equals("int")){//TODO: when type is int, it doesn't go in this if statement
            return "I";
        }
        else if(type instanceof StringType){
            return "Ljava/lang/String;";
        }
        else if(type instanceof BooleanType){
            return  "Z";
        }
        else if(type instanceof ArrayType){
            return  "[I";
        }
        else if(type instanceof UserDefinedType){
            String cn = ((UserDefinedType) type).getName().getName();
            return "L" + cn + ";";
        }
        return "";
    }

    private void writeInFile(){
        String filepath = CodeGenerator.DIR + currentClass + CodeGenerator.FORMAT;
        try {
            File file = new File(filepath);
            if (!file.createNewFile()){
                System.out.println("file cannot be created");
            }
            FileWriter fileWriter = new FileWriter(filepath);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (String line : classCode){
                printWriter.print(line + CodeGenerator.NL);
            }
            printWriter.close();
        }catch(IOException ioe){
            System.out.println(ioe.getLocalizedMessage());
            return;
        }
        classCode.clear();

    }//no need

    private boolean IsField(Identifier identifier){
        String key = SymbolTableVariableItemBase.VARIABLE + identifier.getName();
        try {
            SymbolTableVariableItemBase varItem = (SymbolTableVariableItemBase) SymbolTable.top.getInCurrentScope(key);
            return false;
        }
        catch(ItemNotFoundException itemNotFound){
            return true;
        }
    }//no need


    @Override
    public void visit(Node node) {

    }

    @Override
    public void visit(Program program) {
        programNode = program;
        //make object
        classCode.add(".class public " + currentClass);
        classCode.add(".super java/lang/Object");
        classCode.add(CodeGenerator.NL);
        classCode.add(".method public <init>()V");
        classCode.add(CodeGenerator.T + ".limit stack 20");
        classCode.add(CodeGenerator.T + ".limit locals 20");
        classCode.add(CodeGenerator.T + "aload_0");
        classCode.add(CodeGenerator.T + "invokespecial java/lang/Object/<init>()V");
        classCode.add(CodeGenerator.T + "return");
        classCode.add(".end method");
        classCode.add(CodeGenerator.NL);
        writeInFile();

        this.visit(program.getMainClass());


        for (ClassDeclaration classDeclaration : program.getClasses()) {
            this.visit(classDeclaration);
        }

    }//no need

    @Override
    public void visit(ClassDeclaration classDeclaration) {


        enterScope(classDeclaration);


        currentClass = classDeclaration.getName().getName();
        classCode.add(".class public " + currentClass);
        if(classDeclaration.getParentName().getName() == null)
            classCode.add(".super java/lang/Object");
        else
            classCode.add(".super " + classDeclaration.getParentName().getName());

        classCode.add(CodeGenerator.NL);

        for (VarDeclaration varDeclaration : classDeclaration.getVarDeclarations())
            classCode.add(classFieldGen(varDeclaration));

        classCode.add(CodeGenerator.NL);

        //constructor
        classCode.add(".method public <init>()V");
        classCode.add(CodeGenerator.T + "aload_0");
        if(classDeclaration.getParentName().getName() == null)
            classCode.add(CodeGenerator.T + "invokespecial java/lang/Object/<init>()V");
        else
            classCode.add(CodeGenerator.T + "invokespecial " + classDeclaration.getParentName().getName() + "/<init>()V");

        for(VarDeclaration field : classDeclaration.getVarDeclarations()){
            isfield = 1;
            visit(field);
        }

        classCode.add(CodeGenerator.T + "return");
        classCode.add(".end method");
        classCode.add(CodeGenerator.NL);





        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations())
            if(methodDeclaration instanceof MainMethodDeclaration){
                this.visit((MainMethodDeclaration) methodDeclaration);
            }
            else {
                this.visit(methodDeclaration);
            }

        writeInFile();

        SymbolTable.pop();
    }//no need

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        enterScope(methodDeclaration);

        String prototype = ".method public "+ methodDeclaration.getName().getName() + "(";
        for (VarDeclaration argument : methodDeclaration.getArgs()){
            prototype = prototype + typeStringGenerator(argument.getType());
        }
        prototype += ")";
        prototype += typeStringGenerator(methodDeclaration.getActualReturnType());

        classCode.add(prototype);
        classCode.add(CodeGenerator.T + ".limit stack 50");
        classCode.add(CodeGenerator.T + ".limit locals 50");
        classCode.add(CodeGenerator.NL);

        modifyIndexes(methodDeclaration);

        for (VarDeclaration localVar : methodDeclaration.getLocalVars()) {
            isfield = 0;
            visit(localVar);
        }

        for (Statement statement : methodDeclaration.getBody())
            visitStatement(statement);

        mode = CodeGenerator.load;
        visitExpr(methodDeclaration.getReturnValue());

        Type returnType = methodDeclaration.getActualReturnType();

        if(returnType.toString().equals("int") || returnType instanceof BooleanType){
            classCode.add(CodeGenerator.T + "ireturn");
        }
        else if(returnType instanceof StringType || returnType instanceof ArrayType || returnType instanceof UserDefinedType){
            classCode.add(CodeGenerator.T + "areturn");
        }

        classCode.add(".end method");

        SymbolTable.pop();

    }//no need

    @Override
    public void visit(MainMethodDeclaration mainMethodDeclaration) {

        //should return type be void like java?? how to run this if not??
        classCode.add(".method public static main([Ljava/lang/String;)V");
        classCode.add(CodeGenerator.T + ".limit stack 100");
        classCode.add(CodeGenerator.T + ".limit locals 10");
        classCode.add(CodeGenerator.NL);

        for (Statement statement : mainMethodDeclaration.getBody()) {
            visitStatement(statement);
        }

        //visitExpr(mainMethodDeclaration.getReturnValue());

        classCode.add(CodeGenerator.T + "return");
        classCode.add(".end method");
    }//no need

    @Override
    public void visit(VarDeclaration varDeclaration) {
        if(isfield==1) {
            if (varDeclaration.getType().toString().equals("int")) {
                classCode.add(CodeGenerator.T + "aload_0");
                classCode.add(CodeGenerator.T + "ldc 0");
                classCode.add(CodeGenerator.T + "putfield " + currentClass + "/" + varDeclaration.getIdentifier().getName() + " " +
                        typeStringGenerator(varDeclaration.getType()));
            } else if (varDeclaration.getType() instanceof StringType) {
                classCode.add(CodeGenerator.T + "aload_0");
                classCode.add(CodeGenerator.T + "ldc \"\"");
                classCode.add(CodeGenerator.T + "putfield " + currentClass + "/" + varDeclaration.getIdentifier().getName() + " " +
                        typeStringGenerator(varDeclaration.getType()));
            } else if (varDeclaration.getType() instanceof BooleanType) {
                classCode.add(CodeGenerator.T + "aload_0");
                classCode.add(CodeGenerator.T + "iconst_0");
                classCode.add(CodeGenerator.T + "putfield " + currentClass + "/" + varDeclaration.getIdentifier().getName() + " " +
                        typeStringGenerator(varDeclaration.getType()));
            }
        }
        else{
            if (varDeclaration.getType().toString().equals("int")) {
                classCode.add(CodeGenerator.T + "ldc 0");
                mode = CodeGenerator.load;
                visitExpr(varDeclaration.getIdentifier());
            } else if (varDeclaration.getType() instanceof StringType) {
                classCode.add(CodeGenerator.T + "ldc \"\"");
                mode = CodeGenerator.load;
                visitExpr(varDeclaration.getIdentifier());
            } else if (varDeclaration.getType() instanceof BooleanType) {
                classCode.add(CodeGenerator.T + "iconst_0");
                mode = CodeGenerator.load;
                visitExpr(varDeclaration.getIdentifier());
            }
        }
    }//redundant

    @Override
    public void visit(ArrayCall arrayCall) {
        int firstMode = mode;
        mode = CodeGenerator.load;
        visitExpr(arrayCall.getInstance());
        visitExpr(arrayCall.getIndex());
        mode = firstMode;
        if(mode==CodeGenerator.load){
            classCode.add(CodeGenerator.T + "iaload");
        }
    }//no need

    @Override
    public void visit(BinaryExpression binaryExpression) {
        mode = CodeGenerator.load;
        if(binaryExpression.getBinaryOperator().equals(BinaryOperator.assign)) {
            if (binaryExpression.getLeft() instanceof Identifier) {
                Identifier leftside = (Identifier) binaryExpression.getLeft();

                if (IsField(leftside)) {
                    classCode.add(CodeGenerator.T + "aload_0");
                    mode = CodeGenerator.load;
                    visitExpr(binaryExpression.getRight());
                    classCode.add(CodeGenerator.T + "putfield " + currentClass + "/" + leftside.getName() + " " + typeStringGenerator(findExpressionType(leftside)));
                } else {
                    mode = CodeGenerator.load;
                    visitExpr(binaryExpression.getRight());

                    mode = CodeGenerator.store;
                    visitExpr(binaryExpression.getLeft());

                }

                mode = CodeGenerator.load;
                visitExpr(binaryExpression.getLeft());

            }
        }
        else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.add)){
            visitExpr(binaryExpression.getLeft());
            visitExpr(binaryExpression.getRight());
            classCode.add(CodeGenerator.T + "iadd");
        }
        else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.mult)){
            visitExpr(binaryExpression.getLeft());
            visitExpr(binaryExpression.getRight());
            classCode.add(CodeGenerator.T + "imul");
        }
        else if (binaryExpression.getBinaryOperator().equals(BinaryOperator.sub)){
            visitExpr(binaryExpression.getLeft());
            visitExpr(binaryExpression.getRight());
            classCode.add(CodeGenerator.T + "isub");
        }
        else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.div)){
            visitExpr(binaryExpression.getLeft());
            visitExpr(binaryExpression.getRight());
            classCode.add(CodeGenerator.T + "idiv");
        }
        else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.eq)){
            visitExpr(binaryExpression.getLeft());
            visitExpr(binaryExpression.getRight());
            String inst;
            if(findExpressionType(binaryExpression.getLeft()).toString().equals("int")){
                inst = "if_icmpne";
            }
            else{
                inst = "if_acmpne";
            }

            int falseRes = currentLabel;
            classCode.add(CodeGenerator.T + inst + " " + "Label" + falseRes);
            currentLabel++;
            classCode.add(CodeGenerator.T + "iconst_1");
            int end = currentLabel;
            classCode.add(CodeGenerator.T + "goto " + "Label" + end);
            currentLabel++;
            classCode.add(CodeGenerator.T + "Label" + falseRes + ":");
            classCode.add(CodeGenerator.T + "iconst_0");
            classCode.add(CodeGenerator.T + "Label" + end + ":");
        }
        else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.neq)){
            visitExpr(binaryExpression.getLeft());
            visitExpr(binaryExpression.getRight());
            String inst;
            if(findExpressionType(binaryExpression.getLeft()).toString().equals("int")){
                inst = "if_icmpne";
            }
            else{
                inst = "if_acmpne";
            }

            int falseRes = currentLabel;
            classCode.add(CodeGenerator.T + inst + " " + "Label" + falseRes);
            currentLabel++;
            classCode.add(CodeGenerator.T + "iconst_0");
            int end = currentLabel;
            classCode.add(CodeGenerator.T + "goto " + "Label" + end);
            currentLabel++;
            classCode.add(CodeGenerator.T + "Label" + falseRes + ":");
            classCode.add(CodeGenerator.T + "iconst_1");
            classCode.add(CodeGenerator.T + "Label" + end + ":");
        }
        else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.and)){
            visitExpr(binaryExpression.getLeft());
            int nelse = currentLabel;
            classCode.add(CodeGenerator.T + "ifeq " + "Label" + nelse);
            currentLabel++;
            visitExpr(binaryExpression.getRight());
            int nafter = currentLabel;
            classCode.add(CodeGenerator.T + "goto " + "Label" + nafter);
            currentLabel++;
            classCode.add(CodeGenerator.T + "Label" + nelse + ":");
            classCode.add(CodeGenerator.T + "iconst_0");
            classCode.add(CodeGenerator.T + "Label" + nafter + ":");
        }
        else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.or)){
            visitExpr(binaryExpression.getLeft());
            int nelse = currentLabel;
            classCode.add(CodeGenerator.T + "ifeq " + "Label" + nelse);
            currentLabel++;
            classCode.add(CodeGenerator.T + "iconst_1");
            int nafter = currentLabel;
            classCode.add(CodeGenerator.T + "goto " + "Label" + nafter);
            currentLabel++;
            classCode.add(CodeGenerator.T + "Label" + nelse + ":");
            visitExpr(binaryExpression.getRight());
            classCode.add(CodeGenerator.T + "Label" + nafter + ":");
        }
        else{
            visitExpr(binaryExpression.getLeft());
            visitExpr(binaryExpression.getRight());
            String inst = "";
            if(binaryExpression.getBinaryOperator().equals(BinaryOperator.gt)){
                inst = "if_icmple";
            }
            else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.gte)){
                inst = "if_imcplt";
            }
            else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.lt)){
                inst = "if_icmpge";
            }
            else if(binaryExpression.getBinaryOperator().equals(BinaryOperator.lte)){
                inst = "if_icmpgt";
            }

            int falseRes = currentLabel;
            classCode.add(CodeGenerator.T + inst + " " + "Label" + falseRes);
            currentLabel++;
            classCode.add(CodeGenerator.T + "iconst_1");
            int end = currentLabel;
            classCode.add(CodeGenerator.T + "goto " + "Label" + end);
            currentLabel++;
            classCode.add(CodeGenerator.T + "Label" + falseRes + ":");
            classCode.add(CodeGenerator.T + "iconst_0");
            classCode.add(CodeGenerator.T + "Label" + end + ":");
        }
    }

    @Override
    public void visit(Identifier identifier) {
        String key = SymbolTableVariableItemBase.VARIABLE + identifier.getName();
        try {
            SymbolTableVariableItemBase varItem = (SymbolTableVariableItemBase) SymbolTable.top.getInCurrentScope(key);
            Type varType = varItem.getType();
            int number = varItem.getIndex();
            String inst="";

                if (varType.toString().equals("int") || varType instanceof BooleanType) {
                    inst = (mode == CodeGenerator.load) ? "iload" : "istore";
                } else if (varType instanceof ArrayType || varType instanceof StringType
                        || varType instanceof UserDefinedType) {
                    inst = (mode == CodeGenerator.load) ? "aload" : "astore";
                }
                classCode.add(CodeGenerator.T + inst + " " + number);
        }
        catch(ItemNotFoundException itemNotFound){
            Type fieldType = findFieldType(identifier);
            if(mode == CodeGenerator.load) {
                classCode.add(CodeGenerator.T + "aload_0");
                classCode.add(CodeGenerator.T + "getfield " + currentClass + "/" + identifier.getName() + " " + typeStringGenerator(fieldType));
            }
        }
    }//no need

    @Override
    public void visit(Length length) {
        visitExpr(length.getExpression());
        classCode.add(CodeGenerator.T + "arraylength");
    }//no need

    @Override
    public void visit (MethodCall methodCall){

        findMethodDeclaration(methodCall);

        visitExpr(methodCall.getInstance());


        for (Expression argument : methodCall.getArgs())
            visitExpr(argument);

        String className = classOfMethodCall.getName().getName();
        String methodName = methodCall.getMethodName().getName();

        String invoke =  "invokevirtual " + className + "/" + methodName
                + "(" ;

        for (VarDeclaration argument : methodOfMethodCall.getArgs()){
            invoke = invoke + typeStringGenerator(argument.getType());
        }
        invoke += ")";

        invoke += typeStringGenerator(methodOfMethodCall.getActualReturnType());

        classCode.add(CodeGenerator.T + invoke);
    }//no need

    @Override
    public void visit (NewArray newArray){
        mode = CodeGenerator.load;
        visitExpr(newArray.getExpression());
        classCode.add(CodeGenerator.T + "newarray int");
    }//no need

    @Override
    public void visit (NewClass newClass){
        classCode.add(CodeGenerator.T + "new " + newClass.getClassName().getName());
        classCode.add(CodeGenerator.T + "dup");
        classCode.add(CodeGenerator.T + "invokespecial " + newClass.getClassName().getName() + "/<init>()V");
    }//no need

    @Override
    public void visit (This instance){

        classCode.add(CodeGenerator.T + "aload 0");
    }//no need

    @Override
    public void visit (UnaryExpression unaryExpression){

         visitExpr(unaryExpression.getValue());

         if(findExpressionType(unaryExpression.getValue()).toString().equals("int"))
             classCode.add(CodeGenerator.T + "ineg");
         else if(findExpressionType(unaryExpression.getValue()) instanceof BooleanType) {
             classCode.add(CodeGenerator.T + "ifne " + "Label" + currentLabel);
             int first = currentLabel;
             currentLabel++;
             classCode.add(CodeGenerator.T + "iconst_1");
             classCode.add(CodeGenerator.T + "goto " + "Label" + currentLabel);
             int second = currentLabel;
             currentLabel++;
             classCode.add(CodeGenerator.T + "Label" + first + ":");
             classCode.add(CodeGenerator.T + "iconst_0");
             classCode.add(CodeGenerator.T + "Label" + second + ":");

         }
    }

    @Override
    public void visit (BooleanValue value){
        if(value.isConstant())
            classCode.add(CodeGenerator.T + "iconst_1");
        else
            classCode.add(CodeGenerator.T + "iconst_0");
    }//no need

    @Override
    public void visit (IntValue value){
//
//        if(value.getConstant() < 6){
//            classCode.add(CodeGenerator.T + "iconst_" + value.getConstant());
//        }
//        else if(value.getConstant() < 128) {
//            classCode.add(CodeGenerator.T + "bipush " + value.getConstant());
//        }
//        else if(value.getConstant() < 32768){
//            classCode.add(CodeGenerator.T + "sipush " + value.getConstant());
//        }
//        else{
            classCode.add(CodeGenerator.T + "ldc " + value.getConstant());
//        }
    }//no need

    @Override
    public void visit (StringValue value){

        classCode.add(CodeGenerator.T + "ldc " + value.getConstant());
    }//no need

    @Override
    public void visit (Assign assign){
        Expression leftSide = assign.getlValue();
        Expression rightSide = assign.getrValue();

        if(rightSide==null){

            mode = CodeGenerator.load;
            visitExpr(assign.getlValue());

            if(leftSide instanceof NewClass || leftSide instanceof NewArray){
                classCode.add(CodeGenerator.T + "pop");
            }
        }
        else{
            if(leftSide instanceof Identifier){

                Identifier leftside = (Identifier) leftSide;

                if(IsField(leftside)){
                    classCode.add(CodeGenerator.T + "aload_0");
                    mode = CodeGenerator.load;
                    visitExpr(rightSide);
                    classCode.add(CodeGenerator.T + "putfield " + currentClass + "/" + leftside.getName() + " " + typeStringGenerator(findExpressionType(leftside)) );
                }
                else{
                    mode = CodeGenerator.load;
                    visitExpr(rightSide);

                    mode = CodeGenerator.store;
                    visitExpr(leftSide);
                }
            }
            else if(leftSide instanceof ArrayCall){
                mode = CodeGenerator.store;
                visitExpr(leftSide);
                mode = CodeGenerator.load;
                visitExpr(rightSide);
                classCode.add(CodeGenerator.T + "iastore");
            }
        }
    }//no need

    @Override
    public void visit (Block block){
        for (Statement blockStat : block.getBody())
            this.visitStatement(blockStat);
    }//no need

    @Override
    public void visit (Conditional conditional){
        mode = CodeGenerator.load;
        visitExpr(conditional.getExpression());

        int nthen = currentLabel;
        classCode.add(CodeGenerator.T + "ifne " + "Label" + nthen);
        currentLabel++;


        visitStatement(conditional.getAlternativeBody());

        int nafter = currentLabel;
        classCode.add(CodeGenerator.T + "goto " + "Label" + nafter);
        currentLabel++;

        classCode.add(CodeGenerator.T + "Label" + nthen + ":");

        visitStatement(conditional.getConsequenceBody());

        classCode.add(CodeGenerator.T + "Label" + nafter + ":");

    }//no need

    @Override
    public void visit (While loop){

        int nstart = currentLabel;
        classCode.add(CodeGenerator.T + "Label" + nstart + ":");
        currentLabel++;

        mode = CodeGenerator.load;
        visitExpr(loop.getCondition());

        int nexit = currentLabel;
        classCode.add(CodeGenerator.T + "ifeq " + "Label" + nexit);
        currentLabel++;

        visitStatement(loop.getBody());

        classCode.add(CodeGenerator.T + "goto " + "Label" + nstart);

        classCode.add(CodeGenerator.T + "Label" + nexit + ":");

    }//no need

    @Override
    public void visit (Write write){

        //get a print stream
        classCode.add(CodeGenerator.T + "getstatic java/lang/System/out Ljava/io/PrintStream;");

        mode = CodeGenerator.load;
        visitExpr(write.getArg()); //pushes args to stack

        //invoke
        //same problem with int type here!!! toString of type is int! but is not instance of IntType



        if(findExpressionType(write.getArg()).toString().equals("int")){
            classCode.add(CodeGenerator.T + "invokevirtual java/io/PrintStream/println(I)V");
        }
        else if(findExpressionType(write.getArg()) instanceof StringType){
            classCode.add(CodeGenerator.T + "invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V");
        }
        else if(findExpressionType(write.getArg()) instanceof ArrayType){
            classCode.add(CodeGenerator.T + "invokevirtual java/io/PrintStream/println(Ljava/lang/Object;)V");
        }
    }
}








