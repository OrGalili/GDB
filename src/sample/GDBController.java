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

    @FXML
    public MenuItem openFile;

    @FXML
    public Button RunBtn;

    @FXML
    public Button StepBtn;

    @FXML
    public Button StepInBtn;

    @FXML
    public Button StepOutBtn;

    @FXML
    public Button ContinueBtn;

    public GDBController() throws Exception {
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
        String exeFilePath = file.getAbsolutePath();
        System.out.println(exeFilePath);
        model.setInput("file '"+exeFilePath+"'");
        String loadingExeFileMessage = model.getOutPut();
        loadingExeFileMessage = loadingExeFileMessage.substring(0,loadingExeFileMessage.indexOf('\n'));

        if(loadingExeFileMessage.equals("Reading symbols from "+exeFilePath+"...done.")) {
            model.setInput("info sources");
            Collection<String> linesSources = new ArrayList(Arrays.asList(model.getOutPut().split("\n")));
            ((ArrayList<String>) linesSources).remove(0);
            ((ArrayList<String>) linesSources).remove(0);
            linesSources.remove("Source files for which symbols will be read in on demand:");
            linesSources.remove("(gdb) ");
            for (String ls : linesSources) {
                String[] filePaths = ls.split(",");
                for (String fp : filePaths) {
                    if (fp.length() > 1 && !fp.endsWith(".h")) {
                        Tab tab = new Tab();
                        String fileName = fp.substring(fp.lastIndexOf("/") + 1);
                        Tooltip tt = new Tooltip();
                        tt.setText(fp);
                        tab.setTooltip(tt);
                        tab.setText(fileName);

                        model.setInput("list " + fp + ":0");
                        String out = model.getOutPut();
                        out = out.substring(0, out.length() - 6);
                        String[] codeLines = out.split("\n");

                        ListView code = new ListView();
                        java.util.List<String> lines = Arrays.asList(codeLines);
                        code.setItems(FXCollections.observableList(lines));
                        code.setCellFactory(CheckBoxListCell.forListView((Callback<String, ObservableValue<Boolean>>) item -> {
                            BooleanProperty observable = new SimpleBooleanProperty();
                            observable.addListener((obs, wasSelected, isNowSelected) ->
                                    {
                                        //System.out.println("Check box for "+item+" changed from "+wasSelected+" to "+isNowSelected);
                                        if (!wasSelected) {
                                            model.setInput("break " + item.substring(0, item.indexOf('\t')));
                                        } else
                                            model.setInput("clear " + item.substring(0, item.indexOf('\t')));
                                        model.getOutPut();
                                    }
                            );
                            return observable;
                        }));

                        tab.setContent(code);
                        sourceFiles.getTabs().add(tab);
                    }
                }
            }
            sourceFiles.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            model.setInput("list " + sourceFiles.getSelectionModel().getSelectedItem().getTooltip().getText() + ":0");
            model.getOutPut();

            sourceFiles.getSelectionModel().selectedItemProperty().addListener(
                    new ChangeListener<Tab>() {
                        @Override
                        public void changed(ObservableValue<? extends Tab> ov, Tab oldValue, Tab newValue) {
                            model.setInput("list " + newValue.getTooltip().getText() + ":0");
                            model.getOutPut();
                        }
                    }
            );

            RunBtn.setDisable(false);
            StepBtn.setDisable(false);
            StepInBtn.setDisable(false);
            StepOutBtn.setDisable(false);
            ContinueBtn.setDisable(false);
        }

        else{
            loadingExeFileMessage = loadingExeFileMessage.substring(loadingExeFileMessage.indexOf(":")+1);
            Alert windowErr = new Alert(Alert.AlertType.ERROR, loadingExeFileMessage);
            windowErr.getDialogPane().setPrefWidth(270.0);
            windowErr.setTitle("Load File Error");
            windowErr.setHeaderText(null);
            windowErr.setResizable(true);
            windowErr.showAndWait();
        }
    }

    @FXML
    protected void RunExeFile(ActionEvent event)
    {
        model.setInput("run");
        model.getOutPut();//redundant output
        nextMarkedLine();
    }

    @FXML
    protected void Step(ActionEvent event){
        model.setInput("next");
        model.getOutPut();//redundant output
        nextMarkedLine();
    }

    @FXML
    protected void StepIn(ActionEvent event){
        model.setInput("step");
        model.getOutPut();//redundant output
        nextMarkedLine();
    }

    @FXML
    protected void StepOut(ActionEvent event){
        model.setInput("finish");
        model.getOutPut();//redundant output
        nextMarkedLine();
    }

    @FXML
    protected void Continue(ActionEvent event){
        model.setInput("continue");
        model.getOutPut();//redundant output
        getFocusedSourceFile().getSelectionModel().select(-1);
    }

    private void nextMarkedLine(){
        model.setInput("info source");
        String out = model.getOutPut();
        String[] splittedOut = out.split("\n");
        if(splittedOut[2].startsWith("Located in")){
            String sourceFilePath = splittedOut[2].substring(splittedOut[2].indexOf('/'));
            for(Tab sourceFileTab : sourceFiles.getTabs())
                if(sourceFileTab.getTooltip().getText().equals(sourceFilePath)){
                    sourceFiles.getSelectionModel().select(sourceFileTab);
                    break;
                }
        }

        int lineNumber;
        model.setInput("frame");
        out = model.getOutPut();
        String lineNumberString = out.substring(out.indexOf(":")+1,out.indexOf("\n"));
        try {
            lineNumber = Integer.parseInt(lineNumberString) - 1;
            getFocusedSourceFile().getSelectionModel().select(lineNumber);
        }
        catch (NumberFormatException e) {
        }
    }

    private ListView getFocusedSourceFile(){
        Tab selectedSourceFileTab = sourceFiles.getSelectionModel().getSelectedItem();
        return ((ListView) selectedSourceFileTab.getContent());
    }
}
