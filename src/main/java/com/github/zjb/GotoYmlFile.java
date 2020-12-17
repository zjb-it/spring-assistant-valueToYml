package com.github.zjb;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.*;

/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * 2020/12/14
 */

public class GotoYmlFile implements GotoDeclarationHandler {

    private static final String VALUE_QUALIFIED_NAME = "org.springframework.beans.factory.annotation.Value";

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        if (!(sourceElement instanceof PsiJavaToken)) {
            return new PsiElement[0];
        }
        IElementType tokenType = ((PsiJavaToken) sourceElement).getTokenType();
        if (tokenType != JavaTokenType.STRING_LITERAL) {
            return new PsiElement[0];
        }

        PsiAnnotation psiAnnotation = PsiTreeUtil.getParentOfType(sourceElement, PsiAnnotation.class);
        if (Objects.isNull(psiAnnotation) || !Objects.equals(psiAnnotation.getQualifiedName(),VALUE_QUALIFIED_NAME)) {
            return new PsiElement[0];
        }
        String key = psiAnnotation.findAttributeValue("value").getText();
        key = key.substring(key.indexOf("{") + 1, key.indexOf("}"));

        Project project = sourceElement.getProject();
        Collection<VirtualFile> files = FileTypeIndex.getFiles(YAMLFileType.YML, GlobalSearchScope.projectScope(project));
        if (CollectionUtils.isEmpty(files)) {
            return new PsiElement[0];
        }

        List<PsiElement> result = new ArrayList<>(files.size());
        for (VirtualFile file : files) {
            Pair<PsiElement, String> value = YAMLUtil.getValue((YAMLFile) PsiManager.getInstance(sourceElement.getProject()).findFile(file), key.split("\\."));
            if (Objects.nonNull(value)) {
                PsiElement first = value.getFirst();
                result.add(first);
            }
        }
        return result.toArray(new PsiElement[result.size()]);
    }


}
