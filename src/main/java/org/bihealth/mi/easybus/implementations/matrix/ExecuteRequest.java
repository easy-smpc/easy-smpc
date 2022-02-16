package org.bihealth.mi.easybus.implementations.matrix;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Executes a request to a matrix server
 * 
 * @author Felix Wirth
 *
 */
public class ExecuteRequest <T> {

    /** Connection details */
    private final ConnectionMatrix    connection;
    /** Action path */
    private final String              path;
    /** Data */
    private final Object              data;
    /** To update authorization */
    private final Consumer<String>    authorizationConsumer;
    /** To access an executor */
    private Supplier<ExecutorService> executorSupplier;
    /** Authorization */
    private String                    authorization;
    /** Was new authorization obtained? */
    private boolean                   newAuthorization;
    /** Handle result */
    private Function<String, T>       resultHandler;
    
    /**
     * Creates a new instance
     * 
     * @param connection
     * @param currentAuthorization
     * @param pathCreateRoom
     * @param authorizationUpdater
     * @param data
     */
    public ExecuteRequest(ConnectionMatrix connection,
                          String authorization,
                          String path,
                          Consumer<String> authorizationConsumer,
                          Supplier<ExecutorService> executorSupplier,
                          Object data,
                          Function<String, T> resultHandler) {
        
        this.connection = connection;
        this.authorization = authorization;
        this.path = path;
        this.authorizationConsumer = authorizationConsumer;
        this.executorSupplier = executorSupplier;
        this.data = data;
        this.resultHandler = resultHandler;
    }

    /**
     * Executes a request
     * 
     * @return
     */
    public FutureTask<T> execute() {
        
        // Create future task
        FutureTask<T> task = new FutureTask<>(new Callable<T>() {

            @Override
            public T call() throws Exception {
                
                // Is authorization needed?
                if(authorization == null) {
                    authorize();
                }
                
                // Make request
                String result = makeRequest();
                
                // TODO catch error
                
                if (newAuthorization) {
                    authorizationConsumer.accept(authorization);
                }
                
                // Return either null or calculated result
                if(resultHandler == null) {
                    return null;
                }                
                return resultHandler.apply(result);
            }
        });
        
        // Start and return
        executorSupplier.get().execute(task);
        return task;        
    }

    protected String makeRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    protected void authorize() {
        // TODO Auto-generated method stub       
    }
}
