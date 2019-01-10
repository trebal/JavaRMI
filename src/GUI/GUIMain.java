package GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUIMain extends JFrame implements ActionListener {

    public GUIMain()
    {
        super();
        initialize();
    }

    private void initialize()
    {
        this.setTitle("Hello world");
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public static void main(String[] args)
    {
        GUIMain mainWindow = new GUIMain();
        mainWindow.setVisible(true);
        mainWindow.setButtons();
    }

    private void setButtons()
    {
        JButton test = new JButton();
        test.setText("Working");
        test.setBounds(100,100,200,15);
        this.add(test);
    }
}
