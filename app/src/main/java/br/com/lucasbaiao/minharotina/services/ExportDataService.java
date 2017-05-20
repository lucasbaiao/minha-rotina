package br.com.lucasbaiao.minharotina.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import br.com.lucasbaiao.minharotina.persistence.AppDatabaseHelper;
import br.com.lucasbaiao.minharotina.persistence.CSVFileWriter;
import br.com.lucasbaiao.minharotina.persistence.model.Category;
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
        boolean dirCreated = path.mkdirs();
        return path;
    }

    private void writeToFile(List<? extends CSVFileWriter> list, String fileName, Class<?> clazz) {
        if (list.size() > 0) {
            return;
        }

        File file = new File(this.getFilesDirectory(), fileName + ".csv");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(list.get(0).getFileHeader().getBytes());
            for (CSVFileWriter writer : list) {
                writer.writeLine(stream);
            }
            Log.e(getClass().getSimpleName(), "File write success!!");
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
