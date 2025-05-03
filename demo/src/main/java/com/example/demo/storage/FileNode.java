package com.example.demo.storage;

import java.util.ArrayList;
import java.util.List;

public class FileNode {
    private String name;
    private boolean isFolder;
    private String path;
    private List<FileNode> children;

    // Constructors, getters, and setters
    public FileNode(String name, boolean isFolder, String path) {
        this.name = name;
        this.isFolder = isFolder;
        this.path = path;
        this.children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public boolean getisFolder() {
        return isFolder;
    }

    public String getPath() {
        return path;
    }

    public List<FileNode> getChildren() {
        return children;
    }

    public void addChild(FileNode child) {
        this.children.add(child);
    }
}
