package com.wx.jsync.util.extensions.google;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wx.jsync.util.Common.first;

/**
 * Simple wrapper around {@link Drive} service that facilitates common operations
 *
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.05.17.
 */
public class DriveServiceHelper {

    private static final String FILE_FIELDS = "id,md5Checksum,modifiedTime,name,parents,size";
    private static final String LIST_FIELDS = "files(" + FILE_FIELDS + ")";

    private final Drive service;

    public DriveServiceHelper(Drive service) {
        this.service = service;
    }


    public List<File> listFiles(String rootId) throws IOException {
        return service.files().list()
                .setQ("'" + rootId + "' in parents and trashed = false")
                .setFields(LIST_FIELDS)
                .execute()
                .getFiles();
    }

    public Optional<File> getFile(String parentId, String filename) throws IOException {
        List<File> result = service.files().list()
                .setQ("name = '" + filename + "' and '" + parentId + "' in parents and trashed = false")
                .setFields(LIST_FIELDS)
                .execute()
                .getFiles().stream()
                .filter(file -> first(file.getParents())
                        .map(p -> parentId.equals("root") || p.equals(parentId))
                        .orElse(false)
                ).collect(Collectors.toList());

        if (result.isEmpty()) {
            return Optional.empty();
        } else if (result.size() == 1) {
            return Optional.of(result.get(0));
        } else {
            throw new IOException("Multiple results found for " + filename);
        }
    }

    public File createDirectory(String parentId, String name) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(parentId));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        return service.files().create(fileMetadata)
                .setFields("id")
                .execute();
    }

    public InputStream downloadFile(String id) throws IOException {
        return service.files().get(id).executeMediaAsInputStream();
    }

    public File createFile(String parentId, String filename, InputStream in) throws IOException {
        return createFile(parentId, filename, new InputStreamContent(null, in));
    }

    private File createFile(String parentId, String filename, AbstractInputStreamContent mediaContent) throws IOException {
        File body = new File();
        body.setName(filename);

        if (parentId != null && parentId.length() > 0) {
            body.setParents(Collections.singletonList(parentId));
        }

        return service.files().create(body, mediaContent)
                .setFields(FILE_FIELDS)
                .execute();
    }

    public File updateFile(String id, InputStream in) throws IOException {
        return updateFile(id, new InputStreamContent(null, in));
    }

    private File updateFile(String id, AbstractInputStreamContent mediaContent) throws IOException {
        return service.files().update(id, new File(), mediaContent)
                .setFields(FILE_FIELDS)
                .execute();
    }


    public void removeFile(String id) throws IOException {
        service.files().delete(id).execute();
    }

    public File moveFile(String id, String parentId, String name) throws IOException {
        File file = service.files().get(id)
                .setFields(FILE_FIELDS)
                .execute();

        String previousParents = String.join(",", file.getParents());
        String newParents = parentId;

        return service.files().update(id, new File().setName(name))
                .setRemoveParents(previousParents)
                .setAddParents(newParents)
                .setFields(FILE_FIELDS)
                .execute();
    }
}
