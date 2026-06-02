package cn.ggsn.openrxlight.fsx;

import java.io.File;
import java.io.InputStream;

public interface XFileSystem {

    /**
     * Write the content to the file at the given path. If the file already exists,
     * it will be overwritten in default.
     */
    void write(String filePath, byte[] content);

    /**
     * Delete the file at the given path.
     * 
     * @param filePath
     */
    void delete(String filePath);

    /**
     * Cat the file content as InputStream. Caller is responsible for closing the
     * stream.
     * 
     * @param filePath
     * @return InputStream of the file content
     */
    InputStream cat(String filePath);

    /**
     * Get the file at the given path.
     * 
     * @param filePath
     * @return File object representing the file
     */
    File getFile(String filePath);
}
