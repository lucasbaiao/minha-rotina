package br.com.lucasbaiao.minharotina.persistence.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.lucasbaiao.minharotina.persistence.AppDatabaseHelper;
import br.com.lucasbaiao.minharotina.persistence.CSVFileWriter;
import br.com.lucasbaiao.minharotina.persistence.ForegroundAppTable;

public class ForegroundApp implements CSVFileWriter {

    public static final String TAG = "ForegroundApp";

    private int id;
    private String timestamp;
    private String appName;

    public ForegroundApp(String timestamp, String appName) {
        this(-1, timestamp, appName);
    }

    public ForegroundApp(int id, String timestamp, String appName) {
        this.id = id;
        this.timestamp = timestamp;
        this.appName = appName;
    }

    public ForegroundApp(long timeInMillis, String appName) {
        this(getDate(timeInMillis), appName);
    }

    private static String getDate(long timeInMillis) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(ForegroundAppTable.TimeStampFormat, Locale.getDefault());
        try {
            return dateFormat.format(new Date(timeInMillis));
        } catch (Exception ex) {
            return dateFormat.format(new Date());
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAppName() {
        return appName;
    }

    public int getId() {
        return id;
    }

    @Override
    public void writeLine(FileOutputStream stream) throws IOException {
        String text = String.format("\n%s;%s;%s",
                getId(),
                getTimestamp(),
                getAppName()
        );
        stream.write(text.getBytes());
    }
}
