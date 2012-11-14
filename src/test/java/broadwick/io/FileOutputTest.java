package broadwick.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.MarkerFactory;

/**
 * Test cases for broadwick.io.FileOutput class.
 */
@Slf4j
public class FileOutputTest {

    private static String testsFileName;
    public FileOutputTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        File temp = File.createTempFile("temp-output-test", ".csv");
        testsFileName = temp.getName();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        File temp = new File(testsFileName);
        boolean success = temp.delete();
    }

    @ClassRule // the magic is done here
    public static TestRule classWatchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            log.info(MarkerFactory.getMarker("TEST"), "    Running tests from {} ...", description.getClassName());
        }

    };
    @Rule
    public TestRule watchman = new TestWatcher() {
        @Override
        public Statement apply(Statement base, Description description) {
            log.info(MarkerFactory.getMarker("TEST"), "        Running {} ...", description.getMethodName());
            return base;
        }

    };
    @Test
    public void testWrite_String() {
        InputStream fis = null;
        BufferedReader br;
        String line1 = "token1,token2,token3";
        FileOutput instance = new FileOutput(testsFileName);
        try {
            instance.write(line1);
        } catch (BroadwickIOException ex) {
            fail("Caught BroadwickIOException in testWrite_String()");
        }
        try {
            // TODO review the generated test code and remove the default call to fail.
            fis = new FileInputStream(testsFileName);
        } catch (FileNotFoundException ex) {
            fail("Caught FileNotFoundException in testWrite_String()");
        }
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
        try {
            if (!br.readLine().equals(line1)) {
                fail("Fail in testWrite_String - line written to file unsuccessfully");
            }
        } catch (IOException ex) {
            fail("Caught IOException in testWrite_String()");
        }
    }

    @Test
    public void testWrite_String_ObjectArr() {
        /* String format = "";
         String result = "";
         Object[] args = {""};
         FileOutput instance = new FileOutput(testsFileName);
         String expResult = "%d";
         InputStream fis = null;
         BufferedReader br;
         try {
         result = instance.write(format, args);
         //assertEquals(expResult, result);
         } catch (BroadwickIOException ex) {
         fail("Caught BroadwickIOException in testWrite_String_ObjectArr()");
         }
         try {
         fis = new FileInputStream(testsFileName);
         } catch (FileNotFoundException ex) {
         fail("Caught FileNotFoundException in testWrite_String_ObjectArr()");
         }
         br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
         try {
         if (br.readLine().equals(result)) {
         // Do nothing as we expect it to be equal
         } else {
         fail("Fail in testWrite_String_ObjectArr - line written to file unsuccessfully");
         }
         } catch (IOException ex) {
         fail("Caught IOException in testWrite_String_ObjectArr()");
         }*/
    }

    @Test
    public void testClose() {
        try {
            String line1 = "token1,token2,token3";
            String line2 = "apples, oranges, pears";
            String line3 = "one , two , three";
            String line4 = "a,b,c,d,e,f";
            String line5 = "last, complete, line,";
            FileOutput instance = new FileOutput(testsFileName);
            instance.write(line1);
            instance.write(line2);
            instance.close();
            try {
                instance.write(line5);
                fail("Expected BroadwickIOException to be thrown after using a method on a closed FileOutput object.");
            } catch (BroadwickIOException ex) {
                // we expect an BroadwickIOException to be caught here because the FileOutput object is closed when the write 
                // method is called.
            }
        } catch (BroadwickIOException ex) {
            fail("Caught BroadwickIOException in testClose()");
        }

    }

    @Test
    public void testFlush() {
        /*   System.out.println("flush");
         FileOutput instance = null;
         instance.flush();
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");*/
    }

    @Test
    public void testSaveToFile_3args() {
        InputStream fis = null;
        BufferedReader br;
        String data = "token1,token2,token3";
        boolean addVersion = false;
        FileOutput.saveToFile(testsFileName, data, addVersion);
        try {
            fis = new FileInputStream(testsFileName);
        } catch (FileNotFoundException ex) {
            fail("Fail in testSaveToFile_3args - saved file not found");
        }
        try {
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            String output = br.readLine();
            if (br == null || !output.equals(data)) {
                fail("Fail in testSaveToFile_3args - save to file unsuccessful");
            }
        } catch (IOException ex) {
            fail("Fail in testSaveToFile_3args - IO exception");
        }
    }

    @Test
    public void testSaveToFile_4args() {
        /*    String format = "";
         boolean addVersion = false;
         Object[] args = null;
         InputStream fis = null;
         BufferedReader br;
         String dataFileName = "test-saved-file.csv";
         String data = "token1,token2,token3";
         FileOutput instance = new FileOutput(testsFileName);
         FileOutput.saveToFile(dataFileName, format, addVersion, args);
         try {
         fis = new FileInputStream(testsFileName);
         } catch (FileNotFoundException ex) {
         fail("Fail in testSaveToFile_4args - saved file not found");
         }
         br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
         try {
         if (br.readLine().equals(data)) {
         // Do nothing as we expect it to be equal
         } else {
         fail("Fail in testSaveToFile_4args - save to file unsuccessful");
         }
         } catch (IOException ex) {
         fail("Fail in testSaveToFile_3args - IO exception");
         } */
    }

}
