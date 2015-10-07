/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoginPanel.java
 *
 * Created on 2011/06/17, 9:39:25
 */

package open.dolphin.impl.login;

/**
 *
 * @author kazushi
 */
public class LoginPanelUser extends javax.swing.JPanel {

    /** Creates new form LoginPanel */
    public LoginPanelUser() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        settingBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        loginBtn = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        passwordFld = new javax.swing.JPasswordField();
        usersCmb = new javax.swing.JComboBox();

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/open/dolphin/resources/images/splash.jpg"))); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("open/dolphin/impl/login/resources/LoginPanelUser"); // NOI18N
        jLabel3.setText(bundle.getString("jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(bundle.getString("jLabel3.toolTipText")); // NOI18N

        jLabel4.setText(bundle.getString("jLabel4.text")); // NOI18N

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));

        settingBtn.setText(bundle.getString("settingBtn.text")); // NOI18N
        settingBtn.setToolTipText(""); // NOI18N
        jPanel1.add(settingBtn);

        cancelBtn.setText(bundle.getString("cancelBtn.text")); // NOI18N
        cancelBtn.setToolTipText("");
        jPanel1.add(cancelBtn);

        loginBtn.setText(bundle.getString("loginBtn.text")); // NOI18N
        jPanel1.add(loginBtn);

        progressBar.setPreferredSize(new java.awt.Dimension(146, 10));
        progressBar.setSize(new java.awt.Dimension(146, 10));

        usersCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                            .add(usersCmb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 172, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(passwordFld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 172, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {passwordFld, progressBar, usersCmb}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(usersCmb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel4)
                            .add(passwordFld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(43, 43, 43)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton loginBtn;
    private javax.swing.JPasswordField passwordFld;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton settingBtn;
    private javax.swing.JComboBox usersCmb;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JButton getCancelBtn() {
        return cancelBtn;
    }

    public javax.swing.JButton getLoginBtn() {
        return loginBtn;
    }

    public javax.swing.JPasswordField getPasswordField() {
        return passwordFld;
    }

    public javax.swing.JProgressBar getProgressBar() {
        return progressBar;
    }

    public javax.swing.JButton getSettingBtn() {
        return settingBtn;
    }

    public javax.swing.JComboBox getFacilityCmb() {
        return null;
    }

    public javax.swing.JComboBox getUsersCmb() {
        return usersCmb;
    }
}
