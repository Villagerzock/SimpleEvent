package net.villagerzock.intelliJ.codeVision;


import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering;
import com.intellij.codeInsight.hints.codeVision.CodeVisionProviderBase;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.*;

public class EmittersCodeVision extends CodeVisionProviderBase {
    @Override
    public boolean acceptsFile(@NotNull PsiFile psiFile) {
        return psiFile instanceof PsiJavaFile;
    }

    @Override
    public boolean acceptsElement(@NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiMethod method){
            PsiModifierList mods = method.getModifierList();
            for (PsiAnnotation ann : mods.getAnnotations()){
                String name = ann.getQualifiedName();
                if (name == null) continue;
                if (name.equals("Event") || name.endsWith(".Event")){
                    return true;
                }
            }
        }
        return false;
    }
    public static @NotNull List<PsiElement> findAllListenersFor(@NotNull PsiMethod method, String methodName) {


        PsiClass owner = method.getContainingClass();
        if (owner == null) return List.of();

        String fieldName = method.getName();

        // sollte augmentierte Felder finden, wenn dein AugmentProvider sie "normal" anhängt
        PsiField field = owner.findFieldByName(fieldName, true);
        if (field == null) {
            // Fallback: manchmal tauchen Light-Felder eher hier auf
            for (PsiField f : owner.getAllFields()) {
                if (fieldName.equals(f.getName())) {
                    field = f;
                    break;
                }
            }
            if (field == null) return List.of();
        }

        Project project = method.getProject();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        List<PsiElement> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (PsiReference ref : ReferencesSearch.search(field, scope).findAll()) {
            PsiElement refElement = ref.getElement();

            // Referenz auf Feld als Expression finden
            PsiReferenceExpression fieldRef = PsiTreeUtil.getParentOfType(refElement, PsiReferenceExpression.class, false);
            if (fieldRef == null) continue;

            // nach oben: method call
            PsiMethodCallExpression call = PsiTreeUtil.getParentOfType(fieldRef, PsiMethodCallExpression.class, true);
            if (call == null) continue;

            // muss addListener sein
            PsiReferenceExpression methodExpr = call.getMethodExpression();
            if (!methodName.equals(methodExpr.getReferenceName())) continue;

            // qualifier muss auf genau unser Feld resolven
            PsiExpression qualifier = methodExpr.getQualifierExpression();
            if (!(qualifier instanceof PsiReferenceExpression qRef)) continue;

            PsiElement resolved = qRef.resolve();
            if (resolved == null || !field.isEquivalentTo(resolved)) continue;

            // dedupe
            PsiFile file = call.getContainingFile();
            if (file == null) continue;

            String key = file.getVirtualFile() + ":" + call.getTextRange();
            if (seen.add(key)) out.add(call);
        }

        return out;
    }
    @Override
    public @Nls @Nullable String getHint(@NotNull PsiElement psiElement, @NotNull PsiFile psiFile) {
        int amountOfListeners = 0;
        if (psiElement instanceof PsiMethod method){
            List<PsiElement> listeners = findAllListenersFor(method,"emit");
            amountOfListeners = listeners.size();
        }
        return amountOfListeners == 0 ? "no Emitters" : amountOfListeners == 1 ? "1 Emitter" : amountOfListeners + " Emitters";
    }

    @Override
    public void handleClick(@NotNull Editor editor, @NotNull PsiElement psiElement, @Nullable MouseEvent mouseEvent) {
        if (psiElement instanceof PsiMethod method && mouseEvent != null){
            List<PsiElement> listeners = findAllListenersFor(method,"emit");
            ListenersCodeVision.navigateToTargets(RelativePoint.fromScreen(mouseEvent.getPoint()),listeners,"Emitter");
        }
    }



    @Override
    public @Nls @NotNull String getName() {
        return "Emitters of this Event";
    }

    @Override
    public @NotNull List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return List.of();
    }

    @Override
    public @NotNull String getId() {
        return "net.villagerzock.emitters";
    }
}
