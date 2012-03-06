package example.fileserver.resources;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Inject;

import example.fileserver.util.Downloader;
import example.fileserver.util.ExpiresDateUtil;
import example.fileserver.mime.MimeDetector;
import example.fileserver.repository.Repository;
import example.fileserver.repository.RepositoryStorageException;

@Path("/")
public class FileWS {

    private static final Logger LOG = Logger.getLogger(FileWS.class.getName());

    public static final String BINARY_RESOURCE = "file";
    public static final String HTTP_RESOURCE = "httpFile";

    @Context
    private UriInfo uriInfo;

    private Repository repository;
    private MimeDetector mimeDetector;
    private Downloader downloader;

    @Inject
    public FileWS(Repository repository, MimeDetector mimeDetector, Downloader downloader) {
        this.repository = repository;
        this.mimeDetector = mimeDetector;
        this.downloader = downloader;
    }

    @Path(FileWS.HTTP_RESOURCE + "/{id}{filename:(/filename/[^/]+?)?}")
    @POST
    public Response postFromRemoteUri(@Context HttpHeaders headers, @PathParam("id") final String filename,
                                      @PathParam("filename") final String unused,
                                      @QueryParam("url") final URI fileUrl) {
        LOG.log(Level.INFO, "POSTing http file " + fileUrl);
        byte[] data;
        try {
            data = downloader.download(fileUrl);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not download from url: " + fileUrl, e);
            return Response.serverError().build();
        }
        return postFileResource(filename, unused, data);
    }

    @Path(FileWS.BINARY_RESOURCE + "/{id}{filename:(/filename/[^/]+?)?}")
    @POST
    public Response postFromBinary(@Context HttpHeaders headers, @PathParam("id") final String filename,
                                   @PathParam("filename") final String unused, byte[] data) {
        LOG.log(Level.INFO, "POSTing file " + filename);
        return postFileResource(filename, unused, data);
    }



    @Path(FileWS.BINARY_RESOURCE + "/{id}{filename:(/filename/[^/]+?)?}")
    @GET
    public Response getFileResource(@PathParam("id") String id, @PathParam("filename") String filename) {
        LOG.log(Level.INFO, "GETting file " + id);

        example.fileserver.repository.FileResource file;
        try {
            file = repository.getFileResource(id);
        } catch (RepositoryStorageException e) {
            LOG.log(Level.INFO, "Failed request for " + uriInfo.getAbsolutePath(), e);
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(file.getData(), mimeDetector.getMimeType(file))
                .header("Expires", ExpiresDateUtil.getInfinateExpiresDate()).build();
    }


    private Response postFileResource(String filename, String unused, byte[] data) {
        String id;
        try {
            id = repository.addFileResource(new example.fileserver.repository.FileResource(filename, data));
        } catch (RepositoryStorageException e) {
            LOG.log(Level.WARNING, "Could not store file " + filename, e);
            return Response.serverError().build();
        }

        String uriString = uriInfo.getBaseUri().toString() + BINARY_RESOURCE + "/" + id + "/filename/" + filename;
        String metadataUriString = uriInfo.getBaseUri().toString() + MetadataWS.RESOURCE + "/" + id + "/filename/" + filename + ".metadata";

        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            LOG.log(Level.WARNING, "Could not create uri for file " + filename, e);
            return Response.serverError().build();
        }
        LOG.log(Level.INFO, "Successfully posted " + filename + " as " + id + ", unused is " + unused);
        return Response.created(uri).header("Metadata", metadataUriString).build();
    }

}
