package jp.clipline.clwebwrapperapplication;

import android.app.Application;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

// ((ClWebWrapperApplication)this.getApplication())
public class ClWebWrapperApplication extends Application {

    // ------------------------------------------------------------------------------------------ //
    private Map<String, Object> currentTodoContent = null;

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
    private Uri todoContentDataUri = null;
    private String todoContentDataType = null;

    public void setTodoContent(Uri data, String type) {
        todoContentDataUri = data;
        todoContentDataType = type;
    }

    public Uri getTodoContentData() {
        return todoContentDataUri;
    }

    public String getTodoContentType() {
        return todoContentDataType;
    }

}
