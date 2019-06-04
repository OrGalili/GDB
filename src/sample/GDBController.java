package sample;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import java.io.File;
import java.net.URL;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class GDBController implements Initializable {
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
    public TabPane ToolsPane;

    @FXML
    public ScrollPane IOPane;

    @FXML
    public TableView<FrameInfo> Stack;

    @FXML
    public TableView<FrameInfo> Breakpoints;

    @FXML
    public TableView<VariableInfo> Variables;

    @FXML
    public Text OutputText;

    @FXML
    public TextField InputText;


    public GDBController() throws Exception {
        this.model = new GDBModel();
        model.GDBStart();
        model.setInput("set listsize unlimited");
        model.getOutPut();
    }

    private final ChangeListener<Tab> tabItemSelected = new ChangeListener<Tab>() {
        @Override
        public void changed(ObservableValue<? extends Tab> ov, Tab oldValue, Tab newValue) {
            try {
                model.setInput("list " + newValue.getTooltip().getText() + ":0");
                model.getOutPut();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sourceFiles.getSelectionModel().selectedItemProperty().addListener(tabItemSelected);

        Stack.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            goTo(obs, oldSelection, newSelection);
        });

        Breakpoints.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            goTo(obs, oldSelection, newSelection);
        });

        InputText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                InputText.setPrefWidth(InputText.getText().length() * 20);
            }
        });
    }

    public void shutdown() {
        Platform.exit();
        model.stopGDB();
    }

    private void goTo(Object obs, FrameInfo oldSelection, FrameInfo newSelection) {
        if (newSelection != null) {
            java.util.Optional<Tab> seletedTab = sourceFiles.getTabs().stream().filter(s -> s.getTooltip().getText().endsWith(newSelection.getClassName())).findFirst();
            if (seletedTab.isPresent()) {
                sourceFiles.getSelectionModel().select(seletedTab.get());
                getFocusedSourceFile().getSelectionModel().select(Integer.parseInt(newSelection.getLineNumber()) - 1);
                getFocusedSourceFile().scrollTo(Integer.parseInt(newSelection.getLineNumber()) - 1 - 6);//window set resizable false
            }
        }
    }

    private void onCheckBoxClicked(boolean wasSelected, boolean isNowSelected, String codeLine) {
        try {
            if (isNowSelected) {
                model.setInput("break " + codeLine.substring(0, codeLine.indexOf('\t')));
            } else
                model.setInput("clear " + codeLine.substring(0, codeLine.indexOf('\t')));
            model.getOutPut();
            updateBreakpoints();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ObservableValue<Boolean> refreshCheckboxCelll(String item) {
        BooleanProperty observable = new SimpleBooleanProperty();
        String lineNumber;
        String fileName;
        if (Breakpoints.getItems().size() > 0) {
            lineNumber = item.substring(0, item.indexOf('\t'));
            fileName = sourceFiles.getSelectionModel().getSelectedItem().getText();

            if (Breakpoints.getItems().stream().anyMatch(bp -> bp.getLineNumber().equals(lineNumber) && bp.getClassName().equals(fileName))) {
                observable.set(true);
            }
        }
        observable.addListener((obs, wasSelected, isNowSelected) ->
                {
                    onCheckBoxClicked(wasSelected, isNowSelected, item);
                }
        );
        return observable;
    }

    //Check all the cases can be: if the specific line is simple line , sleep or inputRequest etc.
    private void nextLine() {
        sourceFiles.setMouseTransparent(true);
        sourceFiles.setFocusTraversable(false);
        openFile.setDisable(true);
        RunBtn.setDisable(true);
        InputText.setDisable(false);
        InputText.setOnAction(GDBController.this::onEnter);
        ToolsPane.getSelectionModel().select(2);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                InputText.requestFocus();
            }
        });
        String gdbResponse = "";
        try {
            gdbResponse = model.getOutPut();
            if (!model.GDBAlive())
                return;
            while (!gdbResponse.endsWith("\n(gdb) ")) {
                //   if(!gdbResponse.startsWith("\n"))
                //       gdbResponse = "\n"+gdbResponse;
                String beforeText = OutputText.getText();
                OutputText.setText(beforeText + gdbResponse);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //Update UI here
                        RefreshInputOutPutPane();
                    }
                });
                gdbResponse = model.getOutPut();
                if (!model.GDBAlive())
                    return;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //if(!gdbResponse.startsWith("\n"))
        // gdbResponse = "\n"+gdbResponse;
        if (gdbResponse.endsWith("\n(gdb) "))
            gdbResponse = gdbResponse.substring(0, gdbResponse.indexOf("(gdb) "));
        OutputText.setText(OutputText.getText() + gdbResponse);
        InputText.setDisable(true);
        InputText.setOnAction(null);
        RunBtn.setDisable(false);
        sourceFiles.setMouseTransparent(false);
        sourceFiles.setFocusTraversable(true);
        openFile.setDisable(false);
    }

    @FXML
    protected void OpenExeFile(ActionEvent event) {
        try {
            FileChooser f = new FileChooser();
            File file = f.showOpenDialog(((MenuItem) event.getTarget()).getParentPopup().getOwnerWindow());
            if (file != null) {
                String exeFilePath = file.getAbsolutePath();
                System.out.println(exeFilePath);
                model.setInput("file '" + exeFilePath + "'");
                String loadingExeFileMessage = model.getOutPut();
                if (loadingExeFileMessage.contains("Reading symbols from " + exeFilePath + "...done.")) {
                    OutputText.setText("");
                    sourceFiles.getSelectionModel().selectedItemProperty().removeListener(tabItemSelected);
                    sourceFiles.getTabs().clear();
                    sourceFiles.getSelectionModel().selectedItemProperty().addListener(tabItemSelected);
                    Stack.getItems().clear();
                    Breakpoints.getItems().clear();
                    Variables.getItems().clear();
                    model.setInput("info sources");
                    Collection<String> linesSources = new ArrayList(Arrays.asList(model.getOutPut().split("\n")));
                    ((ArrayList<String>) linesSources).remove(0);
                    ((ArrayList<String>) linesSources).remove(0);
                    linesSources.remove("Source files for which symbols will be read in on demand:");
                    linesSources.remove("(gdb) ");
                    for (String ls : linesSources) {
                        String[] filePaths = ls.split(",");
                        List<String> sourcefilePaths = Arrays.stream(filePaths).filter(fp -> fp.endsWith(".c") || fp.endsWith(".cpp")).collect(toList());
                        for (String fp : sourcefilePaths) {
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
                                    return refreshCheckboxCelll(item);
                                }));
                                tab.setContent(code);
                                sourceFiles.getTabs().add(tab);
                            }
                        }
                    }
                    sourceFiles.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
                    model.setInput("list " + sourceFiles.getSelectionModel().getSelectedItem().getTooltip().getText() + ":0");
                    model.getOutPut();

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onEnter(ActionEvent ae) {
        String text = InputText.getText();
        System.out.println(text);
        model.setInput(text);
        OutputText.setText(OutputText.getText() + text + "\n");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //Update UI here
                InputText.clear();
            }
        });
    }

    @FXML
    protected void RunExeFile(ActionEvent event) {
        new Thread() {
            public void run() {
                try {
                    OutputText.setText("");
                    InputText.clear();
                    disableRunningModeButtons(true);
                    model.setInput("run");
                    nextLine();
                    //OutputText.setText(OutputText.getText() + model.getOutPut());//redundant output
                    if (model.GDBAlive())
                        updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @FXML
    protected void Step(ActionEvent event) {
        new Thread() {
            public void run() {
                try {
                    disableRunningModeButtons(true);
                    model.setInput("next");
                    nextLine();
                    if (model.GDBAlive())
                        updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //next line format \t\t...\n(gdb)
            }
        }.start();
    }

    @FXML
    protected void StepIn(ActionEvent event) {
        new Thread() {
            public void run() {
                try {
                    disableRunningModeButtons(true);
                    model.setInput("step");
                    //OutputText.setText(OutputText.getText() + model.getOutPut());//redundant output
                    nextLine();
                    if (model.GDBAlive())
                        updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @FXML
    protected void StepOut(ActionEvent event) {
        new Thread() {
            public void run() {
                try {
                    disableRunningModeButtons(true);
                    model.setInput("finish");
                    nextLine();
                    //OutputText.setText(OutputText.getText() + model.getOutPut());//redundant output
                    if (model.GDBAlive())
                        updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @FXML
    protected void Continue(ActionEvent event) {
        new Thread() {
            public void run() {
                try {
                    disableRunningModeButtons(true);
                    model.setInput("continue");
                    nextLine();
                    //OutputText.setText(OutputText.getText() + model.getOutPut());//redundant output
                    if (model.GDBAlive())
                        updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void updateUI() throws Exception {
        if (isFinishedRunning()) {
            getFocusedSourceFile().getSelectionModel().select(-1);
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Update UI here
                        nextMarkedLine();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            disableRunningModeButtons(false);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    //Update UI here
                    RefreshInputOutPutPane();
                    updateStack();
                    updateVariables();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    protected void StopDebug(ActionEvent event) {
        new Thread() {
            public void run() {
                try {
                    disableRunningModeButtons(true);
                    model.setInput("detach");
                    //OutputText.setText(OutputText.getText()+model.getOutPut());//redundant output
                    //getFocusedSourceFile().getSelectionModel().select(-1);
                    nextLine();
                    if (model.GDBAlive())
                        updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @FXML
    protected void StopRun(ActionEvent event) {
        try {
            disableRunningModeButtons(true);
            model.setInput("kill");
            model.getOutPut();//redundant output
            getFocusedSourceFile().getSelectionModel().select(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextMarkedLine() throws Exception {
        model.setInput("info source");
        String out = model.getOutPut();
        String[] splittedOut = out.split("\n");
        if (splittedOut[2].startsWith("Located in")) {
            String sourceFilePath = splittedOut[2].substring(splittedOut[2].indexOf('/'));
            for (Tab sourceFileTab : sourceFiles.getTabs())
                if (sourceFileTab.getTooltip().getText().equals(sourceFilePath)) {
                    sourceFiles.getSelectionModel().select(sourceFileTab);
                    break;
                }
        }

        int lineNumber;
        model.setInput("frame");
        out = model.getOutPut();
        String lineNumberString = out.substring(out.indexOf(":") + 1, out.indexOf("\n"));
        try {
            lineNumber = Integer.parseInt(lineNumberString) - 1;
            getFocusedSourceFile().getSelectionModel().select(lineNumber);
            getFocusedSourceFile().scrollTo(lineNumber - 6);//window set resizable false
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private ListView getFocusedSourceFile() {
        Tab selectedSourceFileTab = sourceFiles.getSelectionModel().getSelectedItem();
        return ((ListView) selectedSourceFileTab.getContent());
    }

    private boolean isFinishedRunning() throws Exception {
        model.setInput("info program");
        if (model.getOutPut().contains("The program being debugged is not being run."))
            return true;
        return false;
    }

    private void disableRunningModeButtons(boolean disableMode) {
        StepBtn.setDisable(disableMode);
        StepInBtn.setDisable(disableMode);
        StepOutBtn.setDisable(disableMode);
        ContinueBtn.setDisable(disableMode);
        StopRunBtn.setDisable(disableMode);
        StopDebugBtn.setDisable(disableMode);
    }

    private void updateStack() throws Exception {
        Stack.getItems().clear();
        model.setInput("bt");
        String out = model.getOutPut();
        if (!out.equals("No stack.\n(gdb) ")) {
            try {
                out = out.substring(0, out.length() - 6);
                String[] allMostStackLines = out.split("\n");
                for (int i = 0; i < allMostStackLines.length; i++) {
                    if (allMostStackLines[i].startsWith(" ")) {
                        allMostStackLines[i - 1] += " " + allMostStackLines[i].trim();
                    }
                }
                List<String> StackLines = Arrays.stream(allMostStackLines).filter(sl -> !sl.startsWith(" ") && !sl.startsWith("\t")).collect(toList());
                List<String[]> frameLineSpaceSplitted = StackLines.stream().map(sl -> sl.split("\\s+")).collect(toList());
                List<FrameInfo> frameList = frameLineSpaceSplitted.stream().map(sl -> new FrameInfo(sl[0].substring(1), sl[sl.length - 1].split(":")[0], sl[sl.length - 4], sl[sl.length - 1].split(":")[1])).collect(toList());
                Stack.getItems().addAll(frameList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBreakpoints() throws Exception {
        Breakpoints.getItems().clear();
        model.setInput("info breakpoints");
        String out = model.getOutPut();
        if (!out.equals("No breakpoints or watchpoints.\n(gdb) ")) {
            try {
                out = out.substring(0, out.length() - 6);
                String[] BreakpointsLines = out.split("\n");
                Collection<String> BreakpointsList = new ArrayList(Arrays.asList(BreakpointsLines));
                ((ArrayList<String>) BreakpointsList).remove(0);//titles
                List<String> x = BreakpointsList.stream().filter(sl -> !sl.startsWith(" ") && !sl.startsWith("\t")).collect(toList());
                List<String[]> frameLineSpaceSplitted = x.stream().map(bp -> bp.split("\\s+")).collect(toList());
                List<FrameInfo> FrameList = new ArrayList<>();
                for(String[] sa: frameLineSpaceSplitted )
                {
                    String breakpointNum = sa[0];
                    String breakpointClass = sa[sa.length-1].split(":")[0];
                    String lineNum = sa[sa.length-1].split(":")[1];
                    String func = "";
                    for(int i=6 ; i<=sa.length-3 ; i++){
                        func+=sa[i];
                    }
                    FrameInfo fi = new FrameInfo(breakpointNum,breakpointClass,func,lineNum);
                    FrameList.add(fi);
                }
                //List<FrameInfo> FrameList = frameLineSpaceSplitted.stream().map(bp -> new FrameInfo(bp[0], bp[bp.length - 1].split(":")[0], bp[bp.length - 3], bp[bp.length - 1].split(":")[1])).collect(toList());
                Breakpoints.getItems().addAll(FrameList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateVariables() throws Exception {
        Variables.getItems().clear();
        model.setInput("info locals");
        String out = model.getOutPut();
        if (!out.equals("No locals.\n(gdb) ") && !out.equals("No frame selected.\n(gdb) ")) {
            try {
                out = out.substring(0, out.length() - 6);
                String[] variablesLines = out.split("\n");
                List<VariableInfo> variableList = Arrays.stream(variablesLines).map(vl -> new VariableInfo(vl.split("=", 2)[0], vl.split("=", 2)[1])).collect(toList());
                Variables.getItems().addAll(variableList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Refreshing to bottom
    private void RefreshInputOutPutPane() {
        /*OutputText.applyCss();
        InputText.applyCss();
        InputText.layout();*/


        IOPane.applyCss();
        IOPane.layout();
        IOPane.setVvalue(1);
    }
}
