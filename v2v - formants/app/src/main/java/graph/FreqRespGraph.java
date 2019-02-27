package graph;

import java.util.Locale;

import Signal.CepstrumFormantsFreqRes;
import Signal.Conf;
import Signal.FormantItem;
import Signal.Signal;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class FreqRespGraph extends GraphBase {

	double[]x,y;
	public FreqRespGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Override  protected void onDraw(Canvas canvas) { 
		if (isInEditMode() || ! Signal.hasData) return;

		x=Signal.getFixedSamples(Conf.sampleRate * 2);
		if (x!=null) {
			Signal.cffr.calc(x);					// calc on samples
			FormantItem formants[]=Signal.cffr.getLPCformants();
			String[]strV=new String[formants.length];

			Graph gr=new Graph(canvas);
			gr.setUnits("kHz");
			gr.PlotGraph(Signal.cffr.getFreqsX(), Signal.cffr.getFreqz(), Signal.cffr.getFreqz().length);
//			gr.PlotGraph(Signal.sumInterp);
			gr.Title("Formants");
			for (int i=0; i<formants.length; i++)
				strV[i]=String.format(Locale.ENGLISH, "%6.1f hz, %.1f db\n", formants[i].hz, formants[i].pwr);
			gr.drawInfoLJX(strV, Math.min(strV.length, 5));
		}
	}
}
