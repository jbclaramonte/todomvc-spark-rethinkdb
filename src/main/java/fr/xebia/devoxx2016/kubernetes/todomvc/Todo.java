package fr.xebia.devoxx2016.kubernetes.todomvc;

public class Todo {

    public String id;

    public String title;

    public boolean completed;

    public Todo() {
    }

    public Todo(String id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }
}
