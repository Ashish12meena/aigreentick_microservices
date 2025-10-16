package com.example.aigreentick.services.messaging.flow.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlowEnvelopeDto {
    @JsonProperty("encrypted_flow_data")
    private String encryptedFlowData;
    @JsonProperty("encrypted_aes_key")
    private String encryptedAesKey;
    @JsonProperty("initial_vector")
    private String initialVector;

    // extracted from surrounding webhook
    private String waId;

    public String getEncryptedFlowData() {
        return encryptedFlowData;
    }

    public void setEncryptedFlowData(String encryptedFlowData) {
        this.encryptedFlowData = encryptedFlowData;
    }

    public String getEncryptedAesKey() {
        return encryptedAesKey;
    }

    public void setEncryptedAesKey(String encryptedAesKey) {
        this.encryptedAesKey = encryptedAesKey;
    }

    public String getInitialVector() {
        return initialVector;
    }

    public void setInitialVector(String initialVector) {
        this.initialVector = initialVector;
    }

    public String getWaId() {
        return waId;
    }

    public void setWaId(String waId) {
        this.waId = waId;
    }

    @Override
    public String toString() {
        return "FlowEnvelopeDto{" +
                "encryptedFlowData='[REDACTED]'" +
                ", encryptedAesKey='[REDACTED]'" +
                ", initialVector='[REDACTED]'" +
                ", waId='" + waId + '\'' +
                '}';
    }
}
