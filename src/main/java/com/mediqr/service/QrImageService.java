package com.mediqr.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

@Service
public class QrImageService {

    /** Convierte un texto (normalmente una URL) en una imagen PNG con el código QR. */
    public byte[] png(String contenido, int tamano) throws Exception {
        BitMatrix matriz = new MultiFormatWriter()
            .encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matriz, "PNG", out);
        return out.toByteArray();
    }
}
