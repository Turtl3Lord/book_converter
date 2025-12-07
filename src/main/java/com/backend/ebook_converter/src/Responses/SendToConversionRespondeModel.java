package com.backend.ebook_converter.src.Responses;

public class SendToConversionRespondeModel {
    private final String conversionId;

    public SendToConversionRespondeModel(String conversionId) {
        this.conversionId = conversionId;
    }

    public String getConversionId() {
        return conversionId;
    }
}
