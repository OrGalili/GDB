package sample;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import java.io.File;
import java.util.*;

import static java.util.stream.Collectors.toList;

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

    @FXML
    public Button StopRunBtn;

    @FXML
    public Button StopDebugBtn;

    @FXML
    public ScrollPane IOPane;

    @FXML
    public TableView<FrameInfo> Stack;

    @FXML
    public Text OutputText;

    public GDBController() throws Exception {
        this.model = new GDBModel();
        model.GDBStart();
        model.setInput("set listsize unlimited");
        model.getOutPut();
    }

    @FXML
    protected void OpenExeFile(ActionEvent event)
    {
        IOPane.setVvalue(1);
        FileChooser f = new FileChooser();
        File file = f.showOpenDialog(((MenuItem)event.getTarget()).getParentPopup().getOwnerWindow());
        if(file !=null) {
            String exeFilePath = file.getAbsolutePath();
            System.out.println(exeFilePath);
            model.setInput("file '" + exeFilePath + "'");
            String loadingExeFileMessage = model.getOutPut();
            if (loadingExeFileMessage.contains("Reading symbols from " + exeFilePath + "...done.")) {
                sourceFiles.getTabs().clear();
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
                disableRunningModeButtons(true);
            } else {
                loadingExeFileMessage = loadingExeFileMessage.substring(loadingExeFileMessage.indexOf(":") + 1);
                Alert windowErr = new Alert(Alert.AlertType.ERROR, loadingExeFileMessage);
                windowErr.getDialogPane().setPrefWidth(270.0);
                windowErr.setTitle("Load File Error");
                windowErr.setHeaderText(null);
                windowErr.setResizable(true);
                windowErr.showAndWait();
            }
        }
    }

    @FXML
    protected void RunExeFile(ActionEvent event)
    {
        model.setInput("run");
        OutputText.setText(OutputText.getText()+model.getOutPut());//redundant output
        if(isFinishedRunning()) {
            getFocusedSourceFile().getSelectionModel().select(-1);
            disableRunningModeButtons(true);
        }
        else {
            nextMarkedLine();
            disableRunningModeButtons(false);
        }
        RefreshInputOutPutPane();
        updateStack();
    }



    @FXML
    protected void Step(ActionEvent event){
        model.setInput("next");
        OutputText.setText(OutputText.getText()+model.getOutPut());//redundant output
        if(isFinishedRunning()) {
            getFocusedSourceFile().getSelectionModel().select(-1);
            disableRunningModeButtons(true);
        }
        else {
            nextMarkedLine();
        }
        RefreshInputOutPutPane();
        updateStack();
    }

    @FXML
    protected void StepIn(ActionEvent event){
        model.setInput("step");
        OutputText.setText(OutputText.getText()+model.getOutPut());//redundant output
        if(isFinishedRunning()) {
            getFocusedSourceFile().getSelectionModel().select(-1);
            disableRunningModeButtons(true);
        }
        else {
            nextMarkedLine();
        }
        RefreshInputOutPutPane();
        updateStack();
    }

    @FXML
    protected void StepOut(ActionEvent event){
        model.setInput("finish");
        OutputText.setText(OutputText.getText()+model.getOutPut());//redundant output
        if(isFinishedRunning()) {
            getFocusedSourceFile().getSelectionModel().select(-1);
            disableRunningModeButtons(true);
        }
        else {
            nextMarkedLine();
        }
        RefreshInputOutPutPane();
        updateStack();
    }

    @FXML
    protected void Continue(ActionEvent event){
        model.setInput("continue");
        OutputText.setText(OutputText.getText()+model.getOutPut());//redundant output
        if(isFinishedRunning()) {
            getFocusedSourceFile().getSelectionModel().select(-1);
            disableRunningModeButtons(true);
        }
        else {
            nextMarkedLine();
        }
        RefreshInputOutPutPane();
        updateStack();
    }

    @FXML
    protected void StopDebug(ActionEvent event){
        model.setInput("detach");
        OutputText.setText(OutputText.getText()+model.getOutPut());//redundant output
        getFocusedSourceFile().getSelectionModel().select(-1);
        disableRunningModeButtons(true);
    }

    @FXML
    protected void StopRun(ActionEvent event){
        model.setInput("kill");
        model.getOutPut();//redundant output
        getFocusedSourceFile().getSelectionModel().select(-1);
        disableRunningModeButtons(true);
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

    private boolean isFinishedRunning(){
        model.setInput("info program");
        if(model.getOutPut().contains("The program being debugged is not being run."))
            return true;
        return false;
    }

    private void disableRunningModeButtons(boolean disableMode){
        StepBtn.setDisable(disableMode);
        StepInBtn.setDisable(disableMode);
        StepOutBtn.setDisable(disableMode);
        ContinueBtn.setDisable(disableMode);
        StopRunBtn.setDisable(disableMode);
        StopDebugBtn.setDisable(disableMode);
    }

    private void updateStack(){
        //when step in , in the end of run there is exception maybe related to this function
        Stack.getItems().clear();
        Stack.getColumns().clear();
        model.setInput("bt");
        String out = model.getOutPut();
        out = out.substring(0, out.length() - 6);
        String[] StackLines = out.split("\n");
        java.util.List<String> StackList = Arrays.asList(StackLines);

        List<String[]> frameLineSpaceSplitted = StackList.stream().map(s1-> s1.split(" ")).collect(toList());
        List<FrameInfo> frameList =  frameLineSpaceSplitted.stream().map(s2-> new FrameInfo(s2[0].substring(1),s2[s2.length-4],s2[s2.length-1].split(":")[0],s2[s2.length-1].split(":")[1])).collect(toList());

        TableColumn frameNumberColumn = new TableColumn("#");
        frameNumberColumn.setCellValueFactory(new PropertyValueFactory<>("frameNumber"));
        TableColumn funcColumn = new TableColumn("Function");
        funcColumn.setCellValueFactory(new PropertyValueFactory<>("funcName"));
        TableColumn classColumn = new TableColumn("Class");
        classColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        TableColumn lineColumn = new TableColumn("Line");
        lineColumn.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        Stack.getColumns().addAll(frameNumberColumn, funcColumn, classColumn,lineColumn);

        Stack.getItems().addAll(frameList);
    }


    //Refreshing to bottom
    private void RefreshInputOutPutPane()
    {
        IOPane.applyCss();
        IOPane.layout();
        IOPane.setVvalue(1);
    }
}
