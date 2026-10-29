package com.mediqr.service;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CifradoService {

    private static final int IV_BYTES = 12;
    private final SecretKeySpec clave;
    private final SecureRandom random = new SecureRandom();

    public CifradoService(@Value("${mediqr.cifrado.clave-base64}") String claveBase64) {
        this.clave = new SecretKeySpec(Base64.getDecoder().decode(claveBase64), "AES");
    }

    /** AES-256-GCM: cifra y además detecta si alguien alteró el archivo. */
    public byte[] cifrar(byte[] datos) throws Exception {
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(128, iv));
        byte[] cifrado = c.doFinal(datos);
        // Archivo final: [IV (12 bytes)][contenido cifrado]
        return ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array();
    }

    public byte[] descifrar(byte[] archivo) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(archivo);
        byte[] iv = new byte[IV_BYTES];
        bb.get(iv);
        byte[] cifrado = new byte[bb.remaining()];
        bb.get(cifrado);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(128, iv));
        return c.doFinal(cifrado);
    }
}
