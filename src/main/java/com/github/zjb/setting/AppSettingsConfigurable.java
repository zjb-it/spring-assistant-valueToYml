package com.github.zjb.setting;

import com.github.zjb.form.Settings;
import com.google.common.collect.Lists;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.impl.FileManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.notebooks.visualization.r.inlays.EditorInlaysManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Provides controller functionality for application settings.
 */
public class AppSettingsConfigurable implements Configurable {

    private Settings mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "ValueToYml";
    }


    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new Settings();
        return mySettingsComponent.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        Vector<Vector> dataVector = mySettingsComponent.getDefaultTableModel().getDataVector();
        ArrayList<Object> objects = Lists.newArrayList();

        for (Vector vector : dataVector) {
            objects.add(vector.get(0));
        }
        settings.ANNOTATIONS = objects;
        settings.hintCheckbox = mySettingsComponent.getHintCheckBox().isSelected();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setData(settings);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}