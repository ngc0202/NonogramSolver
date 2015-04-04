package nonogramsolver;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 *
 * @author ngc0202
 */
public class NonogramSolver {

    private static WebDriver driver;

    public static void main(String[] args) {
        String size = (args.length < 1 ? "4" : args[0]); //default 25×25
        String id = (args.length < 2 ? "" : args[1]);

        ArrayList<ArrayList<Long>> rowlist = null;
        ArrayList<ArrayList<Long>> collist = null;

        //Set up Selenium
        System.setProperty("webdriver.chrome.driver", "lib/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        driver = new ChromeDriver(capabilities);

        driver.manage().timeouts().setScriptTimeout(150, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().pageLoadTimeout(1, TimeUnit.MINUTES);

        driver.get("http://nonogram.jay2k1.com/?size=" + size + (args.length < 2 ? "" : ("&id=" + id)));

        JavascriptExecutor exec = (JavascriptExecutor) driver;
        rowlist = (ArrayList) exec.executeScript("return window.numbers[\"row\"]");
        collist = (ArrayList) exec.executeScript("return window.numbers[\"col\"]");
        exec.executeScript("hintsUsed = true;");
        exec.executeScript("nickname = \"ngc0202_bot\";");
        id = (String) exec.executeScript("return id;");
        exec.executeScript("paused = false; pauseValue = 0; unpauseTimer(); togglePause = {};");
        
        //Converting List<List<Long>> to int[][]
        int[][] rows = new int[rowlist.size()][];
        for (int i = 0; i < rowlist.size(); i++) { //build rows
            ArrayList<Long> lrow = rowlist.get(i);
            rows[i] = new int[lrow.size()];
            for (int j = 0; j < lrow.size(); j++) {
                rows[i][j] = lrow.get(j).intValue();
            }
        }
        int[][] cols = new int[collist.size()][]; //build cols
        for (int i = 0; i < collist.size(); i++) {
            ArrayList<Long> lcol = collist.get(i);
            cols[i] = new int[lcol.size()];
            for (int j = 0; j < lcol.size(); j++) {
                cols[i][j] = lcol.get(j).intValue();
            }
        }
        
        long start = System.currentTimeMillis();
        Puzzle p = new Puzzle(Integer.valueOf(id), rows, cols, exec);
        int i = 0;

        while (!p.isSolved()) {
            System.out.println("--------- " + ++i);
            p.solve();
            p.printBoard();
        }

        System.out.println("--------- x");
        System.out.println("SOLVED! " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        exec.executeScript("stopTimer()");
    }
}
