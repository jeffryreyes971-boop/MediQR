-- Datos de EJEMPLO para probar las alertas (valida con una fuente farmacéutica
-- confiable antes de usarlos de verdad).
USE mediqr_db;

INSERT INTO medicamento_catalogo (nombre, eliminacion_renal) VALUES
 ('Ibuprofeno', TRUE), ('Metformina', TRUE), ('Amoxicilina', TRUE),
 ('Paracetamol', FALSE), ('Warfarina', FALSE), ('Aspirina', FALSE),
 ('Enalapril', TRUE);

INSERT INTO interaccion_medicamento (medicamento_a, medicamento_b, gravedad, mensaje) VALUES
 ('Warfarina', 'Ibuprofeno', 'ALTA',  'Aumenta el riesgo de sangrado.'),
 ('Warfarina', 'Aspirina',   'ALTA',  'Aumenta el riesgo de sangrado.'),
 ('Enalapril', 'Ibuprofeno', 'MEDIA', 'Puede reducir el efecto antihipertensivo y afectar el riñón.');

-- Los usuarios se crean desde la pantalla de registro (la contraseña se guarda
-- con BCrypt, por eso no se insertan aquí a mano).
