package com.github.zjb;

import com.github.zjb.setting.AppSettingsState;
import com.intellij.codeInsight.daemon.impl.EditorTracker;
import com.intellij.codeInsight.daemon.impl.EditorTrackerListener;
import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.*;
import com.intellij.psi.util.FileTypeUtils;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.notebooks.visualization.r.inlays.EditorInlaysManager;
import org.jetbrains.yaml.YAMLFileType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class YmlInlineInlay implements BulkFileListener, EditorTrackerListener {

    private static final Logger LOG = Logger.getInstance(YmlInlineInlay.class);


    public static String getEnv(String ymlFileName) {
        if (ymlFileName.contains("-")) {
            return ymlFileName.substring(ymlFileName.indexOf("-") + 1, ymlFileName.lastIndexOf("."));
        }
        return "default";
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        for (VFileEvent event : events) {
            VirtualFile file = event.getFile();
            if (!file.isValid()) {
                continue;
            }
            if (file.getFileType() instanceof YAMLFileType || file.getFileType() instanceof JavaFileType) {
                Project project = ProjectLocator.getInstance().guessProjectForFile(file);
                List<? extends Editor> activeEditors = EditorTracker.getInstance(project).getActiveEditors();
                for (Editor activeEditor : activeEditors) {
                    showInlay(activeEditor, FileDocumentManager.getInstance().getFile(activeEditor.getDocument()), project);
                }
            }
        }


    }

    @Override
    public void activeEditorsChanged(@NotNull List<? extends Editor> activeEditors) {
        for (Editor editor : activeEditors) {
            VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (!file.isValid()) {
                continue;
            }
            try {
                Project project = editor.getProject();
                showInlay(editor, file, project);
            } catch (PsiInvalidElementAccessException e) {
                LOG.error(e);
            }
        }
    }

    private static void showInlay(Editor editor, VirtualFile file, Project project) {
        DumbService.getInstance(project).smartInvokeLater(() -> {
            try {
                PsiFile psiFile = PsiUtilCore.getPsiFile(project, file);
                if (psiFile == null) {
                    return;
                }
                if (!(psiFile.getFileType() instanceof JavaFileType)) {
                    return;
                }

                Collection<PsiAnnotation> psiElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiAnnotation.class);
                if (CollectionUtils.isEmpty(psiElements)) {
                    return;
                }
                InlayModel inlayModel = editor.getInlayModel();
                if (Objects.isNull(inlayModel)) {
                    return;
                }
                psiElements.forEach(element -> {
                    if (AppSettingsState.getInstance().ANNOTATIONS.contains(element.getQualifiedName())) {
                        TextRange textRange = element.getTextRange();
                        inlayModel.getInlineElementsInRange(textRange.getStartOffset(), textRange.getEndOffset(), HintRenderer.class).forEach(Inlay::dispose);
                        @NotNull PsiElement[] ymlPsiElements = GotoYmlFile.getYmlPsiElements(PsiTreeUtil.findChildOfType(element, PsiLiteralExpression.class));
                        for (PsiElement ymlFile : ymlPsiElements) {
                            inlayModel.addInlineElement(element.getTextOffset() + element.getTextLength(), new HintRenderer(getEnv(ymlFile.getContainingFile().getName()) + " : " + ymlFile.getLastChild().getText()));
                        }
                    }
                });
            } catch (Exception e) {
                LOG.error(e);
            }
        });
    }
}
