package graph;

import Signal.Signal;
import Signal.Conf;
import Signal.FFT;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class GraphRadial extends GraphBase { // plots Signal.sumfft
	private long delayMilliseconds=300;
	RadialSpectrum rs=new RadialSpectrum();
	public enum TypeRadial {humanRange, complete};
	TypeRadial type=TypeRadial.complete; // human 80..1100hz, type=1 complete

	public GraphRadial(Context context, AttributeSet attrs) {	super(context, attrs);	}
	
	@Override  protected void onDraw(Canvas canvas) { if (isInEditMode()) return;
	if (Signal.hasData) 
		switch (type) {
		case complete	:plotRadialComplete(canvas);	break;
		case humanRange	:plotRadialHuman(canvas); 		break;
		}
	}
	private void plotRadialHuman(Canvas canvas) {
		rs.setCanvas(canvas);
		int np=36;
		double[]vx=new double[np], vy=new double[Conf.hvDiff];
		for (int i=0; i<np; i++) vx[i]=Conf.hvLow+i*Conf.hvDiff/np;
		for (int i=0; i<Conf.hvDiff; i++) vy[i]=Signal.sumfft[FFT.freq2Index(Conf.hvLow+i, Conf.sampleRate, Conf.nFFT)];
		rs.RadialSpectrograph(rs.RadialFrame(vx, np), vy , Conf.hvDiff);
	}
	void plotRadialComplete(Canvas canvas) { // plot radial spectrum
		rs.setCanvas(canvas);
		int np=18, nf=Conf.nFFT;
		double[]vx=new double[np];
		for (int i=0; i<np; i++) vx[i]=FFT.index2Freq(i*(nf/np), Conf.sampleRate, nf);
		rs.RadialSpectrograph(rs.RadialFrame(vx, np), Signal.sumfft , nf);
	}
	double[]compressMinMax(double[]y, int n) { // compress vector y in n (min,max) segments
		double[]r=new double[n];
		int n2=n/2, l=y.length, sl=l/n2;
		for (int i=0, ir=0; i<l; i+=sl) {
			double max=-Double.MAX_VALUE, min=Double.MAX_VALUE;
			for (int j=0; j<sl & j+i<l; j++) {	if (y[j+i]>max) max=y[j+i]; if (y[j+i]<min) min=y[j+i]; }
			r[ir++ % n]=min; r[ir++ % n]=max;
		}
		return r;
	}
	public void refresh() {  postInvalidate(); /*Delayed(delayMilliseconds);*/	}
	public void setType(TypeRadial type) {	this.type = type;	}
}
