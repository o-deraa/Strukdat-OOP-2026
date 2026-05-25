import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BTreeProgram {
    public static class BTree {
        private static class Node {
            int keyCount;
            int[] keys;
            Node[] children;
            boolean leaf;

            Node(int minimumDegree, boolean leaf) {
                this.leaf = leaf;
                this.keys = new int[2 * minimumDegree - 1];
                this.children = new Node[2 * minimumDegree];
                this.keyCount = 0;
            }
        }

        private Node root;
        private final int minimumDegree;
        private int size;

        public BTree(int minimumDegree) {
            if (minimumDegree < 2) {
                throw new IllegalArgumentException("Minimum degree must be at least 2");
            }
            this.minimumDegree = minimumDegree;
            this.root = new Node(minimumDegree, true);
            this.size = 0;
        }

        public int size() {
            return size;
        }

        public int height() {
            return height(root);
        }

        private int height(Node node) {
            if (node == null || node.keyCount == 0) {
                return 0;
            }
            if (node.leaf) {
                return 1;
            }
            return 1 + height(node.children[0]);
        }

        // method pencarian key dalam b-tree dengan binary search pada setiap node
        public boolean search(int key) {
            return search(root, key) != null;
        }

        // recursive search menggunakan dfs, membandingkan key dengan keys di node saat ini
        private Node search(Node node, int key) {
            int index = 0;
            while (index < node.keyCount && key > node.keys[index]) {
                index++;
            }
            if (index < node.keyCount && key == node.keys[index]) {
                return node;
            }
            if (node.leaf) {
                return null;
            }
            return search(node.children[index], key);
        }

        // method insert key baru, cek duplikat, handle root split, dan update size
        public boolean insert(int key) {
            if (search(key)) {
                return false;
            }

            Node currentRoot = root;
            if (currentRoot.keyCount == 2 * minimumDegree - 1) {
                Node newRoot = new Node(minimumDegree, false);
                root = newRoot;
                newRoot.children[0] = currentRoot;
                splitChild(newRoot, 0, currentRoot);
                insertNonFull(newRoot, key);
            } else {
                insertNonFull(currentRoot, key);
            }
            size++;
            return true;
        }

        // insert ke node yang belum penuh dengan shifting keys dan recursion ke child
        private void insertNonFull(Node node, int key) {
            int index = node.keyCount - 1;
            if (node.leaf) {
                while (index >= 0 && key < node.keys[index]) {
                    node.keys[index + 1] = node.keys[index];
                    index--;
                }
                node.keys[index + 1] = key;
                node.keyCount++;
                return;
            }

            while (index >= 0 && key < node.keys[index]) {
                index--;
            }
            index++;

            if (node.children[index].keyCount == 2 * minimumDegree - 1) {
                splitChild(node, index, node.children[index]);
                if (key > node.keys[index]) {
                    index++;
                }
            }
            insertNonFull(node.children[index], key);
        }

        // split node penuh menjadi dua node dengan middle key naik ke parent
        private void splitChild(Node parent, int index, Node fullChild) {
            Node rightChild = new Node(minimumDegree, fullChild.leaf);
            rightChild.keyCount = minimumDegree - 1;

            for (int j = 0; j < minimumDegree - 1; j++) {
                rightChild.keys[j] = fullChild.keys[j + minimumDegree];
            }

            if (!fullChild.leaf) {
                for (int j = 0; j < minimumDegree; j++) {
                    rightChild.children[j] = fullChild.children[j + minimumDegree];
                }
            }

            fullChild.keyCount = minimumDegree - 1;

            for (int j = parent.keyCount; j >= index + 1; j--) {
                parent.children[j + 1] = parent.children[j];
            }
            parent.children[index + 1] = rightChild;

            for (int j = parent.keyCount - 1; j >= index; j--) {
                parent.keys[j + 1] = parent.keys[j];
            }
            parent.keys[index] = fullChild.keys[minimumDegree - 1];
            parent.keyCount++;
        }

        // method delete dengan cek keberadaan key dan handle root yang kosong
        public boolean delete(int key) {
            if (!search(key)) {
                return false;
            }
            delete(root, key);
            if (root.keyCount == 0 && !root.leaf) {
                root = root.children[0];
            }
            size--;
            return true;
        }

        // recursive delete dengan handle berbagai kasus internal node dan leaf node
        private void delete(Node node, int key) {
            int index = findKeyIndex(node, key);

            if (index < node.keyCount && node.keys[index] == key) {
                if (node.leaf) {
                    removeFromLeaf(node, index);
                } else {
                    removeFromInternal(node, index);
                }
                return;
            }

            if (node.leaf) {
                return;
            }

            boolean keyMayBeInLastChild = index == node.keyCount;
            if (node.children[index].keyCount < minimumDegree) {
                fill(node, index);
            }

            if (keyMayBeInLastChild && index > node.keyCount) {
                delete(node.children[index - 1], key);
            } else {
                delete(node.children[index], key);
            }
        }

        // cari index posisi key dalam node dengan linear search
        private int findKeyIndex(Node node, int key) {
            int index = 0;
            while (index < node.keyCount && node.keys[index] < key) {
                index++;
            }
            return index;
        }

        // hapus key dari leaf node dengan shift keys ke kiri
        private void removeFromLeaf(Node node, int index) {
            for (int i = index + 1; i < node.keyCount; i++) {
                node.keys[i - 1] = node.keys[i];
            }
            node.keyCount--;
        }

        // hapus key dari internal node dengan predecessor, successor, atau merge strategy
        private void removeFromInternal(Node node, int index) {
            int key = node.keys[index];

            if (node.children[index].keyCount >= minimumDegree) {
                int predecessor = getPredecessor(node, index);
                node.keys[index] = predecessor;
                delete(node.children[index], predecessor);
            } else if (node.children[index + 1].keyCount >= minimumDegree) {
                int successor = getSuccessor(node, index);
                node.keys[index] = successor;
                delete(node.children[index + 1], successor);
            } else {
                merge(node, index);
                delete(node.children[index], key);
            }
        }

        // ambil predecessor dari subtree kiri dengan traverse ke rightmost leaf
        private int getPredecessor(Node node, int index) {
            Node current = node.children[index];
            while (!current.leaf) {
                current = current.children[current.keyCount];
            }
            return current.keys[current.keyCount - 1];
        }

        // ambil successor dari subtree kanan dengan traverse ke leftmost leaf
        private int getSuccessor(Node node, int index) {
            Node current = node.children[index + 1];
            while (!current.leaf) {
                current = current.children[0];
            }
            return current.keys[0];
        }

        // penuhi child yang underfull dengan borrowing atau merging
        private void fill(Node node, int index) {
            if (index != 0 && node.children[index - 1].keyCount >= minimumDegree) {
                borrowFromPrevious(node, index);
            } else if (index != node.keyCount && node.children[index + 1].keyCount >= minimumDegree) {
                borrowFromNext(node, index);
            } else {
                if (index != node.keyCount) {
                    merge(node, index);
                } else {
                    merge(node, index - 1);
                }
            }
        }

        // pinjam key dari sibling kiri dengan rotasi melalui parent
        private void borrowFromPrevious(Node node, int index) {
            Node child = node.children[index];
            Node sibling = node.children[index - 1];

            for (int i = child.keyCount - 1; i >= 0; i--) {
                child.keys[i + 1] = child.keys[i];
            }

            if (!child.leaf) {
                for (int i = child.keyCount; i >= 0; i--) {
                    child.children[i + 1] = child.children[i];
                }
            }

            child.keys[0] = node.keys[index - 1];
            if (!child.leaf) {
                child.children[0] = sibling.children[sibling.keyCount];
            }
            node.keys[index - 1] = sibling.keys[sibling.keyCount - 1];

            child.keyCount++;
            sibling.keyCount--;
        }

        // pinjam key dari sibling kanan dengan rotasi melalui parent
        private void borrowFromNext(Node node, int index) {
            Node child = node.children[index];
            Node sibling = node.children[index + 1];

            child.keys[child.keyCount] = node.keys[index];
            if (!child.leaf) {
                child.children[child.keyCount + 1] = sibling.children[0];
            }
            node.keys[index] = sibling.keys[0];

            for (int i = 1; i < sibling.keyCount; i++) {
                sibling.keys[i - 1] = sibling.keys[i];
            }

            if (!sibling.leaf) {
                for (int i = 1; i <= sibling.keyCount; i++) {
                    sibling.children[i - 1] = sibling.children[i];
                }
            }

            child.keyCount++;
            sibling.keyCount--;
        }

        // gabung child dengan sibling kanan, middle key dari parent turun ke child
        private void merge(Node node, int index) {
            Node child = node.children[index];
            Node sibling = node.children[index + 1];

            child.keys[minimumDegree - 1] = node.keys[index];

            for (int i = 0; i < sibling.keyCount; i++) {
                child.keys[i + minimumDegree] = sibling.keys[i];
            }

            if (!child.leaf) {
                for (int i = 0; i <= sibling.keyCount; i++) {
                    child.children[i + minimumDegree] = sibling.children[i];
                }
            }

            for (int i = index + 1; i < node.keyCount; i++) {
                node.keys[i - 1] = node.keys[i];
            }

            for (int i = index + 2; i <= node.keyCount; i++) {
                node.children[i - 1] = node.children[i];
            }

            child.keyCount += sibling.keyCount + 1;
            node.keyCount--;
        }

        // range search untuk mendapatkan semua keys dalam interval [low, high]
        public List<Integer> rangeSearch(int low, int high) {
            List<Integer> result = new ArrayList<>();
            if (low > high) {
                return result;
            }
            rangeSearch(root, low, high, result);
            return result;
        }

        // traversal dfs untuk collect keys dalam range dengan conditional recursion
        private void rangeSearch(Node node, int low, int high, List<Integer> result) {
            int index = 0;
            while (index < node.keyCount) {
                if (!node.leaf && low <= node.keys[index]) {
                    rangeSearch(node.children[index], low, high, result);
                }
                if (node.keys[index] >= low && node.keys[index] <= high) {
                    result.add(node.keys[index]);
                }
                if (node.keys[index] > high) {
                    return;
                }
                index++;
            }
            if (!node.leaf) {
                rangeSearch(node.children[index], low, high, result);
            }
        }

        // inorder traversal untuk mendapatkan semua keys dalam sorted order
        public List<Integer> traverse() {
            List<Integer> result = new ArrayList<>();
            traverse(root, result);
            return result;
        }

        // inorder dfs yang visit child, node, child untuk sorted output
        private void traverse(Node node, List<Integer> result) {
            int index;
            for (index = 0; index < node.keyCount; index++) {
                if (!node.leaf) {
                    traverse(node.children[index], result);
                }
                result.add(node.keys[index]);
            }
            if (!node.leaf) {
                traverse(node.children[index], result);
            }
        }

        public void printTree() {
            if (root.keyCount == 0) {
                System.out.println("Tree is empty");
                return;
            }
            printTree(root, 0);
        }

        private void printTree(Node node, int level) {
            System.out.println("Level " + level + " " + keysToString(node));
            if (!node.leaf) {
                for (int i = 0; i <= node.keyCount; i++) {
                    printTree(node.children[i], level + 1);
                }
            }
        }

        private String keysToString(Node node) {
            List<Integer> items = new ArrayList<>();
            for (int i = 0; i < node.keyCount; i++) {
                items.add(node.keys[i]);
            }
            return items.toString();
        }
    }

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String BORDER = "═══════════════════════════════════════════════";

    public static void main(String[] args) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════╗");
        System.out.println("║          B-Tree Program Initialization      ║");
        System.out.println("╚═════════════════════════════════════════════╝\n");
        
        System.out.print("Minimum degree t (recommended 3 for demo): ");
        int minimumDegree = readIntWithDefault(3);
        BTree tree = new BTree(minimumDegree);

        while (true) {
            clearScreen();
            displayMainMenu();
            System.out.print("Choice: ");

            int choice = readIntWithDefault(-1);
            switch (choice) {
                case 1:
                    handleInsert(tree);
                    break;
                case 2:
                    handleSearch(tree);
                    break;
                case 3:
                    handleDelete(tree);
                    break;
                case 4:
                    handleRangeSearch(tree);
                    break;
                case 5:
                    handleDisplayTree(tree);
                    break;
                case 6:
                    handleShowSorted(tree);
                    break;
                case 7:
                    runBenchmark();
                    break;
                case 0:
                    System.out.println("\nThank you for using B-Tree program. Goodbye!");
                    return;
                default:
                    clearScreen();
                    System.out.println("Invalid choice. Please try again.");
                    pauseMenuReturn();
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  B-TREE MENU") + "║");
        System.out.println("╠" + BORDER + "╣");
        System.out.println("║ 1. Insert key(s)                              ║");
        System.out.println("║ 2. Search key                                 ║");
        System.out.println("║ 3. Delete key                                 ║");
        System.out.println("║ 4. Range search                               ║");
        System.out.println("║ 5. Display tree                               ║");
        System.out.println("║ 6. Show sorted keys                           ║");
        System.out.println("║ 7. Benchmark B-tree only                      ║");
        System.out.println("║ 0. Exit                                       ║");
        System.out.println("╚" + BORDER + "╝\n");
    }

    private static void handleInsert(BTree tree) {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  INSERT KEY(S)") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        System.out.println("Enter keys separated by spaces (e.g., 10 20 30 40):");
        System.out.print("Keys: ");
        String input = SCANNER.nextLine().trim();
        
        if (input.isEmpty()) {
            System.out.println("No keys entered.");
            pauseMenuReturn();
            return;
        }

        try {
            String[] keyStrings = input.split("\\s+");
            int insertedCount = 0;
            int duplicateCount = 0;

            for (String keyStr : keyStrings) {
                int key = Integer.parseInt(keyStr);
                if (tree.insert(key)) {
                    insertedCount++;
                } else {
                    duplicateCount++;
                }
            }

            System.out.println("\n─────────────────────────────────────────────");
            System.out.println("Inserted: " + insertedCount + " key(s)");
            if (duplicateCount > 0) {
                System.out.println("Duplicate(s) ignored: " + duplicateCount);
            }
            System.out.println("Tree size: " + tree.size());
            System.out.println("─────────────────────────────────────────────");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numbers separated by spaces.");
        }

        pauseMenuReturn();
    }

    private static void handleSearch(BTree tree) {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  SEARCH KEY") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        System.out.print("Key: ");
        int searchKey = readIntWithDefault(0);
        
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  SEARCH RESULT") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        boolean found = tree.search(searchKey);
        System.out.println("Key " + searchKey + ": " + (found ? "FOUND" : "NOT FOUND"));
        
        pauseMenuReturn();
    }

    private static void handleDelete(BTree tree) {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  DELETE KEY") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        System.out.print("Key: ");
        int deleteKey = readIntWithDefault(0);
        
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  DELETE RESULT") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        boolean deleted = tree.delete(deleteKey);
        System.out.println("Key " + deleteKey + ": " + (deleted ? "DELETED" : "NOT FOUND"));
        System.out.println("Tree size: " + tree.size());
        
        pauseMenuReturn();
    }

    private static void handleRangeSearch(BTree tree) {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  RANGE SEARCH") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        System.out.print("Low: ");
        int low = readIntWithDefault(0);
        System.out.print("High: ");
        int high = readIntWithDefault(0);
        
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  RANGE SEARCH RESULT") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        List<Integer> result = tree.rangeSearch(low, high);
        System.out.println("Keys in range [" + low + ", " + high + "]:");
        if (result.isEmpty()) {
            System.out.println("No keys found in this range.");
        } else {
            System.out.println(result);
            System.out.println("Total: " + result.size() + " key(s)");
        }
        
        pauseMenuReturn();
    }

    private static void handleDisplayTree(BTree tree) {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  TREE DISPLAY") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        tree.printTree();
        System.out.println("\nHeight: " + tree.height() + ", Size: " + tree.size());
        
        pauseMenuReturn();
    }

    private static void handleShowSorted(BTree tree) {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  SORTED KEYS") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        List<Integer> sorted = tree.traverse();
        if (sorted.isEmpty()) {
            System.out.println("Tree is empty.");
        } else {
            System.out.println(sorted);
            System.out.println("Total: " + sorted.size() + " key(s)");
        }
        
        pauseMenuReturn();
    }

    private static void pauseMenuReturn() {
        System.out.println("\n─────────────────────────────────────────────");
        System.out.print("Press ENTER to return to menu...");
        SCANNER.nextLine();
    }

    private static void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.println("\n");
        }
    }

    private static int readIntWithDefault(int defaultValue) {
        String line = SCANNER.nextLine().trim();
        if (line.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException error) {
            return defaultValue;
        }
    }

    public static void runBenchmark() {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  B-TREE BENCHMARK") + "║");
        System.out.println("╚" + BORDER + "╝\n");

        int[] sizes = {10_000, 50_000, 100_000};
        int minimumDegree = 16;
        int operationCount = 20_000;
        Random random = new Random(42);

        System.out.println("Minimum degree: " + minimumDegree);
        System.out.println();
        System.out.printf("%10s %14s %14s %14s %14s %8s%n", "n", "insert ms", "search ms", "range ms", "delete ms", "height");
        System.out.println("─────────────────────────────────────────────");

        for (int n : sizes) {
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                data.add(i);
            }
            Collections.shuffle(data, random);

            BTree tree = new BTree(minimumDegree);
            long startInsert = System.nanoTime();
            for (int key : data) {
                tree.insert(key);
            }
            long insertTime = System.nanoTime() - startInsert;

            int checks = Math.min(operationCount, n);
            long searchStart = System.nanoTime();
            int found = 0;
            for (int i = 0; i < checks; i++) {
                if (tree.search(data.get(i))) {
                    found++;
                }
            }
            long searchTime = System.nanoTime() - searchStart;

            long rangeStart = System.nanoTime();
            int rangeCount = 0;
            for (int i = 0; i < 1000; i++) {
                int low = random.nextInt(Math.max(1, n - 100));
                rangeCount += tree.rangeSearch(low, low + 100).size();
            }
            long rangeTime = System.nanoTime() - rangeStart;

            long deleteStart = System.nanoTime();
            for (int i = 0; i < checks / 2; i++) {
                tree.delete(data.get(i));
            }
            long deleteTime = System.nanoTime() - deleteStart;

            if (found + rangeCount == -1) {
                System.out.println("Unreachable");
            }

            System.out.printf("%10d %14.3f %14.3f %14.3f %14.3f %8d%n",
                    n,
                    nanosToMillis(insertTime),
                    nanosToMillis(searchTime),
                    nanosToMillis(rangeTime),
                    nanosToMillis(deleteTime),
                    tree.height());
        }

        System.out.println("─────────────────────────────────────────────");
        pauseMenuReturn();
    }

    private static double nanosToMillis(long nanos) {
        return nanos / 1_000_000.0;
    }
}