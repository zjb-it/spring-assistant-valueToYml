package com.github.zjb;

import com.github.zjb.util.EnumUtil;
import com.google.common.collect.Lists;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.JavaMatchers;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.*;

/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * 2020/12/14
 */

public class GotoYmlFile implements GotoDeclarationHandler {

    private static final PsiElement[] DEFAULT_RESULT = new PsiElement[0];


    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement instanceof PsiJavaToken) {
            return javaGoToYml(sourceElement);
        }
        if (sourceElement.getLanguage().is(YAMLLanguage.INSTANCE)) {
            return ymlToJava(sourceElement);
        }
        return DEFAULT_RESULT;
    }

    private PsiElement[] ymlToJava(@NotNull PsiElement sourceElement) {
        String configFullName = YAMLUtil.getConfigFullName(PsiTreeUtil.getParentOfType(sourceElement, YAMLPsiElement.class));
        Pair<PsiElement, String> value = YAMLUtil.getValue((YAMLFile) sourceElement.getContainingFile(), configFullName.split("\\."));
        if (Objects.isNull(value) || !(value.getFirst() instanceof YAMLPlainTextImpl)) {
            return DEFAULT_RESULT;
        }
        Project project = sourceElement.getProject();
        Collection<VirtualFile> files = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        if (CollectionUtils.isEmpty(files)) {
            return DEFAULT_RESULT;
        }
        ArrayList<PsiElement> result = Lists.newArrayList();
        for (VirtualFile file : files) {
            Collection<PsiAnnotation> childrenOfType = PsiTreeUtil.findChildrenOfType(PsiManager.getInstance(project).findFile(file), PsiAnnotation.class);
            if (CollectionUtils.isEmpty(childrenOfType)) {
                continue;
            }

            for (PsiAnnotation psiAnnotation : childrenOfType) {
                if (!EnumUtil.containAnnotation(psiAnnotation)) {
                    continue;
                }
                PsiNameValuePair[] attributes1 = psiAnnotation.getParameterList().getAttributes();
                for (PsiNameValuePair psiNameValuePair : attributes1) {
                    String literalValue = psiNameValuePair.getLiteralValue();
                    if (StringUtils.isBlank(literalValue)) {
                        continue;
                    }

                    String valueKey = literalValue.substring(literalValue.indexOf("${") + 2, literalValue.indexOf("}"));
                    if (Objects.equals(valueKey, configFullName)) {
                        result.add(psiAnnotation);
                    }
                }
            }
        }
        return result.toArray(new PsiElement[result.size()]);
    }

    private PsiElement[] javaGoToYml(@NotNull PsiElement sourceElement) {
        IElementType tokenType = ((PsiJavaToken) sourceElement).getTokenType();
        if (tokenType != JavaTokenType.STRING_LITERAL) {
            return new PsiElement[0];
        }

        PsiAnnotation psiAnnotation = PsiTreeUtil.getParentOfType(sourceElement, PsiAnnotation.class);
        if (!EnumUtil.containAnnotation(psiAnnotation)) {
            return new PsiElement[0];
        }
        String key = sourceElement.getText();
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
