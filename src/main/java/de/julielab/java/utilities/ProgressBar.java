package de.julielab.java.utilities;

public class ProgressBar {
    private final long total;
    private long done;
    private int linewidth = 80;
    private final long startTime = System.nanoTime();
    private boolean showTime = false;
    private long elapsedTime = 0;

    public ProgressBar(long total, long done, int linewidth, boolean showTime) {
        this.total = total;
        this.done = done;
        this.linewidth = linewidth;
        this.showTime = showTime;
    }

    public ProgressBar(long total, int linewidth, boolean showTime) {
        this.total = total;
        this.linewidth = linewidth;
        this.showTime = showTime;
    }

    public ProgressBar(long total, int linewidth) {
        this.total = total;
        this.linewidth = linewidth;
    }

    /**
     * Constructor for the case that the total is not known.
     */
    public ProgressBar() {
        this.total = -1;
    }

    /**
     * Constructor for the case that the total is not known.
     */
    public ProgressBar(int linewidth) {
        this.total = 0;
        this.linewidth = linewidth;
    }

    public ProgressBar(long total) {
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public long getDone() {
        return done;
    }

    public int getLinewidth() {
        return linewidth;
    }

    public void incrementDone() {
        elapsedTime = System.nanoTime() - startTime;
        ++done;
    }

    public void incrementDone(long delta) {
        elapsedTime = System.nanoTime() - startTime;
        done += delta;
    }

    public void incrementDone(boolean printProgressBar) {
        incrementDone();
        if (printProgressBar)
            printProgressBar();
    }

    public void incrementDone(long delta, boolean printProgressBar) {
        incrementDone(delta);
        if (printProgressBar)
            printProgressBar();
    }


    public void printProgressBar() {
        StringBuilder sb = new StringBuilder();
        if (total > 0) {
            double percentage = done / (double) total;
            int progress = (int) (linewidth * percentage);
            sb.append("[");
            for (int j = 0; j < linewidth; j++) {
                if (j < progress)
                    sb.append("=");
                else
                    sb.append(" ");
            }
            sb.append("] ").append(done).append("/").append(total);
        } else {
            sb.append(done).append("/").append(total);
        }
        if (showTime) {
            double elapsedSeconds = elapsedTime / Math.pow(10, 9);
            sb.append(" (").append((long) elapsedSeconds).append("s,").append(" ").append(String.format("%.3f", elapsedSeconds / done)).append("s per item").append(")");
        }
        sb.append("\r");
        System.out.print(sb);
    }
}
