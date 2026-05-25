import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PerformanceComparison {
    private static final int MINIMUM_DEGREE = 16;
    private static final int[] SIZES = {10_000, 50_000, 100_000, 200_000};
    private static final int SEARCH_LIMIT = 30_000;
    private static final int RANGE_QUERY_COUNT = 2_000;
    private static final int RANGE_WIDTH = 100_000;
    private static final long SEED = 42L;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        printProgramHeader();
        printConfiguration();

        for (int size : SIZES) {
            printDatasetHeader(size);

            // use the same generated inputs so both structures are compared fairly
            List<Integer> data = shuffledData(size);
            List<Integer> searchKeys = sampleKeys(data, Math.min(SEARCH_LIMIT, size));
            List<Integer> deleteKeys = sampleKeys(data, Math.min(SEARCH_LIMIT / 2, Math.max(1, size / 5)));
            List<int[]> ranges = buildRanges(size);

            Result bTreeResult = benchmarkBTree(size, data, searchKeys, deleteKeys, ranges);
            Result bPlusTreeResult = benchmarkBPlusTree(size, data, searchKeys, deleteKeys, ranges);

            printTableHeader();
            printResult("B-Tree", bTreeResult);
            printResult("B+ Tree", bPlusTreeResult);
            printTableFooter();

            printWinnerSummary(bTreeResult, bPlusTreeResult);
            System.out.println();
        }

        printProgramFooter();
    }

    private static Result benchmarkBTree(int size, List<Integer> data, List<Integer> searchKeys, List<Integer> deleteKeys, List<int[]> ranges) {
        BTreeProgram.BTree tree = new BTreeProgram.BTree(MINIMUM_DEGREE);

        // measure the cost of building the full tree from shuffled keys
        long insertStart = System.nanoTime();
        for (int key : data) {
            tree.insert(key);
        }
        long insertTime = System.nanoTime() - insertStart;
        int heightAfterInsert = tree.height();

        // measure exact-key lookup using keys that are known to exist before deletion
        long searchStart = System.nanoTime();
        int found = 0;
        for (int key : searchKeys) {
            if (tree.search(key)) {
                found++;
            }
        }
        long searchTime = System.nanoTime() - searchStart;

        // measure range search using fixed-width intervals
        long rangeStart = System.nanoTime();
        int rangeCount = 0;
        for (int[] range : ranges) {
            rangeCount += tree.rangeSearch(range[0], range[1]).size();
        }
        long rangeTime = System.nanoTime() - rangeStart;

        // measure deletion after search and range query are completed
        long deleteStart = System.nanoTime();
        for (int key : deleteKeys) {
            tree.delete(key);
        }
        long deleteTime = System.nanoTime() - deleteStart;

        return new Result(size, insertTime, searchTime, rangeTime, deleteTime, heightAfterInsert, found, rangeCount);
    }

    private static Result benchmarkBPlusTree(int size, List<Integer> data, List<Integer> searchKeys, List<Integer> deleteKeys, List<int[]> ranges) {
        BPlusTreeProgram.BPlusTree tree = new BPlusTreeProgram.BPlusTree(MINIMUM_DEGREE);

        // measure the cost of building the full tree from shuffled keys
        long insertStart = System.nanoTime();
        for (int key : data) {
            tree.insert(key);
        }
        long insertTime = System.nanoTime() - insertStart;
        int heightAfterInsert = tree.height();

        // measure exact-key lookup using keys that are known to exist before deletion
        long searchStart = System.nanoTime();
        int found = 0;
        for (int key : searchKeys) {
            if (tree.search(key)) {
                found++;
            }
        }
        long searchTime = System.nanoTime() - searchStart;

        // measure range search using fixed-width intervals
        long rangeStart = System.nanoTime();
        int rangeCount = 0;
        for (int[] range : ranges) {
            rangeCount += tree.rangeSearch(range[0], range[1]).size();
        }
        long rangeTime = System.nanoTime() - rangeStart;

        // measure deletion after search and range query are completed
        long deleteStart = System.nanoTime();
        for (int key : deleteKeys) {
            tree.delete(key);
        }
        long deleteTime = System.nanoTime() - deleteStart;

        return new Result(size, insertTime, searchTime, rangeTime, deleteTime, heightAfterInsert, found, rangeCount);
    }

    private static List<Integer> shuffledData(int size) {
        List<Integer> data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            data.add(i);
        }
        Collections.shuffle(data, new Random(SEED + size));
        return data;
    }

    private static List<Integer> sampleKeys(List<Integer> data, int count) {
        return new ArrayList<>(data.subList(0, count));
    }

    private static List<int[]> buildRanges(int size) {
        Random random = new Random(SEED * 31 + size);
        List<int[]> ranges = new ArrayList<>(RANGE_QUERY_COUNT);
        int bound = Math.max(1, size - RANGE_WIDTH - 1);

        for (int i = 0; i < RANGE_QUERY_COUNT; i++) {
            int low = random.nextInt(bound);
            ranges.add(new int[]{low, low + RANGE_WIDTH});
        }

        return ranges;
    }

    private static void printProgramHeader() {
        System.out.println(repeat('=', 122));
        System.out.println(center("B-Tree and B+ Tree Performance Comparison", 122));
        System.out.println(center("Real Runtime Benchmark Using Insert, Search, Range Query, and Delete Operations", 122));
        System.out.println(repeat('=', 122));
    }

    private static void printConfiguration() {
        System.out.println();
        System.out.println("Benchmark Configuration");
        System.out.println(repeat('-', 122));
        System.out.printf("Minimum Degree        : %d%n", MINIMUM_DEGREE);
        System.out.printf("Maximum Keys Per Node : %d%n", 2 * MINIMUM_DEGREE - 1);
        System.out.printf("Search Limit          : %,d keys%n", SEARCH_LIMIT);
        System.out.printf("Range Query Count     : %,d queries%n", RANGE_QUERY_COUNT);
        System.out.printf("Range Width           : %,d keys%n", RANGE_WIDTH);
        System.out.println("Timer                 : System.nanoTime(), shown in milliseconds");
        System.out.println(repeat('-', 122));
    }

    private static void printDatasetHeader(int size) {
        System.out.println();
        System.out.println(repeat('=', 122));
        System.out.printf("Dataset Size: %,d Keys%n", size);
        System.out.println(repeat('=', 122));
    }

    private static void printTableHeader() {
        System.out.println(repeat('-', 122));
        System.out.printf("| %-10s | %10s | %12s | %12s | %12s | %12s | %6s | %10s | %12s |%n",
                "Structure", "N", "Insert", "Search", "Range", "Delete", "Height", "Found", "Range Count");
        System.out.printf("| %-10s | %10s | %12s | %12s | %12s | %12s | %6s | %10s | %12s |%n",
                "", "", "(ms)", "(ms)", "(ms)", "(ms)", "", "", "");
        System.out.println(repeat('-', 122));
    }

    private static void printResult(String structure, Result result) {
        System.out.printf("| %-10s | %,10d | %12.3f | %12.3f | %12.3f | %12.3f | %6d | %,10d | %,12d |%n",
                structure,
                result.size,
                toMillis(result.insertNanos),
                toMillis(result.searchNanos),
                toMillis(result.rangeNanos),
                toMillis(result.deleteNanos),
                result.height,
                result.found,
                result.rangeCount);
    }

    private static void printTableFooter() {
        System.out.println(repeat('-', 122));
    }

    private static void printWinnerSummary(Result bTree, Result bPlusTree) {
        System.out.println("Fastest Result Summary");
        System.out.println("Insert      : " + winnerText(bTree.insertNanos, bPlusTree.insertNanos));
        System.out.println("Search      : " + winnerText(bTree.searchNanos, bPlusTree.searchNanos));
        System.out.println("Range Query : " + winnerText(bTree.rangeNanos, bPlusTree.rangeNanos));
        System.out.println("Delete      : " + winnerText(bTree.deleteNanos, bPlusTree.deleteNanos));
    }

    private static String winnerText(long bTreeTime, long bPlusTreeTime) {
        if (bTreeTime < bPlusTreeTime) {
            return "B-Tree";
        }

        if (bPlusTreeTime < bTreeTime) {
            return "B+ Tree";
        }

        return "Tie";
    }

    private static void printProgramFooter() {
        System.out.println(repeat('=', 122));
        System.out.println(center("Benchmark Finished", 122));
        System.out.println(repeat('=', 122));
    }

    private static double toMillis(long nanos) {
        return nanos / 1_000_000.0;
    }

    private static String center(String text, int width) {
        if (text.length() >= width) {
            return text;
        }

        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        return repeat(' ', leftPadding) + text + repeat(' ', rightPadding);
    }

    private static String repeat(char character, int count) {
        StringBuilder builder = new StringBuilder(count);

        for (int i = 0; i < count; i++) {
            builder.append(character);
        }

        return builder.toString();
    }

    private static class Result {
        int size;
        long insertNanos;
        long searchNanos;
        long rangeNanos;
        long deleteNanos;
        int height;
        int found;
        int rangeCount;

        Result(int size, long insertNanos, long searchNanos, long rangeNanos, long deleteNanos, int height, int found, int rangeCount) {
            this.size = size;
            this.insertNanos = insertNanos;
            this.searchNanos = searchNanos;
            this.rangeNanos = rangeNanos;
            this.deleteNanos = deleteNanos;
            this.height = height;
            this.found = found;
            this.rangeCount = rangeCount;
        }
    }
}
