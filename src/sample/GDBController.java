package sample;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
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
    public TabPane sourceFiles;

    //@FXML
    //public ListView code ;

    @FXML
    public MenuItem openFile;


    public GDBController() {
        this.model = new GDBModel();
        model.GDBStart();
        model.setInput("set listsize unlimited");
        model.getOutPut();
    }

    @FXML
    protected void OpenExeFile(ActionEvent event)
    {
        FileChooser f = new FileChooser();
        File file = f.showOpenDialog(((MenuItem)event.getTarget()).getParentPopup().getOwnerWindow());
        System.out.println(file.getAbsolutePath());
        model.setInput("file "+file.getAbsolutePath());
        model.getOutPut();

        model.setInput("info sources");
        Collection<String> linesSources = new ArrayList(Arrays.asList(model.getOutPut().split("\n")));
        ((ArrayList<String>) linesSources).remove(0);
        ((ArrayList<String>) linesSources).remove(0);
        linesSources.remove("Source files for which symbols will be read in on demand:");
        linesSources.remove("(gdb) ");
        for(String ls:linesSources){
            String[] filePaths = ls.split(",");
            for (String fp : filePaths) {
                if (fp.length()>1 && !fp.endsWith(".h")) {
                    Tab tab = new Tab();
                    String fileName = fp.substring(fp.lastIndexOf("/")+1);
                    Tooltip tt = new Tooltip();
                    tt.setText(fp);
                    tab.setTooltip(tt);
                    tab.setText(fileName);

                    model.setInput("list "+fp+":0");
                    String out = model.getOutPut();
                    out = out.substring(0,out.length()-6);
                    String[] codeLines = out.split("\n");

                    ListView code = new ListView();
                    java.util.List<String> lines = Arrays.asList(codeLines);
                    code.setItems(FXCollections.observableList(lines));
                    code.setCellFactory(CheckBoxListCell.forListView((Callback<String, ObservableValue<Boolean>>) item -> {
                        BooleanProperty observable = new SimpleBooleanProperty();
                        observable.addListener((obs, wasSelected, isNowSelected)->
                                {
                                    //System.out.println("Check box for "+item+" changed from "+wasSelected+" to "+isNowSelected);
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

                    tab.setContent(code);
                    sourceFiles.getTabs().add(tab);
                }
            }
        }
        sourceFiles.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        model.setInput("list "+sourceFiles.getSelectionModel().getSelectedItem().getTooltip().getText()+":0");
        model.getOutPut();

        sourceFiles.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> ov, Tab oldValue, Tab newValue) {
                        model.setInput("list "+newValue.getTooltip().getText()+":0");
                        model.getOutPut();
                    }
                }
        );
    }

    @FXML
    protected void RunExeFile(ActionEvent event)
    {
        model.setInput("run");
//        code.getSelectionModel().select(getNextLineNumber());
    }

    @FXML
    protected void Step(ActionEvent event){
        model.setInput("next");
//        code.getSelectionModel().select(getNextLineNumber());
    }

    @FXML
    protected void StepIn(ActionEvent event){
        model.setInput("step");
//        code.getSelectionModel().select(getNextLineNumber());
    }

    private int getNextLineNumber(){
        String[] out = model.getOutPut().split("\n");
        int index = out.length-2;
        char c = out[index].toCharArray()[0];
        int lineNumber = (int)c - 48 - 1;
        return lineNumber;
    }
}
