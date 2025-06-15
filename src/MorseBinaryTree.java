public class MorseBinaryTree implements MorseTranslator {
    private MorseTreeNode root;
    private int operations; // counts total operations in real-time

    public MorseBinaryTree() {
        root = new MorseTreeNode('\0');
        buildTree();
    }

    private void buildTree() {
        insert(".-", 'A');
        insert("-...", 'B');
        insert("-.-.", 'C');
        insert("-..", 'D');
        insert(".", 'E');
        insert("..-.", 'F');
        insert("--.", 'G');
        insert("....", 'H');
        insert("..", 'I');
        insert(".---", 'J');
        insert("-.-", 'K');
        insert(".-..", 'L');
        insert("--", 'M');
        insert("-.", 'N');
        insert("---", 'O');
        insert(".--.", 'P');
        insert("--.-", 'Q');
        insert(".-.", 'R');
        insert("...", 'S');
        insert("-", 'T');
        insert("..-", 'U');
        insert("...-", 'V');
        insert(".--", 'W');
        insert("-..-", 'X');
        insert("-.--", 'Y');
        insert("--..", 'Z');
        insert("-----", '0');
        insert(".----", '1');
        insert("..---", '2');
        insert("...--", '3');
        insert("....-", '4');
        insert(".....", '5');
        insert("-....", '6');
        insert("--...", '7');
        insert("---..", '8');
        insert("----.", '9');
        insert(".-.-.-", '.');
        insert("--..--", ',');
        insert("..--..", '?');
        insert("-.-.--", '!');
        insert("-....-", '-');
        insert("-..-.", '/');
        insert(".--.-.", '@');
        insert("-...-", '=');
        insert(".-.-.", '+');
        insert("..--.-", '_');
        insert("/", ' ');

        operations = 0; // reset after building
    }
    private void insert(String code, char letter) {
        MorseTreeNode current = root;
        for (char c : code.toCharArray()) {
            operations++; // Count each traversal step as operation
            if (c == '.') {
                if (current.dot == null) {
                    current.dot = new MorseTreeNode('\0');
                }
                current = current.dot;
            } else if (c == '-') {
                if (current.dash == null) {
                    current.dash = new MorseTreeNode('\0');
                }
                current = current.dash;
            }
        }
        current.value = letter;
    }

    private char decode(String code) {
        MorseTreeNode current = root;
        for (char c : code.toCharArray()) {
            operations++; // Count each step down the tree
            if (c == '.') {
                current = current.dot;
            } else if (c == '-') {
                current = current.dash;
            }
            if (current == null) return '?';
        }
        return current.value != '\0' ? current.value : '?';
    }

    @Override
    public String toMorse(String input) {
        operations = 0;
        StringBuilder sb = new StringBuilder();
        for (char ch : input.toUpperCase().toCharArray()) {
            String code = findMorseCode(root, ch, "");
            sb.append(code != null ? code : "?").append(" ");
        }
        return sb.toString().trim();
    }

    private String findMorseCode(MorseTreeNode node, char target, String path) {
        if (node == null) return null;
        operations++; // Count every node visited
        if (node.value == target) return path;

        String left = findMorseCode(node.dot, target, path + ".");
        if (left != null) return left;

        return findMorseCode(node.dash, target, path + "-");
    }

    @Override
    public String fromMorse(String morseCode) {
        operations = 0;
        StringBuilder sb = new StringBuilder();
        String[] words = morseCode.trim().split(" / ");

        for (String word : words) {
            String[] letters = word.split(" ");
            for (String code : letters) {
                char decodedChar = decode(code);
                sb.append(decodedChar);
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public int getOperations() {
        return operations;
    }
    @Override
    public long getSpaceUsage() {
        return countNodes(root);
    }

    private long countNodes(MorseTreeNode node) {
        if (node == null) {
            return 0;
        }
        // A node exists, so count it and its children.
        return 1 + countNodes(node.dot) + countNodes(node.dash);
    }
}
