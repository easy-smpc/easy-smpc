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
package de.tu_darmstadt.cbs.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.ComponentTextFieldValidator;
import de.tu_darmstadt.cbs.app.components.EntryBin;
import de.tu_darmstadt.cbs.app.components.EntryParticipant;
import de.tu_darmstadt.cbs.app.resources.Resources;
import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */

public class Perspective1ACreate extends Perspective implements ChangeListener {

    /** Panel for participants */
    private JPanel             participants;
    
    /** Panel for bins */
    private JPanel             bins;
    
    /** Text field containing title of study */
    private ComponentTextField title;
    
    /** Save button */
    private JButton            save;
    
    /** Is interim saving in this perspective possible */
    private final boolean      interimSavingPossible = false;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective1ACreate(App app) {
        super(app, Resources.getString("PerspectiveCreate.0"), 1); //$NON-NLS-1$
    }

    /**
     * Reacts on all changes in any components
     */
    public void stateChanged(ChangeEvent e) {
        this.save.setEnabled(this.areValuesValid());
    }
    
    @Override
    protected boolean isInterimSavingPossible() {
        return interimSavingPossible;
    }
    
    /**
     * Removes empty lines in participants and bins
     */
    private void actionRemoveEmptyLines() {
        for (Component entry : this.participants.getComponents()) {
            if (this.participants.getComponentCount() > 1) {
                // Remove participants if both fields empty
                if (((EntryParticipant) entry).getLeftValue().trim().isEmpty() &&
                    ((EntryParticipant) entry).getRightValue().trim().isEmpty()) {
                    removeParticipant((EntryParticipant) entry);
                }
            }
        }

        for (Component entry : this.bins.getComponents()) {
            if (this.bins.getComponentCount() > 1) {
                // Remove bin if left field empty and right field empty or zero
                if (((EntryBin) entry).getLeftValue().trim().isEmpty() &&
                    ((((EntryBin) entry).getRightValue().trim().isEmpty()) ||
                     ((EntryBin) entry).getRightValue().trim().equals(String.valueOf(0)))) {
                    removeBin((EntryBin) entry);
                }
            }
        }
    }
    
    /**
     * Loads bin names and data from a CSV-file
     */
    private void actionLoadCSV() {
        File file = getApp().getFile(true, new FileNameExtensionFilter(Resources.getString("PerspectiveCreate.CSVFileDescription"), Resources.FILE_ENDING_CSV) );
        if (file != null) {
            try {                
                EntryBin previousBin = null;
                Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(new FileReader(file));
                this.bins.removeAll();                
                for (CSVRecord record : records) {                  
                    previousBin = addBin(previousBin, record.get(0) ,record.get(1), true);
                }
                this.stateChanged(new ChangeEvent(this));
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(getPanel(), Resources.getString("App.11"), Resources.getString("PerspectiveCreate.CSVReadingError"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$               
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Loads bin names and data from an Excel-file
     */
    private void actionLoadExcel() {
        File file = getApp().getFile(true, new FileNameExtensionFilter(Resources.getString("PerspectiveCreate.ExcelFileDescription"), Resources.FILE_ENDING_EXCEL_XLSX) );
        if (file != null) {
            try {                
                Workbook workbook = WorkbookFactory.create(file, "", true);
                Sheet sheet = workbook.getSheetAt(0);
                this.bins.removeAll();
                // get all filled rows and columns
                List<Integer> listRows = new ArrayList<>();
                List<Integer> listColumns = new ArrayList<>();
                for (int row = 0; row < Resources.MAX_COUNT_ROWS_EXCEL; row++) {
                    boolean rowHasContent = false;
                    if (sheet.getRow(row) != null) {
                        for (int column = 0; column < Resources.MAX_COUNT_COLUMN_EXCEL; column++) {
                            if (sheet.getRow(row).getCell(column) != null &&
                                sheet.getRow(row).getCell(column).getCellType() != CellType.BLANK) {
                                rowHasContent = true;
                                if (!listColumns.contains(column)) listColumns.add(column);
                            }
                        }
                        if (rowHasContent) listRows.add(row);
                    }
                }
                //throw error, if if more then two columns or rows 
                if (listRows.size() != 2 && listColumns.size() != 2) {
                    throw new IllegalArgumentException("Exactly two lines or columns required");
                }
                int rowDistancePermanent, colDistancePermanent, rowDistanceTemp, colDistanceTemp;
                boolean columnsOriented;
                if (listColumns.size() == 2) {
                    rowDistancePermanent = 1;
                    colDistancePermanent = 0;      
                    rowDistanceTemp = 0;
                    colDistanceTemp = listColumns.get(1)-listColumns.get(0);
                    columnsOriented = true;
                } else {
                    rowDistancePermanent = 0;
                    colDistancePermanent = 1;
                    rowDistanceTemp = listRows.get(1)-listRows.get(0);
                    colDistanceTemp = 0;
                    columnsOriented = false;
                }
                // read data rows or column wise
                int row = listRows.get(0);
                int col = listColumns.get(0);
                EntryBin previousBin = null;
                while ((columnsOriented && row <= listRows.get(listRows.size() - 1)) ||
                       (!columnsOriented && col <= listColumns.get(listColumns.size() - 1))) {
                    previousBin = addBin(previousBin,
                                         extractExcelCellContent(sheet.getRow(row).getCell(col),true),
                                         extractExcelCellContent(sheet.getRow(row + rowDistanceTemp).getCell(col + colDistanceTemp),true),
                                         true);                    
                    row = row + rowDistancePermanent;
                    col = col + colDistancePermanent;
                }
                this.stateChanged(new ChangeEvent(this));
                workbook.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.ExcelReadingError"), Resources.getString("App.11"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$               
                e.printStackTrace();
            }
            catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.ExcelDataError"), Resources.getString("PerspectiveCreate.ExcelDataErrorTitle"),  JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$               
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Extracts the data in an excel cell as a string
     * @param cell
     */
    private String extractExcelCellContent(Cell cell, boolean originalCellType) {
        if (cell != null) {
        switch (originalCellType ? cell.getCellType() : cell.getCachedFormulaResultType()){
            case NUMERIC:
                double number = cell.getNumericCellValue();
                //return integer if no decimal part
                return number == Math.floor(number) ? String.valueOf((int) number) : String.valueOf(number) ;
            case STRING:
                return cell.getStringCellValue();
            case BLANK:
                return "";
            case _NONE:
                return "";
            case FORMULA:
                return extractExcelCellContent(cell, false);
            default:
                return "";
         }
        }
        else return "";
    }


    /**
     * Save the project and proceed.
     */
    private void actionSave() {
        
        // Check whether at least three participants
        if (this.participants.getComponents().length < 3) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.notEnoughParticipants"));
            return;
        }

        
        // Collect participants
        List<Participant> participants = new ArrayList<>();
        for (Component entry : this.participants.getComponents()) {
            Participant participant = new Participant(((EntryParticipant)entry).getLeftValue(),
                                                      ((EntryParticipant)entry).getRightValue());
            participants.add(participant);
        }
        
        // Collect bins
        List<Bin> bins = new ArrayList<>();
        for (Component entry : this.bins.getComponents()) {
            Bin bin = new Bin(((EntryBin)entry).getLeftValue());
            bin.initialize(participants.size());
            bin.shareValue(new BigInteger(((EntryBin)entry).getRightValue().trim()));
            bins.add(bin);
        }

        // Initialize study
        getApp().actionCreateDone(this.title.getText(), participants.toArray(new Participant[participants.size()]), bins.toArray(new Bin[bins.size()]));
    }

    /**
     * Adds a new line for bin entry
     * @param enabled
     */
    private EntryBin addBin(EntryBin previous, String name, String value, boolean enabled) {

        // Find index
        int index = Arrays.asList(this.bins.getComponents()).indexOf(previous);
        index = index == -1 ? 0 : index + 1;
        
        // Create and add entry
        EntryBin entry = new EntryBin(name, enabled, value, enabled, enabled);
        entry.setChangeListener(this);
        entry.setAddListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
               addBin(entry, "", "", true);
            } 
        });
        entry.setRemoveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeBin(entry);
            }
        });
        this.bins.add(entry, index);
        this.bins.revalidate();
        this.bins.repaint();
        return entry;
    }

    /**
     * Adds a new line for participant entry
     * @param previous
     * @param enabled
     */
    private void addParticipant(EntryParticipant previous, boolean enabled) {
        
        // Find index
        int index = Arrays.asList(this.participants.getComponents()).indexOf(previous);
        index = index == -1 ? 0 : index + 1;
        
        // Create and add entry
        EntryParticipant entry = new EntryParticipant("", "", enabled, enabled);
        entry.setChangeListener(this);
        entry.setAddListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
               addParticipant(entry, true);
            } 
        });
        entry.setRemoveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeParticipant(entry);
            }
        });
        this.participants.add(entry, index);
        this.participants.revalidate();
        this.participants.repaint();
    }

    /**
     * Checks all values for validity
     * @return
     */
    private boolean areValuesValid() {
        
        // Check participants
        for (Component c : this.participants.getComponents()) {
            if (!((EntryParticipant) c).areValuesValid()) {
                return false;
            }
        }
        
        // Check bins
        for (Component c : this.bins.getComponents()) {
            if (!((EntryBin) c).areValuesValid()) { 
                return false; 
            }
        }
      
        // Check title
        if (!title.isValueValid()) {
            return false;
        }
        
        // Done
        return true;
    }

    /**
     * Removes a bin
     * @param entry
     */
    private void removeBin(EntryBin entry) {
        
        // Check whether it's the last entry
        if (this.bins.getComponentCount() == 1) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }
        
        // Remove and update
        this.bins.remove(entry);
        this.bins.revalidate();
        this.bins.repaint();
    }

    /**
     * Removes a participant
     * @param entry
     */
    private void removeParticipant(EntryParticipant entry) {
        
        // Check whether it's the last entry
        if (this.participants.getComponentCount() == 1) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }
        
        // Remove and update
        this.participants.remove(entry);
        this.participants.revalidate();    
        this.participants.repaint();
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Layout
        panel.setLayout(new BorderLayout());

        // -------
        // Name of study
        // -------
        JPanel title = new JPanel();
        panel.add(title, BorderLayout.NORTH);
        title.setLayout(new BorderLayout());
        title.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                         Resources.getString("PerspectiveCreate.studyTitle"),
                                                         TitledBorder.LEFT,
                                                         TitledBorder.DEFAULT_POSITION));
        this.title = new ComponentTextField(new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return !text.trim().isEmpty();
            }
        });
        this.title.setChangeListener(this);                                                     
        title.add(this.title, BorderLayout.CENTER);
        
        // Central panel
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);
        
        // Participants
        this.participants = new JPanel();
        this.participants.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveCreate.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.NORTH);

        // Bins
        this.bins = new JPanel();
        this.bins.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                             Resources.getString("PerspectiveCreate.bins"),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.DEFAULT_POSITION));
        this.bins.setLayout(new BoxLayout(this.bins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(bins);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.SOUTH);
        
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(3, 1));
        
        // load csv buttons
        JPanel loadbuttonsPane = new JPanel();
        loadbuttonsPane.setLayout(new GridLayout(1, 2));
        JButton loadCSV = new JButton(Resources.getString("PerspectiveCreate.loadCSVFile"));
        loadCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoadCSV();
            }
        });
        loadbuttonsPane.add(loadCSV, 0, 0); 
        
        // load excel buttons
        JButton loadExcel = new JButton(Resources.getString("PerspectiveCreate.loadExcelFile"));
        loadExcel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoadExcel();
            }
        });
        loadbuttonsPane.add(loadExcel, 1, 0);        
        buttonsPane.add(loadbuttonsPane, 0, 0);
        
        // Remove empty lines button
        JButton removeEmptylines = new JButton(Resources.getString("PerspectiveCreate.removeEmptyLines"));
        removeEmptylines.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionRemoveEmptyLines();
            }
        });
        buttonsPane.add(removeEmptylines, 0, 1);
        //save button
        save = new JButton(Resources.getString("PerspectiveCreate.save"));
        save.setEnabled(this.areValuesValid());
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        buttonsPane.add(save, 0, 2);
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
        
        // Clear
        this.participants.removeAll();
        this.bins.removeAll();
        this.title.setText("");

        // Add initial
        this.addParticipant(null, true);
        this.addBin(null, "", "", true);
        
        // Update
        this.stateChanged(new ChangeEvent(this));
    }
    
}
