package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import pe.edu.pucp.admitu.config.TransactionContext;

/**
 * Componente Jakarta REST (@Provider) que garantiza la liberación de cualquier
 * conexión transaccional residual en el ThreadLocal al concluir la petición HTTP.
 */
@Provider
public class TransactionCleanupFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        try {
            TransactionContext.rollback();
        } finally {
            TransactionContext.close();
        }
    }
}
