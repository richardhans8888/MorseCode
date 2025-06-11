import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class morseGUI extends JFrame {
    private JTextArea text_area, morse_area;
    private JLabel flashStatus, liveCaption, morseGuideLabel, flashSettingLabel;
    private JButton switch_button, flash_button;
    private JCheckBox toggle_captions;
    private morseMap morse;
    private boolean Eng_to_Morse = true;
    private JPanel flash_light;
    private volatile boolean stopFlash = false;
    private JSpinner dotSpinner;
    private DefaultListModel<String> historyModel = new DefaultListModel<>();



    public morseGUI() {
        super("Morse Code Translator"); //title of the app
        setSize(1200, 800);
        setResizable(false);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        morse = new morseMap(); //Initialize morseMap instance

        //displays image and resize
        ImageIcon ori = new ImageIcon("C:/Users/Latitude 7490/Desktop/morsey.jpg");
        Image scale_Image = ori.getImage().getScaledInstance(1200, 800, Image.SCALE_SMOOTH);
        ImageIcon bg_Image = new ImageIcon(scale_Image);

        JLabel background = new JLabel(bg_Image);
        background.setBounds(0, 0, 1200, 800);
        setContentPane(background);
        background.setLayout(null);

        GUI_Components();
    }

    private void GUI_Components() {
        Font robotoFont = new Font("Roboto", Font.BOLD, 20);
        UIManager.put("Label.font", robotoFont);
        UIManager.put("Button.font", robotoFont);
        UIManager.put("TextArea.font", new Font("Roboto", Font.PLAIN, 18));

        //title attributes
        JLabel titleLabel = new JLabel("Morse Code Translator", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 10, 1200, 50);

        //creators
        JLabel creators = new JLabel("Created by: Richard, Vickel and Timothy", SwingConstants.CENTER);
        creators.setFont(new Font("Dialog", Font.PLAIN, 24));
        creators.setForeground(Color.WHITE);
        creators.setBounds(0, 50, 1200, 50);

        //user input label
        JLabel input_Label = new JLabel("Input a word, sentence, phrase, anything you like :) : ");
        input_Label.setFont(new Font("Dialog", Font.ITALIC, 18));
        input_Label.setForeground(Color.WHITE);
        input_Label.setBounds(20, 100, 560, 40);

        text_area = new JTextArea(); //creates an area for the English text
        text_area.setFont(new Font("Dialog", Font.PLAIN, 18));//font and color
        text_area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));//padding
        text_area.setLineWrap(true);//prevents overflow and stacking
        text_area.setWrapStyleWord(true);

        //allows scrolling in the input box for longer texts and inputs
        JScrollPane user_input_scroll = new JScrollPane(text_area);
        user_input_scroll.setBounds(20, 132, 484, 176);

        //morse code label
        JLabel morse_display = new JLabel("Output: ");
        morse_display.setFont(new Font("Dialog", Font.ITALIC, 16));
        morse_display.setForeground(Color.white);
        morse_display.setBounds(20, 390, 200, 30);

        //switch between english --> morse and morse --> english
        switch_button = new JButton("↔");
        switch_button.setFont(new Font("Dialog", Font.BOLD, 18));
        switch_button.setBounds(520, 200, 80, 40);

        //translate label
        JLabel trans_text = new JLabel("Switch", SwingConstants.CENTER);
        trans_text.setFont(new Font("Dialog", Font.PLAIN, 24));
        trans_text.setForeground(Color.WHITE);
        trans_text.setBounds(520, 230, 89, 50);

        //morse code display
        morse_area = new JTextArea();
        morse_area.setFont(new Font("Dialog", Font.PLAIN, 18));
        morse_area.setEditable(false);
        morse_area.setLineWrap(true);
        morse_area.setWrapStyleWord(true);
        morse_area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //scrolling for the morse code output
        JScrollPane morse_scroll = new JScrollPane(morse_area);
        morse_scroll.setBounds(20, 432, 484, 176);

        //toggle captions (captions formatting)
        toggle_captions = new JCheckBox("Show Captions");
        toggle_captions.setFont(new Font("Dialog", Font.PLAIN, 14));
        toggle_captions.setForeground(Color.white);
        toggle_captions.setOpaque(false);
        toggle_captions.setSelected(true);
        toggle_captions.setBounds(520, 478, 150, 30);
        toggle_captions.setVisible(Eng_to_Morse);//only visible when translating from english to morse code, otherwise, not visible

        //captions formatting and color
        JTextArea caption_area = new JTextArea();
        caption_area.setFont(new Font("Dialog", Font.ITALIC, 16)); // Italic for distinction
        caption_area.setForeground(Color.GRAY); // Set grey text
        caption_area.setEditable(false);
        caption_area.setLineWrap(true);
        caption_area.setWrapStyleWord(true);
        caption_area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        //add scrolling to captions
        JScrollPane caption_scroll = new JScrollPane(caption_area);
        caption_scroll.setBounds(20, 610, 484, 40); //position under morse output


        JLabel light_box = new JLabel("Light", SwingConstants.CENTER);
        light_box.setFont(new Font("Dialog", Font.PLAIN, 24));
        light_box.setForeground(Color.WHITE);
        light_box.setBounds(550,500 , 100, 50);


        //flashing light rectangular box instance
        flash_light = new JPanel();
        flash_light.setBackground(Color.gray);
        flash_light.setBounds(550, 540, 100, 40);


        //trigger light
        flash_button = new JButton("Flash");
        flash_button.setBounds(550, 600, 120, 40);

        //flashing status
        flashStatus = new JLabel("");
        flashStatus.setFont(new Font("Dialog", Font.BOLD, 16));
        flashStatus.setForeground(Color.YELLOW);
        flashStatus.setBounds(700, 610, 200, 40);


        //flashing captions
        liveCaption = new JLabel("");
        liveCaption.setFont(new Font("Dialog", Font.BOLD, 22));
        liveCaption.setForeground(Color.BLACK);
        liveCaption.setOpaque(true);
        liveCaption.setBackground(Color.WHITE);
        liveCaption.setHorizontalAlignment(SwingConstants.CENTER);
        liveCaption.setBounds(550, 490, 100, 40); // positioned above flash_light
        liveCaption.setVisible(false); // initially hidden


        //Settings panel
        JPanel settingsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f)); //85% opacity
                g2.setColor(new Color(40, 40, 40));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBounds(1100, 50, 220, 500);
        settingsPanel.setOpaque(false); // Keep this false so transparency works
        settingsPanel.setVisible(false);

        //label
        JLabel settingsLabel = new JLabel("⚙ Settings");
        settingsLabel.setForeground(Color.WHITE);
        settingsLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        settingsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        //Morse dictionary
        JCheckBox dictToggle = new JCheckBox("Morse Guide");
        dictToggle.setFont(new Font("Dialog", Font.PLAIN, 18));
        dictToggle.setForeground(Color.WHITE);
        dictToggle.setOpaque(false);
        dictToggle.setAlignmentX(Component.CENTER_ALIGNMENT);


        //Morse Guide Image (Initially hidden)
        morseGuideLabel = new JLabel();
        ImageIcon morseIcon = new ImageIcon("C:/Users/Latitude 7490/Desktop/morsecod.jpg"); // update path if needed
        Image scaledImg = morseIcon.getImage().getScaledInstance(400, 300, Image.SCALE_SMOOTH);
        morseGuideLabel.setIcon(new ImageIcon(scaledImg));
        morseGuideLabel.setBounds(700, 200, 400, 300); // adjust position as needed
        morseGuideLabel.setVisible(false);

        flashSettingLabel = new JLabel("Set Flash Time");
        flashSettingLabel.setForeground(Color.WHITE);
        flashSettingLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        flashSettingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dotSpinner = new JSpinner(new SpinnerNumberModel(200, 50, 1000, 10));
        dotSpinner.setMaximumSize(new Dimension(100, 30));
        dotSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        //idk 2
        JCheckBox soundToggle = new JCheckBox("Feature 3");
        soundToggle.setFont(new Font("Dialog", Font.PLAIN, 18));
        soundToggle.setForeground(Color.WHITE);
        soundToggle.setOpaque(false);
        soundToggle.setAlignmentX(Component.CENTER_ALIGNMENT);

        //abouts section pop up window when pressed
        JButton aboutButton = new JButton("❓ About & Help");
        aboutButton.setFont(new Font("Dialog", Font.BOLD, 14));
        aboutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutButton.addActionListener(e -> showAboutWindow());

        //button toggle
        JButton toggleSettingsButton = new JButton("⚙");
        toggleSettingsButton.setFont(new Font("Dialog", Font.BOLD, 18));
        toggleSettingsButton.setBounds(1130, 5, 50, 50);


        // HISTORY PANEL
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBackground(new Color(0, 0, 0, 180)); // Semi-transparent
        historyPanel.setBounds(680, 189, 440, 300);
        historyPanel.setVisible(false);

        JList<String> historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Consolas", Font.PLAIN, 14));
        historyList.setForeground(Color.WHITE);
        historyList.setBackground(Color.DARK_GRAY);

        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        JButton historyButton = new JButton("📜");
        historyButton.setBounds(710, 500, 50, 40);
        historyButton.setFont(new Font("Arial", Font.PLAIN, 18));
        historyButton.setBackground(Color.LIGHT_GRAY);
        historyButton.addActionListener(e -> {
            boolean isVisible = historyPanel.isVisible();
            historyPanel.setVisible(!isVisible);
        });


        // ============================
        //  ADD COMPONENTS TO GUI
        // ============================

        add(titleLabel);
        add(creators);
        add(input_Label);
        add(user_input_scroll);
        add(morse_display);
        add(switch_button);
        add(trans_text);
        add(morse_scroll);
        add(toggle_captions);
        add(caption_scroll);
        add(light_box);
        add(flash_light);
        add(flash_button);
        add(flashStatus);
        add(liveCaption);
        add(morseGuideLabel);
        add(toggleSettingsButton);
        add(settingsPanel);
        getContentPane().add(historyPanel);
        getContentPane().add(historyButton);


        settingsPanel.add(settingsLabel);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(dictToggle);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(flashSettingLabel);
        settingsPanel.add(dotSpinner);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(soundToggle);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(aboutButton);



                //ACTION LISTENER(s)

        //switch button logic
        switch_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Eng_to_Morse = !Eng_to_Morse; // Toggle mode

                // Get current text
                String inputText = text_area.getText().trim();
                String outputText = morse_area.getText().trim();

                // Reset captions
                caption_area.setText("");

                // Update input label
                input_Label.setText(Eng_to_Morse ?
                        "Input a word, sentence, phrase, anything you like :) : " :
                        "Input Morse Code: ");

                // Update caption visibility
                toggle_captions.setVisible(Eng_to_Morse);
                caption_scroll.setVisible(Eng_to_Morse && toggle_captions.isSelected());

                // Translate and switch box contents
                if (Eng_to_Morse) {
                    text_area.setText(outputText);
                    morse_area.setText(morse.toMorse(outputText));
                    caption_area.setText(outputText.replace(" ", " / "));
                } else {
                    text_area.setText(outputText);
                    morse_area.setText(morse.fromMorse(outputText));
                }

                revalidate();
                repaint();
            }
        });

        //sets to invisible at first (parent setting panel)
        toggleSettingsButton.addActionListener(e -> {
            boolean isVisible = settingsPanel.isVisible();
            slidePanel(settingsPanel, !isVisible);
        });

        //Checkbox toggle logic
        dictToggle.addActionListener(e -> {
            morseGuideLabel.setVisible(dictToggle.isSelected());
        });

        //add listener to checkbox to toggle captions
        toggle_captions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                caption_scroll.setVisible(toggle_captions.isSelected());
            }
        });

        flash_button.addActionListener(e -> {
            if (flash_button.getText().equals("Flash")) {
                String morse = Eng_to_Morse ? morse_area.getText() : text_area.getText();
                if (!morse.isEmpty()) {
                    stopFlash = false;
                    int dotDuration = (int) dotSpinner.getValue();
                    flash_button.setText("Stop");

                    new Thread(() -> {
                        flashMorse(morse, dotDuration);
                        SwingUtilities.invokeLater(() -> flash_button.setText("Flash"));
                    }).start();
                }
            } else {
                stopFlash = true;
                flash_button.setText("Flash");
            }
        });


        //add real-time translation
        text_area.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String inputText = text_area.getText().trim(); //trim to avoid extra spaces

                if (Eng_to_Morse) {  //english to Morse
                    if (!inputText.isEmpty()) {
                        String morseText = morse.toMorse(inputText);
                        System.out.println("Operations (English → Morse): " + morse.operations);
                        String captionText = inputText.replace(" ", " / "); //replace spaces with slashes for better vis

                        morse_area.setText(morseText); //set Morse text
                        caption_area.setText(captionText); //set caption text (grey)

                        //show captions only if checkbox is checked
                        caption_scroll.setVisible(toggle_captions.isSelected());
                    } else {
                        morse_area.setText("");
                        caption_area.setText("");
                    }
                } else {  // Morse to English
                    if (!inputText.isEmpty()) {
                        String translatedText = morse.fromMorse(inputText);
                        System.out.println("Operations (Morse → English): " + morse.operations);
                        morse_area.setText(translatedText);
                        caption_area.setText(""); //captions not needed in morse to text
                    } else {
                        morse_area.setText("");
                    }
                }
                if (Eng_to_Morse) {

                } else {
                    if (!inputText.isEmpty()) {
                        String engText = morse.fromMorse(inputText);
                        morse_area.setText(engText);

                        // Save to history
                        historyModel.addElement("MORSE:   " + inputText);
                        historyModel.addElement("ENGLISH: " + engText);
                        historyModel.addElement("------------------------------");
                    }
                }

            }
        });

    }

    //Method(s)

    public void slidePanel(JPanel panel, boolean show) {
        new Thread(() -> {
            int startX = show ? 1200 : panel.getX();
            int endX = show ? 1000 : 1200;
            int step = show ? -5 : 5; // Smaller step for smoother motion

            panel.setVisible(true);
            for (int x = startX; (show ? x >= endX : x <= endX); x += step) {
                panel.setBounds(x, 50, 180, 500);
                try { Thread.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
            }

            if (!show) panel.setVisible(false);
        }).start();
    }

    private void flashMorse(String morse, int dotDuration) {
        int dashDuration = dotDuration * 3;
        int spaceDuration = dotDuration * 2;

        stopFlash = false;

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
            flash_light.setBackground(Color.DARK_GRAY);
            flashStatus.setText(stopFlash ? "⛔ Flashing Stopped" : "✅ Done Flashing");
            flashStatus.setForeground(stopFlash ? Color.RED : Color.GREEN);
        });
    }



    private void flashLight(Color color, int duration) throws InterruptedException {//determines the time of light flashes
        SwingUtilities.invokeLater(() -> flash_light.setBackground(color));
        Thread.sleep(duration);
        SwingUtilities.invokeLater(() -> flash_light.setBackground(Color.DARK_GRAY));
        Thread.sleep(150);
    }


    public void showAboutWindow() {//about window GUI
        JDialog aboutDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "About & Help", true);
        aboutDialog.setSize(300, 400);
        aboutDialog.setLocationRelativeTo(null); // Center on screen
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


}
