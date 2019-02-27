package graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class RadialSpectrum {
	public enum TrsGraphTypes { rsCenter, rsCircumference };
	double radius, Dif, Max, Min;
	public int      x,y,h,w; // graph draw area
	int 	pmax, pmin;
	Canvas	cnv;
	Paint	pnt;
	int		foreColor=Color.GREEN, backColor=Color.TRANSPARENT;
	
	// Usage:
	//	int np=36, nf=signal.nFFT/4;
	//	for (int i=0; i<np; i++) vx[i]=(nf/np)*i*SAMPLE_RATE/nf/16;
	//	gr.RadialSpectrograph(gr.RadialFrame(vx, np), y, (nFFT/8));
	//	gr.Title("Radial spectrum");

	public RadialSpectrum(Canvas cnv) 	{ this.cnv=cnv; init(); }
	public RadialSpectrum() 			{ init();}
	public void setCanvas(Canvas cnv) 	{ this.cnv=cnv; }
	
	public double RadialFrame(double []vx, int nvp)	  { return RadialFrame(vx, null, nvp, 0); }
	public double RadialFrame(String []strlbl, int nvp) { return RadialFrame(null, strlbl, nvp, 1);} // 1=string labels
	private void init() {
		pnt=new Paint(Paint.ANTI_ALIAS_FLAG);
		pnt.setStyle(Paint.Style.STROKE);
	}
	public void GetRect() {
		Rect rec=cnv.getClipBounds();
		w=rec.width(); 
		h=rec.height();
		x=y=0;
	}
	public void cls(){	cnv.drawColor(backColor);}
	double Radius()	{
		GetRect();
		double d=__min(h,w), r=d/2;
		return radius=r;
	}

	int __min(int x, int y) {return (x<y)?x:y;}
	int __max(int x, int y) {return (x>y)?x:y;}
	private int textWidth(String s) {
		Rect r=new Rect();
		pnt.getTextBounds(s, 0, s.length(), r);
		return r.width();		
	}
	private int textHeight(String s) {
		Rect r=new Rect();
		pnt.getTextBounds(s, 0, s.length(), r);
		return r.height();		
	}
	// find Max,Min & diff (return)
	private double MinMax(double []v, int n)
	{
		int i;
		for (Max=-Double.MAX_VALUE,Min=Double.MAX_VALUE, i=0; i<n; i++) {
			if (v[i] > Max) { Max=v[i]; pmax=i; }
			if (v[i] < Min) { Min=v[i]; pmin=i; }
		}
		return  Math.abs(Max-Min);
	}
	public double RadialFrame(double []vx, String []strlbl, int nvp, int type)
	{
		GetRect();
		int pw=4; // works fine with this size of input values

		cls();

		double d=__min(h,w), r=d/2, rm=__max(w,h)/2, dr=(rm-r), is=2*Math.PI/nvp,
				x0=w/2+x, 	 y0=h/2+y, ss=4, bs=2*ss, ri;
		r*=0.9; ri=r-15;

		pnt.setColor(Color.BLUE);

		RectF oval=new RectF((float)(x0-r), (float)(y0-r), (float)(x0+r),(float)(y0+r));
		cnv.drawArc(oval, 0f, 360, false, pnt); 
		oval.set((float)(x0-ri), (float)(y0-ri), (float)(x0+ri), (float)(y0+ri));
		cnv.drawArc(oval, 0f, 360, false, pnt); 

		for (int i=0; i<nvp; i++) {
			double iis=Math.sin(i*is);
			double iic=Math.cos(i*is);
			double xci= x0+ri*iis, yci= y0-ri*iic,
					xco= x0+r *iis, yco= y0-r*iic;

			pnt.setColor(Color.YELLOW);
			cnv.drawLine((float)xci,(float)yci,(float)xco,(float)yco,pnt);

			// string assign from vx[i]
			String s="";
			if (type==0) s=String.format("%.0f",vx[i]); // s=String.format("%4.0f",vx[i]);
			else if (strlbl!=null) s=strlbl[i];

			int dx=(int)(textWidth(s)), // calc where locate text 
					dy=(int)textHeight(s);
			xco= x0+(r+dy)*iis; yco= y0-(r+dy)*iic;
			cnv.drawText(s, (float)(xco-dx/2), (float)(yco-dy/2)+dy, pnt);
			pnt.setColor(Color.CYAN);
			for (int j=1; j<10; j++) {
				iis=i*is+j*is/10.;
				xci=x0+ri*Math.sin(iis); yci=y0-ri*Math.cos(iis);
				double c=ss;
				if (j%2!=0) c=ss; else c=bs;
				xco= x0+(ri+c)*Math.sin(iis);  yco= y0-(ri+c)*Math.cos(iis);
				cnv.drawLine((float)xci, (float)yci, (float)xco, (float)yco, pnt);
			}
		}
		return radius=ri;
	}
	class Contourn { // point contourn class
		PointF []cont;
		int ic=0;
		Contourn(int nvp) {
			cont=new PointF[nvp]; ic=0;
			for (int i=0; i<nvp; i++) cont[i]=new PointF();
		}
		void AddCont(double xv, double yv) {cont[ic].x=(float)xv; cont[ic].y=(float)yv; ic++; }
	}

	public void RadialSpectrograph(double []vy, int nvp)	            {	RadialSpectrograph(radius, TrsGraphTypes.rsCircumference, vy, nvp); }
	public void RadialSpectrograph(double rad, double []vy, int nvp)	{	RadialSpectrograph(rad,    TrsGraphTypes.rsCircumference, vy, nvp); }
	public void RadialSpectrograph(TrsGraphTypes gt, double []vy, int nvp){ RadialSpectrograph(__min(h,w)/2, gt, vy, nvp);}
	public void RadialSpectrograph(double r, TrsGraphTypes gt, double []vy, int nvp)	{

		Contourn cont=new Contourn(nvp);

		GetRect();
		Dif=MinMax(vy,nvp);
		if (Dif==0) Dif=1;
		if (Max==0) return; // nothing to do

		double is=2*Math.PI/nvp,
				x0=w/2+x, y0=h/2+y, r13=r/3., r23=2.*r/3.;

		RectF oval=new RectF( (float)(x0-r), (float)(y0-r), (float)(x0+r), (float)(y0+r)); 
		cnv.drawArc(oval, 0f, 360f, false, pnt);

		pnt.setColor(Color.CYAN);
		for (int i=0; i<nvp; i++) {
			double incr=r*(vy[i]/Max) , ri=r*(1-(vy[i]/Max)),
					s=Math.sin(i*is) , c=Math.cos(i*is),
					xci=x0 + r  * s,  yci=y0 - r  * c,
					xri=x0 + ri * s,  yri=y0 - ri * c,
					xri1, yri1;

			switch (gt) {
			case rsCircumference:
				float xfr=(float)xci, yfr=(float)yci;  // radial from circumference perimeter
				pnt.setColor(Color.RED);
				if (incr<=r13) {
					cnv.drawLine((float)xfr, (float)yfr, (float)xri, (float)yri, pnt);
					cont.AddCont(xri, yri);
				} else {
					xri1=x0 + (r23) * s; // line 1/3 blue
					yri1=y0 - (r23) * c;
					cnv.drawLine((float)xfr, (float)yfr, (float)xri1, (float)yri1, pnt);
					pnt.setColor(Color.YELLOW);

					if (incr<=r23) {  //  line 2/3
						xri1+=(1-(incr-r13)) * s;
						yri1-=(1-(incr-r13)) * c;
						cnv.drawLine((float)xfr, (float)yfr, (float)xri1, (float)yri1, pnt);
						cont.AddCont(xri1,yri1);
					} else {                      // line > 2/3

						xri1+= (1-r13) * s;
						yri1-= (1-r13) * c;
						cnv.drawLine((float)xfr, (float)yfr, (float)xri1, (float)yri1, pnt);
						pnt.setColor(Color.CYAN);
						xri1+=(1-(incr-r23)) * s;
						yri1-=(1-(incr-r23)) * c;
						cnv.drawLine((float)xfr, (float)yfr, (float)xri1, (float)yri1, pnt);
						cont.AddCont(xri1,yri1);
					}
				}
				break;
			case rsCenter:
				cnv.drawLine((float)x0, (float)y0, (float)xri, (float)yri, pnt);
				break;
			}
		}
		// frame with circle.
		pnt.setColor(Color.CYAN);
		oval=new RectF( (float)(x0-r), (float)(y0-r), (float)(x0+r), (float)(y0+r)); 
		cnv.drawArc(oval, 0f, 360f, false, pnt);
		// frame values
		for (int i=1; i<nvp-1; i++) 
			cnv.drawLine(cont.cont[i].x,cont.cont[i].y, cont.cont[i+1].x,cont.cont[i+1].y, pnt);
		cnv.drawLine(cont.cont[0].x,cont.cont[0].y, cont.cont[nvp-1].x,cont.cont[nvp-1].y, pnt);
	}

	// no lables in grid. value MAXINT is no label
	public void RadialGrid(int n, int from)
	{
		GetRect();
		double r=__min(h,w)/2, is=2.*Math.PI/n, x0=w/2+x, y0=h/2+y, r23=2.*r/3., s,c,xc,yc;
		r*=0.9;

		pnt.setColor(Color.YELLOW);
		//		cnv->Font->Color=clYellow;
		for (int i=0; i<n; i++) {
			s=Math.sin(i*is); c=Math.cos(i*is);
			xc=x0 + r  * s;  yc=y0 - r  * c;
			cnv.drawLine((float)x0, (float)y0, (float)xc, (float)yc, pnt);
			s=Math.sin(((2*i+1)/2.)*is); c=Math.cos(((2*i+1)/2.)*is);
			xc=x0 + r23  * s;  yc=y0 - r23  * c;
			if (from!=Integer.MAX_VALUE) 
				cnv.drawText(String.valueOf(from++), (float)xc, (float)yc, pnt); // MAXINT is no label
		}
	}

	public void RadialGrid(int n)
	{
		GetRect();
		double r=__min(h,w)/2, is=2.*Math.PI/n, x0=w/2, y0=h/2;

		pnt.setColor(Color.CYAN);
		for (int i=0; i<n; i++) {
			double s=Math.sin(i*is) , c=Math.cos(i*is),
					xc=x0 + r  * s,  yc=y0 - r  * c;
			cnv.drawLine((float)x0, (float)y0, (float)xc,(float)yc, pnt);
		}
	}
}
