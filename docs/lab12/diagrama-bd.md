# Diagrama de relaciones de la base de pruebas LAB12

Obtenido de information_schema de admitu_db después de las migraciones; no contiene datos ni credenciales. Las asociaciones muestran la clave foránea; las restricciones de estados y unicidad están en los procedimientos y migraciones.

```mermaid
erDiagram
    institucion_educativa ||--o{ antecedente_academico : "institucion_id"
    postulante ||--o{ antecedente_academico : "postulante_id"
    persona ||--o{ apoderado : "persona_id"
    postulante ||--o{ archivo_digital : "postulante_id"
    postulacion ||--o{ carne_postulante : "postulacion_id"
    sede ||--o{ carne_postulante : "sede_id"
    facultad ||--o{ carrera : "facultad_id"
    convocatoria ||--o{ convocatoria_etapa : "convocatoria_id"
    etapa ||--o{ convocatoria_etapa : "etapa_id"
    convocatoria ||--o{ convocatoria_modalidad : "convocatoria_id"
    modalidad ||--o{ convocatoria_modalidad : "modalidad_id"
    convocatoria ||--o{ convocatoria_postulante : "convocatoria_id"
    postulante ||--o{ convocatoria_postulante : "postulante_id"
    persona ||--o{ cuenta_usuario : "persona_id"
    documento_postulacion ||--o{ documento_observacion : "documento_id"
    evaluador ||--o{ documento_observacion : "evaluador_id"
    postulacion ||--o{ documento_postulacion : "postulacion_id"
    requisito_convocatoria_modalidad ||--o{ documento_postulacion : "requisito_aplicable_id"
    persona ||--o{ evaluador : "persona_id"
    pais ||--o{ institucion_educativa : "pais_id"
    documento_observacion ||--o{ notificacion : "observacion_origen_id"
    postulacion ||--o{ notificacion : "postulacion_id"
    carrera ||--o{ oferta_carrera : "carrera_id"
    convocatoria ||--o{ oferta_carrera : "convocatoria_id"
    postulacion ||--o{ pago : "postulacion_id"
    oferta_carrera ||--o{ postulacion : "carrera_elegida_id"
    convocatoria ||--o{ postulacion : "convocatoria_id"
    estado_postulacion ||--o{ postulacion : "estado_actual_id"
    evaluador ||--o{ postulacion : "evaluador_id"
    convocatoria_modalidad ||--o{ postulacion : "modalidad_elegida_id"
    postulante ||--o{ postulacion : "postulante_id"
    sede ||--o{ postulacion : "sede_id"
    estado_postulacion ||--o{ postulacion_historial : "estado_actual_id"
    estado_postulacion ||--o{ postulacion_historial : "estado_anterior_id"
    postulacion ||--o{ postulacion_historial : "postulacion_id"
    apoderado ||--o{ postulante : "apoderado_id"
    persona ||--o{ postulante : "persona_id"
    convocatoria_modalidad ||--o{ requisito_convocatoria_modalidad : "convocatoria_modalidad_id"
    requisito ||--o{ requisito_convocatoria_modalidad : "requisito_id"
    cuenta_usuario ||--o{ sesion_usuario : "cuenta_id"
```

Las entidades nuevas son archivo_digital, cuenta_usuario y sesion_usuario. Postulacion incorpora sede_id y evaluador_id. Los índices uq_postulante_convocatoria y uq_pago_vigente protegen las operaciones concurrentes.
