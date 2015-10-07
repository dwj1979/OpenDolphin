/*
 * AllergyView.java
 *
 * Created on 2007/11/29, 9:11
 */

package open.dolphin.client;

/**
 *
 * @author  kazm
 */
public class AllergyView extends javax.swing.JPanel {
    
    /** Creates new form AllergyView */
    public AllergyView() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        memoFld = new javax.swing.JTextField();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(table);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        memoFld.setEditable(false);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("open/dolphin/client/resources/AllergyView"); // NOI18N
        memoFld.setToolTipText(bundle.getString("AllergyView.memoFld.toolTipText")); // NOI18N
        memoFld.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        memoFld.setName("memoFld"); // NOI18N
        add(memoFld, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField memoFld;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

//    public javax.swing.JButton getAddBtn() {
//        return addBtn;
//    }
//
//    public javax.swing.JButton getDeleteBtn() {
//        return deleteBtn;
//    }
//
//    public javax.swing.JTextField getFactorFld() {
//        return factorFld;
//    }
//
//    public javax.swing.JTextField getIdentifiedFld() {
//        return identifiedFld;
//    }
//
//    public javax.swing.JTextField getMemoFld() {
//        return memoFld;
//    }
//
//    public javax.swing.JComboBox getReactionCombo() {
//        return reactionCombo;
//    }

    public javax.swing.JTable getTable() {
        return table;
    }

    public javax.swing.JTextField getMemoFld() {
        return memoFld;
    }
    
}
