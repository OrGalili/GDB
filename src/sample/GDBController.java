package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GDBController {
    private GDBModel model;

    @FXML
    public TextArea code ;

    @FXML
    public Button btn;

    @FXML
    public MenuItem openFile;


    public GDBController() {
        this.model = new GDBModel();
        model.GDBStart();
    }

    @FXML
    protected void PrintHello(ActionEvent event)
    {
        //Window owner = btn.getScene().getWindow();
        code.setText("hello");
    }

    @FXML
    protected void OpenExeFile(ActionEvent event)
    {

        FileChooser f = new FileChooser();
        File file = f.showOpenDialog(btn.getScene().getWindow());
        System.out.println(file.getAbsolutePath());
        model.setInput("file "+file.getAbsolutePath());
        model.getOutPut();
        model.setInput("list");
        String out = model.getOutPut();
        code.setText(out);


        /*FileDialog f = new FileDialog((Frame)null,"Select file to open");
        f.setMode(FileDialog.LOAD);
        f.setVisible(true);
        String file = f.getFile();
        f.showOpenDialog();*/
    }




    public void initController() { // Components name when defined in XML?
        // Events
        /*view.getFirstnameSaveButton().addActionListener(e -> saveFirstname());
        view.getLastnameSaveButton().addActionListener(e -> saveLastname());
        view.getHello().addActionListener(e -> sayHello());
        view.getBye().addActionListener(e -> sayBye());*/
    }

    private void saveFirstname() {
        // Event Handler
        /*model.setFirstname(view.getFirstnameTextfield().getText());
        JOptionPane.showMessageDialog(null, "Firstname saved : " + model.getFirstname(), "Info", JOptionPane.INFORMATION_MESSAGE);*/
    }

}
