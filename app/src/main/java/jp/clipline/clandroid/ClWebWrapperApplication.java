package jp.clipline.clandroid;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

// ((ClWebWrapperApplication)this.getApplication())
public class ClWebWrapperApplication extends Application {

    // ------------------------------------------------------------------------------------------ //
    private Map<String, Object> currentTodoContent = null;

    private boolean mIsBack = false;

    public void setCurrentTodoContent(Map<String, Object> _currentTodoContent) {
        this.currentTodoContent = _currentTodoContent;
    }

    public Map<String, Object> getCurrentTodoContent() {
        return this.currentTodoContent;
    }

    // ------------------------------------------------------------------------------------------ //
    private Map<String, String> todoParameters = new HashMap<String, String>();

    public void setTodoParameters(String studentId, String categoryId, String todoContentId) {
        todoParameters.clear();
        todoParameters.put("studentId", studentId);
        todoParameters.put("categoryId", categoryId);
        todoParameters.put("todoContentId", todoContentId);
    }

    public Map<String, String> getTodoParameters() {
        return todoParameters;
    }

    // ------------------------------------------------------------------------------------------ //
    private String todoContentDataUri = null;
    private String todoContentDataType = null;

    public void setTodoContent(String data, String type) {
        todoContentDataUri = data;
        todoContentDataType = type;
    }

    public String getTodoContentData() {
        return todoContentDataUri;
    }

    public String getTodoContentType() {
        return todoContentDataType;
    }

    ///// 20170523 ADD START
    public boolean isBack() {
        return mIsBack;
    }

    public void setBack(boolean isBack) {
        mIsBack = isBack;
    }
    ///// 20170523 ADD END
}
