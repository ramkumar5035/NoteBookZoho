package com.android.notebook.model;

import java.io.Serializable;

public class Note implements Serializable {

    private String title;
    private String content;
    private long timeOfAddition;
    private String oldTitle = "";
    private String oldContent = "";

    public Note(String title, String content, long timeOfAddition) {
        this.title = title;
        this.content = content;
        this.timeOfAddition = timeOfAddition;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public long getTimeOfAddition() {
        return timeOfAddition;
    }

    public String getOldTitle() {
        return oldTitle;
    }

    public void setOldTitle(String oldTitle) {
        this.oldTitle = oldTitle;
    }

    public String getOldContent() {
        return oldContent;
    }

    public void setOldContent(String oldContent) {
        this.oldContent = oldContent;
    }
}
