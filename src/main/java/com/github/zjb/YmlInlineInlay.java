package com.github.zjb;

import com.github.zjb.setting.AppSettingsState;
import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class YmlInlineInlay implements FileEditorManagerListener {

    private static final Logger LOG = Logger.getInstance(YmlInlineInlay.class);

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {

        if (!AppSettingsState.getInstance().hintCheckbox) {
            return;
        }
        try {
            Project project = source.getProject();
            DumbService.getInstance(project).smartInvokeLater(() -> {
                PsiFile psiFile = PsiUtilCore.getPsiFile(project, file);
                Collection<PsiAnnotation> psiElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiAnnotation.class);
                if (CollectionUtils.isNotEmpty(psiElements)) {
                    Editor editor = source.getSelectedTextEditor();
                    InlayModel inlayModel = editor.getInlayModel();
                    if (Objects.isNull(inlayModel)) {
                        return;
                    }
                    psiElements.forEach(element -> {
                        if (AppSettingsState.getInstance().ANNOTATIONS.contains(element.getQualifiedName())) {
                            @NotNull PsiElement[] ymlPsiElements = GotoYmlFile.getYmlPsiElements(PsiTreeUtil.findChildOfType(element, PsiLiteralExpression.class));
                            for (PsiElement ymlFile : ymlPsiElements) {
                                inlayModel.addInlineElement(element.getTextOffset() + element.getTextLength(), new HintRenderer(getEnv(ymlFile.getContainingFile().getName()) + " : " + ymlFile.getLastChild().getText()));
                            }
                        }
                    });
                }
            });

        } catch (PsiInvalidElementAccessException e) {
            LOG.error(e);
        }
    }



    public static String getEnv(String ymlFileName) {
        if (ymlFileName.contains("-")) {
            return ymlFileName.substring(ymlFileName.indexOf("-") + 1, ymlFileName.lastIndexOf("."));
        }
        return "default";
    }

}
