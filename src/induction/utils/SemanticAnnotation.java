/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SemanticAnnotation.java
 *
 * Created on 13-Mar-2011, 23:14:16
 */

package induction.utils;

import induction.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

/**
 *
 * @author sinantie
 */
public class SemanticAnnotation extends javax.swing.JFrame {
    private String inputFile = "results/output/weatherGov/alignments/gold_staged/evalGabor/f1-pred.0";
    private List<Annotation> annotations;
    private Annotation currentAnnotation;
    protected UndoManager undoManager = new UndoManager();
    /** Creates new form SemanticAnnotation */
    public SemanticAnnotation() {
        initComponents();
        readInputFile();
        spinner.setValue(0);
        selectButton.doClick();
        splitPane.setDividerLocation(0.5);
        outText.getDocument().addUndoableEditListener(
        new UndoableEditListener() {
          public void undoableEditHappened(UndoableEditEvent e) {
            undoManager.addEdit(e.getEdit());
            
          }
        });
    }

    private void showAnnotation()
    {
        currentAnnotation = annotations.get((Integer)spinner.getValue());
        refText.setText(currentAnnotation.refText);
        trueText.setText(currentAnnotation.trueText);
        // check whether semantic alignment file already exists and is not empty
        File f = new File(currentAnnotation.key + ".salign");
        if(f.exists() && f.getTotalSpace() > 0)
        {
            outText.setText(readFile(f.getPath()));
        }
        else
        {
            outText.setText(readEventsFile(currentAnnotation.key + ".events", currentAnnotation.eventIds));
        }
        keyField.setText(currentAnnotation.key);
    }

    private void advanceAnnotation()
    {
        spinner.setValue((Integer)spinner.getValue()+1);
        selectButton.doClick();
    }

    private void save()
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(currentAnnotation.key + ".salign");
            fos.write(outText.getText().trim().getBytes());
            fos.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        advanceAnnotation();
    }

    private void readInputFile()
    {
        String[] lines = Utils.readLines(inputFile);
        annotations = new ArrayList<Annotation>(lines.length);
        for(String line : lines)
        {
            annotations.add(preProcess(line));           
        }
    }

    private Annotation preProcess(String line)
    {
        String ref = "", tr = "", key;
        List<Integer> eventIds = new ArrayList<Integer>();
        String []chunks = line.split("\t");
        key = stripFilename(chunks[0], "text:");
        int pos = 2;
        while(pos < chunks.length && !chunks[pos].contains("True"))
        {
            String chunk = chunks[pos].replaceAll("[\\*]?\\[TRACK0\\]", "").trim(); // preprocess a bit
            int index1 = chunk.indexOf("("), index2 = chunk.indexOf(")");            
            ref += chunk + "\n\n";
            if(index1>0)
                eventIds.add(Integer.valueOf(chunk.substring(index1+1, index2).trim()));
            pos++;
        } // while        
        return new Annotation(key, readFile(key + ".text"), ref, eventIds);
    }

    private String readEventsFile(String path, List<Integer> eventIds)
    {
        String out = "";
        for(String line : Utils.readLines(path))
        {
            int index1 = line.indexOf(":");
            int index2 = line.indexOf("\t");
            int id = Integer.valueOf(line.substring(index1+1, index2));
            if(eventIds.contains(id))
                out += line + "\n";
        }
        return out;
    }

    private String readFile(String path)
    {
        String f = "";
        for(String s : Utils.readLines(path))
            f += s + "\n";
        return f;
    }

    /**
     * Convert a string that contains fields and their values into a map.
     * @param in the input string that has the following format: field1=value1,field2=value2,...
     * @return
     */
    private HashMap<String, String> fieldsToMap(String in)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        for(String s: in.split(","))
        {
            String[] token = s.split("=");
            map.put(token[0], token[1]);
        }
        return map;
    }

    private String stripFilename(String str, String ext)
    {
        return str.replace("."+ext, "");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        outText = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        refText = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        keyField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        trueText = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        spinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        selectButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        undoItem = new javax.swing.JMenuItem();
        redoItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelComponentResized(evt);
            }
        });

        outText.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(outText);

        splitPane.setRightComponent(jScrollPane1);

        refText.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane2.setViewportView(refText);

        splitPane.setLeftComponent(jScrollPane2);

        jLabel3.setText("Reference Text");

        jLabel4.setText("Output Text");

        jLabel5.setText("Annotation Key:");

        keyField.setEditable(false);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 642, Short.MAX_VALUE)
                        .addComponent(jLabel4))
                    .addGroup(panelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(4, 4, 4)
                        .addComponent(keyField, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(161, 161, 161)))
                .addContainerGap())
            .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(splitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 824, Short.MAX_VALUE))
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(keyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addContainerGap(293, Short.MAX_VALUE))
            .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelLayout.createSequentialGroup()
                    .addGap(65, 65, 65)
                    .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                    .addGap(22, 22, 22)))
        );

        trueText.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setViewportView(trueText);

        jLabel2.setText("True Text");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap(765, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 824, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap(162, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addContainerGap(23, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        spinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), null, Integer.valueOf(200), Integer.valueOf(1)));

        jLabel1.setText("Annotation : ");

        selectButton.setText("Select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        undoItem.setText("Undo");
        jMenu2.add(undoItem);

        redoItem.setText("Redo");
        jMenu2.add(redoItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 576, Short.MAX_VALUE)
                        .addComponent(saveButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButton)
                    .addComponent(saveButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectButtonActionPerformed
    {//GEN-HEADEREND:event_selectButtonActionPerformed
        showAnnotation();
    }//GEN-LAST:event_selectButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
    {//GEN-HEADEREND:event_saveButtonActionPerformed
        save();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void panelComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_panelComponentResized
    {//GEN-HEADEREND:event_panelComponentResized
        splitPane.setDividerLocation(0.5);
    }//GEN-LAST:event_panelComponentResized

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                        // Set cross-platform Java L&F (also called "Metal")
                    UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
                }
                catch(Exception e){}
                new SemanticAnnotation().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField keyField;
    private javax.swing.JTextPane outText;
    private javax.swing.JPanel panel;
    private javax.swing.JMenuItem redoItem;
    private javax.swing.JTextPane refText;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JSpinner spinner;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTextPane trueText;
    private javax.swing.JMenuItem undoItem;
    // End of variables declaration//GEN-END:variables

    class Annotation
    {
        String key, trueText, refText;
        List<Integer> eventIds;

        public Annotation(String key, String trueText, String refText, List<Integer> eventIds)
        {
            this.key = key;
            this.trueText = trueText;
            this.refText = refText;
            this.eventIds = eventIds;
        }

    }
}
