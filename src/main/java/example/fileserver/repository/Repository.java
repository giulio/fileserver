package example.fileserver.repository;

import example.fileserver.mime.Metadata;

public interface Repository
{
    /**
     * @throws RepositoryStorageException if the underlying resource did not permit writing
     * new data.
     *
     * @returns an ID to the created file resource in the repository
     */
    public String addFileResource(FileResource file) throws RepositoryStorageException;

    /**
     * @throws RepositoryStorageException if the underlying resource for some reason could
     * not be found.
     */
    public FileResource getFileResource(String id) throws RepositoryStorageException;

    /**
     * @throws RepositoryStorageException if the underlying resource for some reason could
     * not be found.
     */
    public Metadata getMetadata(String id) throws RepositoryStorageException;

}
