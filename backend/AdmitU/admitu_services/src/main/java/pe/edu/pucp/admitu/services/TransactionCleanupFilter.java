package pe.edu.pucp.admitu.services;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import pe.edu.pucp.admitu.config.TransactionContext;
@Provider
public class TransactionCleanupFilter implements ContainerResponseFilter {
 public void filter(ContainerRequestContext request,ContainerResponseContext response){
  try {TransactionContext.rollback();}finally{TransactionContext.close();}
 }
}
