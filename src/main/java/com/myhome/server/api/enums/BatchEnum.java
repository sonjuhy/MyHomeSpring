package com.myhome.server.api.enums;

import lombok.Getter;

@Getter
public enum BatchEnum {
    CLOUD_PUBLIC_CHUNK_PARTITION_NAME("PublicCloudPartition"),
    CLOUD_PUBLIC_PARALLEL_FLOW_NAME("PublicCloudFlow-"),
    CLOUD_PRIVATE_CHUNK_PARTITION_NAME("PrivateCloudPartition"),
    CLOUD_PRIVATE_PARALLEL_FLOW_NAME("PrivateCloudFlow-");

    private final String target;
    BatchEnum(String target){
        this.target = target;
    }
    public String getPublicParallelFlowName(int num){
        return CLOUD_PUBLIC_PARALLEL_FLOW_NAME+String.valueOf(num);
    }
    public String getPrivateParallelFlowName(int num){
        return CLOUD_PRIVATE_PARALLEL_FLOW_NAME+String.valueOf(num);
    }
}
