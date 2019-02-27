package graph;

import Signal.ColorIndex;
import Signal.Conf;
import Signal.Signal;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class Recurrence extends  GraphBase  {
	Canvas canvas;
	Paint paint=new Paint();
	double[]x;
	private Scaler odd, even;
	private Bitset bs;
	int w,h;	// graph w*h
	int sw, sh; // set w*h
	float sx=1, sy=1, ww, hh; 
	double N;
	private boolean dirty;
	private IntSet is;

	public Recurrence(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
	}

	class IntSet {
		int w,n,ints[];
		ColorIndex colIx=new ColorIndex(256, Color.BLUE, Color.RED );
		public IntSet(int w) {this.w=w;  n=w*w; ints=new int[n];}
		public void set(int i, int j) {ints[j+i*w]++;} // works only in squared 
		public int get(int i, int j) {return ints[j+w*i];}
		public int count() { int s=0, n=w*w; for (int i=0; i<n; i++) s+=ints[i]==0 ? 0:1; return s;}
		public double rec() { return 100. * (double)count()/((double)w*h);}
		public int[]getInts() {return ints;}
		public Bitmap getBitmap() { // this changes ints to colors
			float max=-Float.MAX_VALUE; for (int i=0; i<n; i++) if (ints[i]>max) max=ints[i];
			for (int i=0; i<n; i++) ints[i]= ints[i]!=0 ? colIx.getColor(ints[i]/max) : 0;
			Bitmap bmp=Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
			bmp.setPixels(ints, 0, w, 0, 0, w, w);
			return bmp;
		}
	}
	class Bitset {
		final int sizeInt=32;
		private int w, h, n ,bits[], bRec[], d, r, bl;
		public Bitset(int w, int h) { this.w=w; this.h=h; bits=new int[bl=(w*h)/sizeInt]; }
		public Bitset(int n) { this.n=n; this.w=n; this.h=n; bits=new int[bl=(w*h)/sizeInt]; }
		public void set(int i, int j) 		{ calcDR(i,j);	bits[d] |= (1 << r);	}
		public void setRec(int i, int j) 	{ calcDR(i,j);	bRec[d] |= (1 << r);	}
		private void calcDR(int i, int j) 	{ int m=j*w; d=(m/sizeInt); r=(i % sizeInt); } // D=index, R=remainder in 32 bits
		public boolean get(int i, int j) 	{ calcDR(i,j); return ( bits[d] & (1 << r) ) != 0;}
		public boolean getRec(int i, int j) { calcDR(i,j); return ( bRec[d] & (1 << r) ) != 0;}
		public int count() { int s=0; for (int b:bits) s+=bitCount(b); 	return s;	}
		private int bitCount(int b) { int s=0;  for (int bs=1; bs!=0; bs<<=1) s += (b & bs) !=0 ? 1:0; return s; }
		public double recCount() { 	int s=0; for (int b:bRec) s+=bitCount(b); 	return s;	}
		public void recPoints() { // heaviside( eps=neighborhood radius - euclidean dist (xi xj) )
			int eps=3, e12=2*(eps+2), nnH=2*e12*e12/6; // neighborhood radius, 25% fill
			bRec=new int[bl];
			for (int i=0; i<n; i++) {
				for (int j=0, nn=0; j<n; j++, nn=0) { // count neighbours of (i,j)
					for (int ix=-eps; ix<eps; ix++) { 
						for (int iy=-eps; iy<eps; iy++) {
							if (ix==0 && iy==0) continue;
							int cx=i-ix, cy=j-iy; 
							if (cx<0 || cy<0 || cx>=n || cy>=n) continue;
							else {
								if (get(cx, cy)) nn++;
							}
						}
					}
					if (nn>=nnH) 
						setRec(i,j); // heaviside
				}
			}
			bits=null;
		}
	}
	class Scaler {
		double min, max, diff, ddim, x[];
		int dim, offset, vs[], xl, xl2;
		public Scaler scaleOdd(double[]x, int dim) 		{ return scale(this.x=x, this.dim=dim, offset=1); }
		public Scaler scaleEven(double[]x, int dim) 	{ return scale(this.x=x, this.dim=dim, offset=0); }
		Scaler scale(double[]x, int dim, int offset) {
			xl=x.length; xl2=xl/2;
			min=Double.MAX_VALUE; max=-min; diff=0;	// calc min,max, diff
			for (int i=0; i<xl-1; i+=2) {
				double xa=x[i+offset];
				if (xa > max) max=xa;
				if (xa < min) min=xa;
			}
			diff=max-min; diff=(diff!=0) ? diff:1;

			vs=new int[xl2]; ddim=dim-1;		// create 0..xl/2 scaled 0..dim-1
			for (int i=0, j=0; i<x.length-1; i+=2, j++) {
				double xa=x[i+offset];
				vs[j] = (int)( ddim * ((xa - min) / diff) );
			}
			return this;
		}
		int[] get() { return vs; }
		int get(int i) { return vs[i]; }
		int getSize() { return xl2; }
	}
	class LeastSquareFit {
		float[]calc(float[]x, float[]y) {
			int n=x.length;
			float sumx = 0, sumy = 0, sumxy = 0, sumxx = 0, slope, y_intercept;
			for (int i=0; i<n; i++) {
				sumx += x[i];			    sumy += y[i];
				sumxy += x[i]*y[i];		    sumxx += x[i]*x[i];
			}
			slope = ( sumx*sumy - n*sumxy ) / ( sumx*sumx - n*sumxx );
			y_intercept = ( sumy - slope*sumx ) / n;
			return new float[]{slope,y_intercept};
		}
		float[]calc(double[]x) { // recurrent data
			int n=x.length;
			float sumx = 0, sumy = 0, sumxy = 0, sumxx = 0, slope, y_intercept;
			for (int i=0; i<n-1; i+=2) {
				sumx += x[i];			    sumy += x[i+1];
				sumxy += x[i]*x[i+1];		sumxx += x[i]*x[i];
			}
			slope = ( sumx*sumy - n*sumxy ) / ( sumx*sumx - n*sumxx );
			y_intercept = ( sumy - slope*sumx ) / n;
			return new float[]{slope,y_intercept};
		}
		float[]calc(int[]x, int[]y) {
			int n=x.length;
			float sumx = 0, sumy = 0, sumxy = 0, sumxx = 0, slope, y_intercept;
			for (int i=0; i<n; i++) {
				sumx += x[i];			    sumy += y[i];
				sumxy += x[i]*y[i];		    sumxx += x[i]*x[i];
			}
			slope = ( sumx*sumy - n*sumxy ) / ( sumx*sumx - n*sumxx );
			y_intercept = ( sumy - slope*sumx ) / n;
			return new float[]{slope,y_intercept};
		}
	}
	@Override  protected void onDraw(Canvas canvas) { 
		if (isInEditMode()) return;
		this.canvas=canvas;
		Title("Recurrent");
		setw(160);
		x=Signal.getFixedSamples(w*w);
		if (x!=null) {
			setData(x, Conf.sampleRate, w);
			plot();
		}
	}
	private void setw(int n) { // set w to a max value if fits
		getwh();
		w=Math.min(w, Math.min(h, n));		
	}
	public void setData(double[]x) 			{ this.x=x; sx=sy=1; dirty=true;  }
	public void setData(double[]x, int sampleRate, int w) 	{ // get a sampleRate chunk to analyze
		if (x.length > sampleRate) {
			this.x=new double[sampleRate];  for (int i=0; i<sampleRate; i++) this.x[i]=x[i];
		} else this.x=x;
		this.ww=this.sw=this.w=w; this.hh=this.sh=this.h=w; calc(); 
	}
	void plot() 	{ plotCenteredBitSet(  ); }
	private void calc() 		{ calcBitSet(); }

	private void calcIntSet() { // with w,h
		odd=new Scaler().scaleOdd(x,w);		even=new Scaler().scaleEven(x,w);
		is=new IntSet(w);
		for (int i=0; i<even.getSize(); i++) is.set(even.get(i), odd.get(i));	
		dirty=false;
	}
	void plotIntSet() {
		getWH();
		if (dirty)  calcIntSet();  

		Bitmap bmp=Bitmap.createScaledBitmap( is.getBitmap(), w, h, true ); // draw a scaled flip bitmap
		Matrix flipHorizontalMatrix = new Matrix();
		flipHorizontalMatrix.setScale(-1,1);
		flipHorizontalMatrix.postTranslate(bmp.getWidth(),0);
		canvas.drawBitmap(bmp, flipHorizontalMatrix, paint);

		canvas.drawText(String.format("rec: %.1f", is.rec()), 10, 10, paint);
	}
	void plotBitSet() {
		getWH();
		if (dirty)  calcBitSet();  
		paint.setColor(Color.YELLOW);
		canvas.drawRect(0, 0, sw, sh, paint);
		for (int i=0; i<even.getSize(); i++) 
			if (bs.getRec(even.get(i), odd.get(i))) canvas.drawPoint(even.get(i), sh-odd.get(i), paint);
		float[]line=calcLineInterpolation();
		canvas.drawText(String.format("rec: %.1f, (%.2f X + %.2f)", calcREC(), line[0], line[1]), 10, 10, paint);
		drawInterpolationLine(line);
	}
	void plotCenteredBitSet() {
		getWH();
		if (dirty)  calcBitSet();  
		paint.setColor(Color.YELLOW);
		int xoff=(w-sw)/2, yoff=(h-sh)/2;
		canvas.drawRect(xoff, yoff, sw+xoff, sh+yoff, paint);
		for (int i=0; i<even.getSize(); i++) 
			if (bs.getRec(even.get(i), odd.get(i))) canvas.drawPoint(xoff+even.get(i), yoff+sh-odd.get(i), paint);
		float[]line=calcLineInterpolation();
		canvas.drawText(String.format("rec: %.1f, (%.2f X + %.2f)", calcREC(), line[0], line[1]), 10+xoff, 10+yoff, paint);
		paint.setColor(Color.RED);
		canvas.drawLine(xoff, yoff+sh-line[1], xoff+sw, yoff+sh-(line[0]*sw + line[1]), paint);
	}
	private void drawInterpolationLine(float[] line) {
		paint.setColor(Color.RED);
		canvas.drawLine(0, sh-line[1], sw, sh-(line[0]*sw + line[1]), paint);
	}
	private void calcBitSet() { // with w,h
		odd=new Scaler().scaleOdd(x,sw);		even=new Scaler().scaleEven(x,sw);
		bs=new Bitset(sw);
		for (int i=0; i<even.getSize(); i++) bs.set(even.get(i), odd.get(i));	
		bs.recPoints();
		N=(sw*sw);
		dirty=false;
	}
	private void getWH() { getwh(); if (!dirty) {sx=(float)w/ww; sy=(float)h/hh; }	}
	private void getwh() { Rect rec=canvas.getClipBounds(); w=rec.width(); h=rec.height(); 	}
	private void Title(String s) {
		int TxtSz = 20;
		float ts = paint.getTextSize();
		int color = paint.getColor();
		paint.setTextSize(TxtSz);
		paint.setColor(Color.GREEN);
		canvas.drawText(s, (float) (w - textWidth(s)), TxtSz, paint);
		paint.setTextSize(ts);
		paint.setColor(color);
	}
	private int textWidth(String s) {
		Rect r = new Rect();
		paint.getTextBounds(s, 0, s.length(), r);
		return r.width();
	}
	public double calcREC() { return 100. * (double)bs.recCount() / N; } // Recurrence Rate 
	public float[]calcLineInterpolation() {return new LeastSquareFit().calc(even.get(), odd.get()); } // lms interpolation line
}
