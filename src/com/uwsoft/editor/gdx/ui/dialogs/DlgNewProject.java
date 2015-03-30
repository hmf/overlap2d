package com.uwsoft.editor.gdx.ui.dialogs;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.uwsoft.editor.controlles.FileChooserHandler;
import com.uwsoft.editor.gdx.stage.UIStage;
import com.uwsoft.editor.mvc.Overlap2DFacade;
import com.uwsoft.editor.mvc.proxy.DataManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class DlgNewProject extends CompositeDialog {

    private final Overlap2DFacade facade;
    private final DataManager dataManager;
    private TextButton createBtn;
    private TextButton fileChooserBtn;
    private TextField projectPathTextBox;
    private TextField origWidth;
    private TextField origHeight;

    public DlgNewProject(UIStage s) {
        super(s, "NewProjectDlg", 260, 210);

        setTitle("Create New Project");

        s.setKeyboardFocus(ui.getTextBoxById("projectPath"));

        projectPathTextBox = ui.getTextBoxById("projectPath");
        projectPathTextBox.getOnscreenKeyboard().show(true); //why not working this is :( benbenutta!

        fileChooserBtn = ui.getTextButtonById("directoryChooseBtn");

        origWidth = ui.getTextBoxById("width");
        origHeight = ui.getTextBoxById("height");

        origWidth.setText("2400");
        origHeight.setText("1440");

        crateBtnCrateProject();
        facade = Overlap2DFacade.getInstance();
        dataManager = facade.retrieveProxy(DataManager.NAME);
        fileChooserBtn.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                FileChooserHandler chooseHandler = new FileChooserHandler() {
                    @Override
                    public void FileChoosen(JFileChooser jfc) {
                        if (jfc.getSelectedFile() == null) return;
                        projectPathTextBox.setText(jfc.getSelectedFile().getAbsolutePath());
                    }

                    @Override
                    public boolean isMultiple() {
                        return false;
                    }

                    @Override
                    public String getDefaultPath() {
                        return "";
                    }

                    @Override
                    public int getFileSelectionMode() {
                        return JFileChooser.DIRECTORIES_ONLY;
                    }
                };

                //UIController.instance.sendNotification(NameConstants.SHOW_FILE_CHOOSER, chooseHandler);
            }
        });
    }

    private void crateBtnCrateProject() {
        createBtn = ui.getTextButtonById("createBtn");
        createBtn.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                createProject();
            }
        });
    }

    private void createProject() {
        String projectPath = projectPathTextBox.getText();
        if (projectPath == null || projectPath.equals("")) {
            return;
        }

        String projectName = new File(projectPath).getName();

        if (projectName == null || projectName.equals("")) {
            return;
        }

        String workSpacePath = projectPath.substring(0, projectPath.lastIndexOf(projectName));
        if (workSpacePath.length() > 0) {
            dataManager.setLastOpenedPath(workSpacePath);
            dataManager.setWorkspacePath(workSpacePath);
        }

        int origWidthValue = 0;
        int origHeightValue = 0;

        try {
            origWidthValue = Integer.parseInt(origWidth.getText());
            origHeightValue = Integer.parseInt(origHeight.getText());
        } catch (Exception ignored) {
        }

        try {
            dataManager.createEmptyProject(projectName, origWidthValue, origHeightValue);
            dataManager.openProjectAndLoadAllData(projectName);
            stage.getSandbox().loadCurrentProject();
            stage.loadCurrentProject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        remove();
    }

}
