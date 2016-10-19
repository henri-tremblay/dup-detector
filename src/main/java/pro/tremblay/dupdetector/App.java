package pro.tremblay.dupdetector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    private static final ConcurrentMap<String, List<Path>> duplicates = new ConcurrentHashMap<>();

    public static void main(String... args) throws IOException {
        if(args.length != 1) {
            System.err.println("Usage: App root_dir");
            System.exit(1);
            return;
        }

        Path root = Paths.get(args[0]);


        try(Stream<Path> files = Files.find(root, Integer.MAX_VALUE, App::isFile)) {
            files.parallel()
                .forEach(App::processFile);
        }
        catch(UncheckedIOException e) {
            throw e.getCause();
        }

        duplicates.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(entry -> entry.getValue())
            .forEach(App::processDuplicate);
    }

    private static void processDuplicate(List<Path> paths) {
        for(int i = 0; i < paths.size(); i++) {
            for(int j = i + 1; j < paths.size(); j++) {
                File first = paths.get(i).toFile();
                File second = paths.get(j).toFile();
                if(first.length() != second.length()) {
                    continue; // false positive
                }

            }

        }
        System.out.println("Possible duplicates: " + paths.stream().map(p -> p.toString()).collect(Collectors.joining(",")));
    }

    private static void processFile(Path path) {
        byte[] buffer = new byte[2048];
        try(InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
            int count = in.read(buffer);
            String hash = sha1(buffer);
            duplicates.compute(
                hash,
                (key, value) -> {
                    if(value == null) {
                        value = new ArrayList<Path>();
                    }
                    value.add(path);
                    return value;
                });
        }
        catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String sha1(byte[] buffer) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return new String(md.digest(buffer));
    }

    private static boolean isFile(Path path, BasicFileAttributes attributes) {
        return !attributes.isDirectory();
    }
}
