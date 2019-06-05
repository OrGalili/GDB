package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class VariableInfo {

    private StringProperty variableName;
    private StringProperty variableValue;
    private boolean isNameInitialized;
    private List<VariableInfo> childs;

    public VariableInfo() {
        childs = new ArrayList<VariableInfo>();
    }

    public VariableInfo(String variableName) {
        this.variableName = new SimpleStringProperty(variableName);
        childs = new ArrayList<VariableInfo>();
    }

    public VariableInfo(String variableValue, String variableName) {
        this.variableName = new SimpleStringProperty(variableName);
        this.variableValue = new SimpleStringProperty(variableValue);
        this.isNameInitialized = false;
        childs = new ArrayList<VariableInfo>();
    }

    public StringProperty getVairableName() {
        return variableName;
    }

    public StringProperty getVairableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = new SimpleStringProperty(variableValue);
    }

    public void setVariableName(String variableName) {
        this.variableName = new SimpleStringProperty(variableName);
         this.isNameInitialized = true;
    }

    public boolean getIsNameInitialized(){
        return this.isNameInitialized;
    }

    public List<VariableInfo> getChilds(){
        return childs;
    }
}
