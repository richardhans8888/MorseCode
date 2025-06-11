import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;


public class GUI extends JFrame {
    private BufferedImage backgroundImage;
    private JTextArea inputArea, outputArea, captionArea;
    private morseMap morseMap;
    private MorseBinaryTree morseTree;
    private boolean useMap = true;
    private boolean engToMorse = true;
    private volatile boolean stopFlash = false;
    private volatile boolean stopSound = false;
    private volatile boolean stopSync = false;
    private JPanel flashLight;
    private JLabel flashStatus;
    private JSpinner dotSpinner;
    private JSpinner flashSpeedSpinner;
    private DefaultListModel<String> historyModel;
    private JList<String> historyList;
    private JPanel historyPanel;
    private JCheckBox toggleCaptions;
    private JScrollPane captionScroll;
    private JLabel charCountLabel;
    private final int MAX_CHARS = 1000;
    private JLabel inputLabel;
    private JLabel outputLabel;

    // Buttons declared as fields to be accessed from action listeners
    private JButton lightsBtn;
    private JButton soundBtn;
    private JButton syncBtn;
    private JButton translateBtn;
    private JButton clearBtn;
    private JButton importBtn;
    private JButton historyBtn;
    private JButton gearButton;
    private JButton switchDSButton;
    private JButton swapButton;


    public GUI() {
        setTitle("Morse Code Translator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setUndecorated(false);

        try {
            backgroundImage = ImageIO.read(new File("Assets/Background.jpg"));
        } catch (IOException e) {
            System.err.println("Warning: Background.jpg not found in Assets folder.");
            backgroundImage = null;
        }

        morseMap = new morseMap();
        morseTree = new MorseBinaryTree();
        historyModel = new DefaultListModel<>();

        flashSpeedSpinner = new JSpinner(new SpinnerNumberModel(150, 50, 500, 10));

        RoundedBackgroundPanel bgPanel = new RoundedBackgroundPanel(backgroundImage, 40);
        bgPanel.setLayout(new GridBagLayout());
        bgPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        setContentPane(bgPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.BOTH;

        // --- Top Panel ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Morse Code Translator");
        title.setFont(new Font("Helvetica Neue", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(title, BorderLayout.CENTER);

        gearButton = new JButton("\u2699");
        gearButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 36));
        gearButton.setForeground(Color.WHITE);
        gearButton.setContentAreaFilled(false);
        gearButton.setFocusPainted(false);
        gearButton.setBorder(null);
        gearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topPanel.add(gearButton, BorderLayout.EAST);

        switchDSButton = new JButton("DS");
        switchDSButton.setFont(new Font("Arial", Font.BOLD, 18));
        switchDSButton.setBackground(new Color(0, 120, 215));
        switchDSButton.setForeground(Color.WHITE);
        switchDSButton.setOpaque(true);
        switchDSButton.setFocusPainted(false);
        switchDSButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        switchDSButton.setPreferredSize(new Dimension(50, 50));
        switchDSButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topPanel.add(switchDSButton, BorderLayout.WEST);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.weighty = 0.1;
        bgPanel.add(topPanel, gbc);

        // --- Input Panel ---
        inputArea = createTransparentTextArea();
        ((AbstractDocument) inputArea.getDocument()).setDocumentFilter(new CharacterLimitFilter());
        charCountLabel = new JLabel();
        updateCharCount();
        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateCharCount(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCharCount(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCharCount(); }
        });
        JScrollPane inputScroll = new JScrollPane(inputArea);
        styleScrollPane(inputScroll);
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        countPanel.setOpaque(false);
        charCountLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        charCountLabel.setForeground(Color.LIGHT_GRAY);
        countPanel.add(charCountLabel);
        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setOpaque(false);
        inputContainer.add(inputScroll, BorderLayout.CENTER);
        inputContainer.add(countPanel, BorderLayout.SOUTH);
        inputLabel = new JLabel("English");
        inputLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 20));
        inputLabel.setForeground(Color.WHITE);
        inputLabel.setBorder(new EmptyBorder(0, 5, 5, 0));
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false);
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputContainer, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 0.7;
        bgPanel.add(inputPanel, gbc);

        // --- Swap Button ---
        swapButton = new JButton("\u21C4");
        swapButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 48));
        swapButton.setForeground(Color.WHITE);
        swapButton.setContentAreaFilled(false);
        swapButton.setFocusPainted(false);
        swapButton.setBorder(null);
        swapButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.7;
        bgPanel.add(swapButton, gbc);

        // --- Output Panel ---
        outputArea = createTransparentTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        styleScrollPane(outputScroll);
        outputLabel = new JLabel("Morse");
        outputLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 20));
        outputLabel.setForeground(Color.WHITE);
        outputLabel.setBorder(new EmptyBorder(0, 5, 5, 0));
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setOpaque(false);
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(outputScroll, BorderLayout.CENTER);
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 0.7;
        bgPanel.add(outputPanel, gbc);

        // --- Bottom Left Controls ---
        captionArea = createTransparentTextArea();
        captionArea.setFont(new Font("Helvetica Neue", Font.ITALIC, 16));
        captionArea.setForeground(Color.GRAY);
        captionArea.setEditable(false);
        captionScroll = new JScrollPane(captionArea);
        styleScrollPane(captionScroll);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 0.4;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.NORTH;
        bgPanel.add(captionScroll, gbc);
        toggleCaptions = new JCheckBox("Show Captions");
        toggleCaptions.setFont(new Font("Dialog", Font.PLAIN, 14));
        toggleCaptions.setForeground(Color.WHITE);
        toggleCaptions.setOpaque(false);
        toggleCaptions.setSelected(true);
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        bgPanel.add(toggleCaptions, gbc);

        // --- Bottom Right Controls (Flash Area & Spinner) ---
        flashStatus = new JLabel("");
        flashStatus.setFont(new Font("Dialog", Font.BOLD, 18));
        flashStatus.setForeground(Color.YELLOW);
        flashStatus.setHorizontalAlignment(SwingConstants.CENTER);
        flashLight = new JPanel(new BorderLayout());
        flashLight.setBackground(Color.DARK_GRAY);
        flashLight.setPreferredSize(new Dimension(180, 80));
        flashLight.add(flashStatus, BorderLayout.CENTER);
        JPanel flashPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,0));
        flashPanel.setOpaque(false);
        flashPanel.add(flashLight);
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        bgPanel.add(flashPanel, gbc);
        JPanel spinnerPanel = new JPanel();
        spinnerPanel.setOpaque(false);
        spinnerPanel.add(new JLabel("Dot Duration (ms):"));
        dotSpinner = new JSpinner(new SpinnerNumberModel(200, 50, 1000, 50));
        spinnerPanel.add(dotSpinner);
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        bgPanel.add(spinnerPanel, gbc);

        // --- Main Buttons Panel ---
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 7, 20, 0));
        buttonsPanel.setOpaque(false);
        lightsBtn = createColoredButton("Flash", Color.WHITE, Color.BLACK);
        soundBtn = createColoredButton("Sound", new Color(115, 97, 238), Color.BLACK);
        syncBtn = createColoredButton("Sync", new Color(148, 0, 211), Color.WHITE);
        importBtn = createColoredButton("Import", new Color(0, 69, 138), Color.BLACK);
        historyBtn = createColoredButton("History", new Color(51, 26, 194), Color.BLACK);
        translateBtn = createColoredButton("Translate", new Color(0, 150, 136), Color.BLACK);
        clearBtn = createColoredButton("Clear", new Color(244, 67, 54), Color.BLACK);
        buttonsPanel.add(lightsBtn);
        buttonsPanel.add(soundBtn);
        buttonsPanel.add(syncBtn);
        buttonsPanel.add(importBtn);
        buttonsPanel.add(historyBtn);
        buttonsPanel.add(translateBtn);
        buttonsPanel.add(clearBtn);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0.2;
        bgPanel.add(buttonsPanel, gbc);

        // --- History Panel ---
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
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        bgPanel.add(historyPanel, gbc);

        setupActionListeners();
    }

    private void setupActionListeners(){
        gearButton.addActionListener(e -> {
            JDialog settingsDialog = new JDialog(this, "Settings", true);
            settingsDialog.setSize(350, 300);
            settingsDialog.setLocationRelativeTo(this);
            settingsDialog.setLayout(new BoxLayout(settingsDialog.getContentPane(), BoxLayout.Y_AXIS));
            settingsDialog.getContentPane().setBackground(new Color(30, 30, 30));
            JLabel settingsLabel = new JLabel("⚙ Settings");
            settingsLabel.setForeground(Color.WHITE);
            settingsLabel.setFont(new Font("Dialog", Font.BOLD, 20));
            settingsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
                    guideDialog.add(new JLabel(new ImageIcon(scaled)));
                    guideDialog.setVisible(true);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not load Morse Guide image: Assets/Morse.jpg");
                }
            });
            JButton aboutButton = new JButton("❓ About & Help");
            aboutButton.setFont(new Font("Dialog", Font.BOLD, 14));
            aboutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            aboutButton.addActionListener(ev -> showAboutWindow());
            JPanel flashSpeedPanel = new JPanel();
            flashSpeedPanel.setBackground(new Color(30, 30, 30));
            JLabel flashSpeedLabel = new JLabel("Flash Speed (ms):");
            flashSpeedLabel.setForeground(Color.WHITE);
            flashSpeedPanel.add(flashSpeedLabel);
            flashSpeedPanel.add(flashSpeedSpinner);
            flashSpeedPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            settingsDialog.add(Box.createVerticalStrut(20));
            settingsDialog.add(settingsLabel);
            settingsDialog.add(Box.createVerticalStrut(20));
            settingsDialog.add(flashSpeedPanel);
            settingsDialog.add(Box.createVerticalStrut(15));
            settingsDialog.add(morseGuideButton);
            settingsDialog.add(Box.createVerticalStrut(20));
            settingsDialog.add(aboutButton);
            settingsDialog.add(Box.createVerticalGlue());
            settingsDialog.setVisible(true);
        });

        switchDSButton.addActionListener(e -> {
            useMap = !useMap;
            String mode = useMap ? "HashMap" : "Binary Tree";
            JOptionPane.showMessageDialog(this, "Switched to " + mode + " mode.");
        });

        swapButton.addActionListener(e -> {
            engToMorse = !engToMorse;
            String inputText = inputArea.getText();
            String outputText = outputArea.getText();
            inputArea.setText(outputText);
            outputArea.setText(inputText);
            String tempLabel = inputLabel.getText();
            inputLabel.setText(outputLabel.getText());
            outputLabel.setText(tempLabel);
            toggleCaptions.setVisible(engToMorse);
            captionScroll.setVisible(engToMorse && toggleCaptions.isSelected());
        });

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
                lightsBtn.setText("Stop");
                new Thread(() -> flashMorse(morseText, (int) dotSpinner.getValue())).start();
            } else {
                stopFlash = true;
            }
        });

        soundBtn.addActionListener(e -> {
            String morseText = engToMorse ? outputArea.getText() : inputArea.getText();
            if (morseText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No Morse code to play.");
                return;
            }
            if (soundBtn.getText().equals("Sound")) {
                stopSound = false;
                soundBtn.setText("Stop");
                new Thread(() -> playMorseSound(morseText, (int) dotSpinner.getValue())).start();
            } else {
                stopSound = true;
            }
        });

        syncBtn.addActionListener(e -> {
            String morseText = engToMorse ? outputArea.getText() : inputArea.getText();
            if(morseText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No Morse code to sync.");
                return;
            }
            if(syncBtn.getText().equals("Sync")) {
                stopSync = false;
                syncBtn.setText("Stop");
                new Thread(() -> performSync(morseText, (int) dotSpinner.getValue())).start();
            } else {
                stopSync = true;
            }
        });

        importBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    String content = new String(java.nio.file.Files.readAllBytes(selectedFile.toPath())).trim();
                    inputArea.setText(content);
                    translateBtn.doClick(); // Automatically translate after importing
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to read file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        historyBtn.addActionListener(e -> historyPanel.setVisible(!historyPanel.isVisible()));

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
            }
        });
    }

    private void updateCharCount() {
        int count = inputArea.getText().length();
        charCountLabel.setText(count + " / " + MAX_CHARS);
        charCountLabel.setForeground(count >= MAX_CHARS ? Color.RED : Color.LIGHT_GRAY);
    }

    private void performSync(String morse, int dotDuration) {
        SwingUtilities.invokeLater(() -> {
            flashStatus.setText("Syncing...");
            flashStatus.setForeground(Color.CYAN);
        });
        try {
            int dashDuration = dotDuration * 3;
            int spaceDuration = dotDuration * 2;
            for (char c : morse.toCharArray()) {
                if (stopSync) break;
                switch (c) {
                    case '.': playAndFlash(dotDuration); break;
                    case '-': playAndFlash(dashDuration); break;
                    case ' ': Thread.sleep(spaceDuration); break;
                    case '/': Thread.sleep(dotDuration * 7); break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            SwingUtilities.invokeLater(() -> {
                flashLight.setBackground(Color.DARK_GRAY);
                flashStatus.setText(stopSync ? "Sync Stopped" : "Sync Finished");
                flashStatus.setForeground(stopSync ? Color.RED : Color.GREEN);
                syncBtn.setText("Sync");
            });
        }
    }

    private void playAndFlash(int duration) throws InterruptedException {
        SwingUtilities.invokeLater(() -> flashLight.setBackground(Color.YELLOW));
        playBeep(duration);
        SwingUtilities.invokeLater(() -> flashLight.setBackground(Color.DARK_GRAY));
        Thread.sleep((int) flashSpeedSpinner.getValue());
    }

    private void flashMorse(String morse, int dotDuration) {
        SwingUtilities.invokeLater(() -> {
            flashStatus.setText("Flashing...");
            flashStatus.setForeground(Color.YELLOW);
        });
        try {
            int dashDuration = dotDuration * 3;
            int spaceDuration = dotDuration * 2;
            for (char c : morse.toCharArray()) {
                if (stopFlash) break;
                switch (c) {
                    case '.': flashLight(Color.YELLOW, dotDuration); break;
                    case '-': flashLight(Color.YELLOW, dashDuration); break;
                    case ' ': Thread.sleep(spaceDuration); break;
                    case '/': Thread.sleep(dotDuration * 7); break;
                }
            }
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        } finally {
            SwingUtilities.invokeLater(() -> {
                flashLight.setBackground(Color.DARK_GRAY);
                flashStatus.setText(stopFlash ? "Flashing Stopped" : "Done Flashing");
                flashStatus.setForeground(stopFlash ? Color.RED : Color.GREEN);
                lightsBtn.setText("Flash");
            });
        }
    }

    private void playMorseSound(String morseCode, int dotDuration) {
        try{
            int dashDuration = dotDuration * 3;
            int spaceDuration = dotDuration * 2;
            for (char c : morseCode.toCharArray()) {
                if (stopSound) break;
                if (c == '.') playBeep(dotDuration);
                else if (c == '-') playBeep(dashDuration);
                else if (c == ' ') Thread.sleep(spaceDuration);
                else if (c == '/') Thread.sleep(dotDuration * 7);
                Thread.sleep(dotDuration);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            SwingUtilities.invokeLater(() -> soundBtn.setText("Sound"));
        }
    }

    private void flashLight(Color color, int duration) throws InterruptedException {
        SwingUtilities.invokeLater(() -> flashLight.setBackground(color));
        Thread.sleep(duration);
        SwingUtilities.invokeLater(() -> flashLight.setBackground(Color.DARK_GRAY));
        Thread.sleep((int) flashSpeedSpinner.getValue());
    }

    private void addToHistory(String inputText, String translatedText) {
        historyModel.addElement((engToMorse ? "ENGLISH: " : "MORSE: ") + inputText);
        historyModel.addElement((engToMorse ? "MORSE:   " : "ENGLISH: ") + translatedText);
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
        area.setBorder(new EmptyBorder(10,10,10,10));
        return area;
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 2, true));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private JButton createColoredButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        btn.setPreferredSize(new Dimension(140, 80));
        return btn;
    }

    class RoundedBackgroundPanel extends JPanel {
        private BufferedImage image;
        private int cornerRadius;
        public RoundedBackgroundPanel(BufferedImage image, int cornerRadius) { this.image = image; this.cornerRadius = cornerRadius; setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.setClip(clip);
            if (image != null) g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            else { g2.setColor(getBackground()); g2.fill(clip); }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void playBeep(int duration) {
        try {
            float frequency = 800f;
            int sampleRate = 44100;
            byte[] buf = new byte[duration * sampleRate / 1000];
            for (int i = 0; i < buf.length; i++) {
                double angle = 2.0 * Math.PI * i / (sampleRate / frequency);
                buf[i] = (byte) (Math.sin(angle) * 127);
            }
            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            sdl.write(buf, 0, buf.length);
            sdl.drain();
            sdl.stop();
            sdl.close();
        } catch (Exception e) {
            System.err.println("Audio playback error: " + e.getMessage());
        }
    }

    class CharacterLimitFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if ((fb.getDocument().getLength() + string.length()) <= MAX_CHARS) {
                super.insertString(fb, offset, string, attr);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            int currentLength = fb.getDocument().getLength();
            int newLength = currentLength - length + (text == null ? 0 : text.length());
            if (newLength <= MAX_CHARS) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    class morseMap {
        public String toMorse(String text) { return ".... . .-.. .-.. --- / .-- --- .-. .-.. -.."; }
        public String fromMorse(String morse) { return "HELLO WORLD"; }
    }

    class MorseBinaryTree {
        public String toMorse(String text) { return ".... . .-.. .-.. --- / .-- --- .-. .-.. -.. / -...- --. -.-- / - .-. . ."; }
        public String fromMorse(String morse) { return "HELLO WORLD FROM TREE"; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}
