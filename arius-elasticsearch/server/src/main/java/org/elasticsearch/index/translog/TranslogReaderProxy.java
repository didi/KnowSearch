package org.elasticsearch.index.translog;

import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.util.concurrent.ConcurrentHashMapLong;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.index.translog.Translog;
import org.elasticsearch.index.translog.TranslogOperationReader;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * author weizijun
 * date：2019-08-08
 */
public class TranslogReaderProxy implements Closeable {
    private LongSupplier globalCheckpointSupplier;
    private AtomicLong currentGeneration;
    private AtomicLong commitGeneration;
    private final AtomicReference<Boolean> checkCommit = new AtomicReference<>(false);
    private Map<Long, TranslogOperationReader> translogReaders;

    public TranslogReaderProxy(List<TranslogOperationReader> readerList, TranslogOperationReader currentReader, LongSupplier globalCheckpointSupplier) {
        translogReaders = new ConcurrentHashMap<>();
        for (TranslogOperationReader translogOperationReader : readerList) {
            translogReaders.put(translogOperationReader.generation, translogOperationReader);
        }

        translogReaders.put(currentReader.generation, currentReader);

        this.globalCheckpointSupplier = globalCheckpointSupplier;
        this.currentGeneration = new AtomicLong(currentReader.generation);
        this.commitGeneration = new AtomicLong(currentReader.generation);
    }

    public void addTranslogReader(TranslogOperationReader reader) {
        this.translogReaders.put(reader.generation, reader);
        if (reader.generation > currentGeneration.get()) {
            currentGeneration.set(reader.generation);
        }
    }

    public void removeTranslogReader(long generation) {
        TranslogOperationReader reader = this.translogReaders.remove(generation);
        if (reader != null) {
            IOUtils.closeWhileHandlingException(reader);
        }
    }

    public void resetReaderTranslog(long generation, Checkpoint checkpoint) {
        TranslogOperationReader reader = translogReaders.get(generation);
        if (reader != null) {
            reader.setCheckpoint(checkpoint);
        }
    }

    private TranslogOperationReader getReader(long generation) {
        TranslogOperationReader reader = translogReaders.get(generation);
        if (reader == null) {
            throw new TranslogOffsetBehindException("current generation("+generation+") is expire");
        }

        return reader;
    }

    /**
     * 读取tranlog列表
     * @param generation generation
     * @param position translog记录的position
     * @param maxCount 最多拉取的条数
     * @return translog列表
     * @throws IOException IOException
     */
    public List<TranslogOperation> readTranslog(long generation, long position, int maxCount) throws IOException {
        TranslogOperationReader reader = getReader(generation);
        long globalCheckpoint = globalCheckpointSupplier.getAsLong();
        List<TranslogOperation> operations = new ArrayList<>();
        TranslogOperation result = reader.readOperation(position);
        while (true) {
            if (result == null) {
                if (generation < currentGeneration.get()) {
                    generation += 1;
                    reader = getReader(generation);
                    position = reader.getFirstOperationOffset();
                    result = reader.readOperation(position);
                    continue;
                } else {
                    break;
                }
            }

            operations.add(result);
            if (result.getOperation().seqNo() > globalCheckpoint || operations.size() >= maxCount) {
                break;
            }

            result = reader.readOperation(result.getNext().translogLocation);
        }

        return operations;
    }

    /**
     * 根据checkpoint获取到translog的location
     * @param checkpoint checkpoint
     * @return Translog.Location
     */
    public Translog.Location getCheckpointLocation(long checkpoint) {
        Translog.Location location = null;
        for (Map.Entry<Long, TranslogOperationReader> entry : translogReaders.entrySet()) {
            TranslogOperationReader reader = entry.getValue();
            if (reader.getCheckpoint() == null && reader.generation == currentGeneration.get()) {
                location = getCheckpointLocationInReader(reader, checkpoint);
                if (location != null) {
                    break;
                } else {
                    long globalCheckpoint = globalCheckpointSupplier.getAsLong();
                    if (checkpoint >= globalCheckpoint) {
                        return new Translog.Location(reader.generation, reader.getFirstOperationOffset(), 0);
                    }
                }
            }

            if (reader.getCheckpoint() != null
                && (checkpoint >= (reader.getCheckpoint().minSeqNo-1) && checkpoint <= reader.getCheckpoint().maxSeqNo)) {
                location = getCheckpointLocationInReader(reader, checkpoint);
                break;
            }
        }

        return location;
    }

    private Translog.Location getCheckpointLocationInReader(TranslogOperationReader reader, long checkpoint) {
        long offset = reader.getFirstOperationOffset();

        while (true) {
            try {
                TranslogOperation translogLocation = reader.readOperation(offset);
                if (translogLocation == null) {
                    return null;
                }

                long seqNo = translogLocation.getOperation().seqNo();

                if (seqNo == checkpoint) {
                    return translogLocation.getNext();
                }

                if (seqNo == (checkpoint + 1)) {
                    return translogLocation.getCurrent();
                }

                if (seqNo > (checkpoint + 1)) {
                    return null;
                }

                offset = translogLocation.getNext().translogLocation;
            } catch (IOException e) {
                return null;
            }

        }
    }

    /**
     * 判断start的位置和end的位置是否是连续接上的
     * 如果在同一个translog中，判断是否是否一样
     * 如果不在同一个translog，如果end比start大，则要确认start是否为translog的末尾，同时end是下一个translog的开头
     * @param start start的translog位置
     * @param end end的translog位置
     * @return 是否连续
     */
    public boolean isTranslogPosContinuous(Translog.Location start, Translog.Location end) {
        if (start.equals(end)) {
            return true;
        }

        if (start.compareTo(end) > 0) {
            return false;
        }

        TranslogOperationReader startReader = translogReaders.get(start.generation);
        TranslogOperationReader endReader = translogReaders.get(end.generation);

        if (startReader == null || endReader == null) {
            // TODO throw exception
            return false;
        }

        // 如果start在末尾，而且end在开头，判断end，两个translog文件是否连续，或者中间间隔了很多没有数据的tranlog文件
        if (start.translogLocation == startReader.sizeInBytes() && end.translogLocation == endReader.getFirstOperationOffset()) {
            // end
            for (long l = start.translogLocation+1; l < end.translogLocation; l++) {
                TranslogOperationReader reader = translogReaders.get(l);
                if (reader != null && reader.sizeInBytes() > reader.getFirstOperationOffset()) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    public long getMinCommitTranslogGen() {
        if (checkCommit.get().equals(true)) {
            return commitGeneration.get();
        } else {
            return currentGeneration.get();
        }
    }

    public synchronized void updateCommitOffset(Translog.Location commitOffset) {
        if (translogReaders.containsKey(commitOffset.generation)) {
            commitGeneration.set(commitOffset.generation);
        }
    }

    public void startCheckCommitGen() {
        checkCommit.compareAndSet(false, true);
    }

    public void stopCheckCommitGen() {
        checkCommit.compareAndSet(true, false);
    }

    public Translog.Location getInitLocation() {
        long minGen = translogReaders.keySet().stream().min(Comparator.comparingLong(Long::longValue)).get();
        TranslogOperationReader reader = translogReaders.get(minGen);
        return new Translog.Location(minGen, reader.getFirstOperationOffset(), 0);
    }

    @Override
    public void close() throws IOException {
        translogReaders.forEach((k, reader) -> {
            IOUtils.closeWhileHandlingException(reader);
        });
    }
}
