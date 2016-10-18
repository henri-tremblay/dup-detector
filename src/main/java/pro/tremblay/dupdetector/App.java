package pro.tremblay.dupdetector;

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

    public static void main( String[] args ) throws IOException {
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
        dsff                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
        System.out.println("Possible duplicates: " + paths.stream().map(p -> p.toString()).collect(Collectors.joining(",")));
    }

    private static void processFile(Path path) {
        byte[] buffer = new byte[2048];
        try(InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
            in.read(buffer);
            String hash = sha1(buffer);
            duplicates.merge(
                hash,
                new ArrayList<>(),
                (oldValue, value) -> {
                    synchronized(oldValue) {
                        oldValue.add(path);
                    }
                    return oldValue;
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
