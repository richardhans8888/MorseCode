import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.sound.sampled.*;

public class GUI extends JFrame {
    private BufferedImage backgroundImage;
    private JTextArea inputArea, outputArea, captionArea;
    private morseMap morseMap;
    private MorseBinaryTree morseTree; // Assuming MorseBinaryTree class exists
    private boolean useMap = true;
    private Thread voiceThread;

    private boolean engToMorse = true;
    private volatile boolean stopFlash = false;
    private JPanel flashLight;
    private JLabel flashStatus;
    private JSpinner dotSpinner;
    private DefaultListModel<String> historyModel;
    private JList<String> historyList;
    private JPanel historyPanel;
    private JCheckBox toggleCaptions;
    private JScrollPane captionScroll;
    private JLabel morseGuideLabel;
    private JPanel settingsPanel;

    public GUI() {
        setTitle("Morse Code Translator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setUndecorated(false);

        try {
            backgroundImage = ImageIO.read(new File("Assets/Background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            backgroundImage = null;
        }

        morseMap = new morseMap();
        morseTree = new MorseBinaryTree(); // Initialize MorseBinaryTree
        historyModel = new DefaultListModel<>();

        RoundedBackgroundPanel bgPanel = new RoundedBackgroundPanel(backgroundImage, 40);
        bgPanel.setLayout(new GridBagLayout());
        bgPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        setContentPane(bgPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;

        // Title label
        JLabel title = new JLabel("Morse Code Translator");
        title.setFont(new Font("Helvetica Neue", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0.1;
        bgPanel.add(title, gbc);

        // Settings button
        JButton gearButton = new JButton("\u2699");
        gearButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 36));
        gearButton.setForeground(Color.WHITE);
        gearButton.setContentAreaFilled(false);
        gearButton.setFocusPainted(false);
        gearButton.setBorder(null);
        gearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        bgPanel.add(gearButton, gbc);

        // --- Switch Data Structure Button (DS Button - Styled with blue and pop-up) ---
        JButton switchDSButton = new JButton("DS");
        switchDSButton.setFont(new Font("Arial", Font.BOLD, 18));
        switchDSButton.setBackground(new Color(0, 120, 215)); // Changed background to blue
        switchDSButton.setForeground(Color.WHITE); // Keep text white for contrast
        switchDSButton.setOpaque(true);
        switchDSButton.setFocusPainted(false);
        switchDSButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        switchDSButton.setPreferredSize(new Dimension(50, 50));
        switchDSButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 10, 20, 20);
        bgPanel.add(switchDSButton, gbc);
        gbc.insets = new Insets(20, 20, 20, 20); // Reset insets

        // Add ActionListener for pop-up message
        switchDSButton.addActionListener(e -> {
            useMap = !useMap;
            String method = useMap ? "Map" : "Tree";
            JOptionPane.showMessageDialog(this, "Data structure changed to: " + method); // Added pop-up
        });


        // Input Text Area
        inputArea = createTransparentTextArea();
        JScrollPane inputScroll = new JScrollPane(inputArea);
        styleScrollPane(inputScroll);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.4;
        gbc.weighty = 0.7;
        bgPanel.add(inputScroll, gbc);

        // Swap button
        JButton swapButton = new JButton("\u21C4");
        swapButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 48));
        swapButton.setForeground(Color.WHITE);
        swapButton.setContentAreaFilled(false);
        swapButton.setFocusPainted(false);
        swapButton.setBorder(null);
        swapButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0.7;
        bgPanel.add(swapButton, gbc);

        // Output Text Area
        outputArea = createTransparentTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        styleScrollPane(outputScroll);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.4;
        gbc.weighty = 0.7;
        bgPanel.add(outputScroll, gbc);

        // Captions area
        captionArea = createTransparentTextArea();
        captionArea.setFont(new Font("Helvetica Neue", Font.ITALIC, 16));
        captionArea.setForeground(Color.GRAY);
        captionArea.setEditable(false);
        captionScroll = new JScrollPane(captionArea);
        styleScrollPane(captionScroll);
        captionScroll.setBounds(20, 610, 484, 40);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 0.4;
        gbc.weighty = 0.1;
        bgPanel.add(captionScroll, gbc);

        // Toggle captions checkbox
        toggleCaptions = new JCheckBox("Show Captions");
        toggleCaptions.setFont(new Font("Dialog", Font.PLAIN, 14));
        toggleCaptions.setForeground(Color.WHITE);
        toggleCaptions.setOpaque(false);
        toggleCaptions.setSelected(true);

        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        bgPanel.add(toggleCaptions, gbc);

        // Flash light panel
        flashLight = new JPanel();
        flashLight.setBackground(Color.DARK_GRAY);
        flashLight.setBounds(550, 540, 100, 40);

        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        bgPanel.add(flashLight, gbc);

        // Flash status label
        flashStatus = new JLabel("");
        flashStatus.setFont(new Font("Dialog", Font.BOLD, 16));
        flashStatus.setForeground(Color.YELLOW);

        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        bgPanel.add(flashStatus, gbc);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 6, 30, 0));
        buttonsPanel.setOpaque(false);

        JButton lightsBtn = createColoredButton("Flash", Color.WHITE, Color.BLACK);
        JButton importBtn = createColoredButton("Import", new Color(0, 69, 138), Color.BLACK);
        JButton soundBtn = createColoredButton("Sound", new Color(115, 97, 238), Color.BLACK);
        JButton historyBtn = createColoredButton("History", new Color(51, 26, 194), Color.BLACK);
        JButton translateBtn = createColoredButton("Translate", new Color(0, 150, 136), Color.BLACK);
        JButton clearBtn = createColoredButton("Clear", new Color(244, 67, 54), Color.BLACK);

        buttonsPanel.add(lightsBtn);
        buttonsPanel.add(importBtn);
        buttonsPanel.add(soundBtn);
        buttonsPanel.add(historyBtn);
        buttonsPanel.add(translateBtn);
        buttonsPanel.add(clearBtn);

        historyBtn.addActionListener(e -> {
            historyPanel.setVisible(!historyPanel.isVisible());
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0.2;
        bgPanel.add(buttonsPanel, gbc);

        // Dot duration spinner
        JPanel spinnerPanel = new JPanel();
        spinnerPanel.setOpaque(false);
        spinnerPanel.add(new JLabel("Dot Duration (ms):"));
        dotSpinner = new JSpinner(new SpinnerNumberModel(200, 50, 1000, 50));
        spinnerPanel.add(dotSpinner);

        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        bgPanel.add(spinnerPanel, gbc);

// 📜 History Panel Setup
        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(new Color(0, 0, 0, 180));
        historyPanel.setVisible(true); // Hidden by default

        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Consolas", Font.PLAIN, 14));
        historyList.setForeground(Color.WHITE);
        historyList.setBackground(Color.DARK_GRAY);

        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

// 🧱 Layout positioning (IMPORTANT: this adds to bgPanel!)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        bgPanel.add(historyPanel, gbc);

        // Morse guide label
        try {
            BufferedImage morseGuideImage = ImageIO.read(new File("Assets/morsecod.jpg"));
            Image scaledImg = morseGuideImage.getScaledInstance(400, 300, Image.SCALE_SMOOTH);
            morseGuideLabel = new JLabel(new ImageIcon(scaledImg));
        } catch (IOException e) {
            morseGuideLabel = new JLabel("Morse Guide Image Not Found");
        }
        morseGuideLabel.setBounds(700, 200, 400, 300);
        morseGuideLabel.setVisible(false);
        bgPanel.add(morseGuideLabel);

// ⚙ Settings Panel as a Popup Dialog
        gearButton.addActionListener(e -> {
            // Create the settings dialog
            JDialog settingsDialog = new JDialog(this, "Settings", true);
            settingsDialog.setSize(300, 250);
            settingsDialog.setLocationRelativeTo(this); // Center it on the main window
            settingsDialog.setLayout(new BoxLayout(settingsDialog.getContentPane(), BoxLayout.Y_AXIS));
            settingsDialog.getContentPane().setBackground(new Color(30, 30, 30)); // Dark theme

            // ⚙ Label
            JLabel settingsLabel = new JLabel("⚙ Settings");
            settingsLabel.setForeground(Color.WHITE);
            settingsLabel.setFont(new Font("Dialog", Font.BOLD, 20));
            settingsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 📘 Morse Guide toggle that opens image popup and closes settings
            JButton morseGuideButton = new JButton("📘 Open Morse Guide");
            morseGuideButton.setFont(new Font("Dialog", Font.PLAIN, 16));
            morseGuideButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            morseGuideButton.addActionListener(ev -> {
                settingsDialog.dispose();

                JDialog guideDialog = new JDialog(this, "Morse Guide Chart", false);
                guideDialog.setSize(500, 400);
                guideDialog.setLocationRelativeTo(this);

                try {
                    BufferedImage morseImage = ImageIO.read(new File("Assets/Morse.jpg"));
                    Image scaled = morseImage.getScaledInstance(480, 360, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                    guideDialog.add(imageLabel);
                    guideDialog.setVisible(true);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not load Morse Guide image.");
                }
            });

            // ❓ About & Help button
            JButton aboutButton = new JButton("❓ About & Help");
            aboutButton.setFont(new Font("Dialog", Font.BOLD, 14));
            aboutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            aboutButton.addActionListener(ev -> showAboutWindow());

            // Add components to the dialog
            settingsDialog.add(Box.createVerticalStrut(20));
            settingsDialog.add(settingsLabel);
            settingsDialog.add(Box.createVerticalStrut(15));
            settingsDialog.add(morseGuideButton);
            settingsDialog.add(Box.createVerticalStrut(20));
            settingsDialog.add(aboutButton);
            settingsDialog.add(Box.createVerticalGlue());

            // Show the dialog
            settingsDialog.setVisible(true);
        });

        // Event listeners

        // --- Swap button action listener ---
        swapButton.addActionListener(e -> {
            engToMorse = !engToMorse;
            String inputText = inputArea.getText().trim();
            String outputText = outputArea.getText().trim();

            inputArea.setText(outputText);
            outputArea.setText(engToMorse ? morseMap.toMorse(outputText) : morseMap.fromMorse(outputText));

            toggleCaptions.setVisible(engToMorse);
            captionScroll.setVisible(engToMorse && toggleCaptions.isSelected());
        });

        // --- Translate button action listener (Uses useMap for DS selection) ---
        translateBtn.addActionListener(e -> {
            String input = inputArea.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter text to translate.");
                return;
            }

            String result;
            if (engToMorse) {
                result = useMap ? morseMap.toMorse(input) : morseTree.toMorse(input);
            } else {
                result = useMap ? morseMap.fromMorse(input) : morseTree.fromMorse(input);
            }
            outputArea.setText(result);

            if (engToMorse) {
                captionArea.setText(input.replace(" ", " / "));
            } else {
                captionArea.setText("");
            }

            addToHistory(input, result);
        });

        clearBtn.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
            captionArea.setText("");
        });

        lightsBtn.addActionListener(e -> {
            String morseText = engToMorse ? outputArea.getText() : inputArea.getText();
            if (morseText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No Morse code to flash.");
                return;
            }

            if (lightsBtn.getText().equals("Flash")) {
                stopFlash = false;
                int dotDuration = (int) dotSpinner.getValue();
                lightsBtn.setText("Stop");
                new Thread(() -> flashMorse(morseText, dotDuration)).start();
            } else {
                stopFlash = true;
                lightsBtn.setText("Flash");
            }
        });

        importBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    String content = new String(java.nio.file.Files.readAllBytes(selectedFile.toPath())).trim();
                    inputArea.setText(content);

                    String translated = engToMorse
                            ? (useMap ? morseMap.toMorse(content) : morseTree.toMorse(content))
                            : (useMap ? morseMap.fromMorse(content) : morseTree.fromMorse(content));

                    outputArea.setText(translated);

                    if (engToMorse) {
                        captionArea.setText(content.replace(" ", " / "));
                    }

                    addToHistory(content, translated);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to read file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        soundBtn.addActionListener(e -> {
            String morseText = engToMorse ? outputArea.getText() : inputArea.getText();
            if (morseText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No Morse code to play.");
                return;
            }

            int dotDuration = (int) dotSpinner.getValue();

            new Thread(() -> playMorseSound(morseText, dotDuration)).start();
        });



        // Real-time translation (Uses useMap for DS selection)
//        inputArea.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyReleased(KeyEvent e) {
//                String inputText = inputArea.getText().trim();
//
//                if (engToMorse) {
//                    if (!inputText.isEmpty()) {
//                        String morseText = useMap ? morseMap.toMorse(inputText) : morseTree.toMorse(inputText);
//                        outputArea.setText(morseText);
//                        captionArea.setText(inputText.replace(" ", " / "));
//                    } else {
//                        outputArea.setText("");
//                        captionArea.setText("");
//                    }
//                } else {
//                    if (!inputText.isEmpty()) {
//                        String englishText = useMap ? morseMap.fromMorse(inputText) : morseTree.fromMorse(inputText);
//                        outputArea.setText(englishText);
//                    } else {
//                        outputArea.setText("");
//                    }
//                }
//            }
//        });

        // Make frame rounded
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 0, 0));
            }
        });
    }


    private void flashMorse(String morse, int dotDuration) {
        int dashDuration = dotDuration * 3;
        int spaceDuration = dotDuration * 2;

        SwingUtilities.invokeLater(() -> {
            flashStatus.setText("⚠ Flashing...");
            flashStatus.setForeground(Color.YELLOW);
        });

        for (char c : morse.toCharArray()) {
            if (stopFlash) break;
            try {
                switch (c) {
                    case '.':
                        flashLight(Color.YELLOW, dotDuration);
                        break;
                    case '-':
                        flashLight(Color.YELLOW, dashDuration);
                        break;
                    case ' ':
                        Thread.sleep(spaceDuration);
                        break;
                    case '/':
                        Thread.sleep(dotDuration * 7);
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        SwingUtilities.invokeLater(() -> {
            flashLight.setBackground(Color.DARK_GRAY);
            flashStatus.setText(stopFlash ? "⛔ Flashing Stopped" : "✅ Done Flashing");
            flashStatus.setForeground(stopFlash ? Color.RED : Color.GREEN);
        });
    }

    private void flashLight(Color color, int duration) throws InterruptedException {
        SwingUtilities.invokeLater(() -> flashLight.setBackground(color));
        Thread.sleep(duration);
        SwingUtilities.invokeLater(() -> flashLight.setBackground(Color.DARK_GRAY));
        Thread.sleep(150);
    }

    private void addToHistory(String inputText, String translatedText) {
        historyModel.addElement(engToMorse ? "ENGLISH: " + inputText : "MORSE: " + inputText);
        historyModel.addElement(engToMorse ? "MORSE:   " + translatedText : "ENGLISH: " + translatedText);
        historyModel.addElement("------------------------------");
    }

    private void showAboutWindow() {
        JDialog aboutDialog = new JDialog(this, "About & Help", true);
        aboutDialog.setSize(300, 400);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setLayout(new BorderLayout());

        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
        aboutPanel.setBackground(new Color(30, 30, 30));
        aboutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel aboutTitle = new JLabel("📖 About this App");
        aboutTitle.setFont(new Font("Roboto", Font.BOLD, 16));
        aboutTitle.setForeground(Color.WHITE);
        aboutPanel.add(aboutTitle);

        JLabel aboutText = new JLabel("<html>A simple Morse Code Translator.<br>Created by Richard, Vickel, and Timothy.</html>");
        aboutText.setFont(new Font("Roboto", Font.PLAIN, 12));
        aboutText.setForeground(Color.LIGHT_GRAY);
        aboutPanel.add(aboutText);

        aboutPanel.add(Box.createVerticalStrut(10));

        JLabel faqTitle = new JLabel("❓ FAQ & Help");
        faqTitle.setFont(new Font("Roboto", Font.BOLD, 14));
        faqTitle.setForeground(Color.WHITE);
        aboutPanel.add(faqTitle);

        JLabel faqText = new JLabel("<html>Q: How do I switch modes?<br>A: Use the ↔ button!</html>");
        faqText.setFont(new Font("Roboto", Font.PLAIN, 12));
        faqText.setForeground(Color.LIGHT_GRAY);
        aboutPanel.add(faqText);

        aboutDialog.add(aboutPanel, BorderLayout.CENTER);
        aboutDialog.setVisible(true);
    }

    private JTextArea createTransparentTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Helvetica Neue", Font.PLAIN, 26));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setForeground(new Color(255, 255, 255, 210));
        area.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 2, true));
        return area;
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private JButton createColoredButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Helvetica Neue", Font.PLAIN, 22));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        btn.setPreferredSize(new Dimension(150, 100));
        return btn;
    }

    class RoundedBackgroundPanel extends JPanel {
        private BufferedImage image;
        private int cornerRadius;

        public RoundedBackgroundPanel(BufferedImage image, int cornerRadius) {
            this.image = image;
            this.cornerRadius = cornerRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.setClip(clip);

            if (image != null) {
                g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(getBackground());
                g2.fill(clip);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
    private void playBeep(int duration) {
        float frequency = 800f; // Hz
        int sampleRate = 44100;
        byte[] buf = new byte[duration * sampleRate / 1000];

        for (int i = 0; i < buf.length; i++) {
            double angle = 2.0 * Math.PI * i / (sampleRate / frequency);
            buf[i] = (byte) (Math.sin(angle) * 127);
        }

        try {
            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            sdl.write(buf, 0, buf.length);
            sdl.drain();
            sdl.stop();
            sdl.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Audio playback error.");
        }
    }

    private void playMorseSound(String morseCode, int dotDuration) {
        int dashDuration = dotDuration * 3;
        int spaceDuration = dotDuration * 2;

        for (char c : morseCode.toCharArray()) {
            if (c == '.') {
                playBeep(dotDuration);
            } else if (c == '-') {
                playBeep(dashDuration);
            } else if (c == ' ') {
                try { Thread.sleep(spaceDuration); } catch (InterruptedException ignored) {}
            } else if (c == '/') {
                try { Thread.sleep(dotDuration * 7); } catch (InterruptedException ignored) {}
            }

            try { Thread.sleep(dotDuration); } catch (InterruptedException ignored) {}
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}

