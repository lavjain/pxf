package org.greenplum.pxf.automation.utils.system;

/**
 * Enum to reflect the protocols supported
 */
public enum ProtocolEnum {
    ADL("adl"),
    GS("gs"),
    HDFS("hdfs"),
    NFS("nfs"),
    S3("s3"),
    WASBS("wasbs");

    private final String value;

    ProtocolEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
