package net.villagerzock.intelliJ.usage;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public class UsageProvider implements ImplicitUsageProvider {
    @Override
    public boolean isImplicitUsage(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod psiMethod)) return false;
        for (PsiAnnotation ann : psiMethod.getModifierList().getAnnotations()){
            String name = ann.getQualifiedName();
            if (name == null) continue;
            if (name.equals("Event") || name.endsWith(".Event")){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isImplicitRead(@NotNull PsiElement psiElement) {
        return false;
    }

    @Override
    public boolean isImplicitWrite(@NotNull PsiElement psiElement) {
        return false;
    }
}
