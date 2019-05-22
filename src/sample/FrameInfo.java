package sample;

public class FrameInfo {

    private  String frameNumber;
    private  String funcName;
    private  String className;
    private  String lineNumber;

    public FrameInfo(String frameNumber ,String className, String funcName, String lineNumber) {
        this.frameNumber = frameNumber;
        this.funcName = funcName;
        this.className = className;
        this.lineNumber = lineNumber;
    }

    public FrameInfo(String className, String funcName, String lineNumber) {
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
