package example.fileserver.meta;

import example.fileserver.repository.FileResource;

/**
 * A mime type detector
 */
public interface MimeDetector
{
    /**
     * Gets the mime type for an image
     * 
     * @param fileResource file to detect mime type from
     * @return String representation of the mime type
     */
    public String getMimeType(FileResource fileResource);
}
