package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionImpl;
import pe.edu.pucp.admitu.postulacion.Postulacion;

import java.util.List;

public class PostulacionBOImpl implements IPostulacionBO {

    private PostulacionDAO daoPostulacion;

    public PostulacionBOImpl() {
        daoPostulacion = new PostulacionImpl();
    }

    private void validar(Postulacion p) {
        if (p == null) throw new IllegalArgumentException("La postulacion es null");
        if (p.getPostulante() == null || p.getPostulante().getId() <= 0)
            throw new IllegalArgumentException("La postulacion no tiene postulante valido");
        if (p.getConvocatoria() == null || p.getConvocatoria().getId() <= 0)
            throw new IllegalArgumentException("La postulacion no tiene convocatoria valida");
        if (p.getModalidadElegida() == null || p.getModalidadElegida().getId() <= 0)
            throw new IllegalArgumentException("La postulacion no tiene modalidad valida");
        if (p.getCarreraElegida() == null || p.getCarreraElegida().getId() <= 0)
            throw new IllegalArgumentException("La postulacion no tiene oferta de carrera valida");
        if (p.getEstadoActual() == null || p.getEstadoActual().getId() <= 0)
            throw new IllegalArgumentException("La postulacion no tiene estado valido");
        Validaciones.codigo(p.getCodigoInscripcion(), "Codigo de inscripcion", 20);
    }

    @Override
    public int insertar(Postulacion p) {
        validar(p);
        try {
            int r = daoPostulacion.insertar(p);
            TransactionContext.commit();
            return r;
        } catch (Exception ex) {
            TransactionContext.rollback();
            throw new IllegalStateException("No fue posible registrar la postulación",ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int modificar(Postulacion p) {
        if (p == null) throw new IllegalArgumentException("La postulacion es null");
        Validaciones.positivo(p.getId(), "Id de postulacion");
        validar(p);
        try {int r=daoPostulacion.modificar(p);TransactionContext.commit();return r;}
        catch(Exception e){TransactionContext.rollback();throw new IllegalStateException("No se pudo modificar la postulación",e);}
        finally{TransactionContext.close();}
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulacion no valido");
        return daoPostulacion.eliminar(id);
    }

    @Override
    public List<Postulacion> listarTodos() {
        return daoPostulacion.listarTodos();
    }

    @Override
    public Postulacion buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulacion no valido");
        return daoPostulacion.buscarPorId(id);
    }

    @Override public int enviar(pe.edu.pucp.admitu.postulacion.EnvioPostulacion envio){
        if(envio==null||envio.getPostulacion()==null)throw new IllegalArgumentException("Adjunta la postulación");
        Postulacion p=envio.getPostulacion();
        p.setEstadoActual(new pe.edu.pucp.admitu.postulacion.EstadoPostulacion());p.getEstadoActual().setId(2);
        p.setCodigoInscripcion("ADM-"+java.util.UUID.randomUUID().toString().replace("-", "").substring(0,15).toUpperCase());
        validar(p);
        var documentos=envio.getDocumentos()==null?java.util.List.<pe.edu.pucp.admitu.archivo.ArchivoDigital>of():envio.getDocumentos();
        if(documentos.size()>20)throw new IllegalArgumentException("Máximo 20 archivos por postulación");
        long total=0;for(var a:documentos){if(a==null||a.getContenidoBase64()==null)throw new IllegalArgumentException("Adjunta los bytes de cada requisito");total+=a.getContenidoBase64().length();}
        if(total>36L*1024*1024)throw new IllegalArgumentException("La suma de archivos supera el límite de envío");
        try{
            int id=daoPostulacion.insertar(p);
            var daoDocumento=new pe.edu.pucp.admitu.postulacion.impl.DocumentoPostulacionImpl();
            var archivos=new pe.edu.pucp.admitu.archivo.bo.ArchivoBOImpl();
            for(var a:documentos){
                var d=new pe.edu.pucp.admitu.postulacion.DocumentoPostulacion();d.setPostulacion(p);
                var req=new pe.edu.pucp.admitu.configuracion.RequisitoConvocatoriaModalidad();req.setId(a.getRequisitoAplicableId());d.setRequisitoAplicable(req);
                d.setNombreArchivo(a.getNombre());d.setNumeroVersion(1);d.setTipoArchivo("application/pdf".equals(a.getMime())?pe.edu.pucp.admitu.configuracion.TipoArchivo.PDF:pe.edu.pucp.admitu.configuracion.TipoArchivo.IMAGEN);
                d.setTamanioArchivo(java.util.Base64.getDecoder().decode(a.getContenidoBase64()).length);d.setRutaArchivo("archivo:pendiente");d.setFechaCarga(java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")));d.setEstadoDocumento(pe.edu.pucp.admitu.postulacion.EstadoDocumento.PENDIENTE);
                int doc=daoDocumento.insertar(d);a.setPostulanteId(p.getPostulante().getId());a.setTipo("DOCUMENTO");a.setReferenciaId(doc);archivos.guardarEnTransaccion(a);
            }
            daoPostulacion.validarEnvio(id,envio.getSedeId()>0?envio.getSedeId():(p.getSedeId()==null?0:p.getSedeId()));TransactionContext.commit();return id;
        }catch(Exception e){TransactionContext.rollback();throw e instanceof IllegalArgumentException?(IllegalArgumentException)e:new IllegalStateException("No fue posible enviar la postulación",e);}
        finally{TransactionContext.close();}
    }
    @Override public int asignarEvaluador(int id,int evaluadorId){
        if(id<=0||evaluadorId<=0)throw new IllegalArgumentException("Postulación y evaluador requeridos");
        try{int r=daoPostulacion.asignarEvaluador(id,evaluadorId);TransactionContext.commit();return r;}
        catch(Exception e){TransactionContext.rollback();throw new IllegalStateException("No fue posible asignar evaluador",e);}
        finally{TransactionContext.close();}
    }
}
