package com.mediqr.service;

import com.mediqr.model.Documento;
import com.mediqr.model.Perfil;
import com.mediqr.repository.DocumentoRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentoService {

    // SVG no se permite: puede contener scripts
    private static final Set<String> PERMITIDOS =
        Set.of("application/pdf", "image/jpeg", "image/png", "image/gif", "image/webp");

    private final CifradoService cifrado;
    private final DocumentoRepository repo;
    private final Path carpeta;

    public DocumentoService(CifradoService cifrado, DocumentoRepository repo,
                            @Value("${mediqr.almacen}") String ruta) throws IOException {
        this.cifrado = cifrado;
        this.repo = repo;
        this.carpeta = Files.createDirectories(Path.of(ruta));
    }

    public Documento guardar(Perfil perfil, MultipartFile archivo) throws Exception {
        String tipo = archivo.getContentType();
        if (tipo == null || !PERMITIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Solo se permiten imágenes (JPG, PNG, GIF, WEBP) o PDF");
        }
        String nombreDisco = UUID.randomUUID() + ".bin";
        Files.write(carpeta.resolve(nombreDisco), cifrado.cifrar(archivo.getBytes()));

        Documento d = new Documento();
        d.setPerfil(perfil);
        d.setNombreOriginal(archivo.getOriginalFilename() == null ? "documento" : archivo.getOriginalFilename());
        d.setTipoMime(tipo);
        d.setNombreArchivo(nombreDisco);
        return repo.save(d);
    }

    /** Borra el archivo cifrado del disco y su registro en la base de datos. */
    public void eliminar(Documento d) {
        borrarArchivo(d);
        repo.delete(d);
    }

    /** Solo borra el archivo del disco (se usa también al eliminar un perfil completo). */
    public void borrarArchivo(Documento d) {
        try {
            Files.deleteIfExists(carpeta.resolve(d.getNombreArchivo()));
        } catch (IOException e) {
            System.err.println("No se pudo borrar el archivo " + d.getNombreArchivo() + ": " + e.getMessage());
        }
    }

    public byte[] leer(Documento d) throws Exception {
        return cifrado.descifrar(Files.readAllBytes(carpeta.resolve(d.getNombreArchivo())));
    }
}
