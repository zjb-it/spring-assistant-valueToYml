package com.github.zjb.form;

import com.github.zjb.setting.AppSettingsState;
import com.google.common.collect.Lists;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

public class Settings {
    private JPanel mainPanel;
    private JPanel qualifiedNameTablePanel;
    private JPanel configPanel;
    private JPanel donatePanel;
    private JCheckBox hintCheckBox;
    private JButton donateBtn;

    private DefaultTableModel defaultTableModel;

    private static Vector<Object> columnNames = new Vector<>(Lists.newArrayList("Qualified Name"));

//    private AppSettingsState settingsState = AppSettingsState.getInstance();

    public JPanel getMainPanel() {
        return mainPanel;
    }


    public Settings() {
        AppSettingsState settingsState = AppSettingsState.getInstance();
        donateBtn.setIcon(IconLoader.getIcon("/icons/support.svg", Settings.class.getClassLoader()));
        hintCheckBox.addChangeListener(e -> settingsState.hintCheckbox = hintCheckBox.isSelected());

        donateBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Donate dialog = new Donate();
                    dialog.pack();
                    dialog.setVisible(true);
                }
                super.mousePressed(e);
            }
        });
    }

    private void createUIComponents() {
        AppSettingsState settingsState = AppSettingsState.getInstance();
        List<Object> annotations = settingsState.ANNOTATIONS;
        Vector<Vector<Object>> data = this.getData(annotations);
        defaultTableModel = new DefaultTableModel(data, columnNames);
        qualifiedNameTablePanel = ToolbarDecorator.createDecorator(new JBTable(defaultTableModel))
                .setAddAction(anActionButton -> defaultTableModel.addRow(new String[]{}))
                .setRemoveAction(anActionButton -> defaultTableModel.removeRow(((JBTable) anActionButton.getContextComponent()).getSelectedRow()))
                .createPanel();
    }

    public JCheckBox getHintCheckBox() {
        return hintCheckBox;
    }

    public DefaultTableModel getDefaultTableModel() {
        return defaultTableModel;
    }

    public void setData(AppSettingsState settings) {
        this.defaultTableModel.setDataVector(getData(settings.ANNOTATIONS), columnNames);
        this.hintCheckBox.setSelected(settings.hintCheckbox);
    }

    @NotNull
    public Vector<Vector<Object>> getData(List<Object> annotations) {
        Vector<Vector<Object>> data = new Vector<>(annotations.size());
        for (Object annotation : annotations) {
            data.add(new Vector(Lists.newArrayList(annotation)));
        }
        return data;
    }
}
