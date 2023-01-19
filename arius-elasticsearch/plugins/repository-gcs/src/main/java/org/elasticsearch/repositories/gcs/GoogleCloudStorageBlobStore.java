/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.repositories.gcs;

import com.google.api.gax.paging.Page;
import com.google.cloud.BatchResult;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageBatch;
import com.google.cloud.storage.StorageException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.blobstore.BlobContainer;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.BlobStore;
import org.elasticsearch.common.blobstore.BlobStoreException;
import org.elasticsearch.common.blobstore.DeleteResult;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.core.internal.io.Streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_GONE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;

class GoogleCloudStorageBlobStore implements BlobStore {

    private static final Logger logger = LogManager.getLogger(GoogleCloudStorageBlobStore.class);

    // The recommended maximum size of a blob that should be uploaded in a single
    // request. Larger files should be uploaded over multiple requests (this is
    // called "resumable upload")
    // https://cloud.google.com/storage/docs/json_api/v1/how-tos/resumable-upload
    public static final int LARGE_BLOB_THRESHOLD_BYTE_SIZE = 5 * 1024 * 1024;

    private final String bucketName;
    private final String clientName;
    private final GoogleCloudStorageService storageService;

    GoogleCloudStorageBlobStore(String bucketName, String clientName, GoogleCloudStorageService storageService) {
        this.bucketName = bucketName;
        this.clientName = clientName;
        this.storageService = storageService;
        if (doesBucketExist(bucketName) == false) {
            throw new BlobStoreException("Bucket [" + bucketName + "] does not exist");
        }
    }

    private Storage client() throws IOException {
        return storageService.client(clientName);
    }

    @Override
    public BlobContainer blobContainer(BlobPath path) {
        return new GoogleCloudStorageBlobContainer(path, this);
    }

    @Override
    public void close() {
    }

    /**
     * Return true iff the given bucket exists
     *
     * @param bucketName name of the bucket
     * @return true iff the bucket exists
     */
    private boolean doesBucketExist(String bucketName) {
        try {
            final Bucket bucket = SocketAccess.doPrivilegedIOException(() -> client().get(bucketName));
            return bucket != null;
        } catch (final Exception e) {
            throw new BlobStoreException("Unable to check if bucket [" + bucketName + "] exists", e);
        }
    }

    /**
     * List blobs in the specific bucket under the specified path. The path root is removed.
     *
     * @param path base path of the blobs to list
     * @return a map of blob names and their metadata
     */
    Map<String, BlobMetaData> listBlobs(String path) throws IOException {
        return listBlobsByPrefix(path, "");
    }

    /**
     * List all blobs in the specific bucket with names prefixed
     *
     * @param path
     *            base path of the blobs to list. This path is removed from the
     *            names of the blobs returned.
     * @param prefix prefix of the blobs to list.
     * @return a map of blob names and their metadata.
     */
    Map<String, BlobMetaData> listBlobsByPrefix(String path, String prefix) throws IOException {
        final String pathPrefix = buildKey(path, prefix);
        final MapBuilder<String, BlobMetaData> mapBuilder = MapBuilder.newMapBuilder();
        SocketAccess.doPrivilegedVoidIOException(
            () -> client().get(bucketName).list(BlobListOption.currentDirectory(), BlobListOption.prefix(pathPrefix)).iterateAll().forEach(
                blob -> {
                    assert blob.getName().startsWith(path);
                    if (blob.isDirectory() == false) {
                        final String suffixName = blob.getName().substring(path.length());
                        mapBuilder.put(suffixName, new PlainBlobMetaData(suffixName, blob.getSize()));
                    }
                }));
        return mapBuilder.immutableMap();
    }

    Map<String, BlobContainer> listChildren(BlobPath path) throws IOException {
        final String pathStr = path.buildAsString();
        final MapBuilder<String, BlobContainer> mapBuilder = MapBuilder.newMapBuilder();
        SocketAccess.doPrivilegedVoidIOException
            (() -> client().get(bucketName).list(BlobListOption.currentDirectory(), BlobListOption.prefix(pathStr)).iterateAll().forEach(
                blob -> {
                    if (blob.isDirectory()) {
                        assert blob.getName().startsWith(pathStr);
                        assert blob.getName().endsWith("/");
                        // Strip path prefix and trailing slash
                        final String suffixName = blob.getName().substring(pathStr.length(), blob.getName().length() - 1);
                        if (suffixName.isEmpty() == false) {
                            mapBuilder.put(suffixName, new GoogleCloudStorageBlobContainer(path.add(suffixName), this));
                        }
                    }
                }));
        return mapBuilder.immutableMap();
    }

    /**
     * Returns an {@link java.io.InputStream} for the given blob name
     *
     * @param blobName name of the blob
     * @return the InputStream used to read the blob's content
     */
    InputStream readBlob(String blobName) throws IOException {
        final BlobId blobId = BlobId.of(bucketName, blobName);
        final ReadChannel readChannel = SocketAccess.doPrivilegedIOException(() -> client().reader(blobId));
        return Channels.newInputStream(new ReadableByteChannel() {
            @SuppressForbidden(reason = "Channel is based of a socket not a file")
            @Override
            public int read(ByteBuffer dst) throws IOException {
                try {
                    return SocketAccess.doPrivilegedIOException(() -> readChannel.read(dst));
                } catch (StorageException e) {
                    if (e.getCode() == HTTP_NOT_FOUND) {
                        throw new NoSuchFileException("Blob [" + blobName + "] does not exist");
                    }
                    throw e;
                }
            }

            @Override
            public boolean isOpen() {
                return readChannel.isOpen();
            }

            @Override
            public void close() throws IOException {
                SocketAccess.doPrivilegedVoidIOException(readChannel::close);
            }
        });
    }

    /**
     * Writes a blob in the specific bucket
     *  @param inputStream content of the blob to be written
     * @param blobSize    expected size of the blob to be written
     * @param failIfAlreadyExists whether to throw a FileAlreadyExistsException if the given blob already exists
     */
    void writeBlob(String blobName, InputStream inputStream, long blobSize, boolean failIfAlreadyExists) throws IOException {
        final BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, blobName).build();
        if (blobSize > getLargeBlobThresholdInBytes()) {
            writeBlobResumable(blobInfo, inputStream, failIfAlreadyExists);
        } else {
            writeBlobMultipart(blobInfo, inputStream, blobSize, failIfAlreadyExists);
        }
    }

    // non-static, package private for testing
    long getLargeBlobThresholdInBytes() {
        return LARGE_BLOB_THRESHOLD_BYTE_SIZE;
    }

    /**
     * Uploads a blob using the "resumable upload" method (multiple requests, which
     * can be independently retried in case of failure, see
     * https://cloud.google.com/storage/docs/json_api/v1/how-tos/resumable-upload
     * @param blobInfo the info for the blob to be uploaded
     * @param inputStream the stream containing the blob data
     * @param failIfAlreadyExists whether to throw a FileAlreadyExistsException if the given blob already exists
     */
    private void writeBlobResumable(BlobInfo blobInfo, InputStream inputStream, boolean failIfAlreadyExists) throws IOException {
        // We retry 410 GONE errors to cover the unlikely but possible scenario where a resumable upload session becomes broken and
        // needs to be restarted from scratch. Given how unlikely a 410 error should be according to SLAs we retry only twice.
        assert inputStream.markSupported();
        inputStream.mark(Integer.MAX_VALUE);
        StorageException storageException = null;
        final Storage.BlobWriteOption[] writeOptions = failIfAlreadyExists ?
            new Storage.BlobWriteOption[]{Storage.BlobWriteOption.doesNotExist()} : new Storage.BlobWriteOption[0];
        for (int retry = 0; retry < 3; ++retry) {
            try {
                final WriteChannel writeChannel = SocketAccess
                    .doPrivilegedIOException(() -> client().writer(blobInfo, writeOptions));
                Streams.copy(inputStream, Channels.newOutputStream(new WritableByteChannel() {
                    @Override
                    public boolean isOpen() {
                        return writeChannel.isOpen();
                    }

                    @Override
                    public void close() throws IOException {
                        SocketAccess.doPrivilegedVoidIOException(writeChannel::close);
                    }

                    @SuppressForbidden(reason = "Channel is based of a socket not a file")
                    @Override
                    public int write(ByteBuffer src) throws IOException {
                        return SocketAccess.doPrivilegedIOException(() -> writeChannel.write(src));
                    }
                }));
                return;
            } catch (final StorageException se) {
                final int errorCode = se.getCode();
                if (errorCode == HTTP_GONE) {
                    logger.warn(() -> new ParameterizedMessage("Retrying broken resumable upload session for blob {}", blobInfo), se);
                    storageException = ExceptionsHelper.useOrSuppress(storageException, se);
                    inputStream.reset();
                    continue;
                } else if (failIfAlreadyExists && errorCode == HTTP_PRECON_FAILED) {
                    throw new FileAlreadyExistsException(blobInfo.getBlobId().getName(), null, se.getMessage());
                }
                if (storageException != null) {
                    se.addSuppressed(storageException);
                }
                throw se;
            }
        }
        assert storageException != null;
        throw storageException;
    }

    /**
     * Uploads a blob using the "multipart upload" method (a single
     * 'multipart/related' request containing both data and metadata. The request is
     * gziped), see:
     * https://cloud.google.com/storage/docs/json_api/v1/how-tos/multipart-upload
     *  @param blobInfo the info for the blob to be uploaded
     * @param inputStream the stream containing the blob data
     * @param blobSize the size
     * @param failIfAlreadyExists whether to throw a FileAlreadyExistsException if the given blob already exists
     */
    private void writeBlobMultipart(BlobInfo blobInfo, InputStream inputStream, long blobSize, boolean failIfAlreadyExists)
        throws IOException {
        assert blobSize <= getLargeBlobThresholdInBytes() : "large blob uploads should use the resumable upload method";
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.toIntExact(blobSize));
        Streams.copy(inputStream, baos);
        try {
            final Storage.BlobTargetOption[] targetOptions = failIfAlreadyExists ?
                new Storage.BlobTargetOption[] { Storage.BlobTargetOption.doesNotExist() } :
                new Storage.BlobTargetOption[0];
            SocketAccess.doPrivilegedVoidIOException(
                    () -> client().create(blobInfo, baos.toByteArray(), targetOptions));
        } catch (final StorageException se) {
            if (failIfAlreadyExists && se.getCode() == HTTP_PRECON_FAILED) {
                throw new FileAlreadyExistsException(blobInfo.getBlobId().getName(), null, se.getMessage());
            }
            throw se;
        }
    }

    /**
     * Deletes the given path and all its children.
     *
     * @param pathStr Name of path to delete
     */
    DeleteResult deleteDirectory(String pathStr) throws IOException {
        return SocketAccess.doPrivilegedIOException(() -> {
            DeleteResult deleteResult = DeleteResult.ZERO;
            Page<Blob> page = client().get(bucketName).list(BlobListOption.prefix(pathStr));
            do {
                final Collection<String> blobsToDelete = new ArrayList<>();
                final AtomicLong blobsDeleted = new AtomicLong(0L);
                final AtomicLong bytesDeleted = new AtomicLong(0L);
                page.getValues().forEach(b -> {
                    blobsToDelete.add(b.getName());
                    blobsDeleted.incrementAndGet();
                    bytesDeleted.addAndGet(b.getSize());
                });
                deleteBlobsIgnoringIfNotExists(blobsToDelete);
                deleteResult = deleteResult.add(blobsDeleted.get(), bytesDeleted.get());
                page = page.getNextPage();
            } while (page != null);
            return deleteResult;
        });
    }

    /**
     * Deletes multiple blobs from the specific bucket using a batch request
     *
     * @param blobNames names of the blobs to delete
     */
    void deleteBlobsIgnoringIfNotExists(Collection<String> blobNames) throws IOException {
        if (blobNames.isEmpty()) {
            return;
        }
        final List<BlobId> blobIdsToDelete = blobNames.stream().map(blob -> BlobId.of(bucketName, blob)).collect(Collectors.toList());
        final List<BlobId> failedBlobs = Collections.synchronizedList(new ArrayList<>());
        try {
            SocketAccess.doPrivilegedVoidIOException(() -> {
                final AtomicReference<StorageException> ioe = new AtomicReference<>();
                final StorageBatch batch = client().batch();
                for (BlobId blob : blobIdsToDelete) {
                    batch.delete(blob).notify(
                        new BatchResult.Callback<Boolean, StorageException>() {
                            @Override
                            public void success(Boolean result) {
                            }

                            @Override
                            public void error(StorageException exception) {
                                if (exception.getCode() != HTTP_NOT_FOUND) {
                                    failedBlobs.add(blob);
                                    if (ioe.compareAndSet(null, exception) == false) {
                                        ioe.get().addSuppressed(exception);
                                    }
                                }
                            }
                        });
                }
                batch.submit();

                final StorageException exception = ioe.get();
                if (exception != null) {
                    throw exception;
                }
            });
        } catch (final Exception e) {
            throw new IOException("Exception when deleting blobs [" + failedBlobs + "]", e);
        }
        assert failedBlobs.isEmpty();
    }

    private static String buildKey(String keyPath, String s) {
        assert s != null;
        return keyPath + s;
    }
}
