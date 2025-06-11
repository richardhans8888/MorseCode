public interface MorseTranslator {
    String toMorse(String input);
    String fromMorse(String morseCode);
    int getOperations();
}
