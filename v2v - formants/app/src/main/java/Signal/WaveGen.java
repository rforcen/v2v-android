/*
	WaveGen class
*/

package Signal;


public class WaveGen  {

    private static final int CHAN_LEFT=1, CHAN_RIGHT=2;
    public  static final int STEREO=2, MONO=1;

    int chan;
    double Rphase, Samp, _amp, pulse;
    FastSin[] fsL, fsR, fsA;
    double[] amp, Hz, phase,  Sinc, Ainc;
    double t, inc;

    public int n; // number of sine waves
    public double cycle; // duration until re-start in secs
    public double Ccycle;
    public double fade_factor; // -0.2 is suitable for a 5 sec duration


public WaveGen(){_init();};
public WaveGen(int pchan, double pSamp){_init(); chan=pchan; Samp=pSamp; };

void _init()
{ n=0; chan=0; Rphase=0; 
  fade_factor=-0.2; cycle=5; Ccycle=0;
}

public void SetDif(double prPH)
{
  Rphase=prPH;
}

public void SetL(double []Pamp, double []PHz, double []Pphase, int nv)
{
  n=nv;
  amp=Pamp; Hz=PHz; phase=Pphase; t=0; inc=Hz[0] * 2.0 *Math.PI/Samp;
  fsL=new FastSin[n]; 
  for (int i=0; i<n; i++) {
      fsL[i]=new FastSin();
      fsL[i].init(amp[i], Hz[i], Samp, (phase!=null)?phase[i]:0.0);
  }
}

public void SetR(double []Pamp, double []PHz, double []Pphase, int nv)
{
  n=nv;
  amp=Pamp; Hz=PHz; phase=Pphase; t=0; inc=Hz[0] * 2.0 * Math.PI/Samp;
  fsR=new FastSin[n];
  for (int i=0; i<n; i++) {
      fsR[i]=new FastSin();
      fsR[i].init(amp[i], Hz[i],        Samp, (phase!=null)?phase[i]:0);
  }
}

public void SetAmp(double []Pamp, int chan)
{
  amp=Pamp;
  for (int i=0; i<n; i++) {
      if ((chan&CHAN_LEFT )!=0) fsL[i].SetAmp(amp[i]);
      if ((chan&CHAN_RIGHT)!=0) fsR[i].SetAmp(amp[i]);
  }
}

public void SetAmp(double []Pamp)
{
  amp=Pamp;
  for (int i=0; i<n; i++) {
      fsL[i].SetAmp(amp[i]);
      fsR[i].SetAmp(amp[i]);
  }
}

public void Pulse(double hz)
{
 for (int i=0; i<n; i++) {
     fsA[i].init(amp[i], hz, Samp, 0);
 }
}

public void Set(double []Pamp, double []PHz, double []Pphase, int nv)
{
  n=nv;
  amp=Pamp; Hz=PHz; phase=Pphase;

  fsL=new FastSin[n]; fsR=new FastSin[n];
  Sinc=new double[n];  Ainc=new double[n];
  Ccycle=0;

  for (int i=0; i<n; i++) {
      fsL[i]=new FastSin();	fsR[i]=new FastSin();
      fsL[i].init(amp[i], Hz[i],        Samp, (phase!=null)?phase[i]:0);
      fsR[i].init(amp[i], Hz[i]+Rphase, Samp, (phase!=null)?phase[i]:0);
      Sinc[i]=1*(2*Math.PI/Samp);
      Ainc[i]=0;
  }
}

// only 1 pitch with am+fm combinated effect
// sin(pulse*t) * sin(pitch*t + sin(2*t*pulse))
//    AM              pitch          FM
public void Set(double[]Pamp, double[]PHz, double Ppulse)
{
  amp=Pamp; Hz=PHz; pulse=Ppulse;
  t=0; inc=Hz[0] * 2.0 *Math.PI/Samp; n=1;

  fsL=new FastSin[1]; fsR=new FastSin[1];

  fsL[0]=new FastSin();	fsR[0]=new FastSin();
  fsL[0].init(amp[0], pulse ,  Samp, 0); // AM component
  fsR[0].init(1     , pulse*2, Samp, 0); // FM component
}

public void gen(short []sbuff)
{
  int sb=sbuff.length;
  
  switch (chan) { 
  case 2: // stereo
   
   for (int i=0; i<sb; i+=2) { // +2 stereo device
     double yL=0, yR=0;
     for (int j=0; j<n; j++) {
      yL+=fsL[j].calc();
      yR+=fsR[j].calc();
     } yL/=n; yR/=n;
     sbuff[i+0] =(short)yL;
     sbuff[i+1] =(short)yR;
   }
  break;
  case 1:
   for (int i=0; i<sb; i+=1) { // +1 mono device
     double y=0;
     for (int j=0; j<n; j++) {
      y+=fsL[j].calc();
     } y/=n; 
     sbuff[i] =(short)y;
   }
   break;
  }
  
}

public void genAM(short []sbuff, int sb)
{
  if (chan==2) { // stereo
   for (int i=0; i<sb; i+=2) { // +2 stereo device

     for (int j=0; j<n; j++) {
          fsL[j].SetAmp(fsA[j].calc());
          fsR[j].SetAmp(fsA[j].calc());
     }
     double yL=0, yR=0;
     for (int j=0; j<n; j++) {
      yL+=fsL[j].calc();
      yR+=fsR[j].calc();
     } yL/=n; yR/=n;
     sbuff[i+0] =(short)yL;
     sbuff[i+1] =(short)yR;
   }
  }
}

public void genAMFM(short []sbuff, int sb)
{
  if (chan==2) { // stereo
   for (int i=0; i<sb; i+=2) { // +2 stereo device
     double yL;
     yL=fsL[0].calc() * Math.sin( t + fsR[0].calc() );
     t+=inc;
     sbuff[i+0] =(short)yL;   sbuff[i+1] =(short)yL;
   }
  }
}


}

