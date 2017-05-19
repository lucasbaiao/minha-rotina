package br.com.lucasbaiao.minharotina.services;

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
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.com.lucasbaiao.minharotina.persistence.AppDatabaseHelper;
import br.com.lucasbaiao.minharotina.persistence.CSVFileWriter;
import br.com.lucasbaiao.minharotina.persistence.model.Category;
import br.com.lucasbaiao.minharotina.persistence.model.Event;
import br.com.lucasbaiao.minharotina.persistence.model.ForegroundApp;

public class ExportDataService extends IntentService {

    private static final String TAG = "ExportDataService";

    public ExportDataService() {
        super("ExportDataService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        this.writeToFile(AppDatabaseHelper.getCategories(getApplicationContext(), true),
                "categories",
                Category.class);
        this.writeToFile(AppDatabaseHelper.loadForegroundApps(getApplicationContext()),
                "foreground_apps",
                ForegroundApp.class);
    }

    private File getFilesDirectory() {
        File path = new File(Environment.getExternalStorageDirectory(), "minha_rotina");
        path.mkdirs();
        return path;
    }

    private String getFileHeader(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        for (Field f : clazz.getDeclaredFields()) {
            sb.append(f.getName());
            sb.append(";");
        }
        return sb.toString();
    }

    private void writeToFile(List<? extends CSVFileWriter> list, String fileName, Class<?> clazz) {
        File file = new File(this.getFilesDirectory(), fileName + ".csv");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(getFileHeader(clazz).getBytes());
            for (CSVFileWriter writer : list) {
                writer.writeLine(stream);
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
}
