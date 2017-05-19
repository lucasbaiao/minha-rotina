package br.com.lucasbaiao.minharotina.persistence;

import java.io.FileOutputStream;
import java.io.IOException;

public interface CSVFileWriter {

    void writeLine(FileOutputStream stream) throws IOException;
}
