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

public class GUI extends JFrame {
    private BufferedImage backgroundImage;
    private JTextArea inputArea, outputArea, captionArea;
    private morseMap morseMap;
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
        setUndecorated(true);

        try {
            backgroundImage = ImageIO.read(new File("Assets/Background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            backgroundImage = null;
        }

        morseMap = new morseMap();
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
        JButton voiceBtn = createColoredButton("Voice", new Color(0, 69, 138), Color.WHITE);
        JButton soundBtn = createColoredButton("Sound", new Color(115, 97, 238), Color.WHITE);
        JButton historyBtn = createColoredButton("History", new Color(51, 26, 194), Color.WHITE);
        JButton translateBtn = createColoredButton("Translate", new Color(0, 150, 136), Color.WHITE);
        JButton clearBtn = createColoredButton("Clear", new Color(244, 67, 54), Color.WHITE);

        buttonsPanel.add(lightsBtn);
        buttonsPanel.add(voiceBtn);
        buttonsPanel.add(soundBtn);
        buttonsPanel.add(historyBtn);
        buttonsPanel.add(translateBtn);
        buttonsPanel.add(clearBtn);

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

        // History panel
        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(new Color(0, 0, 0, 180));
        historyPanel.setVisible(false);

        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Consolas", Font.PLAIN, 14));
        historyList.setForeground(Color.WHITE);
        historyList.setBackground(Color.DARK_GRAY);

        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0.4;
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

        // Settings panel
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBounds(1100, 50, 220, 500);
        settingsPanel.setOpaque(false);
        settingsPanel.setVisible(false);

        JLabel settingsLabel = new JLabel("⚙ Settings");
        settingsLabel.setForeground(Color.WHITE);
        settingsLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        settingsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox dictToggle = new JCheckBox("Morse Guide");
        dictToggle.setFont(new Font("Dialog", Font.PLAIN, 18));
        dictToggle.setForeground(Color.WHITE);
        dictToggle.setOpaque(false);
        dictToggle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox soundToggle = new JCheckBox("Sound");
        soundToggle.setFont(new Font("Dialog", Font.PLAIN, 18));
        soundToggle.setForeground(Color.WHITE);
        soundToggle.setOpaque(false);
        soundToggle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton aboutButton = new JButton("❓ About & Help");
        aboutButton.setFont(new Font("Dialog", Font.BOLD, 14));
        aboutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        settingsPanel.add(settingsLabel);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(dictToggle);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(soundToggle);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(aboutButton);
        bgPanel.add(settingsPanel);

        // Event listeners
        swapButton.addActionListener(e -> {
            engToMorse = !engToMorse;
            String inputText = inputArea.getText().trim();
            String outputText = outputArea.getText().trim();

            inputArea.setText(outputText);
            outputArea.setText(engToMorse ? morseMap.to_Morse(outputText) : morseMap.from_Morse(outputText));

            toggleCaptions.setVisible(engToMorse);
            captionScroll.setVisible(engToMorse && toggleCaptions.isSelected());
        });

        translateBtn.addActionListener(e -> {
            String inputText = inputArea.getText().trim();
            if (inputText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter text to translate.");
                return;
            }

            String translated = engToMorse ? morseMap.to_Morse(inputText) : morseMap.from_Morse(inputText);
            outputArea.setText(translated);

            if (engToMorse) {
                captionArea.setText(inputText.replace(" ", " / "));
            }

            addToHistory(inputText, translated);
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


        soundBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Sound feature coming soon!");
        });

        historyBtn.addActionListener(e -> {
            historyPanel.setVisible(!historyPanel.isVisible());
        });

        toggleCaptions.addActionListener(e -> {
            captionScroll.setVisible(toggleCaptions.isSelected());
        });

        dictToggle.addActionListener(e -> {
            morseGuideLabel.setVisible(dictToggle.isSelected());
        });

        gearButton.addActionListener(e -> {
            settingsPanel.setVisible(!settingsPanel.isVisible());
        });

        aboutButton.addActionListener(e -> showAboutWindow());

        // Real-time translation
//        inputArea.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyReleased(KeyEvent e) {
//                String inputText = inputArea.getText().trim();
//
//                if (engToMorse) {
//                    if (!inputText.isEmpty()) {
//                        String morseText = morseMap.to_Morse(inputText);
//                        outputArea.setText(morseText);
//                        captionArea.setText(inputText.replace(" ", " / "));
//                    } else {
//                        outputArea.setText("");
//                        captionArea.setText("");
//                    }
//                } else {
//                    if (!inputText.isEmpty()) {
//                        String englishText = morseMap.from_Morse(inputText);
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
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
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
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            GUI app = new GUI();
            app.setVisible(true);
        });
    }
}

