package br.com.lucasbaiao.minharotina.persistence.model;

public class Event {

    public static final String TAG = "Event";

    private int id;
    private String start;
    private String stop;

    public Event(String start, String stop) {
        this(-1, start, stop);
    }

    public Event(int id, String start, String stop) {
        this.id = id;
        this.start = start;
        this.stop = stop;
    }

    public int getId() {
        return id;
    }

    public String getStart() {
        return start;
    }

    public String getStop() {
        return stop;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }
}
