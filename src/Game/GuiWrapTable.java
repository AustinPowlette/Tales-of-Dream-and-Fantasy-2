package Game;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/** The table of every list window (`GuiWindows.table`): text that does not fit its column wraps onto more lines instead of being cut off, and the row grows to show it.
 *  Cells keep the renderer of their table (colours, fonts, borders); only a String cell that is too wide for its column is drawn as wrapped text with the same colours.
 *  Row heights are recomputed when the rows, the sort or the column widths change. The first time rows arrive the columns get widths that follow their contents (short columns stay short,
 *  text columns share the rest), so wrapping only happens where the text is truly long. */
final class GuiWrapTable extends JTable {
    private static final int PAD = 14, MAX_ROWS = 1500, SAMPLE = 200, CAP = 320;
    private final JTextArea area = new JTextArea();
    private boolean fitted = false;
    private int sig = Integer.MIN_VALUE;

    GuiWrapTable(DefaultTableModel m) {
        super(m);
        area.setLineWrap(true); area.setWrapStyleWord(true); area.setOpaque(true); area.setEditable(false);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
    }

    @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
        Component c = super.prepareRenderer(r, row, col);
        if (!(c instanceof JLabel)) return c;
        Object v = getValueAt(row, col);
        if (!(v instanceof String)) return c;
        JLabel l = (JLabel) c;
        String s = l.getText();
        if (s == null || s.length() < 6 || l.getIcon() != null || s.startsWith("<html>")) return c;
        int w = getColumnModel().getColumn(col).getWidth() - PAD;
        if (w < 20 || l.getFontMetrics(l.getFont()).stringWidth(s) <= w) return c; // it fits: the normal renderer (with its alignment)
        area.setFont(l.getFont()); area.setForeground(l.getForeground()); area.setBackground(l.getBackground());
        area.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        area.setText(s);
        return area;
    }

    @Override public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) fitted = false;
        sig = Integer.MIN_VALUE;
        if (getWidth() > 0) { maybeFitColumns(); fitRows(); }
    }

    @Override public void sorterChanged(RowSorterEvent e) {
        super.sorterChanged(e);
        sig = Integer.MIN_VALUE;
        if (getWidth() > 0) fitRows();
    }

    @Override public void doLayout() {
        super.doLayout();
        if (getWidth() <= 0) return;
        if (!fitted) { maybeFitColumns(); super.doLayout(); }
        int s = getRowCount() * 31 + getWidth();
        for (int i = 0; i < getColumnCount(); i++) s = s * 31 + getColumnModel().getColumn(i).getWidth();
        if (s != sig) { sig = s; fitRows(); }
    }

    private int viewWidth() { return getParent() instanceof JViewport ? getParent().getWidth() : getWidth(); }

    /** Column widths from the contents: short columns stay short, the long text columns take the rest. */
    private void maybeFitColumns() {
        int n = getColumnCount(), total = viewWidth();
        if (fitted || n == 0 || getRowCount() == 0 || total <= 0) return;
        fitted = true;
        FontMetrics fm = getFontMetrics(getFont()), hm = getTableHeader() == null ? fm : getFontMetrics(getTableHeader().getFont());
        int[] nat = new int[n];
        int rows = Math.min(getRowCount(), SAMPLE);
        for (int c = 0; c < n; c++) {
            int w = hm.stringWidth(String.valueOf(getColumnName(c))) + 12;
            for (int r = 0; r < rows; r++) {
                Object v = getValueAt(r, c);
                if (v != null) w = Math.max(w, fm.stringWidth(String.valueOf(v)));
            }
            nat[c] = Math.min(CAP, w + PAD + 4);
        }
        int sum = 0;
        for (int w : nat) sum += w;
        int[] out = nat.clone();
        if (sum <= total) { // spare room: it goes to the widest column
            int widest = 0;
            for (int c = 1; c < n; c++) if (nat[c] > nat[widest]) widest = c;
            out[widest] += total - sum;
        } else { // too wide: the short columns keep their size, the long ones share what is left (and wrap)
            int fixed = 0, flex = 0;
            for (int w : nat) { if (w <= 90) fixed += w; else flex += w; }
            double scale = flex == 0 ? 1 : Math.max(0.3, (double) (total - fixed) / flex);
            for (int c = 0; c < n; c++) if (nat[c] > 90) out[c] = Math.max(90, (int) (nat[c] * scale));
        }
        for (int c = 0; c < n; c++) {
            TableColumn tc = getColumnModel().getColumn(c);
            if (tc.getMaxWidth() < out[c]) continue; // a column the window has capped keeps its cap
            tc.setPreferredWidth(out[c]);
        }
    }

    private void fitRows() {
        int cols = getColumnCount(), rows = getRowCount();
        if (cols == 0 || rows == 0 || rows > MAX_ROWS) return;
        FontMetrics fm = getFontMetrics(getFont());
        int lh = fm.getHeight(), def = super.getRowHeight();
        for (int r = 0; r < rows; r++) {
            int lines = 1;
            for (int c = 0; c < cols; c++) {
                Object v = getValueAt(r, c);
                if (!(v instanceof String)) continue;
                String s = (String) v;
                int w = getColumnModel().getColumn(c).getWidth() - PAD;
                if (s.length() < 6 || w < 20 || s.startsWith("<html>")) continue;
                if (s.indexOf('\n') < 0 && fm.stringWidth(s) <= w) continue;
                lines = Math.max(lines, wrapLines(s, fm, w));
            }
            int h = lines == 1 ? def : Math.max(def, lines * lh + 4);
            if (getRowHeight(r) != h) setRowHeight(r, h);
        }
    }

    /** How many lines `s` takes in a width of `w` pixels (words wrap; a word wider than the width breaks). */
    static int wrapLines(String s, FontMetrics fm, int w) {
        int total = 0, sp = fm.charWidth(' ');
        for (String line : s.split("\n", -1)) {
            int lines = 1, x = 0;
            for (String word : line.split(" ")) {
                int ww = fm.stringWidth(word);
                if (x == 0) x = ww;
                else if (x + sp + ww <= w) x += sp + ww;
                else { lines++; x = ww; }
                while (x > w && w > 0) { lines++; x -= w; }
            }
            total += lines;
        }
        return total;
    }
}
