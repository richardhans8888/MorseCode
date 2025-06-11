import java.util.HashMap;

public class morseMap implements MorseTranslator{
    private HashMap<Character, String> morse_map;
    private HashMap<String, Character> reverse;
    public int operations;

    public morseMap() {
        morse_map = new HashMap<>();
        reverse = new HashMap<>();
        morse_dict();
    }

    public void morse_dict() {
        // letters
        morse_map.put('A', ".-");
        morse_map.put('B', "-...");
        morse_map.put('C', "-.-.");
        morse_map.put('D', "-..");
        morse_map.put('E', ".");
        morse_map.put('F', "..-.");
        morse_map.put('G', "--.");
        morse_map.put('H', "....");
        morse_map.put('I', "..");
        morse_map.put('J', ".---");
        morse_map.put('K', "-.-");
        morse_map.put('L', ".-..");
        morse_map.put('M', "--");
        morse_map.put('N', "-.");
        morse_map.put('O', "---");
        morse_map.put('P', ".--.");
        morse_map.put('Q', "--.-");
        morse_map.put('R', ".-.");
        morse_map.put('S', "...");
        morse_map.put('T', "-");
        morse_map.put('U', "..-");
        morse_map.put('V', "...-");
        morse_map.put('W', ".--");
        morse_map.put('X', "-..-");
        morse_map.put('Y', "-.--");
        morse_map.put('Z', "--..");

        // numbers
        morse_map.put('0', "-----");
        morse_map.put('1', ".----");
        morse_map.put('2', "..---");
        morse_map.put('3', "...--");
        morse_map.put('4', "....-");
        morse_map.put('5', ".....");
        morse_map.put('6', "-....");
        morse_map.put('7', "--...");
        morse_map.put('8', "---..");
        morse_map.put('9', "----.");

        //special chars
        morse_map.put('.', ".-.-.-");
        morse_map.put(',', "--..--");
        morse_map.put('?', "..--..");
        morse_map.put('!', "-.-.--");
        morse_map.put('-', "-....-");
        morse_map.put('/', "-..-.");
        morse_map.put('@', ".--.-.");
        morse_map.put('=', "-...-");
        morse_map.put(' ', "/"); //slash as space
        morse_map.put('[', "-.--.");
        morse_map.put(']', "-.--.-");
        morse_map.put('{', "-.--.");
        morse_map.put('}', "-.--.-");
        morse_map.put('+', ".-.-.");
        morse_map.put('_', "..--.-");

        // Build reverse lookup
        for (Character key : morse_map.keySet()) {
            reverse.put(morse_map.get(key), key);
        }
    }
    public String toMorse(String input) {
        long startTime = System.nanoTime();

        int localOps = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toUpperCase().toCharArray()) {
            sb.append(morse_map.getOrDefault(c, "?")).append(" ");
            localOps++;
        }

        long endTime = System.nanoTime();
        double runtimeSec = (endTime - startTime) / 1_000_000_000.0;

        System.out.println("Operations (English -> Morse): " + localOps);
        System.out.printf("Runtime (English -> Morse): %.6f s%n", runtimeSec);

        operations += localOps;
        return sb.toString().trim();
    }

    public String fromMorse(String translateToEnglish) {
        long startTime = System.nanoTime();

        int localOps = 0;
        StringBuilder text = new StringBuilder();
        String[] words = translateToEnglish.trim().split(" / ");

        for (String word : words) {
            String[] letters = word.trim().split(" ");
            for (String letter : letters) {
                text.append(reverse.getOrDefault(letter, '?'));
                localOps++;
            }
            text.append(" ");
        }

        long endTime = System.nanoTime();
        double runtimeSec = (endTime - startTime) / 1_000_000_000.0;

        System.out.println("Operations (Morse -> English): " + localOps);
        System.out.printf("Runtime (Morse -> English): %.6f s%n", runtimeSec);

        operations += localOps;
        return text.toString().trim();
    }
    @Override
    public int getOperations() {
        return operations;
    }
}