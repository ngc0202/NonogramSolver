package nonogramsolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.openqa.selenium.JavascriptExecutor;

/**
 *
 * @author ngc0202
 */
public class Puzzle {

    private Cell[][] board;
    private final int[][] rows;
    private final int[][] cols;
    public final int nRows;
    public final int nCols;
    private final int ID;
    private final long startTime;
    private final JavascriptExecutor exec;

    public enum Cell {

        EMPTY, X, O;
    }

    public Puzzle(int id, JavascriptExecutor e) {
        //hard coded example puzzle
        rows = new int[][]{new int[]{2}, new int[]{3}, new int[]{2}, new int[]{4}, new int[]{1, 1}, new int[]{2}, new int[]{3}, new int[]{2}, new int[]{4}, new int[]{1, 1}};
        cols = new int[][]{new int[]{2}, new int[]{4}, new int[]{1, 1}, new int[]{2, 2}, new int[]{1}, new int[]{2}, new int[]{3}, new int[]{2}, new int[]{4}, new int[]{1, 1}};
        
        nRows = rows.length;
        nCols = cols.length;
        board = new Cell[nRows][nCols]; //0 if empty, 1 if x, 2 if √
        for (Cell[] row : board) {
            Arrays.fill(row, Cell.EMPTY);
        }
        ID = id;
        startTime = System.currentTimeMillis();
        exec = e;
    }

    public Puzzle(int id, int[][] r, int[][] c, JavascriptExecutor d) {
        rows = r;
        cols = c;
        nRows = r.length;
        nCols = c.length;
        board = new Cell[nRows][nCols]; //0 if empty, 1 if x, 2 if √
        for (Cell[] row : board) {
            Arrays.fill(row, Cell.EMPTY);
        }
        exec = d;
        ID = id;
        startTime = System.currentTimeMillis();
    }

    /**
     * Solves the puzzle.
     *
     * @return The solution in a row-major 2d array.
     */
    public Cell[][] solve() {
        for (int i = 0; i < nRows; i++) { //solve rows
            Cell[] sol = solveRow(board[i], rows[i]);
            if (!Arrays.equals(board[i], sol)) {
                for (int j = 0; j < nCols; j++) {
                    if (sol[j] != Cell.EMPTY) {
                        board[i][j] = sol[j];
                    }
                }
                applyChange();
            }
        }
        for (int i = 0; i < nCols; i++) { //solve cols
            Cell[] col = new Cell[nRows];
            for (int j = 0; j < nRows; j++) {
                col[j] = board[j][i];
            }
            Cell[] sol = solveRow(col, cols[i]);
            if (!Arrays.equals(col, sol)) {
                for (int j = 0; j < nRows; j++) {
                    if (sol[j] != Cell.EMPTY) {
                        board[j][i] = sol[j];
                    }
                }
                applyChange();
            }
        }
        return board;
    }

    /**
     * Solves as much as the given row as possible.
     *
     * @param row The row to be solved.
     * @param req The requirements for that row.
     * @return The solved row.
     */
    private static Cell[] solveRow(Cell[] row, int[] req) {
        int minlen = req.length - 1;
        int maxreq = 0;
        for (int creq : req) {
            minlen += creq;
            if (creq > maxreq) {
                maxreq = creq;
            }
        }
        boolean isempty = true;
        for (Cell b : row) {
            if (b != Cell.EMPTY) {
                isempty = false;
                break;
            }
        }
        if (maxreq <= (row.length - minlen) && isempty) {
            return row;
        }
        
        ArrayList<Cell[]> perms = new ArrayList<>();
        permLoop(perms, row, new Cell[row.length], req, 0, 0);
        Cell[] solved = Arrays.copyOf(row, row.length);

        //Determines whether an O or an X belongs in each Cell.
        for (int i = 0; i < row.length; i++) {
            boolean hasO = true;
            boolean hasX = true;
            for (Cell[] perm : perms) {
                if (hasO && perm[i] != Cell.O) {
                    hasO = false;
                }
                if (hasX && perm[i] == Cell.O) {
                    hasX = false;
                }
            }
            if (hasO) {
                solved[i] = Cell.O;
            }
            if (hasX) {
                solved[i] = Cell.X;
            }
        }
        return solved;
    }

    /**
     * Loops for the position of the blocks in the permutation.
     *
     * @param perms List of permutations to be added to if valid.
     * @param oldrow The row that is being solved.
     * @param perm The proposed permutation.
     * @param req List of block-group lengths.
     * @param rpos The position in the req list that the current block is.
     * @param start First possible position of this block.
     */
    @SuppressWarnings("empty-statement")
    private static void permLoop(ArrayList<Cell[]> perms, Cell[] oldrow, Cell[] perm, int[] req, int rpos, int start) {
        int length = req[rpos];
        for (; start < oldrow.length && (oldrow[start] == Cell.X || perm[start] == Cell.O); start++);
        if (start > 0 && perm[start - 1] == Cell.O) {
            start++;
        }
        int end; //index of last ending index possible.
        int reqsum; //How many spaces MUST be reserved at the end.
        reqsum = req.length - rpos - 1;
        for (int i = rpos + 1; i < req.length; i++) {
            reqsum += req[i];
        }
        for (end = oldrow.length - reqsum - 1; end >= start + length - 1 && oldrow[end] == Cell.X; end--);
        if (start >= perm.length || end < start + length - 1) {
            return; //return if impossible start/end
        }
        
        //loops through possible starting positions to make permutations
        for (int i = start; i <= end - length + 1; i++) {
            boolean hasX = false;
            for (int j = i; j < i + length; j++) {
                if (oldrow[j] == Cell.X) {
                    hasX = true;
                    break;
                }
            }
            //If an X exists in the range of this permutations, skip it
            if (hasX) {
                continue;
            }
            Cell[] nperm = Arrays.copyOf(perm, perm.length);
            for (int j = i; j < i + length; j++) {
                nperm[j] = Cell.O;
            }
            
            //recursive base case - stop if this is last Cell block
            if (rpos + 1 >= req.length) {
                for (int j = 0; j < nperm.length; j++) {
                    if (oldrow[j] != Cell.EMPTY && oldrow[j] != null) {
                        nperm[j] = oldrow[j];
                    }
                }
                if (checkRow(nperm, req)) {
                    perms.add(nperm); //add this permutation to the list 
                }
            } else {
                permLoop(perms, oldrow, nperm, req, rpos + 1, start + length + 1); //recurse!
            }
        }
    }

    /**
     * Checks if a row is possible based on current information. Credit: Jay2k1
     *
     * @param row The row to be checked.
     * @param req The requirement for the given row.
     * @return Whether the row is valid.
     */
    public static boolean checkRow(Cell[] row, int[] req) {
        String srow = "";
        int boxes = 0;
        for (Cell b : row) { //convert to string of o's and _'s
            if (b == Cell.O) {
                srow += "o";
                boxes++;
            } else {
                srow += "_";
            }
        }
        int reqsum = 0;
        //prepare regex to match
        String regex = "^_*";
        for (int i = 0; i < req.length; i++) {
            if (i > 0) {
                regex += "_+";
            }
            regex += "o{" + req[i] + "}";
            reqsum += req[i];
        }
        if (boxes != reqsum) {
            return false;
        }

        //use regex to check validity
        regex += "_*$";
        return Pattern.matches(regex, srow);
    }

    /**
     * Checks if the entire puzzle has been solved.
     *
     * @return Whether the puzzle is completed.
     */
    public boolean isSolved() {
        for (int i = 0; i < nRows; i++) { //solve rows
            if (!checkRow(board[i], rows[i])) {
                return false;
            }
        }
        for (int i = 0; i < nCols; i++) { //solve cols
            Cell[] col = new Cell[nRows];
            for (int j = 0; j < nRows; j++) {
                col[j] = board[j][i];
            }
            if (!checkRow(col, cols[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Empties the board and pushes it to Chrome.
     */
    public void clearBoard() {
        board = new Cell[nRows][nCols];
        applyChange();
    }

    /**
     * Prints out the board into the terminal using ASCII. "o" is a filled cell,
     * x is a marked X, and . is an empty cell.
     */
    public void printBoard() {
        printBoard(board);
    }

    /**
     * Prints out the board into the terminal using ASCII. "o" is a filled cell,
     * x is a marked X, and . is an empty cell.
     *
     * @param board The board to be printed.
     */
    public static void printBoard(Cell[][] board) {
        for (Cell[] sol : board) {
            for (Cell cell : sol) {
                System.out.print(cell == Cell.O ? "o" : (cell == Cell.X ? "x" : "."));
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    /**
     * Translates the board into a String parsable by Jay's website.
     *
     * @return The state string.
     */
    public String getStateString() {
        String str = "";
        for (Cell[] row : board) {
            for (Cell b : row) {
                str += (b == Cell.EMPTY ? "n" : (b == Cell.X ? "x" : "y")) + ",";
            }
        }
        str = str.substring(0, str.length() - 1);
        return str;
    }

    /**
     * Sends a Javascript command to the website to update the visual board.
     */
    private void applyChange() {
        String data = this.getStateString();
        long time = System.currentTimeMillis() - startTime;
        String state = "[" + ID + ", " + time + ", \"" + data + "\"]";
        exec.executeAsyncScript("return applyCookieState(" + state + ");");
    }
}
