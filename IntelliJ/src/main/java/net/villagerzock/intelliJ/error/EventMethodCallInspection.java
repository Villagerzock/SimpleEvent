package net.villagerzock.intelliJ.error;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public final class EventMethodCallInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                PsiMethod target = expression.resolveMethod();
                if (target == null) return;

                if (!hasEventAnnotation(target)) return;

                PsiReferenceExpression methodExpr = expression.getMethodExpression();

                holder.registerProblem(
                        methodExpr,
                        "@Event methods cannot be called directly. Use the event field instead.",
                        ProblemHighlightType.ERROR
                );
            }
        };
    }

    private static boolean hasEventAnnotation(@NotNull PsiMethod m) {
        for (PsiAnnotation ann : m.getModifierList().getAnnotations()) {
            String qn = ann.getQualifiedName();
            if (qn == null) continue;
            if (qn.equals("Event") || qn.endsWith(".Event")) return true;
        }
        return false;
    }
}
