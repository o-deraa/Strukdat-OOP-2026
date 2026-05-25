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
    private static final int RANGE_WIDTH = 100;
    private static final long SEED = 42L;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        System.out.println("real performance comparison");
        System.out.println("minimum degree: " + MINIMUM_DEGREE + ", max keys per node: " + (2 * MINIMUM_DEGREE - 1));
        System.out.println("time uses System.nanoTime() and is shown in milliseconds");
        System.out.println();
        System.out.printf("%10s %8s %12s %12s %12s %12s %8s %8s%n",
                "structure", "n", "insert", "search", "range", "delete", "height", "found");

        for (int size : SIZES) {
            List<Integer> data = shuffledData(size);
            List<Integer> searchKeys = sampleKeys(data, Math.min(SEARCH_LIMIT, size));
            List<Integer> deleteKeys = sampleKeys(data, Math.min(SEARCH_LIMIT / 2, Math.max(1, size / 5)));
            List<int[]> ranges = buildRanges(size);

            Result bTreeResult = benchmarkBTree(size, data, searchKeys, deleteKeys, ranges);
            Result bPlusTreeResult = benchmarkBPlusTree(size, data, searchKeys, deleteKeys, ranges);

            printResult("B-Tree", bTreeResult);
            printResult("B+Tree", bPlusTreeResult);
            System.out.println();
        }
    }

    private static Result benchmarkBTree(int size, List<Integer> data, List<Integer> searchKeys, List<Integer> deleteKeys, List<int[]> ranges) {
        BTreeProgram.BTree tree = new BTreeProgram.BTree(MINIMUM_DEGREE);

        long insertStart = System.nanoTime();
        for (int key : data) {
            tree.insert(key);
        }
        long insertTime = System.nanoTime() - insertStart;

        long searchStart = System.nanoTime();
        int found = 0;
        for (int key : searchKeys) {
            if (tree.search(key)) {
                found++;
            }
        }
        long searchTime = System.nanoTime() - searchStart;

        long rangeStart = System.nanoTime();
        int rangeCount = 0;
        for (int[] range : ranges) {
            rangeCount += tree.rangeSearch(range[0], range[1]).size();
        }
        long rangeTime = System.nanoTime() - rangeStart;

        long deleteStart = System.nanoTime();
        for (int key : deleteKeys) {
            tree.delete(key);
        }
        long deleteTime = System.nanoTime() - deleteStart;

        return new Result(size, insertTime, searchTime, rangeTime, deleteTime, tree.height(), found, rangeCount);
    }

    private static Result benchmarkBPlusTree(int size, List<Integer> data, List<Integer> searchKeys, List<Integer> deleteKeys, List<int[]> ranges) {
        BPlusTreeProgram.BPlusTree tree = new BPlusTreeProgram.BPlusTree(MINIMUM_DEGREE);

        long insertStart = System.nanoTime();
        for (int key : data) {
            tree.insert(key);
        }
        long insertTime = System.nanoTime() - insertStart;

        long searchStart = System.nanoTime();
        int found = 0;
        for (int key : searchKeys) {
            if (tree.search(key)) {
                found++;
            }
        }
        long searchTime = System.nanoTime() - searchStart;

        long rangeStart = System.nanoTime();
        int rangeCount = 0;
        for (int[] range : ranges) {
            rangeCount += tree.rangeSearch(range[0], range[1]).size();
        }
        long rangeTime = System.nanoTime() - rangeStart;

        long deleteStart = System.nanoTime();
        for (int key : deleteKeys) {
            tree.delete(key);
        }
        long deleteTime = System.nanoTime() - deleteStart;

        return new Result(size, insertTime, searchTime, rangeTime, deleteTime, tree.height(), found, rangeCount);
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

    private static void printResult(String structure, Result result) {
        System.out.printf("%10s %8d %12.3f %12.3f %12.3f %12.3f %8d %8d%n",
                structure,
                result.size,
                toMillis(result.insertNanos),
                toMillis(result.searchNanos),
                toMillis(result.rangeNanos),
                toMillis(result.deleteNanos),
                result.height,
                result.found);
    }

    private static double toMillis(long nanos) {
        return nanos / 1_000_000.0;
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
