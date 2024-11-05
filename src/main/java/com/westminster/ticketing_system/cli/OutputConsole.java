package com.westminster.ticketing_system.cli;

import javax.swing.*;
import java.awt.*;

public class OutputConsole extends JFrame {
    private final JTextArea textArea;
    private static OutputConsole instance;

    private OutputConsole() {
        setTitle("Simulation Output");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane);
        setLocationRelativeTo(null);
    }

    public static OutputConsole getInstance() {
        if (instance == null) {
            instance = new OutputConsole();
        }
        return instance;
    }

    public void println(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> textArea.setText(""));
    }
}