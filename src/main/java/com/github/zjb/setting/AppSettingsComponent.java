package com.github.zjb.setting;

import com.google.common.collect.Lists;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Vector;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {

    private Vector<Object> columnNames = new Vector<>(Lists.newArrayList("Qualified Name"));

    private final JPanel myMainPanel;

    private DefaultTableModel defaultTableModel;


    {
        AppSettingsState settingsState = AppSettingsState.getInstance();
        List<Object> annotations = settingsState.ANNOTATIONS;
        Vector<Vector<Object>> data = getData(annotations);
        defaultTableModel = new DefaultTableModel(data, columnNames);
    }

    @NotNull
    private Vector<Vector<Object>> getData(List<Object> annotations) {


        Vector<Vector<Object>> data = new Vector<>(annotations.size());
        for (Object annotation : annotations) {
            data.add(new Vector(Lists.newArrayList(annotation)));
        }
        return data;
    }

    public AppSettingsComponent() {

        JPanel actionsPanel = ToolbarDecorator.createDecorator(new JBTable(defaultTableModel))
                .setAddAction(anActionButton -> {
                    defaultTableModel.addRow(new String[]{});

                }).setRemoveAction(anActionButton -> {
                    defaultTableModel.removeRow(((JBTable) anActionButton.getContextComponent()).getSelectedRow());
                })
                .createPanel();


        myMainPanel = FormBuilder.createFormBuilder()
                .addComponentFillVertically(actionsPanel, 0)
                .getPanel();

    }


    public JPanel getPanel() {
        return myMainPanel;
    }


    public DefaultTableModel getDefaultTableModel() {
        return defaultTableModel;
    }

    public void setData(List<Object> datas) {
        this.defaultTableModel.setDataVector(getData(datas), columnNames);

    }
}