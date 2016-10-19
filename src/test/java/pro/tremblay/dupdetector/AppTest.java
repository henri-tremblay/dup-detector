package pro.tremblay.dupdetector;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.Assert.*;

public class AppTest {

    @Test
    public void test() throws IOException {
        PrintStream oldOut = System.out;
        String result;
        try(ByteArrayOutputStream bOut = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bOut)) {
            System.setOut(out);
            App.main("src/test/data");
            result = bOut.toString();
        }
        finally {
            System.setOut(oldOut);
        }

        String[] expected = {
            "Possible duplicates: src/test/data/a/first.txt,src/test/data/b/first-two.txt",
            "Possible duplicates: src/test/data/a/third.txt,src/test/data/b/third-two.txt",
            "Possible duplicates: src/test/data/a/four.txt,src/test/data/b/four-one.txt,src/test/data/four-two.txt",
        };

        Arrays.stream(expected).forEach(e -> assertTrue(result, result.contains(e)));
    }
}
