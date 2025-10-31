
package org.rosetta.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class App {

    private JFrame frame;
    private JTextField projectPath;
    private JTextField outDirField;
    private JTextField cliPathField;
    private JComboBox<String> langBox;
    private JTextField metricsField;
    private JCheckBox jsonOutput;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().start());
    }

    private void start() {
        frame = new JFrame("Rosetta GUI — CLI Frontend");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(720, 320);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12,12,12,12));

        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildBottom(), BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;

        gc.gridx=0; gc.gridy=r; p.add(new JLabel("Project root:"), gc);
        gc.gridx=1; gc.weightx=1.0; projectPath = new JTextField(); p.add(projectPath, gc);
        gc.gridx=2; gc.weightx=0; JButton br1 = new JButton("Browse…"); br1.addActionListener(this::chooseDir); p.add(br1, gc);
        r++;

        gc.gridx=0; gc.gridy=r; p.add(new JLabel("Output dir:"), gc);
        gc.gridx=1; gc.weightx=1.0; outDirField = new JTextField("rosetta-out"); p.add(outDirField, gc);
        gc.gridx=2; JButton br2 = new JButton("Browse…"); br2.addActionListener(this::chooseOut); p.add(br2, gc);
        r++;

        gc.gridx=0; gc.gridy=r; p.add(new JLabel("Rosetta CLI path:"), gc);
        gc.gridx=1; gc.weightx=1.0; cliPathField = new JTextField(""); p.add(cliPathField, gc);
        gc.gridx=2; JButton br3 = new JButton("Browse…"); br3.addActionListener(this::chooseExe); p.add(br3, gc);
        r++;

        gc.gridx=0; gc.gridy=r; p.add(new JLabel("Language:"), gc);
        gc.gridx=1; langBox = new JComboBox<>(new String[] {"java","c","cpp","c-cpp"}); p.add(langBox, gc);
        r++;

        gc.gridx=0; gc.gridy=r; p.add(new JLabel("Metrics (comma-separated):"), gc);
        gc.gridx=1; metricsField = new JTextField("LOC,MCCABE,FAN_IN,FAN_OUT,CALLS"); p.add(metricsField, gc);
        r++;

        gc.gridx=0; gc.gridy=r; p.add(new JLabel("JSON output:"), gc);
        gc.gridx=1; jsonOutput = new JCheckBox(); p.add(jsonOutput, gc);
        r++;

        return p;
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton run = new JButton("Run");
        run.addActionListener(this::runCli);
        JButton help = new JButton("Help");
        help.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Choose project, output, CLI path, language and metrics, then click Run."));
        p.add(help); p.add(run);
        return p;
    }

    private void chooseDir(ActionEvent e) {
        JFileChooser ch = new JFileChooser();
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (ch.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            projectPath.setText(ch.getSelectedFile().getAbsolutePath());
        }
    }
    private void chooseOut(ActionEvent e) {
        JFileChooser ch = new JFileChooser();
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (ch.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            outDirField.setText(ch.getSelectedFile().getAbsolutePath());
        }
    }
    private void chooseExe(ActionEvent e) {
        JFileChooser ch = new JFileChooser();
        if (ch.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            cliPathField.setText(ch.getSelectedFile().getAbsolutePath());
        }
    }

    private void runCli(ActionEvent e) {
        String proj = projectPath.getText().trim();
        String out = outDirField.getText().trim();
        String cli = cliPathField.getText().trim();
        String lang = String.valueOf(langBox.getSelectedItem());
        String metrics = metricsField.getText().trim();
        boolean json = jsonOutput.isSelected();

        if (proj.isEmpty() || cli.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Project root and CLI path are required.");
            return;
        }
        if (out.isEmpty()) out = new File(proj, "rosetta-out").getAbsolutePath();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                cli,
                "--project", proj,
                "--lang", lang,
                "--metrics", metrics,
                "--out", out,
                json ? "--json" : "--tsv"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line; StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            int code = p.waitFor();
            JTextArea ta = new JTextArea(sb.toString(), 20, 80);
            ta.setEditable(false);
            JOptionPane.showMessageDialog(frame, new JScrollPane(ta), "CLI output (exit " + code + ")", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }
}
