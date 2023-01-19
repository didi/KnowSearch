package org.elasticsearch.index.translog;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.Channels;

/**
 * author weizijun
 * date：2019-08-06
 */
public class TranslogOperationReader extends BaseTranslogReader implements Closeable {
    private static final Logger logger = LogManager.getLogger(TranslogOperationReader.class);

    private final ByteBuffer reusableBuffer;
    private BufferedChecksumStreamInput reuse;
    private long globalCheckpoint = Long.MAX_VALUE;

    private Checkpoint checkpoint;
    protected final AtomicBoolean closed = new AtomicBoolean(false);

    TranslogOperationReader(long generation, FileChannel channel, Path path, TranslogHeader header) throws IOException {
        super(generation, channel, path, header);
        this.reusableBuffer = ByteBuffer.allocate(1024);
        this.reuse = null;
    }

    public static TranslogOperationReader open(long generation, Path path, final String translogUUID) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        final TranslogHeader header = TranslogHeader.read(translogUUID, path, channel);
        return new TranslogOperationReader(generation, channel, path, header);
    }

    @Override
    public long sizeInBytes() {
        try {
            return channel.size();
        } catch (IOException e) {
            logger.warn("{} get channel size error, {}", path, e);
            return  0;
        }
    }

    @Override
    public int totalOperations() {
        return 0;
    }

    @Override
    Checkpoint getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(Checkpoint checkpoint) {
        this.checkpoint = checkpoint;
    }

    /**
     * reads an operation at the given position into the given buffer.
     */
    @Override
    protected void readBytes(ByteBuffer buffer, long position) throws IOException {
        if (position < getFirstOperationOffset()) {
            throw new IOException("read requested before position of first ops. pos [" + position + "] first op on: [" +
                getFirstOperationOffset() + "], generation: [" + getGeneration() + "], path: [" + path + "]");
        }
        Channels.readFromFileChannelWithEofException(channel, position, buffer);
    }

    public synchronized TranslogOperation readOperation(long position) throws IOException {
        try {
            final int opSize = readSize(reusableBuffer, position);
            reuse = checksummedStream(reusableBuffer, position, opSize, reuse);
            Translog.Operation op = read(reuse);

            long nextPosition = position + opSize;
            Translog.Location current = new Translog.Location(generation, position, 0);
            Translog.Location next = new Translog.Location(generation, nextPosition, 0);
            return new TranslogOperation(op, current, next);
        } catch (EOFException e) {
            return null;
        } catch (TranslogCorruptedException e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            channel.close();
        }
    }

    protected final boolean isClosed() {
        return closed.get();
    }
}
