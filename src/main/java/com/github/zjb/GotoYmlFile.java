package com.github.zjb;

import com.github.zjb.setting.AppSettingsState;
import com.google.common.collect.Lists;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * 2020/12/14
 */

public class GotoYmlFile implements GotoDeclarationHandler {

    private static final PsiElement[] DEFAULT_RESULT = new PsiElement[0];
    public static final String DEFAULT_SPLIT = ":";


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
//        List<VirtualFile> collect = files.stream().filter(file -> file.getName().equals("ConfigApplication.java")).collect(Collectors.toList());
        for (VirtualFile file : files) {
            Collection<PsiAnnotation> childrenOfType = PsiTreeUtil.findChildrenOfType(PsiManager.getInstance(project).findFile(file), PsiAnnotation.class);
            if (CollectionUtils.isEmpty(childrenOfType)) {
                continue;
            }

            for (PsiAnnotation psiAnnotation : childrenOfType) {
                if (!AppSettingsState.getInstance().ANNOTATIONS.contains(psiAnnotation.getQualifiedName())){
                    continue;
                }
                PsiNameValuePair[] attributes1 = psiAnnotation.getParameterList().getAttributes();
                for (PsiNameValuePair psiNameValuePair : attributes1) {
                    JvmAnnotationAttributeValue attributeValue = psiNameValuePair.getAttributeValue();
                    if (checkEquals(configFullName, attributeValue)) {
                        result.add(psiAnnotation);
                        break;
                    }
                }
            }
        }
        return result.toArray(new PsiElement[result.size()]);
    }

    private Boolean checkEquals(String configFullName, JvmAnnotationAttributeValue constantValue) {
        if (constantValue instanceof JvmAnnotationConstantValue) {
            String literalValue = ((JvmAnnotationConstantValue) constantValue).getConstantValue().toString();
            if (!literalValue.startsWith("$")) {
                return false;
            }
            String valueKey = getValueKey(literalValue);
            return Objects.equals(valueKey, configFullName);
        }
        if (constantValue instanceof JvmAnnotationArrayValue) {
            JvmAnnotationArrayValue attributeValue1 = (JvmAnnotationArrayValue) constantValue;
            List<JvmAnnotationAttributeValue> annotationArrayValues = attributeValue1.getValues();
            for (JvmAnnotationAttributeValue value : annotationArrayValues) {
                if (checkEquals(configFullName, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getValueKey(String literalValue) {
        if (!literalValue.contains("${")) {
            return literalValue;
        }
        String valueKey = literalValue.substring(literalValue.indexOf("${") + 2, literalValue.indexOf("}"));
        //带有默认值的注解的解析，比如@Value("${a.b.c:123}")
        if (valueKey.contains(DEFAULT_SPLIT)) {
            valueKey = valueKey.split(DEFAULT_SPLIT)[0];
        }
        return valueKey;
    }

    private static final Logger LOG =
            Logger.getInstance(GotoYmlFile.class);

    private PsiElement[] javaGoToYml(@NotNull PsiElement sourceElement) {
        IElementType tokenType = ((PsiJavaToken) sourceElement).getTokenType();
        if (tokenType != JavaTokenType.STRING_LITERAL) {
            return new PsiElement[0];
        }

        PsiAnnotation psiAnnotation = PsiTreeUtil.getParentOfType(sourceElement, PsiAnnotation.class);
        if (Objects.isNull(psiAnnotation)) {
            return new PsiElement[0];
        }

        if (!AppSettingsState.getInstance().ANNOTATIONS.contains(psiAnnotation.getQualifiedName())) {
            return new PsiElement[0];
        }
//        if (!EnumUtil.containAnnotation(psiAnnotation)) {
//            return new PsiElement[0];
//        }

        return getYmlPsiElements(sourceElement);
    }

    @NotNull
    public static PsiElement[] getYmlPsiElements(@NotNull PsiElement sourceElement) {
        Project project = sourceElement.getProject();
        Collection<VirtualFile> files = FileTypeIndex.getFiles(YAMLFileType.YML, GlobalSearchScope.projectScope(project));

        LOG.info(CollectionUtils.isEmpty(files) + " yml文件是否为空");
        LOG.info(files.stream().map(VirtualFile::getName).collect(Collectors.joining()) + "yml文件名称");

        if (CollectionUtils.isEmpty(files)) {
            return new PsiElement[0];
        }

        String key = sourceElement.getText();
        key = getValueKey(key);
        List<PsiElement> result = new ArrayList<>(files.size());
        for (VirtualFile file : files) {
            YAMLKeyValue qualifiedKeyInFile = YAMLUtil.getQualifiedKeyInFile((YAMLFile) PsiManager.getInstance(project).findFile(file), key.split("\\."));
            if (Objects.nonNull(qualifiedKeyInFile)){
                result.add(qualifiedKeyInFile);
            }
        }
        return result.toArray(new PsiElement[result.size()]);
    }

}
