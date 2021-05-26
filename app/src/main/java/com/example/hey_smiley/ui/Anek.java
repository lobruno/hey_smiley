package com.example.hey_smiley.ui;

import com.google.gson.annotations.SerializedName;

public class Anek {
    @SerializedName("content")
    private String content;
    public Anek(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }
}
