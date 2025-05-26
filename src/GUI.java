import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GUI extends JFrame {

    private BufferedImage backgroundImage;
    private JTextArea inputArea;
    private JTextArea outputArea;

    private morseMap morseMap; // your morse translation class
    private VoiceRecognition voiceRecognition;
    private Thread voiceThread;

    private JButton startVoiceBtn;
    private JButton stopVoiceBtn;

    public GUI() {
        setTitle("Morse Code Translator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setUndecorated(true);

        // Load background image
        try {
            backgroundImage = ImageIO.read(new File("Assets/Background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            backgroundImage = null;
        }

        morseMap = new morseMap();

        RoundedBackgroundPanel bgPanel = new RoundedBackgroundPanel(backgroundImage, 40);
        bgPanel.setLayout(new GridBagLayout());
        bgPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        setContentPane(bgPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;

        // Title label top center
        JLabel title = new JLabel("Morse Code Transelator");
        title.setFont(new Font("Helvetica Neue", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0.1;
        bgPanel.add(title, gbc);

        // Gear icon top right
        JButton gearButton = new JButton("\u2699");
        gearButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 36));
        gearButton.setForeground(Color.WHITE);
        gearButton.setContentAreaFilled(false);
        gearButton.setFocusPainted(false);
        gearButton.setBorder(null);
        gearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gearButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings clicked!"));

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
        JScrollPane outputScroll = new JScrollPane(outputArea);
        styleScrollPane(outputScroll);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.4;
        gbc.weighty = 0.7;
        bgPanel.add(outputScroll, gbc);

        // Buttons panel with feature buttons + translate and clear buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 6, 30, 0));
        buttonsPanel.setOpaque(false);

        JButton lightsBtn = createColoredButton("Lights", Color.WHITE, Color.BLACK);
        JButton voiceBtn = createColoredButton("Voice Recognition", new Color(0, 69, 138), Color.WHITE);
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

        // Swap button action
        swapButton.addActionListener(e -> {
            String temp = inputArea.getText();
            inputArea.setText(outputArea.getText());
            outputArea.setText(temp);
        });

        // Translate button action - translate input to morse
        translateBtn.addActionListener(e -> {
            String inputText = inputArea.getText().trim();
            if (inputText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter text to translate.");
                return;
            }
            String translated = morseMap.to_Morse(inputText);
            outputArea.setText(translated);
        });

        // Clear button action
        clearBtn.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
        });

        // Voice Recognition button toggles mic input
        voiceBtn.addActionListener(e -> {
            if (voiceThread != null && voiceThread.isAlive()) {
                stopVoiceRecognition();
                voiceBtn.setText("Voice Recognition");
            } else {
                startVoiceRecognition();
                voiceBtn.setText("Stop Voice");
            }
        });

        // Other buttons currently placeholders
        lightsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Lights feature coming soon!"));
        soundBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Sound feature coming soon!"));
        historyBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "History feature coming soon!"));

        // Make frame rounded
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
            }
        });
    }

    private void startVoiceRecognition() {
        if (voiceThread != null && voiceThread.isAlive()) return;

        voiceRecognition = new VoiceRecognition(morse -> {
            SwingUtilities.invokeLater(() -> inputArea.setText(morse));
        });

        voiceThread = new Thread(voiceRecognition);
        voiceThread.start();
    }

    private void stopVoiceRecognition() {
        if (voiceRecognition != null) {
            voiceRecognition.stop();
        }
        try {
            if (voiceThread != null) voiceThread.join();
        } catch (InterruptedException ignored) {}
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
            GUI app = new GUI();
            app.setVisible(true);
        });
    }
}