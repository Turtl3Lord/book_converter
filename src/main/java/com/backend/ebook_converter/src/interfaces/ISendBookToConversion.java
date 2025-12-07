package com.backend.ebook_converter.src.interfaces;

import com.backend.ebook_converter.src.Responses.SendToConversionRespondeModel;

public interface ISendBookToConversion {

    SendToConversionRespondeModel sendBookToConversion(byte[] bookData, String originalFormat, String targetFormat);
}
