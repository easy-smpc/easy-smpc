package org.bihealth.mi.easybus.implementations.http;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Executes a REST request
 * 
 * @author Felix Wirth
 *
 */
public class ExecutHTTPRequest<T> {

    /**
     * Type of request
     * 
     * @author Felix Wirth
     *
     */
    public enum REST_TYPE {
                           GET,
                           POST,
                           PUT,
                           DELETE
    };
    
    /** Default error handler */
    private static Function<Response, String> defaultErrorHandler = new Function<>() {
        @Override
        public String apply(Response response) {
            String body = "Body not readable";
            try {
                body = response.readEntity(String.class);
            } catch (Exception e) {
                // Ignore
            }
            return  String.format("Return code %s and body %s", response.getStatus(), body);
        }
    };
    /** Connection details */
    private Builder                           requestBuilder;
    /** Data */
    private final String                      inputData;
    /** To access an executor */
    private final Supplier<ExecutorService>   executorSupplier;
    /** Handle result */
    private final Function<Response, T>       resultHandler;
    /** Type of request */
    private final REST_TYPE                   type;
    /** Error handler */
    private Function<Response, String>        errorHandler;
    /** Authorization handler */
    private UnaryOperator<Builder>            authentificationHandler;
    /** Indicates if task was started - remains true after it was finished */
    private boolean                           started             = false;
    /** Task */
    private FutureTask<T>                     task;
    
    /**
     * Creates a new instance
     * 
     * @param connection
     * @param currentAuthorization
     * @param pathCreateRoom
     * @param authorizationUpdater
     * @param data
     */
    public ExecutHTTPRequest(Builder requestBuilder,
                              REST_TYPE type,
                              Supplier<ExecutorService> executorSupplier,
                              String inputData,
                              Function<Response, T> resultHandler,
                              Function<Response, String> errorHandler,
                              UnaryOperator<Builder> authentificationHandler) {
        
        // Check
        if (requestBuilder == null || type == null || executorSupplier == null) {
            throw new IllegalArgumentException("Please provide non-null values for requestbuilder, type and executorSupplier");
        }

        // Store
        this.requestBuilder = requestBuilder;
        this.executorSupplier = executorSupplier;
        this.inputData = inputData;
        this.resultHandler = resultHandler;       
        this.type = type;
        this.authentificationHandler = authentificationHandler;
        if (errorHandler != null) {
            this.errorHandler = errorHandler;
        } else {
            this.errorHandler = defaultErrorHandler;
        }        
    }

    /**
     * Executes a request
     * 
     * @return
     */
    public FutureTask<T> execute() {
        // Init
        this.started = true;
        
        // Create future task
        task = new FutureTask<>(new Callable<T>() {

            @Override
            public T call() throws Exception {

                // Execute request
                Response response = executeRequest();

                // Handle failed auth if authentificationHandler provided
                if (response.getStatus() == 401 && authentificationHandler != null) {

                    // Replace builder with new authorization
                    requestBuilder = authentificationHandler.apply(requestBuilder);

                    // Successful authorization, execute request again
                    response = executeRequest();
                }
                
                // General error handling
                if (response.getStatus() != 200 && response.getStatus() != 201 && response.getStatus() != 202) {
                    throw new IllegalStateException(String.format("Error executing HTTP request: %s", errorHandler.apply(response)));
                }

                // Return either null or calculated result
                if (resultHandler == null) { 
                    return null; 
                }
                return resultHandler.apply(response);
            }
        });

        // Start and return
        executorSupplier.get().execute(task);
        return task;
    }

    /**
     * Execute the HTTP request
     * 
     * @return
     */
    private Response executeRequest() {

        switch (type) {
        case GET:
            return requestBuilder.get(Response.class);
        case POST:
            return requestBuilder.post(Entity.entity(inputData, MediaType.TEXT_PLAIN));
        case PUT:
            return requestBuilder.put(Entity.entity(inputData, MediaType.TEXT_PLAIN));
        case DELETE:
            return requestBuilder.delete(Response.class);
        default:
            throw new IllegalArgumentException("Request type unknown");
        }
    }
    
    /**
     * Is request started? remains true after it was finished 
     */
     public boolean isStarted() {
        return this.started;
    }
     
     /**
      * Is request finished?
      */
      public boolean isFinished() {
          return task == null ? false : task.isCancelled() || task.isDone();
     }
     
     /**
      * Will execute and return after all requests have been finished
      */
     public static void executeRequestPackage(List<ExecutHTTPRequest<?>> requests,
                                              long timeoutMatrixActivity,
                                              Consumer<Exception> errorHandler) {    
        
         // Prepare
         boolean finished = false;
         
         // Check all tasks
         int index = 0;
         for(ExecutHTTPRequest<?> request : requests) {
             if(request.isStarted()) {
                 throw new IllegalArgumentException(String.format("Request at position %d has already been started. Aborting", index));
             }
             index++;
         }
         
         // Execute
         for(ExecutHTTPRequest<?> request : requests) {
             
             // Create thread, which waits for future to finish
             new Thread(new Runnable() {
                @Override
                public void run() {
                     // Execute
                     FutureTask<?> future = request.execute();
                     try {
                         future.get(timeoutMatrixActivity, TimeUnit.MILLISECONDS);
                     } catch (Exception e) {
                         if(errorHandler != null) {
                             errorHandler.accept(e);
                         }
                     }
                }
            }).start();
         }         
         
         // Loop until finished
         while (!finished) {
             finished = true;

             for (ExecutHTTPRequest<?> request : requests) {
                 if (!request.isFinished()) {
                     finished = false;
                     break;
                 }
             }
         }    
     }
}
