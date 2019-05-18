package sample;

public class FrameInfo {

    private  String frameNumber;
    private  String funcName;
    private  String className;
    private  String lineNumber;

    public FrameInfo(String frameNumber ,String funcName, String className, String lineNumber) {
        this.frameNumber = frameNumber;
        this.funcName = funcName;
        this.className = className;
        this.lineNumber = lineNumber;
    }

    public String getFrameNumber() {
        return frameNumber;
    }

    public String getFuncName() {
        return funcName;
    }

    public String getClassName() {
        return className;
    }

    public String getLineNumber() {
        return lineNumber;
    }
}
