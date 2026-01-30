package ingstudios.turtlebrowse;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainWindow extends JFrame {
    public MainWindow() {
        super("Turtlebrowse");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        setSize(1200, 800);
        setLocationRelativeTo(null);

        root.add(new JLabel("Hello Turtlebrowse"), BorderLayout.CENTER);
    }

    /*
    private void setIconImage() {
        // Set icon image in the future
    }
    */
}
