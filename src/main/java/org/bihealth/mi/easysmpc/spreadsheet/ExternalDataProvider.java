package org.bihealth.mi.easysmpc.spreadsheet;

import java.math.BigDecimal;

/**
 * Interface to add external data to spreadsheet
 * 
 * @author Felix Wirth
 *
 */
public interface ExternalDataProvider {
    
    // TODO  make interface return type generic?
    BigDecimal getDataFor(int row, int column);
}
