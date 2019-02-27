package Signal;

public class Conf {
	static public int maxRate=44100, sampleRate=8000, sampleRatePlay=maxRate/4;
	static public int maxSecs=6;
	static public int nForm=8;
	static public int nFFT2=12, nFFTd2=nFFT2/2, nFFT=1<<nFFT2;
	static public int hvLow=80, hvHigh=1100, hvDiff=hvHigh-hvLow; // human voice range
}
