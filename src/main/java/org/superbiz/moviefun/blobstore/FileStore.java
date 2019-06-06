package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;


public class FileStore implements BlobStore {
    private final Tika tika = new Tika();



    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = new File(blob.name);

        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        byte[] buffer = new byte[blob.inputStream.available()];
        blob.inputStream.read(buffer);


        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(buffer);
        }

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File file = new File(name);

        if (!file.exists()) {
            return Optional.empty();
        }

        return Optional.of(new Blob(name,new FileInputStream(file),tika.detect(file)));
    }

    @Override
    public void deleteAll() {

    }
}
