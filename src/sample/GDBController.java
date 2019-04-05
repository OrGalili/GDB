package sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import java.io.File;
import java.util.*;

public class GDBController {
    private GDBModel model;

    @FXML
    public ListView code ;

    @FXML
    public MenuItem openFile;


    public GDBController() {
        this.model = new GDBModel();
        model.GDBStart();
    }

    @FXML
    protected void OpenExeFile(ActionEvent event)
    {
        FileChooser f = new FileChooser();
        File file = f.showOpenDialog(((MenuItem)event.getTarget()).getParentPopup().getOwnerWindow());
        System.out.println(file.getAbsolutePath());
        model.setInput("file "+file.getAbsolutePath());
        model.getOutPut();
        model.setInput("list");
        String out = model.getOutPut();
        out = out.substring(0,out.length()-6);
        String[] codeLines = out.split("\n");



        //code.backgroundProperty().setValue(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        java.util.List<String> lines = Arrays.asList(codeLines);
        code.setItems(FXCollections.observableList(lines));
        code.setCellFactory(CheckBoxListCell.forListView((Callback<String, ObservableValue<Boolean>>) item -> {
            BooleanProperty observable = new SimpleBooleanProperty();
            observable.addListener((obs, wasSelected, isNowSelected)->
                    {
                        System.out.println("Check box for "+item+" changed from "+wasSelected+" to "+isNowSelected);
                        if(!wasSelected) {
                            model.setInput("break " + item.toCharArray()[0]);
                        }
                        else
                            model.setInput("clear "+item.toCharArray()[0]);
                        model.getOutPut();
                    }
            );
            return observable ;
        }));
    }

    @FXML
    protected void RunExeFile(ActionEvent event)
    {
        model.setInput("run");
        code.getSelectionModel().select(getNextLineNumber());
    }

    @FXML
    protected void Step(ActionEvent event){
        model.setInput("step");
        code.getSelectionModel().select(getNextLineNumber());
    }

    private int getNextLineNumber(){
        String[] out = model.getOutPut().split("\n");
        int index = out.length-2;
        char c = out[index].toCharArray()[0];
        int lineNumber = (int)c - 48 - 1;
        return lineNumber;
    }
}
