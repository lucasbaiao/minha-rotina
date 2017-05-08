package br.com.lucasbaiao.minharotina.persistence.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public class Category implements Parcelable {

    public static final String TAG = "Category";

    private String id;
    private String name;
    private Theme theme;
    private SparseArray<Event> events;

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public Category(String name) {
        this(name, Theme.blue);
    }

    public Category(String name, Theme theme) {
        this(name, name, theme);
    }

    public Category(String id, String name, Theme theme) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.events = new SparseArray<>();
    }

    protected Category(Parcel in) {
        name = in.readString();
        id = in.readString();
        theme = Theme.values()[in.readInt()];
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeInt(theme.ordinal());
    }

    public Theme getTheme() {
        return theme;
    }

    public void addEvent(Event event) {
        this.events.put(event.getId(), event);
    }

    public void addEvent(SparseArray<Event> events) {
        int size = events.size();
        for (int index = 0; index < size; index++) {
            this.addEvent(events.valueAt(index));
        }
    }

    public SparseArray<Event> getEvents() {
        return events;
    }
}
