package com.zlab.audiobooks;

public class ListElements {
    public int icon;
    public String title;
    public String author;
    public String discription;
    public String size;
    public ListElements(){
        super();
    }

    public ListElements(int icon, String title, String author, String discription, String size) {
        super();
        this.icon = icon;
        this.title = title;
        this.author = author;
        this.discription = discription;
        this.size = size;
    }
}