package Signal;

import java.util.Arrays;
import java.util.Comparator;
import org.jscience.mathematics.number.Complex;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class CepstrumFormantsFreqRes {

	public int sampleRate;
	private int nFFT2 = 10; // 2^14=16k
	public int nFFT = 1 << nFFT2;
	
	double maxCepstrumHz, cepstrum[], freqz[], freqsX[];
	FormantItem formants[];
	
	public CepstrumFormantsFreqRes(int sampleRate, int nFFT2) {
		this.sampleRate=sampleRate;
		this.nFFT2=nFFT2;
		nFFT = 1 << nFFT2;
	}
	public double[]getCepstrum() 			{ return cepstrum;				}
	public double getMaxCepstrumHz() 		{ return this.maxCepstrumHz; 	}
	public FormantItem[]getLPCformants() 	{ return this.formants;			}
	public double[]getFreqz() 				{ return this.freqz; 			}
	public double[]getFreqsX()				{ return this.freqsX; 			}
	
	// checked against :
		// http://www.phon.ucl.ac.uk/courses/spsci/matlab/lect10.html
		// http://iitg.vlab.co.in/?sub=59&brch=164&sim=615&cnt=1
		// calcs: double maxCepstrumHz, cepstrum[], freqz[]; FormantItem formants[]
		public void calc(double[]samps) { 
			if (samps==null) return;

			SignalWindow ham=new SignalWindow();

			int nSamples=nFFT; 
			double[]samples=new double[nSamples]; 		// get nSamples from samps -> samples
			for (int i=0; i<nSamples; i++) samples[i]=samps[i]/(double)Short.MAX_VALUE; samps=null; // scale to range -1..+1
			DoubleFFT_1D fft1d=new DoubleFFT_1D(nSamples);

			// cepstrum
			double[]hammWin=ham.generate(SignalWindow.WindowType.HAMMING, nSamples);	// hamming window(nSamples)
			double[]y=new double[nSamples];
			for (int i=0; i<nSamples; i++) y[i]=hammWin[i] * samples[i];				// y = samples * hamming
			double[]yfft=new double[2*nSamples];
			for (int i=0; i<nSamples; i++) { yfft[i*2+0]=y[i]; yfft[2*i+1]=0; } 		// yfft=y
			fft1d=new DoubleFFT_1D(nSamples);
			fft1d.complexForward(yfft);
			for (int i=0; i<nSamples; i++) yfft[i]=Math.sqrt(yfft[i*2+0]*yfft[i*2+0] + yfft[i*2+1]*yfft[i*2+1]); 
			double eps=1e-16;					// cepstrum is DFT of log spectrum, i.e. the spectrum of a spectrum
			cepstrum=new double[nSamples*2];
			for (int i=0; i<nSamples; i++) cepstrum[i*2+0]=Math.log(yfft[i]+eps);
			fft1d.complexForward(cepstrum);
			for (int i=0; i<nSamples; i++) cepstrum[i]=Math.sqrt(cepstrum[i*2+0]*cepstrum[i*2+0] + cepstrum[i*2+1]*cepstrum[i*2+1]); 
			double max=-Double.MAX_VALUE; int mix=0;
			int ms1=sampleRate/1000, ms2=sampleRate/50;
			for (int i=ms1; i<ms2; i++) {
				double v=cepstrum[i];
				if (v>max) { max=v; mix=i; }
			}
			maxCepstrumHz = (mix!=0) ? (double)sampleRate /  ((double)mix+1)  : 0;				// max cepstrum freq
			double sfft[]=new double[nSamples*2];												// sfft = fft(samples)
			for (int i=0; i<nSamples; i++) sfft[i*2]=samples[i];
			fft1d.complexForward(sfft);
			for (int i=0; i<nSamples; i++) sfft[i]=Math.sqrt(sfft[i*2+0]*sfft[i*2+0] + sfft[i*2+1]*sfft[i*2+1]); 
			scale(sfft, 100);

			// formants
			int ncoeff=2+sampleRate/1000; 														// LPC coeff
			double []lpcCoeff=LPC.calcCoeff(ncoeff, samples);
			Complex[]roots=ComplexPolynomial.roots( ComplexPolynomial.complexArray(lpcCoeff) );	// poly roots

			double srDivpi2=(double)sampleRate/(Math.PI*2), srDivpi=(double)sampleRate/Math.PI;
			double hz, bw, pwr;
			FormantItem frmt[]=new FormantItem[roots.length]; int ifr=0;
			for (int i=0; i<roots.length; i++) {												// only look for roots >0Hz up to fs/2
				if (roots[i].getImaginary() >= 0.01) {
					hz=srDivpi2 * Math.atan2(roots[i].getImaginary(), roots[i].getReal());
					bw=srDivpi  * Math.log(roots[i].magnitude());								// bw = -*(Fs/pi)*log(abs(rts(indices)));
					if (hz > 80 & bw < 400) 													// formant frequencies should be greater than 80 Hz with bandwidths less than 400 Hz 
						frmt[ifr++]=new FormantItem(hz, bw, 0); 
				} 
			} 
			formants=new FormantItem[ifr]; for (int i=0; i<ifr; i++) formants[i]=frmt[i]; 	// copy ifr items frm=frmt
			Arrays.sort(formants, new Comparator<FormantItem>(){ 									// sort by hz, also by power would be a choice
				@Override public int compare(FormantItem f1, FormantItem f2) {	return (f1.hz > f2.hz) ? 1:-1; }});

			// frequency response freqz is the fft(output lpc filtered signal) / fft(input)
			int n=nSamples, N=n*2;
			double a[]=new double[N*2];
			for (int i=0; i<lpcCoeff.length; i++) a[i*2]=lpcCoeff[i];
			fft1d=new DoubleFFT_1D(N);
			fft1d.complexForward(a);
			
			Complex cOne=Complex.valueOf(1, 0);
			freqz=new double[n]; // n Symmetrical
			for (int i=0; i<n; i++) {				// 20 * log10 ( abs( 1 / fft(lpcCoeff : N) ) )
				Complex ch=cOne.divide( Complex.valueOf(a[i*2+0], a[i*2+1]) );
				freqz[i]=20.0 * Math.log10( ch.magnitude() + eps );
			}
			
			freqsX=new double[n];	// get freq vector in kHz
			for (int i=0; i<n; i++) freqsX[i]=sampleRate * ((double)i/(double)N) / 1000.; // in kHz
			
			for (int i=0; i<ifr; i++) {		// update power in formants from freqz
				int j;
				double hzf=formants[i].hz / 1000.;	// fin this in freqsX
				for (j=0; j<n && freqsX[j]<hzf; j++);
				if (j<n) formants[i].pwr = freqz[j];
			}
		}
		private void scale(double[] y, double sc) {
			double max=-Double.MAX_VALUE;
			for (double v:y) max=Math.max(v, max); sc/=max;
			for (int i=0; i<y.length; i++) y[i]*=sc;
		}
}
