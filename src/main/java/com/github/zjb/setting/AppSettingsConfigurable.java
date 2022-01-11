package com.github.zjb.setting;

import com.google.common.collect.Lists;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Provides controller functionality for application settings.
 */
public class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;

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
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
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
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setData(settings.ANNOTATIONS);


    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}