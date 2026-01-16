package net.villagerzock.compiler;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.model.JavacElements;
import com.sun.source.util.*;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.ArrayList;

public class EventPlugin implements Plugin {
    @Override
    public String getName() {
        return "net.villagerzock.Event";
    }

    @Override
    public void init(JavacTask task, String... args) {
        BasicJavacTask basic = (BasicJavacTask) task;
        Context context = basic.getContext();

        TreeMaker maker = TreeMaker.instance(context);
        Names names = Names.instance(context);
        Attr attr = Attr.instance(context);
        JavacElements elements = JavacElements.instance(context);
        Symtab symtab = Symtab.instance(context);

        task.addTaskListener(new TaskListener() {
            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() != TaskEvent.Kind.PARSE) return;

                JCCompilationUnit cu = (JCCompilationUnit) e.getCompilationUnit();
                for (JCTree def : cu.defs){
                    if (def instanceof JCClassDecl cls){
                        transformIfAnnotated(maker, names, symtab, attr, cls, context);
                    }
                }
            }
        });
    }

    private void transformIfAnnotated(TreeMaker maker, Names names, Symtab symtab, Attr attr, JCClassDecl cls, Context context) {
        java.util.List<JCTree> defs = new ArrayList<>();
        for (JCTree tree : cls.defs){
            if (tree instanceof JCMethodDecl methodDecl){
                JCModifiers modifiers = methodDecl.getModifiers();
                if (hasAnnotation(modifiers)){
                    JCClassDecl interfaceDecl = createEventInterface(maker, names, methodDecl, symtab);
                    defs.add(interfaceDecl);

                    JCExpression interfaceExpr = maker.Select(maker.Ident(cls.name), interfaceDecl.name);

                    JCClassDecl listenerDecl = createEventFieldClass(maker, names, methodDecl, symtab, interfaceExpr);
                    defs.add(listenerDecl);

                    JCExpression listenerExpr = maker.Select(maker.Ident(cls.name), listenerDecl.name);


                    int flags = Flags.FINAL;
                    flags |= getAccessFlag(methodDecl.mods);
                    if ((methodDecl.mods.flags & Flags.STATIC) != 0){
                        flags |= Flags.STATIC;
                    }
                    JCExpression initializer = maker.NewClass(
                            null,
                            List.nil(),
                            listenerExpr,
                            List.nil(),
                            null
                    );
                    JCVariableDecl field = maker.VarDef(
                            maker.Modifiers(flags),
                            methodDecl.name,
                            listenerExpr,
                            initializer
                    );
                    defs.add(field);
                }else {
                    defs.add(tree);
                }
            }else {
                defs.add(tree);
            }
        }
        cls.defs = List.from(defs);
    }

    private JCClassDecl createEventInterface(TreeMaker maker, Names names, JCMethodDecl methodDecl, Symtab symtab) {
        int flags = Flags.ABSTRACT;
        flags |= getAccessFlag(methodDecl.mods);

        java.util.List<JCVariableDecl> params = new ArrayList<>();
        TreeCopier<?> copier = new TreeCopier<>(maker);
        for (JCVariableDecl param : methodDecl.params){
            JCVariableDecl p = copier.copy(param);
            params.add(p);
        }

        JCExpression returnType = maker.Type(symtab.voidType);

        JCExpression throwableExpr = dotTogether(maker,names,"java","lang","Throwable");

        JCMethodDecl interfaceMethod = maker.MethodDef(
                maker.Modifiers(flags),
                names.fromString("run"),
                returnType,
                List.nil(),
                List.from(params),
                List.of(throwableExpr),
                null,
                null
        );

        JCClassDecl classDecl = maker.ClassDef(
                maker.Modifiers(Flags.PUBLIC | Flags.STATIC | Flags.INTERFACE),
                names.fromString("eventListener$" + methodDecl.name.toString()),
                List.nil(),
                null,
                List.nil(),
                List.of(interfaceMethod)
        );
        return classDecl;
    }

    private JCClassDecl createEventFieldClass(TreeMaker maker, Names names, JCMethodDecl methodDecl, Symtab symtab, JCExpression interfaceDecl){
        int flags = 0;
        flags |= getAccessFlag(methodDecl.mods);

        java.util.List<JCVariableDecl> params = new ArrayList<>();
        TreeCopier<?> copier = new TreeCopier<>(maker);
        for (JCVariableDecl param : methodDecl.params) {
            JCVariableDecl p = copier.copy(param);
            params.add(p);
        }

        JCExpression returnType = maker.Type(symtab.voidType);

        JCVariableDecl listenerDecl = maker.VarDef(
                maker.Modifiers(0),
                names.fromString("listener"),
                interfaceDecl,
                null
        );

        JCExpression listenersValue = maker.Select(maker.Ident(names._this),names.fromString("listeners"));

        java.util.List<JCExpression> paramExpressions = new ArrayList<>();
        for (JCVariableDecl decl : params){
            paramExpressions.add(maker.Ident(decl.name));
        }

        JCExpression runExpression = maker.Apply(
                List.nil(),
                maker.Select(maker.Ident(names.fromString("listener")),names.fromString("run")),
                List.from(paramExpressions)
        );

        JCStatement runCall = maker.Exec(runExpression);

        JCExpression throwableExpr = dotTogether(maker,names,"java","lang","Throwable");
        JCVariableDecl catchVar = maker.VarDef(
                maker.Modifiers(0),
                names.fromString("ignored"),
                throwableExpr,
                null
        );

        JCExpression catchExpr = maker.Select(maker.Ident(names._this), names.fromString("catchException"));
        JCStatement catchCall = maker.Exec(maker.Apply(
                List.nil(),
                catchExpr,
                List.of(maker.Ident(names.fromString("ignored")), maker.Literal(methodDecl.name.toString()))
        ));

        JCBlock catchBlock = maker.Block(
                0,
                List.of(catchCall)
        );
        JCStatement statement = maker.Try(
                maker.Block(0,List.of(runCall)),
                List.of(maker.Catch(catchVar, catchBlock)),
                null
        );

        JCStatement foreachBody = maker.Block(0, List.of(statement));

        JCStatement foreachStatement = maker.ForeachLoop(
                listenerDecl,
                listenersValue,
                foreachBody
        );

        java.util.List<JCStatement> blockStatements = new ArrayList<>();
        blockStatements.add(foreachStatement);

        for (JCAnnotation anno : methodDecl.mods.annotations){
            if (anno.annotationType instanceof JCIdent expression){
                if (expression.getName().toString().equals("net.villagerzock.Event") || expression.getName().toString().equals("Event")) {
                    if (readValue(anno,false)){
                        JCStatement clearStatement = maker.Exec(maker.Apply(
                                List.nil(),
                                maker.Select(maker.Select(maker.Ident(names._this),names.fromString("listeners")),names.fromString("clear")),
                                List.nil()
                        ));
                        blockStatements.add(clearStatement);
                    }
                    break;
                }
            }
        }

        JCBlock block = maker.Block(0,List.from(blockStatements));
        JCMethodDecl emitMethod = maker.MethodDef(
                maker.Modifiers(flags),
                names.fromString("emit"),
                returnType,
                List.nil(),
                List.from(params),
                List.nil(),
                block,
                null
        );

        JCExpression extendsExpression = maker.TypeApply(
                dotTogether(maker,names,"net","villagerzock","AnnotationLibEventImpl"),
                List.of(interfaceDecl)
        );

        JCClassDecl classDecl = maker.ClassDef(
                maker.Modifiers(Flags.PUBLIC | Flags.STATIC),
                names.fromString("eventObject$" + methodDecl.name.toString()),
                List.nil(),
                extendsExpression,
                List.nil(),
                List.of(emitMethod)
        );
        return classDecl;
    }

    private int getAccessFlag(JCModifiers modifiers){
        if ((modifiers.flags & Flags.PUBLIC) != 0){
            return Flags.PUBLIC;
        }else if ((modifiers.flags & Flags.PROTECTED) != 0){
            return Flags.PROTECTED;
        }else if ((modifiers.flags & Flags.PRIVATE) != 0){
            return Flags.PRIVATE;
        }
        return 0;
    }
    private boolean hasAnnotation(JCModifiers modifiers){
        for (JCAnnotation anno : modifiers.annotations){
            if (anno.annotationType instanceof JCIdent expression){
                if (expression.getName().toString().equals("net.villagerzock.Event") || expression.getName().toString().equals("Event")) return true;
            }
        }
        return false;
    }
    private JCExpression dotTogether(TreeMaker maker, Names names,String... toDot){
        if (toDot.length == 0){
            return null;
        }
        JCExpression expression = maker.Ident(names.fromString(toDot[0]));
        for (int i = 1; i < toDot.length; i++) {
            expression = maker.Select(expression,names.fromString(toDot[i]));
        }
        return expression;
    }

    /**
     * Liest das boolean-Attribut "value" aus:
     * - @Foo              -> defaultValue (z.B. false)
     * - @Foo(true)        -> true
     * - @Foo(value=true)  -> true
     *
     * @return Boolean (nie null), solange defaultValue gesetzt ist
     */
    public static boolean readValue(JCTree.JCAnnotation ann, boolean defaultValue) {
        if (ann == null || ann.args == null || ann.args.isEmpty()) {
            return defaultValue; // @Foo
        }

        // @Foo(true)  (implizit value)
        if (ann.args.size() == 1 && !(ann.args.head instanceof JCTree.JCAssign)) {
            Boolean b = asBooleanLiteralOrIdent(ann.args.head);
            return b != null ? b : defaultValue;
        }

        // @Foo(value = true) (oder mehrere args)
        for (JCTree.JCExpression e : ann.args) {
            if (e instanceof JCTree.JCAssign a) {
                // lhs ist normalerweise ein JCIdent "value"
                String name = a.lhs.toString();
                if ("value".equals(name)) {
                    Boolean b = asBooleanLiteralOrIdent(a.rhs);
                    return b != null ? b : defaultValue;
                }
            }
        }

        return defaultValue;
    }

    private static Boolean asBooleanLiteralOrIdent(JCTree.JCExpression expr) {
        if (expr instanceof JCTree.JCLiteral lit) {
            Object v = lit.getValue();
            if (v instanceof Boolean b) return b;
        }
        if (expr instanceof JCTree.JCIdent id) {
            String s = id.getName().toString();
            if ("true".equals(s)) return Boolean.TRUE;
            if ("false".equals(s)) return Boolean.FALSE;
        }
        return null;
    }
}
