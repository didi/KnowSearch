package org.elasticsearch.dcdr.indices.recovery;

public enum RecoverCase {
    MappingUpdate,
    Outter,
    CheckPointError,
    CheckPointDelay,
    Other;
}
