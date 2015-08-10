package eu.mihosoft.vrl.instrumentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.LocatedMessage;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import com.google.common.base.Objects;

import eu.mihosoft.vrl.lang.VCommentParser;
import eu.mihosoft.vrl.lang.model.Argument;
import eu.mihosoft.vrl.lang.model.BinaryOperatorInvocationImpl;
import eu.mihosoft.vrl.lang.model.ClassDeclaration;
import eu.mihosoft.vrl.lang.model.CodeEntity;
import eu.mihosoft.vrl.lang.model.CodeLineColumnMapper;
import eu.mihosoft.vrl.lang.model.CodeLocation;
import eu.mihosoft.vrl.lang.model.CodeRange;
import eu.mihosoft.vrl.lang.model.Comment;
import eu.mihosoft.vrl.lang.model.CommentImpl;
import eu.mihosoft.vrl.lang.model.CompilationUnitDeclaration;
import eu.mihosoft.vrl.lang.model.ConstantValueFactory;
import eu.mihosoft.vrl.lang.model.ControlFlowScope;
import eu.mihosoft.vrl.lang.model.DeclarationInvocation;
import eu.mihosoft.vrl.lang.model.Extends;
import eu.mihosoft.vrl.lang.model.IArgument;
import eu.mihosoft.vrl.lang.model.ICodeRange;
import eu.mihosoft.vrl.lang.model.IModifiers;
import eu.mihosoft.vrl.lang.model.IType;
import eu.mihosoft.vrl.lang.model.IdRequest;
import eu.mihosoft.vrl.lang.model.Invocation;
import eu.mihosoft.vrl.lang.model.Modifier;
import eu.mihosoft.vrl.lang.model.Modifiers;
import eu.mihosoft.vrl.lang.model.Operator;
import eu.mihosoft.vrl.lang.model.Parameter;
import eu.mihosoft.vrl.lang.model.Parameters;
import eu.mihosoft.vrl.lang.model.Scope;
import eu.mihosoft.vrl.lang.model.SimpleForDeclaration_Impl;
import eu.mihosoft.vrl.lang.model.Type;
import eu.mihosoft.vrl.lang.model.Variable;
import eu.mihosoft.vrl.lang.model.VisualCodeBuilder_Impl;
import eu.mihosoft.vrl.workflow.FlowFactory;
import eu.mihosoft.vrl.workflow.IdGenerator;

class VGroovyCodeVisitor extends org.codehaus.groovy.ast.ClassCodeVisitorSupport {

    private SourceUnit sourceUnit;
    private VisualCodeBuilder_Impl codeBuilder;
    private final Scope rootScope;
    private Scope currentScope;
    private Stack<String> vIdStack = new Stack<>();
    private final StateMachine stateMachine = new StateMachine();
    private IdGenerator generator = FlowFactory.newIdGenerator();
    private List<Comment> comments = new ArrayList<>();
    private Reader codeReader;
    private CodeLineColumnMapper mapper;

    private Map<Expression, Invocation> returnVariables
            = new HashMap<>();

    public VGroovyCodeVisitor(SourceUnit sourceUnit, VisualCodeBuilder_Impl codeBuilder) {

        this.sourceUnit = sourceUnit;
        this.codeBuilder = codeBuilder;

        codeBuilder.setIdRequest(new IdRequest() {
            @Override
            public String request() {
                return requestId();
            }
        });

        try {
            BufferedReader br = new BufferedReader(sourceUnit.getSource().getReader());

            String tmp = null;
            StringBuilder sb = new StringBuilder();
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp).append("\n");
            }

            codeReader = sourceUnit.getSource().getReader();
            mapper = new CodeLineColumnMapper();
            mapper.init(codeReader);

        } catch (IOException ex) {
            Logger.getLogger(VGroovyCodeVisitor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                codeReader.reset();
            } catch (IOException ex) {
                Logger.getLogger(VGroovyCodeVisitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String packageName = "";

        if (sourceUnit.getAST().getPackage() != null) {
            packageName = sourceUnit.getAST().getPackage().getName();
        }

        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.length() - 1);
        }

//        this.rootScope = codeBuilder.createScope(null, ScopeType.NONE, sourceUnit.getName(), new Object[0]);
        this.rootScope = codeBuilder.declareCompilationUnit(sourceUnit.getName(), packageName);

        setRootCodeRange(rootScope, codeReader);

        try {
            comments.addAll(VCommentParser.parse(codeReader, false));
        } catch (IOException ex) {
            Logger.getLogger(VGroovyCodeVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        addCommentsToScope(rootScope, comments);
        this.currentScope = rootScope;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    private String requestId() {

        String result = "";

        if (!vIdStack.isEmpty()) {
            result = vIdStack.pop();

            if (generator.getIds().contains(result)) {
//                System.err.println(">> requestId(): Id already defined: " + result);
                result = generator.newId();
            } else {
                generator.addId(result);
//                System.out.println(">> USING ID: " + result);
            }
        } else {
            result = generator.newId();
        }

        return result;
    }

    @Override
    public void visitClass(ClassNode s) {

//        System.out.println("CLASS: " + s.getName());

//        currentScope = codeBuilder.createScope(currentScope, ScopeType.CLASS, s.getName(), new Object[0]);
        currentScope = codeBuilder.declareClass((CompilationUnitDeclaration) currentScope,
                new Type(s.getName(), false),
                convertModifiers(s.getModifiers()),
                convertExtends(s),
                convertImplements(s));

        setCodeRange(currentScope, s);
        addCommentsToScope(currentScope, comments);

        super.visitClass(s);

        currentScope = currentScope.getParent();

//        currentScope.setCode(getCode(s));
    }

    @Override
    public void visitMethod(MethodNode s) {

//        System.out.println("m: " + s.getName() + ", parentscope: " + currentScope.getName() + ": " + currentScope.getType());

        if (currentScope instanceof ClassDeclaration) {

            currentScope = codeBuilder.declareMethod(
                    (ClassDeclaration) currentScope, convertModifiers(s.getModifiers()), new Type(s.getReturnType().getName(), true),
                    s.getName(), convertMethodParameters(s.getParameters()));
            setCodeRange(currentScope, s);
            addCommentsToScope(currentScope, comments);
        } else {
            throwErrorMessage("method cannot be declared here! Scope: " + currentScope.getName() + ": " + currentScope.getType(), s);
        }

//        currentScope.setCode(getCode(s));
        super.visitMethod(s);

        currentScope = currentScope.getParent();

//        currentScope.setCode(getCode(s));
    }

    @Override
    public void visitReturnStatement(ReturnStatement s) {
        if (currentScope instanceof ControlFlowScope) {
            ControlFlowScope cfS = (ControlFlowScope) currentScope;
            IArgument arg = convertExpressionToArgument(s.getExpression());
            codeBuilder.returnValue(cfS, arg);
        }
    }

    @Override
    public void visitBreakStatement(BreakStatement s) {
        if (currentScope instanceof ControlFlowScope) {
            ControlFlowScope cfS = (ControlFlowScope) currentScope;
            codeBuilder.invokeBreak(cfS);
        }
    }

    @Override
    public void visitContinueStatement(ContinueStatement s) {
        if (currentScope instanceof ControlFlowScope) {
            ControlFlowScope cfS = (ControlFlowScope) currentScope;
            codeBuilder.invokeContinue(cfS);
        }
    }

    @Override
    public void visitNotExpression(NotExpression n) {

        if (returnVariables.containsKey(n)) {
            return;
        }

        if (currentScope instanceof ControlFlowScope) {
            ControlFlowScope cfS = (ControlFlowScope) currentScope;
            IArgument arg = convertExpressionToArgument(n.getExpression());
            Invocation notInvocation = codeBuilder.invokeNot(cfS, arg);
            setCodeRange(notInvocation, n);
            returnVariables.put(n, notInvocation);
        }
    }

//    @Override
//    public void visitBlockStatement(BlockStatement s) {
//        System.out.println(" --> new Scope");
//        super.visitBlockStatement(s);
//        System.out.println(" --> leave Scope");
//    }
    @Override
    public void visitForLoop(ForStatement s) {
        System.out.println(" --> FOR-LOOP: " + s.getVariable());

        if (!(currentScope instanceof ControlFlowScope)) {
            throwErrorMessage("For-Loop can only be invoked inside ControlFlowScopes!", s);
        }

        // predeclaration, ranges will be defined later
        currentScope = codeBuilder.invokeForLoop((ControlFlowScope) currentScope, null, 0, 0, 0);
        setCodeRange(currentScope, s);
        addCommentsToScope(currentScope, comments);

        stateMachine.push("for-loop", true);

        super.visitForLoop(s);

        if (!stateMachine.getBoolean("for-loop:declaration")) {
            throwErrorMessage(
                    "For loop must contain a variable declaration "
                    + "such as 'int i=0'!", s.getVariable());
        }

        if (!stateMachine.getBoolean("for-loop:compareExpression")) {
            throwErrorMessage("for-loop: must contain binary"
                    + " expressions of the form 'a <= b'/'a >= b' with a, b being"
                    + " constant integers!", s);
        }

        if (!stateMachine.getBoolean("for-loop:incExpression")) {
            throwErrorMessage("for-loop: must contain binary"
                    + " expressions of the form 'i+=a'/'i-=a' with i being"
                    + " an integer variable and a being a constant integer!", s);
        }

        stateMachine.pop();

        currentScope = currentScope.getParent();

//        currentScope.setCode(getCode(s));
//        System.exit(1);
    }

    @Override
    public void visitWhileLoop(WhileStatement s) {

        stateMachine.push("while-loop", true);

        BooleanExpression expression = s.getBooleanExpression();

        if (expression == null || expression.getExpression() == null) {
            throwErrorMessage("while-loop: must contain boolean"
                    + " expression! No expression is not supported", s);

            return;
        }

        if (expression.getExpression() instanceof ConstantExpression) {
            ConstantExpression constExpression
                    = (ConstantExpression) expression.getExpression();

            if (constExpression.isNullExpression()) {
                throwErrorMessage("while-loop: must contain boolean"
                        + " expression! Null expression is not supported.", s);
            } else {

                IType expressionType
                        = Type.fromObject(constExpression.getValue(), true);

                if (!Type.BOOLEAN.equals(expressionType)
                        && !new Type("java.lang.Boolean").equals(expressionType)) {
                    throwErrorMessage("while-loop: must contain boolean"
                            + " expression! '" + expressionType
                            + "' is not supported.", s);
                }
            }
        } else if (expression.getExpression() instanceof BinaryExpression) {
            Operator operator = convertOperator(
                    (BinaryExpression) expression.getExpression());

            if (!BinaryOperatorInvocationImpl.booleanOperator(operator)) {
                throwErrorMessage("while-loop: must contain boolean"
                        + " expression! Only boolean binary operations are "
                        + "supported.", s);
            }
        } else if (expression.getExpression() instanceof MethodCallExpression) {

            MethodCallExpression mExp
                    = (MethodCallExpression) expression.getExpression();

            if (!convertMethodReturnType(mExp).equals(Type.BOOLEAN)) {
                throwErrorMessage("while-loop: must contain boolean"
                        + " expression! Only boolean method calls are "
                        + "supported.", s);
            }
        } else if (expression.getExpression() instanceof StaticMethodCallExpression) {
            StaticMethodCallExpression mExp
                    = (StaticMethodCallExpression) expression.getExpression();

            if (!convertMethodReturnType(mExp).equals(Type.BOOLEAN)) {
                throwErrorMessage("while-loop: must contain boolean"
                        + " expression! Only boolean method calls are "
                        + "supported.", s);
            }
        } else {
            throwErrorMessage("while-loop: must contain boolean"
                    + " expression!" + expression.getExpression(), s);
        }

        if (!(currentScope instanceof ControlFlowScope)) {
            throwErrorMessage("While-Loop can only be invoked "
                    + "inside ControlFlowScopes!", s);
        }

        currentScope = codeBuilder.invokeWhileLoop((ControlFlowScope) currentScope,
                convertExpressionToArgument(
                        s.getBooleanExpression().getExpression()));

        setCodeRange(currentScope, s);
        addCommentsToScope(currentScope, comments);
        super.visitWhileLoop(s);
        currentScope = currentScope.getParent();

        stateMachine.pop();
    }

    @Override
    public void visitIfElse(IfStatement s) {
//        System.out.println(" --> IF-STATEMENT: " + ifElse.getBooleanExpression());
//
//        currentScope = codeBuilder.createScope(currentScope, ScopeType.IF, "if", new Object[0]);
//
//        ifElse.getBooleanExpression().visit(this);
//        ifElse.getIfBlock().visit(this);
//
//        currentScope = currentScope.getParent();
//
//        currentScope = codeBuilder.createScope(currentScope, ScopeType.ELSE, "else", new Object[0]);
//        setCodeRange(currentScope, ifElse);
//        addCommentsToScope(currentScope, comments);
//
//        Statement elseBlock = ifElse.getElseBlock();
//        if (elseBlock instanceof EmptyStatement) {
//            // dispatching to EmptyStatement will not call back visitor, 
//            // must call our visitEmptyStatement explicitly
//            visitEmptyStatement((EmptyStatement) elseBlock);
//        } else {
//            elseBlock.visit(this);
//        }
//
//        currentScope = currentScope.getParent();
//
////        currentScope.setCode(getCode(ifElse));

        boolean isElseIf = stateMachine.getBoolean("else-statement:else-is-if");

        stateMachine.push("if-statement", true);

        if (s.getBooleanExpression().getExpression() == null) {
            throwErrorMessage("if-statement: must contain boolean"
                    + " expression!", s.getBooleanExpression());
        }

        if (!(currentScope instanceof ControlFlowScope)) {
            throwErrorMessage("If-Statement can only be invoked inside ControlFlowScopes!", s);
        }

        if (isElseIf) {
            currentScope = codeBuilder.invokeElseIf((ControlFlowScope) currentScope,
                    convertExpressionToArgument(
                            s.getBooleanExpression().getExpression()));
        } else {
            currentScope = codeBuilder.invokeIf((ControlFlowScope) currentScope,
                    convertExpressionToArgument(
                            s.getBooleanExpression().getExpression()));
        }

        setCodeRange(currentScope, s);
        addCommentsToScope(currentScope, comments);

        s.getIfBlock().visit(this);

        currentScope = currentScope.getParent();

        Statement elseBlock = s.getElseBlock();
        if (elseBlock instanceof EmptyStatement) {
            // dispatching to EmptyStatement will not call back visitor, 
            // must call our visitEmptyStatement explicitly
            visitEmptyStatement((EmptyStatement) elseBlock);
        } else {

            stateMachine.push("else-statement", true);

            boolean elseIsIf = (elseBlock instanceof IfStatement);

            stateMachine.setBoolean("else-statement:else-is-if", elseIsIf);

            if (elseIsIf) {
                visitIfElse((IfStatement) elseBlock);
            } else {
                currentScope = codeBuilder.invokeElse((ControlFlowScope) currentScope);
                setCodeRange(currentScope, s);
                addCommentsToScope(currentScope, comments);
                elseBlock.visit(this);

                currentScope = currentScope.getParent();
            }

            stateMachine.pop();
        }

        stateMachine.pop();
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression s) {

        if (returnVariables.containsKey(s)) {
            return;
        }

        super.visitConstructorCallExpression(s);

        ArgumentListExpression args = (ArgumentListExpression) s.getArguments();

        IArgument[] arguments = convertArguments(args);

        Invocation invocation = codeBuilder.createInstance(
                currentScope, new Type(s.getType().getName(), false),
                arguments);

        setCodeRange(invocation, s);

        if (stateMachine.getBoolean("variable-declaration")) {
            stateMachine.addToList("variable-declaration:assignment-invocations", invocation);
            System.out.println("DECL-add-inv: " + invocation);
        }

        returnVariables.put(s, invocation);

        // TODO range
    }

    private String getCode(ASTNode n) {
        String code = sourceUnit.getSample(n.getLineNumber(), n.getColumnNumber(), null);
        return code;
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression s) {

        if (returnVariables.containsKey(s)) {
            return;
        }

        super.visitStaticMethodCallExpression(s);

        ArgumentListExpression args = (ArgumentListExpression) s.getArguments();
        IArgument[] arguments = convertArguments(args);

        String objectName = null;

        boolean isIdCall = false;

        String returnValueName = "void";

        IType returnType = convertMethodReturnType(s);

        if (!(currentScope instanceof ControlFlowScope)) {
            throwErrorMessage("Method can only be invoked inside ControlFlowScopes!", s);
        }


        Invocation invocation = codeBuilder.invokeStaticMethod(
                (ControlFlowScope) currentScope,
                convertStaticMethodOwnerType(s),
                s.getMethod(),
                returnType,
                arguments);

        if (stateMachine.getBoolean("variable-declaration")) {

            stateMachine.addToList("variable-declaration:assignment-invocations", invocation);

        }

        setCodeRange(invocation, s);
        addCommentsToScope(currentScope, comments);

        returnVariables.put(s, invocation);

    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression s) {

        if (returnVariables.containsKey(s)) {
            return;
        }


        super.visitMethodCallExpression(s);

        ArgumentListExpression args = (ArgumentListExpression) s.getArguments();
        IArgument[] arguments = convertArguments(args);

        String objectName = null;

        boolean isIdCall = false;

        if (s.getObjectExpression() instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) s.getObjectExpression();
            objectName = ve.getName();
        } else if (s.getObjectExpression() instanceof ClassExpression) {
            ClassExpression ce = (ClassExpression) s.getObjectExpression();
            objectName = ce.getType().getName();

            if (ce.getType().getName().equals(VSource.class.getName())) {
                isIdCall = true;

                for (IArgument arg : arguments) {

                    // TODO is this still in use? 18.02.2014
                    vIdStack.push(arg.toString());
                }
            }
        }

        String returnValueName = "void";

        IType returnType = convertMethodReturnType(s);

        if (!(currentScope instanceof ControlFlowScope)) {
            throwErrorMessage("Method can only be invoked inside ControlFlowScopes!", s);
        }

        if (!isIdCall) {
            if (objectName != null) {

                Invocation invocation = codeBuilder.invokeMethod(
                        (ControlFlowScope) currentScope, objectName,
                        s.getMethod().getText(),
                        returnType,
                        arguments);

                if (stateMachine.getBoolean("variable-declaration")) {

                    stateMachine.addToList("variable-declaration:assignment-invocations", invocation);

                }

                setCodeRange(invocation, s);
                addCommentsToScope(currentScope, comments);

                returnVariables.put(s, invocation);

            } else if (s.getMethod().getText().equals("println")) {
//                codeBuilder.invokeStaticMethod(currentScope, new Type("System.out"), s.getMethod().getText(), isVoid,
//                        returnValueName, arguments).setCode(getCode(s));
                Invocation invocation = codeBuilder.invokeStaticMethod(
                        (ControlFlowScope) currentScope, new Type("System.out"),
                        s.getMethod().getText(), Type.VOID,
                        arguments);
                setCodeRange(invocation, s);
                addCommentsToScope(currentScope, comments);
//                if (invocation.getReturnValue().isPresent()) {
                returnVariables.put(s, invocation);
//                }
            }
        }

    }

    private IType convertMethodReturnType(MethodCallExpression s) {
        boolean isVoid = true;
        MethodNode mTarget = (MethodNode) s.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mTarget != null && mTarget.getReturnType() != null) {
            isVoid = mTarget.getReturnType().getName().toLowerCase().equals("void");
            //System.out.println("TYPECHECKED!!!");
        } else {
            System.out.println("NO TYPECHECKING!!!");
        }
        IType returnType;
        if (!isVoid) {
            returnType = new Type(mTarget.getReturnType().getName());
        } else {
            returnType = Type.VOID;
        }
        return returnType;
    }

    private IType convertMethodReturnType(StaticMethodCallExpression s) {
        boolean isVoid = true;
        MethodNode mTarget = (MethodNode) s.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mTarget != null && mTarget.getReturnType() != null) {
            isVoid = mTarget.getReturnType().getName().toLowerCase().equals("void");
            //System.out.println("TYPECHECKED!!!");
        } else {
            System.out.println("NO TYPECHECKING!!!");
        }
        IType returnType;
        if (!isVoid) {
            returnType = new Type(mTarget.getReturnType().getName());
        } else {
            returnType = Type.VOID;
        }
        return returnType;
    }

    private IType convertStaticMethodOwnerType(StaticMethodCallExpression s) {
        return convertType(s.getOwnerType());
    }

    @Override
    public void visitField(FieldNode field) {
        if (!(currentScope instanceof ClassDeclaration)) {
            throwErrorMessage("Field '"
                    + field.getName()
                    + "' cannot be declared inside a scope of type '"
                    + currentScope.getType() + "'.", field);

            return;
        }

        String varType = field.getType().getName();
        String varName = field.getName();

        DeclarationInvocation declInv
                = codeBuilder.declareVariable(currentScope,
                        new Type(varType, true),
                        varName);

        IModifiers fieldModifiers = convertModifiers(field.getModifiers());

        declInv.getDeclaredVariable().setModifiers(fieldModifiers);

        Expression initialValueExpression = field.getInitialExpression();

        if (initialValueExpression != null) {

            throwErrorMessage("Direct field initialization currently not supported. Field '"
                    + field.getName()
                    + "' cannot be initialized. Please move initialization to a constructor.", initialValueExpression);

            return;
//            TODO 30.07.2014 : fix this!
//            codeBuilder.assign(currentScope, varName,
//                    convertExpressionToArgument(initialValueExpression)
//            );
        }

        setCodeRange(declInv, field);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression s) {

        if (currentScope instanceof SimpleForDeclaration_Impl
                && !stateMachine.getBoolean("for-loop:declaration")) {

            SimpleForDeclaration_Impl forD = (SimpleForDeclaration_Impl) currentScope;

            if (!stateMachine.getBoolean("for-loop:declaration")) {

                String varType = s.getVariableExpression().getType().getNameWithoutPackage();
                String varName = s.getVariableExpression().getAccessedVariable().getName();

                if (!(Objects.equal(varType, "int") || Objects.equal(varType, "Integer"))) {
                    throwErrorMessage("In for-loop: variable '" + varName
                            + "' must be of type integer!", s.getVariableExpression());
                }

                forD.setVarName(s.getVariableExpression().getName(), setCodeRange(s));

                if (!(s.getRightExpression() instanceof ConstantExpression)) {
                    throwErrorMessage("In for-loop: variable '" + forD.getVarName()
                            + "' must be initialized with an integer constant!", s);
                }

                ConstantExpression ce = (ConstantExpression) s.getRightExpression();

                if (!(ce.getValue() instanceof Integer)) {
                    throwErrorMessage("In for-loop: variable '" + forD.getVarName()
                            + "' must be initialized with an integer constant!", s);
                }

                forD.setFrom((Integer) ce.getValue());

                stateMachine.setBoolean("for-loop:declaration", true);
            }

        } else {

            stateMachine.setBoolean("variable-declaration", true);

            DeclarationInvocation declInv
                    = codeBuilder.declareVariable(currentScope,
                            new Type(s.getVariableExpression().getType().getName(), true),
                            s.getVariableExpression().getName());

            setCodeRange(declInv, s);
//
//            stateMachine.setEntity("variable-declaration:declaration-invocation", declInv);

//            Variable variable = declInv.getDeclaredVariable();
//            System.out.println("decl: " + declInv);

//            if (s.getRightExpression() != null) {
//
//                if (s.getRightExpression() instanceof ConstantExpression) {
//                    ConstantExpression ce = (ConstantExpression) s.getRightExpression();
//
//                    System.out.println("ce: " + ce.getValue());
//
//                    BinaryOperatorInvocation assignInv = codeBuilder.assignConstant(
//                            currentScope, variable.getName(), ce.getValue());
//                    setCodeRange(assignInv, s);
//                }
//            }
            super.visitDeclarationExpression(s);

//            List<Invocation> assignmentInvocations = stateMachine.getList("variable-declaration:assignment-invocations");
//
//            if (!assignmentInvocations.isEmpty()) {
//
//                Invocation argumentInv = assignmentInvocations.get(assignmentInvocations.size() - 1);
//
////                Invocation argumentInvocation
////                        
////                        currentScope.getControlFlow().getInvocations().add(declInvIndex + 1, argumentInv);
//                Invocation assignInvocation = codeBuilder.assignInvocationResult(currentScope, declInv.getDeclaredVariable().getName(), argumentInv);
//
//                setCodeRange(assignInvocation, s);
//
//            } else {
//                System.err.println("EMPTY");
//            }
            stateMachine.setBoolean("variable-declaration", false);

        }
    }

    @Override
    public void visitBinaryExpression(BinaryExpression s) {

        if (stateMachine.getBoolean("for-loop")
                && !stateMachine.getBoolean("for-loop:compareExpression")
                && !stateMachine.getBoolean("for-loop:incExpression")) {

            SimpleForDeclaration_Impl forD = (SimpleForDeclaration_Impl) currentScope;

            if (stateMachine.getBoolean("for-loop:declaration")
                    && !stateMachine.getBoolean("for-loop:compareExpression")) {

                if (!(s.getLeftExpression() instanceof VariableExpression)) {
                    throwErrorMessage("In for-loop: only binary"
                            + " expressions of the form 'a <= b'/'a >= b' with a, b being"
                            + " constant integers are supported!", s);
                }

                if (!"<=".equals(s.getOperation().getText())
                        && !">=".equals(s.getOperation().getText())) {
                    throwErrorMessage("In for-loop: only binary"
                            + " expressions of the form 'a <= b' or 'a >= b' with a, b being"
                            + " constant integers are supported!", s);
                }

                stateMachine.setString("for-loop:compareOperation", s.getOperation().getText());

                if (!(s.getRightExpression() instanceof ConstantExpression)) {
                    throwErrorMessage("In for-loop: only binary"
                            + " expressions of the form 'a <= b' or 'a >= b' with a, b being"
                            + " constant integers are supported!", s);
                }

                ConstantExpression ce = (ConstantExpression) s.getRightExpression();

                if (!(ce.getValue() instanceof Integer)) {
//                    throw new IllegalStateException("In for-loop: value '" + ce.getValue()
//                            + "' is not an integer constant! ");

                    throwErrorMessage("In for-loop: only binary"
                            + " expressions of the form 'a <= b' or 'a >= b' with a, b being"
                            + " constant integers are supported!", s);
                }

                forD.setTo((int) ce.getValue());

                stateMachine.setBoolean("for-loop:compareExpression", true);
            }
        } else if (stateMachine.getBoolean("for-loop")
                && stateMachine.getBoolean("for-loop:declaration")
                && stateMachine.getBoolean("for-loop:compareExpression")
                && !stateMachine.getBoolean("for-loop:incExpression")) {

            SimpleForDeclaration_Impl forD = (SimpleForDeclaration_Impl) currentScope;

            if (!"+=".equals(s.getOperation().getText())
                    && !"-=".equals(s.getOperation().getText())) {
                throw new IllegalStateException("In for-loop: inc/dec '"
                        + s.getOperation().getText()
                        + "' not spupported! Must be '+=' or '-=' or '++' or '--'!");
            }

            if (!(s.getRightExpression() instanceof ConstantExpression)) {
                throwErrorMessage("In for-loop: variable '" + forD.getVarName()
                        + "' must be initialized with an integer constant!", s);
            }

            ConstantExpression ce = (ConstantExpression) s.getRightExpression();

            if (!(ce.getValue() instanceof Integer)) {
                throwErrorMessage(
                        "In for-loop: inc/dec must be an integer constant!", s);
            }

            if ("+=".equals(s.getOperation().getText())) {
                forD.setInc((int) ce.getValue());
            } else if ("-=".equals(s.getOperation().getText())) {
                forD.setInc(-(int) ce.getValue());
            }

            if (forD.getInc() > 0 && ">=".
                    equals(stateMachine.getString("for-loop:compareOperation"))) {
                throwErrorMessage("In for-loop: infinite loops"
                        + " are not supported! Change '>=' to '<=' to prevent that.", s
                );
            }

            if (forD.getInc() < 0 && "<=".
                    equals(stateMachine.getString("for-loop:compareOperation"))) {
//                    throw new IllegalStateException("In for-loop: infinite loops"
//                            + " are not supported! Change '<=' to '>=' to prevent that."
//                    );
                throwErrorMessage("In for-loop: infinite loops"
                        + " are not supported! Change '<=' to '>=' to prevent that.", s
                );
            }

//                System.out.println("s: " + s.getOperation().getText() + ", " + forD.getInc());
//                System.exit(0);
//                if (forD.getInc() < 0 && "<=".equals(s.getOperation().getText())) {
//                    throw new IllegalStateException("In for-loop: infinite loops"
//                            + " are not supported! Change '<=' to '>=' to prevent that."
//                    );
//                }
            stateMachine.setBoolean("for-loop:incExpression", true);

            //
        } else {

            if (!returnVariables.containsKey(s)) {

                Operator operator = convertOperator(s);
                IArgument leftArg = convertExpressionToArgument(s.getLeftExpression());
                IArgument rightArg = convertExpressionToArgument(s.getRightExpression());

                boolean emptyAssignment = (Objects.equal(Argument.NULL, rightArg) && operator == Operator.ASSIGN);

                if (!emptyAssignment) {

                    Invocation invocation = codeBuilder.invokeOperator(
                            currentScope,
                            leftArg, rightArg, operator
                    );

                    setCodeRange(invocation, s);

                    returnVariables.put(s, invocation);
                }
            }
        }

        super.visitBinaryExpression(s);
    }

    @Override
    public void visitBooleanExpression(BooleanExpression s) {

        super.visitBooleanExpression(s);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression s) {

        if (stateMachine.getBoolean("for-loop")) {

            SimpleForDeclaration_Impl forD = (SimpleForDeclaration_Impl) currentScope;

            if ("++".equals(s.getOperation().getText())) {
                forD.setInc(1);
            } else if ("--".equals(s.getOperation().getText())) {
                forD.setInc(-1);
            }

            if (forD.getInc() > 0 && ">=".
                    equals(stateMachine.getString("for-loop:compareOperation"))) {
//                throw new IllegalStateException("In for-loop: infinite loops"
//                        + " are not supported! Change '>=' to '<=' to prevent that."
//                );
                throwErrorMessage("In for-loop: infinite loops"
                        + " are not supported! Change '>=' to '<=' to prevent that.", s
                );
            }

            if (forD.getInc() < 0 && "<=".
                    equals(stateMachine.getString("for-loop:compareOperation"))) {
//                throw new IllegalStateException("In for-loop: infinite loops"
//                        + " are not supported! Change '<=' to '>=' to prevent that."
//                );
                throwErrorMessage("In for-loop: infinite loops"
                        + " are not supported! Change '<=' to '>=' to prevent that.", s);
            }

            stateMachine.setBoolean("for-loop:incExpression", true);
        }

        super.visitPostfixExpression(s);
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        super.visitPrefixExpression(expression);
    }

    /**
     * @return the rootScope
     */
    public Scope getRootScope() {
        return rootScope;
    }

    private void throwErrorMessage(String text, ASTNode node) {

        // thanks to http://grails.io/post/15965611310/lessons-learnt-developing-groovy-ast-transformations
        Token token = Token.newString(
                node.getText(),
                node.getLineNumber(),
                node.getColumnNumber());
        LocatedMessage message = new LocatedMessage(text, token, sourceUnit);
        sourceUnit
                .getErrorCollector()
                .addError(message);
    }

//    /**
//     * @param rootScope the rootScope to set
//     */
//    public void setRootScope(Scope rootScope) {
//        this.rootScope = rootScope;
//    }
    private Operator convertOperator(BinaryExpression be) {
        switch (be.getOperation().getType()) {
            case org.codehaus.groovy.syntax.Types.PLUS:
                return Operator.PLUS;
            case org.codehaus.groovy.syntax.Types.MINUS:
                return Operator.MINUS;
            case org.codehaus.groovy.syntax.Types.MULTIPLY:
                return Operator.TIMES;
            case org.codehaus.groovy.syntax.Types.DIVIDE:
                return Operator.DIV;
            case org.codehaus.groovy.syntax.Types.ASSIGN:
                return Operator.ASSIGN;
            case org.codehaus.groovy.syntax.Types.PLUS_EQUAL:
                return Operator.PLUS_ASSIGN;
            case org.codehaus.groovy.syntax.Types.MINUS_EQUAL:
                return Operator.MINUS_ASSIGN;
            case org.codehaus.groovy.syntax.Types.MULTIPLY_EQUAL:
                return Operator.TIMES_ASSIGN;
            case org.codehaus.groovy.syntax.Types.DIVIDE_EQUAL:
                return Operator.DIV_ASSIGN;
            case org.codehaus.groovy.syntax.Types.COMPARE_EQUAL:
                return Operator.EQUALS;
            case org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL:
                return Operator.NOT_EQUALS;
            case org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN:
                return Operator.GREATER;
            case org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN_EQUAL:
                return Operator.GREATER_EQUALS;
            case org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN:
                return Operator.LESS;
            case org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN_EQUAL:
                return Operator.LESS_EQUALS;
            case org.codehaus.groovy.syntax.Types.LOGICAL_OR:
                return Operator.OR;
            case org.codehaus.groovy.syntax.Types.LOGICAL_AND:
                return Operator.AND;
            case org.codehaus.groovy.syntax.Types.LEFT_SQUARE_BRACKET:
                return Operator.ACCESS_ARRAY_ELEMENT;

            default:

                String leftStr = be.getLeftExpression().getText();
                String opStr = be.getOperation().getText();
                String rightStr = be.getRightExpression().getText();

                throw new UnsupportedOperationException(
                        "Operation " + opStr + " not supported! Left: " + leftStr + ", right: " + rightStr);

        }
    }

    private IArgument[] convertArguments(ArgumentListExpression args) {
        IArgument[] arguments = new IArgument[args.getExpressions().size()];
        for (int i = 0; i < args.getExpressions().size(); i++) {
            arguments[i] = convertExpressionToArgument(args.getExpression(i));
        }
        return arguments;
    }

    private IArgument convertExpressionToArgument(Expression e) {

        stateMachine.push("convert-argument", true);

        IArgument result = null;

        if (e instanceof ConstantExpression) {
            ConstantExpression ce = (ConstantExpression) e;

            if (ce.isNullExpression()) {
                result = Argument.NULL;
            } else {
            	result = Argument.constArg(ConstantValueFactory.createConstantValue(ce.getValue(),new Type(ce.getType().getName())));
            }
        } else if (e instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) e;

            Variable v = currentScope.getVariable(ve.getName());
            result = Argument.varArg(v);

        } else if (e instanceof PropertyExpression) {
//            PropertyExpression pe = (PropertyExpression) e;
//
//            Variable v = VariableFactory.createObjectVariable(currentScope, new Type("vrl.internal.PROPERTYEXPR", true), "don't know");
//            result = Argument.varArg(v);

            throw new UnsupportedOperationException("vrl.internal.PROPERTYEXPR not supported");

        } else if (e instanceof MethodCallExpression) {
            System.out.println("TYPE: " + e);
            visitMethodCallExpression((MethodCallExpression) e);
            result = Argument.invArg(returnVariables.get((MethodCallExpression) e));
        } else if (e instanceof StaticMethodCallExpression) {
            System.out.println("TYPE: " + e);
            visitStaticMethodCallExpression((StaticMethodCallExpression) e);
            result = Argument.invArg(returnVariables.get((StaticMethodCallExpression) e));
        } else if (e instanceof ConstructorCallExpression) {
            System.out.println("TYPE: " + e);
            System.out.println("CONSTRUCTOR: " + returnVariables.get((ConstructorCallExpression) e));
            visitConstructorCallExpression((ConstructorCallExpression) e);
            result = Argument.invArg(returnVariables.get((ConstructorCallExpression) e));
        } else if (e instanceof BinaryExpression) {
            System.out.println("TYPE: " + e);
            System.out.println("BINARY-EXPR: " + returnVariables.get((BinaryExpression) e));
            System.out.println("ARG: " + stateMachine.getBoolean("convert-argument"));
            visitBinaryExpression((BinaryExpression) e);
            result = Argument.invArg(returnVariables.get((BinaryExpression) e));
        } else if (e instanceof NotExpression) {
            System.out.println("TYPE: " + e);
            System.out.println("NOT-EXPR: " + returnVariables.get((NotExpression) e));
            System.out.println("ARG: " + stateMachine.getBoolean("convert-argument"));
            visitNotExpression((NotExpression) e);
            result = Argument.invArg(returnVariables.get((NotExpression) e));
        } else // if nothing worked so far, we assumen null arg
        if (result == null) {
            System.err.println(" -> UNSUPPORTED-ARG: " + e);
            result = Argument.NULL;
        }

        stateMachine.pop();

        return result;
    }

    private IType convertType(ClassNode type) {
        return new Type(type.getName());
    }

    private Parameters convertMethodParameters(org.codehaus.groovy.ast.Parameter... params) {
        Parameter[] result = new Parameter[params.length];

        for (int i = 0; i < params.length; i++) {
            org.codehaus.groovy.ast.Parameter p = params[i];

            String pType = p.getType().getName();

//            if (pType.startsWith("[L")) {
//                System.err.print("convertMethodParameters(): array param not supported! " + pType);
//                pType = pType.replace("[L", "").replace(";", "");
//            }
            result[i] = new Parameter(new Type(pType, true), p.getName(), setCodeRange(p));
        }

        return new Parameters(result);
    }

    private IModifiers convertModifiers(int modifiers) {

        List<Modifier> modifierList = new ArrayList<>();

        // TODO rethink modifiers design (21.10.2013)
        if (java.lang.reflect.Modifier.isPublic(modifiers)) {
            modifierList.add(Modifier.PUBLIC);
        }
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
            modifierList.add(Modifier.PRIVATE);
        }
        if (java.lang.reflect.Modifier.isProtected(modifiers)) {
            modifierList.add(Modifier.PROTECTED);
        }
        if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
            modifierList.add(Modifier.ABSTRACT);
        }
        if (java.lang.reflect.Modifier.isFinal(modifiers)) {
            modifierList.add(Modifier.FINAL);
        }
        if (java.lang.reflect.Modifier.isStatic(modifiers)) {
            modifierList.add(Modifier.STATIC);
        }

        return new Modifiers(modifierList.toArray(new Modifier[modifierList.size()]));
    }

    private Extends convertExtends(ClassNode n) {

        ClassNode superType = n.getSuperClass();

        Type type = new Type(superType.getName(), false);

        Extends result = new Extends(type);

        return result;
    }

    private Extends convertImplements(ClassNode n) {

        Collection<ClassNode> interfaces = n.getAllInterfaces();

        Type[] types = new Type[interfaces.size()];

        int i = 0;
        for (ClassNode classNode : interfaces) {
            types[i] = new Type(classNode.getName(), false);
            i++;
        }

        Extends result = new Extends(types);

        return result;
    }

    private void setCodeRange(CodeEntity codeEntity, ASTNode astNode) {

        codeEntity.setRange(new CodeRange(
                astNode.getLineNumber() - 1, astNode.getColumnNumber() - 1,
                astNode.getLastLineNumber() - 1, astNode.getLastColumnNumber() - 1,
                mapper));
//
//        System.out.println("range: " + codeEntity.getRange());
//
//        CodeReader reader = new CodeReader(codeReader);
//        try {
//
//            System.out.println("----code:----\n" + reader.read(codeEntity.getRange()) + "\n-------------");
//        } catch (IOException ex) {
//            Logger.getLogger(VGroovyCodeVisitor.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }

    private ICodeRange setCodeRange(ASTNode astNode) {

        return new CodeRange(
                astNode.getLineNumber() - 1, astNode.getColumnNumber() - 1,
                astNode.getLastLineNumber() - 1, astNode.getLastColumnNumber() - 1,
                mapper);
    }

    private void setRootCodeRange(Scope scope, Reader codeReader) {

    	CodeLineColumnMapper mapper = new CodeLineColumnMapper();
    	mapper.init(codeReader);
        scope.setRange(new CodeRange(new CodeLocation(0, mapper),
                mapper));

//        System.out.println("range: " + scope.getRange());

    }

    private void addCommentsToScope(Scope scope, List<Comment> comments) {
        for (Comment comment : comments) {
//            System.out.println("comment: " + comment.getRange());
            if (scope.getRange().contains(comment.getRange())) {
                ((CommentImpl) comment).setParent(scope);
                scope.getComments().add(comment);
            }
        }
    }
}