/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bihealth.mi.easybus.implementations.http;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Executes a REST request
 * 
 * @author Felix Wirth
 */
public class ExecuteHTTPRequest<T> {

    /**
     * Type of request
     * 
     * @author Felix Wirth
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
    private AuthHandler                       authentificationHandler;
    /** Task */
    private FutureTask<T>                     task;
    /** Sleep time to re-try request */
    private long                              sleepTimeRetry;
    /** Number of retries */
    private int                               numberRetry;
    
    /**
     * Creates a new instance
     * 
     * @param requestBuilder
     * @param type
     * @param executorSupplier
     * @param inputData
     * @param resultHandler
     * @param errorHandler
     * @param authentificationHandler
     * @param sleepTimeRetry
     * @param numberRetry
     */
    public ExecuteHTTPRequest(Builder requestBuilder,
                             REST_TYPE type,
                             Supplier<ExecutorService> executorSupplier,
                             String inputData,
                             Function<Response, T> resultHandler,
                             Function<Response, String> errorHandler,
                             AuthHandler authentificationHandler,
                             int numberRetry,
                             long sleepTimeRetry) {

        // Check
        if (requestBuilder == null || type == null || executorSupplier == null) {
            throw new IllegalArgumentException("Requestbuilder, type and executorSupplier must not be null!");
        }
        if(numberRetry < 0 || sleepTimeRetry < 0) {
        	throw new IllegalArgumentException("Number retry and sleep time retry must no be smaller than 0");
        }
        
        if(numberRetry > 0 && sleepTimeRetry == 0) {
            throw new IllegalArgumentException("If number retry given, sleep time must be greater than 0");
        }

        // Store
        this.requestBuilder = requestBuilder;
        this.executorSupplier = executorSupplier;
        this.inputData = inputData;
        this.resultHandler = resultHandler;       
        this.type = type;
        this.authentificationHandler = authentificationHandler;
        this.sleepTimeRetry = sleepTimeRetry;
        this.numberRetry = numberRetry;
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

        // Create future task
        task = new FutureTask<>(new Callable<T>() {

            @Override
            public T call() throws Exception {

                // Execute request
                Response response = executeRequest();

                // Handle failed auth if authentificationHandler provided
                if (response.getStatus() == 401 && authentificationHandler != null) {

                    // Replace builder with new authorization
                    requestBuilder = authentificationHandler.authenticate(requestBuilder);

                    // Successful authorization, execute request again
                    response = executeRequest();
                }
                
                // Wait and retry once if too many requests
                if (response.getStatus() == 429 && numberRetry > 0) {
                    int retryCounter = 0;
                    
                    while(true) {
                        
                        // Check to make more retries
                        if(retryCounter >= numberRetry) {
                            throw new IllegalStateException(String.format("Error executing HTTP request: %s", errorHandler.apply(response)));
                        }
                        
                        // Sleep
                        try {
                            Thread.sleep(sleepTimeRetry);
                        } catch (InterruptedException e) {
                            // Ignore
                        }

                        // Execute request again
                        response = executeRequest();
                        retryCounter++;
                        
                        // If error is not "too many requests" anymore, proceed
                        if(response.getStatus() != 429) {
                            break;
                        }
                    }
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
}
