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
            "\tsrc/test/data/a/first.txt",
            "\tsrc/test/data/b/first-two.txt",
            "\tsrc/test/data/a/third.txt",
            "\tsrc/test/data/b/third-two.txt",
            "\tsrc/test/data/a/four.txt",
            "\tsrc/test/data/b/four-one.txt",
            "\tsrc/test/data/four-two.txt",
            "\tsrc/test/data/empty.txt"
        };

        Arrays.stream(expected).forEach(e -> assertTrue(result, result.contains(e)));
    }
}
