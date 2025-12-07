package com.backend.ebook_converter.src.services.convertion;

import com.backend.ebook_converter.src.interfaces.IGetConvertedBook;

public class GetConvertedBook implements IGetConvertedBook {
    @Override
    public byte[] getConvertedBook(String conversionId) {
        // Lógica para obtener el libro convertido usando el conversionId
        // Esto podría implicar consultar una base de datos o un sistema de almacenamiento
        return new byte[0]; // Retorna los datos del libro convertido como un arreglo de bytes
    }
}
