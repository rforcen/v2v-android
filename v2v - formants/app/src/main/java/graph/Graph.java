package graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.SurfaceView;

// usage with SurfaceView:
// graph gr(sv); gr.Plot... ; gr.end();

public class Graph {
    int axDec = 0; // dicimals in axis
    public String s_units = "";
    boolean GridToPoint = false;
    double Yscale = 1;
    static int SPV = 25; // vertical axis margin
    static int SPH = 25; // horz. axis margin

    double Min, Max;
    int pmax, pmin;
    public boolean scaled, pDoAxis;
    double Dif, ix, iy, px, py, max_ant;
    int off, off_ant;
    int n, np;
    public int x, y, h, w; // graph draw area
    public Canvas cnv = null;
    public Paint pnt = null;
    SurfaceView sv = null;
    int foreColor = Color.GREEN, backColor = Color.BLACK;
    double radius;

    public enum TrsGraphTypes {
        rsCenter, rsCircumference
    }

    ;

    public Graph() {
    }

    public Graph(Canvas c) {
        cnv = c;
        GraphInit();
    }

    public Graph(Canvas c, int backColor) {
        cnv = c;
        this.backColor = backColor;
        GraphInit();
    }

    public Graph(SurfaceView _sv) {
        sv = _sv;
        cnv = sv.getHolder().lockCanvas();
        if (cnv == null)
            return;
        GraphInit();
    }

    protected void finalize() {
        // sv.getHolder().unlockCanvasAndPost(cnv);
    }

    public Canvas begin(SurfaceView _sv) {
        sv = _sv;
        return sv.getHolder().lockCanvas();
    }

    public Canvas begin() {
        return sv.getHolder().lockCanvas();
    }

    public void end() {
        sv.getHolder().unlockCanvasAndPost(cnv);
    }

    public void setCanvas(Canvas canvas) {
        cnv = canvas;
        GraphInit();
    }

    private void GraphInit() {
        pnt = new Paint(Paint.ANTI_ALIAS_FLAG);
        pnt.setColor(foreColor); // fore & back default
        GetRect();
        cls();
        Init();
        pnt.setStyle(Paint.Style.STROKE);
    }

    public void GetRect() {
        Rect rec = cnv.getClipBounds();
        w = rec.width();
        h = rec.height();
        x = y = 0;
    }

    void setBorder() {
        h -= SPV;
        x += SPH * 1.3;
        w -= x * 2;
    }

    public int getBorderX() {
        return x;
    }

    // public void GetRect0() { GetRect(); }
    public void setUnits(String s) {
        s_units = s;
    }

    public void Init() {
        off = 0;
        off_ant = 0;
        np = 0;
        n = 0;
        scaled = false;
        pDoAxis = true;
        Yscale = 1;
    }

    // find Max,Min & diff (return)
    int MinMax(short[] v, int n) {
        int i;
        for (Max = -Integer.MAX_VALUE, Min = Integer.MAX_VALUE, i = 0; i < n; i++) {
            if (v[i] > Max) {
                Max = v[i];
                pmax = i;
            }
            if (v[i] < Min) {
                Min = v[i];
                pmin = i;
            }
        }
        return (int) Math.abs(Max - Min);
    }

    // find Max,Min & diff (return)
    private double MinMax(double[] v, int n) {
        int i;
        for (Max = -Double.MAX_VALUE, Min = Double.MAX_VALUE, i = 0; i < n; i++) {
            if (v[i] > Max) {
                Max = v[i];
                pmax = i;
            }
            if (v[i] < Min) {
                Min = v[i];
                pmin = i;
            }
        }
        return Math.abs(Max - Min);
    }

    private double MinMax(double[] v, int n, int offset) {
        int i;
        for (Max = -Double.MAX_VALUE, Min = Double.MAX_VALUE, i = offset; i < n
                + offset; i++) {
            if (v[i] > Max) {
                Max = v[i];
                pmax = i;
            }
            if (v[i] < Min) {
                Min = v[i];
                pmin = i;
            }
        }
        return Math.abs(Max - Min);
    }

    // find Max,Min & diff (return)
    private float MinMax(float[] v, int n) {
        int i;
        for (Max = -Double.MAX_VALUE, Min = Double.MAX_VALUE, i = 0; i < n; i++) {
            if (v[i] > Max) {
                Max = v[i];
                pmax = i;
            }
            if (v[i] < Min) {
                Min = v[i];
                pmin = i;
            }
        }
        return (float) Math.abs(Max - Min);
    }

    private void ScaleGraph(double[] v, int nvp) {
        if (!scaled)
            Dif = MinMax(v, nvp);
        if (Dif == 0)
            Dif = 1;
        ix = (double) w / nvp;
        iy = (double) h / Dif; // step in X,Y
        //
        n = nvp;
        np = nvp;
        off = 0;
        off_ant = 0;
    }

    // Line graph a vector
    public void PlotGraph(double[] vx, double[] v, int nvp) {
        setBorder();
        ScaleGraph(v, nvp);
        if (pDoAxis)
            DrawAxis(v, vx, nvp);
        DrawLineGraph(v, nvp);
    }

    public void PlotGraph(double[] v, int nvp) {
        setBorder();
        ScaleGraph(v, nvp);
        if (pDoAxis)
            DrawAxis(v, null, nvp);
        DrawLineGraph(v, nvp);
    }

    public void PlotGraph(double[] v) {
        setBorder();
        int nvp = v.length;
        ScaleGraph(v, nvp);
        if (pDoAxis)
            DrawAxis(v, null, nvp);
        DrawLineGraph(v, nvp);
    }

    // draw Y only line graph
    private void DrawLineGraphSmall(double[] v, int nvp) {
        double px_ant = 0, py_ant = 0;
        // graph vector from (Min..Max) to (0..my)
        int i;
        for (i = 0, px = x, py = y; i < nvp; i++, px += ix) {
            py = y + h - (v[i] - Min) * iy;

            if (i != 0)
                cnv.drawLine((float) px_ant, (float) py_ant, (float) px,
                        (float) py, pnt);
            px_ant = px;
            py_ant = py;
        }
    }

    public int coordX2Index(int xcoord, double[] vP) // calc the index of a X
    // coord
    {
        int nvp = vP.length;
        float wv = (float) nvp / (float) w;
        return (int) ((xcoord - ((xcoord > x) ? x : xcoord)) * wv);
    }


    void DrawSoundAxis(float from_sec, int sampleRate, int nv) {
        int nHTics = w / 50; // ticks in H axis
        float sec_nv = ((float) nv / (float) sampleRate); // secs in nv values

        int ac = pnt.getColor();
        pnt.setColor(Color.YELLOW); // X,Y axis
        cnv.drawLine(x, y + h, x + w, y + h, pnt);
        cnv.drawLine(x, y, x, y + h, pnt);

        for (int i = 0, j = 0; j <= nHTics; i += w / nHTics, j++) {
            cnv.drawLine(x + i, y + h - 2, x + i, y + h + 2, pnt);
            String s = String.format("%3.1f", from_sec
                    + ((float) i / (float) w) * sec_nv);
            if (j >= nHTics)
                s += " (sec)";
            cnv.drawText(s, x + i - pnt.measureText(s) / 2,
                    y + h + pnt.measureText("0") * 2, pnt);
        }
        // ticks in V axis
        int nVTics = h / 40;
        if (nVTics == 0)
            nVTics = 1;
        for (int i = 0, j = 0; j <= nVTics; i += h / nVTics, j++)
            cnv.drawLine(x - 2, y + h - i, x + 2, y + h - i, pnt);
        pnt.setColor(ac);
    }

    public void drawShowValues(double[] v, int nv, int nform, double[] vals,
                               double[] xvals) {
        int col = pnt.getColor();
        pnt.setColor(Color.RED);
        Paint.Style st = pnt.getStyle();

        pnt.setStyle(Paint.Style.FILL);

        int j;
        for (int i = 0; i < nform; i++) {
            for (j = 0; v[j] != vals[i] && j < nv; j++) {
            } // find vals[i] in v
            if (v[j] == vals[i]) {
                px = ix * j;
                py = y + h - (v[j] - Min) * iy;
                cnv.drawCircle((float) px, (float) py, 4, pnt);
                cnv.drawText(String.format("%.2f", xvals[i]) + s_units,
                        (float) px + 5, (float) py + 10, pnt);
            }
        }
        pnt.setColor(col); // restore
        pnt.setStyle(st);
    }

    public void DrawLineGraph(double[] vP, int nvp) {
        double[] v, vm;
        // graph vector from (Min..Max) to (0..my)
        pnt.setColor(Color.CYAN);
        // big vector's

        if (nvp > w * 2) {
            v = new double[w + 10]; // __min
            vm = new double[w + 10]; // max pairs
            int mx = Integer.MIN_VALUE, mi = Integer.MAX_VALUE, cc = 0;
            for (int i = 0; i < nvp; i++, px += ix) {
                if (px < w) {
                    int cv = (int) (y + h - (vP[i] - Min) * iy);
                    if (cv > mx)
                        mx = cv;
                    if (cv < mi)
                        mi = cv;
                    if ((int) px != cc) { // index changes, advances 1
                        v[cc] = mi;
                        vm[cc] = mx; // assign __min,max pairs
                        cc = (int) px; // change counter
                        mx = Integer.MIN_VALUE;
                        mi = Integer.MAX_VALUE; // update limits
                    }
                }
            }
            v[cc] = mi;
            vm[cc] = mx;
            // plot v-vm lines
            for (int i = 0; i < w - 1; i++) {
                cnv.drawLine((float) (i + x), (float) v[i], (float) (i + x),
                        (float) vm[i], pnt);
                cnv.drawLine((float) (i + 1 + x), (float) vm[i],
                        (float) (i + 1 + x), (float) v[i + 1], pnt);
            }
        } else {
            this.DrawLineGraphSmall(vP, nvp);
        }
    }

    // Line graph a vector with only Y values
    public void PlotGraphY(double v[], int nvp) {
        ScaleGraph(v, nvp);
        if (pDoAxis)
            DrawAxis(v, null, nvp);
        DrawLineGraph(v, nvp);
    }

    public void cls() {
        cnv.drawColor(backColor);
    }

    public void setColor(int fg, int bg) {
        foreColor = fg;
        backColor = bg;
    }

    void DrawAxis(double[] v, double[] vx, int nvp) {
        // prepare axis
        pnt.setColor(Color.YELLOW);
        // draw them
        cnv.drawLine(x, y, x, y + h, pnt);
        cnv.drawLine(x, y + h, x + w, y + h, pnt);
        // ticks in H axis
        int nHTics = w / 50;
        axDec = 0;
        // if (vx==null)
        {
            if (nvp < 10000)
                axDec = 1;
            if (nvp < 1000)
                axDec = 2;
        } // decimals in axis
        for (int i = 0, j = 0; j <= nHTics; i += w / nHTics, j++) {
            cnv.drawLine(x + i, y + h - 2, x + i, y + h + 2, pnt);
            int ind = (j < nHTics) ? (j * nvp / nHTics) : (nvp - 1);
            if (ind >= nvp)
                ind = n - 1;
            String s;
            if (vx != null) {
                String fmt = String.format("%%5.%df", axDec);
                s = String.format(fmt, vx[ind]);
            } else
                s = String.format("%5d", ind);

            if (j >= nHTics)
                s += " (" + s_units + ")";
            cnv.drawText(s, x + i - pnt.measureText(s) / 2,
                    y + h + pnt.measureText("0") * 2, pnt);
            // grid to point
            if (GridToPoint) {
                int c = pnt.getColor();
                pnt.setColor(Color.BLUE);
                float f1 = x + i, f2 = (float) (y + h - (v[ind] - Min) * iy);
                cnv.drawLine((float) (x + i), (float) (y + h - 2), f1, f2, pnt);
                cnv.drawLine(f1, f2, (float) (x + 0),
                        (float) (y + h - (v[ind] - Min) * iy), pnt);
                pnt.setColor(c);
            }
        }

        // ticks in V axis
        int nVTics = h / 40;
        if (nVTics == 0)
            nVTics = 1;
        axDec = 0;
        if (Max < 10000)
            axDec = 1;
        if (Max < 1000)
            axDec = 2; // decimals in axis
        for (int i = 0, j = 0; j <= nVTics; i += h / nVTics, j++) {
            cnv.drawLine(x - 2, y + h - i, x + 2, y + h - i, pnt);
            double yv = Yscale * Min + j * Dif / nVTics;
            String s = String.format("%5.1f", yv);
            cnv.drawText(
                    s,
                    (float) (x - SPH),
                    (float) (y + h - i + ((j >= nVTics) ? 0 : textHeight(s) / 2)),
                    pnt);
        }

        // X axis
        if (Min < 0) {
            double t = y + h - (0.0 - Min) * iy;
            cnv.drawLine((float) x, (float) t, (float) (x + w), (float) t, pnt);
        }

        // Y axis when exist x[i]==0
        cnv.drawLine(x, y + h, x, y, pnt);
    }

    private int textWidth(String s) {
        Rect r = new Rect();
        pnt.getTextBounds(s, 0, s.length(), r);
        return r.width();
    }

    private int textHeight(String s) {
        Rect r = new Rect();
        pnt.getTextBounds(s, 0, s.length(), r);
        return r.height();
    }

    public void Title(String s) {
        int TxtSz = 20;
        float ts = pnt.getTextSize();
        int color = pnt.getColor();
        pnt.setTextSize(TxtSz);
        pnt.setColor(Color.GREEN);
        cnv.drawText(s, (float) ((w + x) - textWidth(s)), TxtSz, pnt);
        ;
        pnt.setTextSize(ts);
        pnt.setColor(color);
    }

    public void drawInfoLJX(String[] s, int nv) { // left just to maxlen
        int color = pnt.getColor();

        pnt.setColor(Color.YELLOW); // s.length > nv
        int mx = Integer.MIN_VALUE;
        for (int i = 0; i < nv; i++)
            if (textWidth(s[i]) > mx)
                mx = textWidth(s[i]);
        for (int i = 0; i < nv; i++)
            cnv.drawText(s[i], w - mx, (i + 2) * 20, pnt); // below title
        pnt.setColor(color);
    }

    public void drawInfo(String[] s, int nv) { // right just.
        int color = pnt.getColor();

        pnt.setColor(Color.YELLOW);
        for (int i = 0; i < nv; i++)
            cnv.drawText(s[i], w - textWidth(s[i]), (i + 2) * 20, pnt); // below
        // title
        pnt.setColor(color);
    }

    public void setFont(String fnt) {
        pnt.setTypeface(Typeface.create(fnt, Typeface.NORMAL));
    }


}
