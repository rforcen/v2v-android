package Signal;

import java.util.Arrays;
import java.util.Comparator;

public class Signal {
    public static Rec rec = new Rec();
    private static PlaySound play = new PlaySound();
    public static double[] yfft, sumfft, samples, fixedSamples, sumInterp;
    static FFT fft = new FFT();
    static int ifftFrame = 0, ifxs = 0;
    public static boolean hasData = false; // updated in Rec
    static int nSamples = 0;
    public static CepstrumFormantsFreqRes cffr;
    static double amp[], hz[];
    static PolynomialRegression polyReg;

    public static void startRecording() {
        yfft = new double[Conf.nFFT + 2]; // zero n,n+1 to avoid problems
        sumfft = new double[Conf.nFFT];
        sumInterp = new double[Conf.nFFT];
        samples = new double[Conf.nFFT];
        cffr = new CepstrumFormantsFreqRes(Conf.sampleRate, Conf.nFFT2);
        reset();
        rec.startRecording();
        hasData = false;
    }

    public static void stopAll() {
        rec.stopRecording();
        play.stopPlaying();
    }

    public static void recFFT() { // fft from next sample chunk
        for (int i = 0; i < Conf.nFFT; i++)
            samples[i] = yfft[i] = (double) rec.samplesBuffer[i + ifftFrame]; // yfft=rec.samplesBuffer
        processFFT();

        ifftFrame += Conf.nFFT;
        nSamples += Conf.nFFT;
        if (ifftFrame + Conf.nFFT >= rec.lSampBuff) ifftFrame = 0;
    }

    private static void processFFT() {
        fft.fft(yfft, Conf.nFFT2);
        for (int i = 0; i < FFT.freq2Index(60, Conf.sampleRate, Conf.nFFT); i++)
            yfft[i] = 0; // filter below 60hz
        fft.absScale(yfft, Conf.nFFT, 1);
        yfft[Conf.nFFT] = yfft[Conf.nFFT + 1] = 0;
        for (int i = 0; i < Conf.nFFT; i++)
            if (yfft[i] > .1) sumfft[i] += yfft[i]; // filter below 10%
        filterPeaks();
    }

    private static void polynomialInterpolate() {
        double[] xp = new double[Conf.nFFT];
        for (int i = 0; i < Conf.nFFT; i++) xp[i] = i; // interpolation polynomial
        polyReg = new PolynomialRegression(sumfft, 15);
        for (int i = 0; i < Conf.nFFT; i++) sumInterp[i] = Math.max(0, polyReg.predict(i));
    }

    private static void filterPeaks() {
        // filter peaks
        class Pair {
            public int i;
            public double v;

            public Pair(int i, double v) {
                this.i = i;
                this.v = v;
            }

            public Pair(Pair p) {
                i = p.i;
                v = p.v;
            }
        }
        Pair _peak[] = new Pair[Conf.nFFT];
        int ipk = 0;
        for (int nit = 0; nit < 3; nit++) {
            ipk = 0;
            for (int i = 1; i < Conf.nFFT - 2; i++) {
                if (sumfft[i] > sumfft[i - 1] && sumfft[i] > sumfft[i + 1]) {
                    sumfft[i - 1] = sumfft[i + 1] = 0;
                    _peak[ipk++] = new Pair(i, sumfft[i]);
                }
            }
        }
        Pair peak[] = new Pair[ipk];
        for (int i = 0; i < ipk; i++) peak[i] = new Pair(_peak[i]);
        Arrays.sort(peak, new Comparator<Pair>() {
            @Override
            public int compare(Pair p1, Pair p2) {
                return p1.v < p2.v ? 1 : 0;
            }
        });

        int np = Math.min(5, ipk);
        double xp[] = new double[np + 1], yp[] = new double[np + 1];
        for (int i = 0; i < np; i++) {
            xp[i] = peak[i].i;
            yp[i] = peak[i].v;
        }
        xp[np] = Conf.nFFT;
        yp[np] = 0;
    }

    public static void reset() {
        ifftFrame = nSamples = ifxs = 0;
        hasData = false;
        if (yfft != null) {
            Arrays.fill(yfft, 0);
            Arrays.fill(sumfft, 0);
            Arrays.fill(samples, 0);
        }
    }

    public static double[] getSamples() {
        return samples;
    }

    public static int getNsamples() {
        return nSamples;
    }

    public static double[] getFixedSamples(int n) {
        if (n >= nSamples) fixedSamples = null;
        else {
            if (n + ifxs < nSamples) {
                fixedSamples = new double[n];
                for (int i = 0; i < n; i++)
                    fixedSamples[i] = (double) rec.samplesBuffer[(i + ifxs) % rec.lSampBuff];
                ifxs += n;
            }
        }
        return fixedSamples;
    }

    public static void addListener(AsyncListener listener) {
        rec.addListener(listener);
    }

    public static void addListener() { // default listener -> calc fft from rec chuck
        addListener(new AsyncListener() {
            @Override
            public void onDataReady() {
                synchronized (this) {
                    recFFT();
                }
            }
        });
    }

    private static void generateV2V() { // in amp, hz
        FormantItem formants[] = Signal.cffr.getLPCformants();
        int fl = formants.length;
        if (fl == 0) return;
        amp = new double[fl];
        hz = new double[fl];
        double max = -Double.MAX_VALUE, min = Double.MAX_VALUE, diff, maxSh = Short.MAX_VALUE;
        for (int i = 0; i < fl; i++) {
            amp[i] = Math.pow(10, formants[i].pwr / 20.);
            hz[i] = formants[i].hz;
            max = Math.max(max, amp[i]);
            min = Math.min(min, amp[i]);
        }
        diff = Math.abs(max - min);
        for (int i = 0; i < fl && diff != 0; i++)
            amp[i] = (1. - ((amp[i] - min) / diff)) * maxSh;    // scale 0..1, mirror, adj Vol.
    }

    public static void startPlaying() {
        if (hasData) {
            generateV2V();
            play.prepareSound(Conf.sampleRatePlay, amp, hz);
            play.startPlaying();
        }
    }

    public static void stopPlaying() {
        play.stopPlaying();
    }

    public static boolean isPlaying() {
        return play.isPlaying();
    }
}
