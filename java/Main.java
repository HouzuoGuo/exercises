import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(final String[] args) throws Exception {
        System.err.println("Hello world - again!");
        TextProcessing.readPasswd();
        FileProcessing.walkFiles(Path.of("/etc"));
        StringMethods.indexing();
        StringMethods.regex();
        Multiprocessing.threads();
        System.exit(0);
    }
}

final class TextProcessing {
    public static void readPasswd() throws IOException {
        for (final File root : File.listRoots()) {
            System.err.println(String.format("root is: %s", root.getAbsolutePath()));
        }
        final List<String> lines = Files.readAllLines(Path.of("/etc", "passwd"));
        for (final String line : lines.subList(2, 4)) {
            final String[] fields = line.split(":");
            System.err.println(String.format("fields are: %s", String.join(",", fields)));
        }
    }
}

final class FileProcessing {
    public static void walkFiles(final Path start) throws Exception {
        final Stream<Path> visited = Files.walk(start, FileVisitOption.FOLLOW_LINKS);
        try {
            final List<String> result = visited.limit(12).map(p -> p.toString()).collect(Collectors.toList());
            System.err.println(result);
        } finally {
            visited.close();
        }
    }
}

final class StringMethods {
    public static void indexing() {
        final String sample = " abc123";
        final String[] strings = {
                // Indexing
                sample.substring(2),
                sample.endsWith("123") ? sample.substring(0, sample.length() - 3) : sample,
                sample.strip(),
                sample.startsWith(" ab") ? sample.substring(" ab".length()) : sample,
                sample.replace("abc", "def"),
        };
        Arrays.asList(strings).forEach(System.err::println);
    }

    public static void regex() throws Exception {
        final Pattern kvPattern = Pattern.compile("^([^=]+)=\"?(\\p{Print}+)\"?$");
        Files.readAllLines(Path.of("/etc/os-release")).forEach(line -> {
            System.err.println("Trying to find match in line: " + line);
            final Matcher lineMatcher = kvPattern.matcher(line);
            if (lineMatcher.find()) {
                final String whole = lineMatcher.group(0);
                final String key = lineMatcher.group(1);
                final String value = lineMatcher.group(2);
                System.err.println(String.format("Whole: %s, key: %s, value: %s", whole, key, value));
            }
        });
    }
}

final class SumCalculator implements Callable<Integer> {
    int begin, end;
    int[] input;
    CountDownLatch latch;

    public SumCalculator(int[] input, int fromIndex, int toIndex, CountDownLatch latch) {
        this.input = input;
        this.begin = fromIndex;
        this.end = toIndex;
        this.latch = latch;
    }

    public Integer call() {
        int sum = 0;
        for (int ti = this.begin; ti < this.end; ti++) {
            sum += input[ti];
        }
        this.latch.countDown();
        System.err.println(String.format("sum between %d and %d is %d", this.begin, this.end, sum));
        return sum;
    }
}

final class Multiprocessing {
    public static void threads() throws Exception {
        final int input[] = { 0, 1, 2, 3, 4, 5, 6 };
        final int nThreads = 3;
        final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        final CountDownLatch waitGroup = new CountDownLatch(nThreads);
        final Collection<Future<Integer>> results = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < nThreads; i++) {
            int begin = i * nThreads;
            int end = begin + nThreads;
            end = end > input.length ? input.length : end;
            // Could also use InvokeAll.
            System.err.println(String.format("submitting %d - %d", begin, end));
            results.add(executor.submit(new SumCalculator(input, begin, end, waitGroup)));
        }
        waitGroup.await();
        final int sum = results.stream().map(r -> {
            try {
                return r.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).mapToInt(Integer::intValue).sum();
        System.err.println(String.format("sum is %d", sum));
    }
}
