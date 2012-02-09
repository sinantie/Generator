package induction.problem.dmv;

/**
 *
 * @author konstas
 */
public class Constants
{
    // Direction : Left, right
    public static final int D_LEFT = 0, D_RIGHT = 1, D = 2; 
    public static final String[] D_STRING = {"left", "right"};
    // Direction x Adjacency : {left, right} x {adj, non-adj}
    public static final int R_LEFT0 = 0, R_LEFT1 = 1, R_RIGHT0 = 2, R_RIGHT1 = 3, R = 4; 
    public static final String[] R_STR = {"left0", "left1", "right0", "right1"};
    // Stop Parameter : Continue, stop
    public static final int F_CONT = 0, F_STOP = 1, F = 2;
    public static final String[] F_STR = {"continue", "stop"};
}
