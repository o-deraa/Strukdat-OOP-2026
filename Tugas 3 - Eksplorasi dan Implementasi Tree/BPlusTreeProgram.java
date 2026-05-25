import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BPlusTreeProgram {
    public static class BPlusTree {
        private abstract static class Node {
            List<Integer> keys = new ArrayList<>();

            abstract boolean isLeaf();
        }

        private static class InternalNode extends Node {
            List<Node> children = new ArrayList<>();

            boolean isLeaf() {
                return false;
            }
        }

        private static class LeafNode extends Node {
            List<Integer> values = new ArrayList<>();
            LeafNode next;

            boolean isLeaf() {
                return true;
            }
        }

        private static class Split {
            int separator;
            Node rightNode;

            Split(int separator, Node rightNode) {
                this.separator = separator;
                this.rightNode = rightNode;
            }
        }

        private LeafNode firstLeaf;
        private Node root;
        private final int minimumDegree;
        private final int maxKeys;
        private final int minKeys;
        private int size;

        public BPlusTree(int minimumDegree) {
            if (minimumDegree < 2) {
                throw new IllegalArgumentException("Minimum degree must be at least 2");
            }
            this.minimumDegree = minimumDegree;
            this.maxKeys = 2 * minimumDegree - 1;
            this.minKeys = minimumDegree - 1;
            this.firstLeaf = new LeafNode();
            this.root = firstLeaf;
            this.size = 0;
        }

        public int size() {
            return size;
        }

        public int height() {
            return height(root);
        }

        private int height(Node node) {
            if (node == null || node.keys.isEmpty()) {
                return 0;
            }
            if (node.isLeaf()) {
                return 1;
            }
            return 1 + height(((InternalNode) node).children.get(0));
        }

        // method insert key baru dengan cek duplikat, handle split, dan update size
        public boolean insert(int key) {
            if (search(key)) {
                return false;
            }

            Split split = insert(root, key, key);
            if (split != null) {
                InternalNode newRoot = new InternalNode();
                newRoot.children.add(root);
                newRoot.children.add(split.rightNode);
                rebuildKeys(newRoot);
                root = newRoot;
            }
            size++;
            return true;
        }

        // recursive insert menemukan leaf dan melakukan split jika perlu
        private Split insert(Node node, int key, int value) {
            if (node.isLeaf()) {
                LeafNode leaf = (LeafNode) node;
                int index = lowerBound(leaf.keys, key);
                leaf.keys.add(index, key);
                leaf.values.add(index, value);

                if (leaf.keys.size() <= maxKeys) {
                    return null;
                }
                return splitLeaf(leaf);
            }

            InternalNode internal = (InternalNode) node;
            int childIndex = childIndex(internal.keys, key);
            Split split = insert(internal.children.get(childIndex), key, value);
            if (split == null) {
                rebuildKeys(internal);
                return null;
            }

            internal.children.add(childIndex + 1, split.rightNode);
            rebuildKeys(internal);

            if (internal.keys.size() <= maxKeys) {
                return null;
            }
            return splitInternal(internal);
        }

        // split leaf node menjadi dua dengan maintain linked list untuk sequential access
        private Split splitLeaf(LeafNode leaf) {
            LeafNode rightLeaf = new LeafNode();
            int splitIndex = leaf.keys.size() / 2;

            rightLeaf.keys.addAll(leaf.keys.subList(splitIndex, leaf.keys.size()));
            rightLeaf.values.addAll(leaf.values.subList(splitIndex, leaf.values.size()));
            leaf.keys.subList(splitIndex, leaf.keys.size()).clear();
            leaf.values.subList(splitIndex, leaf.values.size()).clear();

            rightLeaf.next = leaf.next;
            leaf.next = rightLeaf;
            return new Split(rightLeaf.keys.get(0), rightLeaf);
        }

        // split internal node dengan redistribute children dan rebuild keys
        private Split splitInternal(InternalNode internal) {
            InternalNode rightInternal = new InternalNode();
            int splitChildIndex = internal.children.size() / 2;

            rightInternal.children.addAll(internal.children.subList(splitChildIndex, internal.children.size()));
            internal.children.subList(splitChildIndex, internal.children.size()).clear();

            rebuildKeys(internal);
            rebuildKeys(rightInternal);
            return new Split(firstKey(rightInternal), rightInternal);
        }

        // method search dengan traverse ke leaf node kemudian binary search
        public boolean search(int key) {
            LeafNode leaf = findLeaf(key);
            int index = lowerBound(leaf.keys, key);
            return index < leaf.keys.size() && leaf.keys.get(index) == key;
        }

        // traverse dari root ke leaf sesuai dengan key value
        private LeafNode findLeaf(int key) {
            Node current = root;
            while (!current.isLeaf()) {
                InternalNode internal = (InternalNode) current;
                current = internal.children.get(childIndex(internal.keys, key));
            }
            return (LeafNode) current;
        }

        // method delete dengan cek keberadaan, handle rebalance, dan update size
        public boolean delete(int key) {
            if (!search(key)) {
                return false;
            }
            delete(root, key, null, -1);
            shrinkRootIfNeeded();
            resetFirstLeaf();
            size--;
            return true;
        }

        // recursive delete dari leaf ke atas dengan rebalance jika underfull
        private void delete(Node node, int key, InternalNode parent, int childPosition) {
            if (node.isLeaf()) {
                LeafNode leaf = (LeafNode) node;
                int index = lowerBound(leaf.keys, key);
                if (index < leaf.keys.size() && leaf.keys.get(index) == key) {
                    leaf.keys.remove(index);
                    leaf.values.remove(index);
                }
            } else {
                InternalNode internal = (InternalNode) node;
                int index = childIndex(internal.keys, key);
                delete(internal.children.get(index), key, internal, index);
                rebuildKeys(internal);
            }

            if (node != root && node.keys.size() < minKeys) {
                rebalance(parent, childPosition);
            }
        }

        // rebalance node yang underfull dengan borrowing atau merging
        private void rebalance(InternalNode parent, int index) {
            if (parent == null || index < 0 || index >= parent.children.size()) {
                return;
            }

            Node node = parent.children.get(index);
            Node leftSibling = index > 0 ? parent.children.get(index - 1) : null;
            Node rightSibling = index + 1 < parent.children.size() ? parent.children.get(index + 1) : null;

            if (node.isLeaf()) {
                rebalanceLeaf(parent, index, (LeafNode) node, leftSibling, rightSibling);
            } else {
                rebalanceInternal(parent, index, (InternalNode) node, leftSibling, rightSibling);
            }
            rebuildKeys(parent);
        }

        // rebalance leaf node dengan borrowing dari sibling atau merging
        private void rebalanceLeaf(InternalNode parent, int index, LeafNode leaf, Node leftSibling, Node rightSibling) {
            if (leftSibling instanceof LeafNode && leftSibling.keys.size() > minKeys) {
                LeafNode leftLeaf = (LeafNode) leftSibling;
                int lastIndex = leftLeaf.keys.size() - 1;
                leaf.keys.add(0, leftLeaf.keys.remove(lastIndex));
                leaf.values.add(0, leftLeaf.values.remove(lastIndex));
                return;
            }

            if (rightSibling instanceof LeafNode && rightSibling.keys.size() > minKeys) {
                LeafNode rightLeaf = (LeafNode) rightSibling;
                leaf.keys.add(rightLeaf.keys.remove(0));
                leaf.values.add(rightLeaf.values.remove(0));
                return;
            }

            if (leftSibling instanceof LeafNode) {
                LeafNode leftLeaf = (LeafNode) leftSibling;
                leftLeaf.keys.addAll(leaf.keys);
                leftLeaf.values.addAll(leaf.values);
                leftLeaf.next = leaf.next;
                parent.children.remove(index);
                return;
            }

            if (rightSibling instanceof LeafNode) {
                LeafNode rightLeaf = (LeafNode) rightSibling;
                leaf.keys.addAll(rightLeaf.keys);
                leaf.values.addAll(rightLeaf.values);
                leaf.next = rightLeaf.next;
                parent.children.remove(index + 1);
            }
        }

        // rebalance internal node dengan borrowing dari sibling atau merging children
        private void rebalanceInternal(InternalNode parent, int index, InternalNode internal, Node leftSibling, Node rightSibling) {
            if (leftSibling instanceof InternalNode && ((InternalNode) leftSibling).children.size() > minimumDegree) {
                InternalNode leftInternal = (InternalNode) leftSibling;
                Node borrowedChild = leftInternal.children.remove(leftInternal.children.size() - 1);
                internal.children.add(0, borrowedChild);
                rebuildKeys(leftInternal);
                rebuildKeys(internal);
                return;
            }

            if (rightSibling instanceof InternalNode && ((InternalNode) rightSibling).children.size() > minimumDegree) {
                InternalNode rightInternal = (InternalNode) rightSibling;
                Node borrowedChild = rightInternal.children.remove(0);
                internal.children.add(borrowedChild);
                rebuildKeys(rightInternal);
                rebuildKeys(internal);
                return;
            }

            if (leftSibling instanceof InternalNode) {
                InternalNode leftInternal = (InternalNode) leftSibling;
                leftInternal.children.addAll(internal.children);
                rebuildKeys(leftInternal);
                parent.children.remove(index);
                return;
            }

            if (rightSibling instanceof InternalNode) {
                InternalNode rightInternal = (InternalNode) rightSibling;
                internal.children.addAll(rightInternal.children);
                rebuildKeys(internal);
                parent.children.remove(index + 1);
            }
        }

        // shrink root jika hanya punya satu child dan bukan leaf
        private void shrinkRootIfNeeded() {
            while (!root.isLeaf()) {
                InternalNode internal = (InternalNode) root;
                if (internal.children.size() == 1) {
                    root = internal.children.get(0);
                } else {
                    break;
                }
            }
        }

        // range search menggunakan leaf chain untuk sequential access yang efisien
        public List<Integer> rangeSearch(int low, int high) {
            List<Integer> result = new ArrayList<>();
            if (low > high) {
                return result;
            }

            LeafNode leaf = findLeaf(low);
            while (leaf != null) {
                for (int i = 0; i < leaf.keys.size(); i++) {
                    int key = leaf.keys.get(i);
                    if (key > high) {
                        return result;
                    }
                    if (key >= low) {
                        result.add(leaf.values.get(i));
                    }
                }
                leaf = leaf.next;
            }
            return result;
        }

        // traverse semua keys dari leaf chain dalam sorted order
        public List<Integer> traverse() {
            List<Integer> result = new ArrayList<>();
            LeafNode current = firstLeaf;
            while (current != null) {
                result.addAll(current.keys);
                current = current.next;
            }
            return result;
        }

        public void printTree() {
            if (root.keys.isEmpty()) {
                System.out.println("Tree is empty");
                return;
            }
            printTree(root, 0);
            printLeafChain();
        }

        private void printTree(Node node, int level) {
            String type = node.isLeaf() ? "Leaf" : "Internal";
            System.out.println("Level " + level + " " + type + " " + node.keys);
            if (!node.isLeaf()) {
                InternalNode internal = (InternalNode) node;
                for (Node child : internal.children) {
                    printTree(child, level + 1);
                }
            }
        }

        private void printLeafChain() {
            System.out.print("Leaf chain: ");
            LeafNode current = firstLeaf;
            while (current != null) {
                System.out.print(current.keys);
                if (current.next != null) {
                    System.out.print(" -> ");
                }
                current = current.next;
            }
            System.out.println();
        }

        // cari child index berdasarkan key dengan traversal di keys array
        private int childIndex(List<Integer> keys, int key) {
            int index = 0;
            while (index < keys.size() && key >= keys.get(index)) {
                index++;
            }
            return index;
        }

        // binary search untuk menemukan position key dalam sorted list
        private int lowerBound(List<Integer> keys, int key) {
            int low = 0;
            int high = keys.size();
            while (low < high) {
                int middle = low + (high - low) / 2;
                if (keys.get(middle) < key) {
                    low = middle + 1;
                } else {
                    high = middle;
                }
            }
            return low;
        }

        // cari first key dari subtree dengan traverse ke leftmost leaf
        private int firstKey(Node node) {
            Node current = node;
            while (!current.isLeaf()) {
                current = ((InternalNode) current).children.get(0);
            }
            LeafNode leaf = (LeafNode) current;
            if (leaf.keys.isEmpty()) {
                return Integer.MIN_VALUE;
            }
            return leaf.keys.get(0);
        }

        // rebuild keys internal node berdasarkan first key dari setiap child
        private void rebuildKeys(InternalNode internal) {
            internal.keys.clear();
            for (int i = 1; i < internal.children.size(); i++) {
                internal.keys.add(firstKey(internal.children.get(i)));
            }
        }

        // rebuild semua keys dari root secara recursive
        private void rebuildAllKeys(Node node) {
            if (node.isLeaf()) {
                return;
            }
            InternalNode internal = (InternalNode) node;
            for (Node child : internal.children) {
                rebuildAllKeys(child);
            }
            rebuildKeys(internal);
        }

        // reset first leaf pointer ke leftmost leaf dari current root
        private void resetFirstLeaf() {
            Node current = root;
            while (!current.isLeaf()) {
                current = ((InternalNode) current).children.get(0);
            }
            firstLeaf = (LeafNode) current;
        }
    }

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String BORDER = "═══════════════════════════════════════════════";

    public static void main(String[] args) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════╗");
        System.out.println("║       B+ Tree Program Initialization        ║");
        System.out.println("╚═════════════════════════════════════════════╝\n");
        
        System.out.print("Minimum degree t (recommended 3 for demo): ");
        int minimumDegree = readIntWithDefault(3);
        BPlusTree tree = new BPlusTree(minimumDegree);

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
                    System.out.println("\nThank you for using B+ Tree program. Goodbye!");
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
        System.out.println("║" + String.format("%-47s", "  B+ TREE MENU") + "║");
        System.out.println("╠" + BORDER + "╣");
        System.out.println("║ 1. Insert key(s)                              ║");
        System.out.println("║ 2. Search key                                 ║");
        System.out.println("║ 3. Delete key                                 ║");
        System.out.println("║ 4. Range search                               ║");
        System.out.println("║ 5. Display tree                               ║");
        System.out.println("║ 6. Show sorted keys from leaf chain           ║");
        System.out.println("║ 7. Benchmark B+ tree only                     ║");
        System.out.println("║ 0. Exit                                       ║");
        System.out.println("╚" + BORDER + "╝\n");
    }

    private static void handleInsert(BPlusTree tree) {
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

    private static void handleSearch(BPlusTree tree) {
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

    private static void handleDelete(BPlusTree tree) {
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

    private static void handleRangeSearch(BPlusTree tree) {
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

    private static void handleDisplayTree(BPlusTree tree) {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  TREE DISPLAY") + "║");
        System.out.println("╚" + BORDER + "╝\n");
        
        tree.printTree();
        System.out.println("\nHeight: " + tree.height() + ", Size: " + tree.size());
        
        pauseMenuReturn();
    }

    private static void handleShowSorted(BPlusTree tree) {
        clearScreen();
        System.out.println("╔" + BORDER + "╗");
        System.out.println("║" + String.format("%-47s", "  SORTED KEYS FROM LEAF CHAIN") + "║");
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
        System.out.println("║" + String.format("%-47s", "  B+ TREE BENCHMARK") + "║");
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

            BPlusTree tree = new BPlusTree(minimumDegree);
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