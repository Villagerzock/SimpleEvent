package net.villagerzock.intelliJ.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public final class EventMethodCompletionFilter extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet result) {

        result.runRemainingContributors(parameters, completionResult -> {
            LookupElement le = completionResult.getLookupElement();
            PsiElement psi = le.getPsiElement();

            PsiMethod method = null;
            if (psi instanceof PsiMethod m) method = m;

            if (method != null && hasEventAnnotation(method)) {
                return; // rausfiltern
            }

            result.passResult(completionResult);
        });
    }

    private boolean hasEventAnnotation(PsiMethod m) {
        for (PsiAnnotation ann : m.getModifierList().getAnnotations()) {
            String qn = ann.getQualifiedName();
            if (qn == null) continue;
            if (qn.equals("Event") || qn.endsWith(".Event")) return true;
        }
        return false;
    }
}

