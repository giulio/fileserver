package example.fileserver.util;

import java.io.IOException;
import java.net.URI;

public interface Downloader {


    /**
     * Download data bytes from the given URL.
     *
     * @param uri
     * @return
     */
    byte[] download(URI uri) throws IOException;

}
