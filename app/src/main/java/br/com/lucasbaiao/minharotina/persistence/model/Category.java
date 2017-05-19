package br.com.lucasbaiao.minharotina.persistence.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.lucasbaiao.minharotina.persistence.CSVFileWriter;

public class Category implements Parcelable, CSVFileWriter {

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

    @Override
    public void writeLine(FileOutputStream stream) throws IOException {
        String categoryName = this.getName();
        SparseArray<Event> events = this.getEvents();
        int counter = 0;
        int size = events.size();
        for (int i = 0; i < size; i++ ) {
            counter += 1;
            Event event = events.valueAt(i);
            String text = String.format("\n%s;%s;%s;%s",
                    categoryName,
                    counter,
                    getFormattedDate(event.getStart()),
                    getFormattedDate(event.getStop())
            );
            stream.write(text.getBytes());
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getFormattedDate(String timeMillis) {
        if (timeMillis != null && !timeMillis.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return sdf.format(new Date(Long.parseLong(timeMillis)));
        }
        return timeMillis;
    }
}
