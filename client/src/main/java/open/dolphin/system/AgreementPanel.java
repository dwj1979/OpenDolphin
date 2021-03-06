package open.dolphin.system;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import open.dolphin.client.ClientContext;

/**
 * AgreementPanel
 * 
 * @author Minagawa,Kazushi
 */
public final class AgreementPanel extends JPanel {

    public static final String AGREE_PROP = "agreeProp";
    // モデル
    private AgreementModel model;
    // 束縛サポート
    private final PropertyChangeSupport boundSupport = new PropertyChangeSupport(this);
    // GUI コンポーネント
    private JTextArea agreeArea;
    private JRadioButton agreeBtn;
    private JRadioButton disagreeBtn;

    /**
     * AgreementPanelを生成する。
     * @param model AgreementModel
     */
    public AgreementPanel(AgreementModel model) {
        initialize();
        connect();
        setModel(model);
    }

    public AgreementModel getModel() {
        return model;
    }

    public void setModel(AgreementModel model) {
        this.model = model;
        if (model != null) {
            agreeArea.setText(model.getAgreeText());
            agreeBtn.setSelected(model.isAgree());
            disagreeBtn.setSelected(!model.isAgree());
        }
    }

    public boolean isAgree() {
        return getModel().isAgree();
    }

    public void setAgree(boolean newAgree) {
        boolean old = getModel().isAgree();
        getModel().setAgree(newAgree);
        boundSupport.firePropertyChange(AGREE_PROP, old, getModel().isAgree());
    }

    public void addAgreePropertyListener(PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(AGREE_PROP, l);
    }

    public void removeAgreePropertyListener(PropertyChangeListener l) {
        boundSupport.removePropertyChangeListener(AGREE_PROP, l);
    }

    private void initialize() {

        agreeArea = new JTextArea();
        agreeArea.setEditable(false);
        agreeArea.setLineWrap(true);
        agreeArea.setMargin(new Insets(10, 10, 10, 10));

        java.util.ResourceBundle bundle = ClientContext.getMyBundle(AgreementPanel.class);
        agreeBtn = new JRadioButton(bundle.getString("actionText.agree"));
        disagreeBtn = new JRadioButton(bundle.getString("actionText.disagree"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(agreeBtn);
        bg.add(disagreeBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(agreeBtn);
        btnPanel.add(disagreeBtn);

        JScrollPane scroller = new JScrollPane(agreeArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.setLayout(new BorderLayout());
        this.add(scroller, BorderLayout.CENTER);
        this.add(btnPanel, BorderLayout.SOUTH);
    }

    private void connect() {
        ActionListener agreeListener = (ActionEvent e) -> {
            setAgree(agreeBtn.isSelected());
        };
        agreeBtn.addActionListener(agreeListener);
        disagreeBtn.addActionListener(agreeListener);
    }
}
