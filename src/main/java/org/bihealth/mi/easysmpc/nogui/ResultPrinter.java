package org.bihealth.mi.easysmpc.nogui;

import java.io.IOException;

/**
 * Interface to print results of a performance evaluation
 * 
 * @author Felix Wirth
 *
 */
public interface ResultPrinter {

    /**
     * Print results
     * 
     * @param values
     * @throws IOException
     */
    public void print(final Object... values) throws IOException;
    
    /**
     * Flush
     * 
     * @throws IOException
     */
    public void flush() throws IOException;
    
}
