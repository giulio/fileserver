package example.fileserver.util;

import com.google.inject.Singleton;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * @author gmola
 *         date: 3/5/12
 */
@Singleton
public class HttpClientDownloader implements Downloader {

    public HttpClient client;
    private static final Logger LOG = Logger.getLogger(HttpClientDownloader.class.toString());

    public HttpClientDownloader() {
        this.client = new HttpClient();
    }

    @Override
    public byte[] download(URI uri) throws IOException {
        HttpMethod method = new GetMethod(uri.toString());
        client.executeMethod(method);
        LOG.fine("HTTP-" + method.getStatusCode() + " : " + uri);
        if (method.getStatusCode() >= 400) {
            String message = "error while fetching remote resource: " + uri + " - HTTP-" + method.getStatusCode();
            LOG.warning(message);
            throw new RemoteResourceException(message);
        }
        return method.getResponseBody();
    }


    public class RemoteResourceException extends WebApplicationException {
        public RemoteResourceException(String message) {
            super(Response.status(500)
                    .entity(message).type(MediaType.TEXT_PLAIN).build());
        }
    }

}
