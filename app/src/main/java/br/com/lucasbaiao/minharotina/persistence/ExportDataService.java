package br.com.lucasbaiao.minharotina.persistence;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.com.lucasbaiao.minharotina.persistence.model.Category;
import br.com.lucasbaiao.minharotina.persistence.model.Event;

public class ExportDataService extends IntentService {

    private static final String TAG = "ExportDataService";

    public ExportDataService() {
        super("ExportDataService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        this.writeToFile(AppDatabaseHelper.getCategories(getApplicationContext(), true));
    }

    private File getFilesDirectory() {
        File path = new File(Environment.getExternalStorageDirectory(), "minha_rotina");
        path.mkdirs();
        return path;
    }

    private void writeToFile(List<Category> list) {
        File file = new File(this.getFilesDirectory(), "exporter.csv");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write("categoria;evento;inicio;fim".getBytes());
            for (Category category : list) {
                String categoryName = category.getName();
                SparseArray<Event> events = category.getEvents();
                int counter = 0;
                int size = events.size();
                for (int i = 0; i < size; i++ ) {
                    counter += 1;
                    Event event = events.valueAt(i);
                    String text = String.format("\n%s;%s;%s;%s", categoryName, counter, getFormattedDate(event.getStart()), getFormattedDate(event.getStop()));
                    stream.write(text.getBytes());
                }
            }
            Log.e(getClass().getSimpleName(), "File write success!!");
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        } finally {
            if (stream !=  null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
