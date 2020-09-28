package org.greenplum.pxf.automation.utils.system;

/**
 * Enum to reflect the protocols supported
 */
public enum ProtocolEnum {
    HDFS("hdfs"),
    S3("s3"),
    ADL("adl"),
    WASBS("wasbs"),
    OSS("oss"),
    GS("gs");
    private String value;

    ProtocolEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
