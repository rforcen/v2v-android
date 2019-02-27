//------------------------------------------------------------------------------
// FastSin
// calc y(n) = Math.sin(n*W+B)
// W, B in RAD
// usage FastSin fs(a,w,b)
//

package Signal;


public class FastSin {
	double y0, y1, y2, p;
	double Zfade, xfade;

	double cW, cB, cA, x;
	double n;
	boolean isInit;


	public FastSin() { isInit=false; }
	public FastSin(double a, double w, double b) { init(a,w,b); }
	public FastSin(double amp, double Hz, double Samp, double phase) {   init(amp, Hz, Samp, phase); }

	public boolean isInit() { return isInit; }
	public void init(double amp, double Hz, double Samp, double phase) { // init and save for reset
		isInit=true;
		cA=amp; cW=Freq2Inc(Hz,Samp); cB=phase;
		y0 = Math.sin(-2*cW + cB);  y1 = Math.sin(-cW + cB);  p = 2.0 * Math.cos(cW);
		n=-1;   x=0;
	}

	public void init(double a, double w, double b) { // init and save for reset
		isInit=true;
		cA=a; cW=w; cB=b;
		y0 = Math.sin(-2*w + b);  y1 = Math.sin(-w + b);  p = 2.0 * Math.cos(w);
		n=-1;  x=0;
	}

	public void reset() { // start n=s0;
		init(cA,cW,cB);
	}

	// set amplitude.
	public void SetAmp(double amp)
	{
		cA=amp;
	}

	// Math.sin(wt+phase + x)
	public double calcSin(double px)
	{
		n++;  x=n*cW;
		return cA*Math.sin(cW*n+cB + px);
	}

	public double calc()
	{
		n++; x=n*cW;
		y2 = p*y1 - y0;
		y0 = y1;
		y1 = y2;
		return cA*y2;   // mutl by amp.
	}

	public double Freq2Inc(double freq, double samp)
	{
		return freq*2.*Math.PI/samp;
	}

	// fill a short int mono buffer
	public void FillBuffer(short [] sbuff, int sb)
	{
		for (int i=0; i<sb; i++) { //
			sbuff[i] =(short)calc();
		}
	}

	public double initFade(double samp, double sec)
	{
		return  xfade=Zfade=Math.pow(10,-5/(samp*sec));
	}

	public double Fade()
	{
		xfade*=Zfade;
		return xfade;
	}

}

