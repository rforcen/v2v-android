package Signal;

public class FFT {

	// max fft items
	private int L2MAX_LEN = 19;
	private int MAX_LEN = (1 << L2MAX_LEN);
	private double TWOPI = Math.PI * 2;
	private double SQRT2 = Math.sqrt(2);

	private int last_n = 0;
	private int last_m = 0;
	double[] x1 = new double[MAX_LEN + 2];
	public double coherence; // calc coherence &
	int nform; // nform
	public double PowMax; // power of max hz
	public int PosMax; // position of max
	public double HzMax; // scaled values in power & Hz '&&'

	double[][] ccc1 = new double[L2MAX_LEN + 1][];
	double[][] sss1 = new double[L2MAX_LEN + 1][];
	double[][] ccc3 = new double[L2MAX_LEN + 1][];
	double[][] sss3 = new double[L2MAX_LEN + 1][];

	public FFT() {
	}

	// m= 1 2 3 4 5 6 7 8 9 10 11...
	// works with power of 2: 2,4,8,16,32,64,128,256,512,1024,2048...
	// m=2^n
	// y[n+2] len important !!
	//
	public void fft(double[] y, int m) {
		int j = 1, is, id, k, n1, i, i0, n2, n4, n8, i1, i2, i3, i4, i5, i6, i7, i8;
		int n;
		double xt, r1, t1, t2, t3, t4, t5, t6, cc1, ss1, cc3, ss3;
		double e, a, a3;

		// calc n=2^m
		n = 1 << m;

		/******* Check transform length *********************************************/
		if (n > MAX_LEN || m > L2MAX_LEN)
			return;

		/******* Compute table of sines/cosines *************************************/
		if (n != last_n) // Transform size has changed
		{
			n2 = 4;
			for (k = 3; k <= m; k++) // Get space for new ones
			{
				n2 <<= 1;
				n8 = n2 >> 3;
				e = a = TWOPI / n2;
				sss1[k] = new double[n8 + 1];
				sss3[k] = new double[n8 + 1];
				ccc1[k] = new double[n8 + 1];
				ccc3[k] = new double[n8 + 1];
				if (sss1[k] == null || sss3[k] == null || ccc1[k] == null
						|| ccc3[k] == null)
					return;
				for (j = 2; j <= n8; j++) {
					a3 = a + a + a;
					ccc1[k][j] = Math.cos(a);
					sss1[k][j] = Math.sin(a);
					ccc3[k][j] = Math.cos(a3);
					sss3[k][j] = Math.sin(a3);
					a += e;
				}
			}
			last_n = n; // Update last size
			last_m = m;
		}

		// Copy items to temp. array
		x1 = y.clone();

		// Direct transform
		// Digit reverse counter
		{
			j = 1;
			n1 = n - 1;
			for (i = 1; i <= n1; i++) {
				if (i < j) {
					xt = x1[j - 1];
					x1[j - 1] = x1[i - 1];
					x1[i - 1] = xt;
				}
				k = n >> 1; /* n/2; */
				while (k < j) {
					j -= k;
					k >>= 1; /* k/=2 */
				}
				j += k;
			}

			// Length two butterflies

			is = 1;
			id = 4;
			do {
				for (i0 = is; i0 <= n; i0 += id) {
					i1 = i0 + 1;
					r1 = x1[i0 - 1];
					x1[i0 - 1] = r1 + x1[i1 - 1];
					x1[i1 - 1] = r1 - x1[i1 - 1];
				}
				is = (id << 1) - 1;
				id <<= 2; /* id *= 4; */
			} while (is < n);

			/*************** L-shaped butterflies **************************************/

			n2 = 2;
			for (k = 2; k <= m; k++) {
				n2 <<= 1; /* n2 *= 2; */
				n4 = n2 >> 2; /* n2/4; */
				n8 = n2 >> 3; /* n2/8; */
				is = 0;
				id = n2 << 1;
				do {
					for (i = is; i < n; i += id) {
						i1 = i + 1;
						i2 = i1 + n4;
						i3 = i2 + n4;
						i4 = i3 + n4;
						t1 = x1[i4 - 1] + x1[i3 - 1];
						x1[i4 - 1] -= x1[i3 - 1];
						x1[i3 - 1] = x1[i1 - 1] - t1;
						x1[i1 - 1] += t1;
						if (n4 != 1) {
							i1 += n8;
							i2 += n8;
							i3 += n8;
							i4 += n8;
							t1 = (x1[i3 - 1] + x1[i4 - 1]) / SQRT2;
							t2 = (x1[i3 - 1] - x1[i4] - 1) / SQRT2;
							x1[i4 - 1] = x1[i2 - 1] - t1;
							x1[i3 - 1] = -x1[i2 - 1] - t1;
							x1[i2 - 1] = x1[i1 - 1] - t2;
							x1[i1 - 1] += t2;
						}
					}
					is = (id << 1) - n2;
					id <<= 2; /* id *= 4; */
				} while (is < n);
				for (j = 2; j <= n8; j++) {
					cc1 = ccc1[k][j];
					ss1 = sss1[k][j];
					cc3 = ccc3[k][j];
					ss3 = sss3[k][j];
					is = 0;
					id = n2 << 1;
					do {
						for (i = is; i < n; i += id) {
							i1 = i + j;
							i2 = i1 + n4;
							i3 = i2 + n4;
							i4 = i3 + n4;
							i5 = i + n4 - j + 2;
							i6 = i5 + n4;
							i7 = i6 + n4;
							i8 = i7 + n4;
							t1 = x1[i3 - 1] * cc1 + x1[i7 - 1] * ss1;
							t2 = x1[i7 - 1] * cc1 - x1[i3 - 1] * ss1;
							t3 = x1[i4 - 1] * cc3 + x1[i8 - 1] * ss3;
							t4 = x1[i8 - 1] * cc3 - x1[i4 - 1] * ss3;
							t5 = t1 + t3;
							t6 = t2 + t4;
							t3 = t1 - t3;
							t4 = t2 - t4;
							t2 = x1[i6 - 1] + t6;
							x1[i3 - 1] = t6 - x1[i6 - 1];
							x1[i8 - 1] = t2;
							t2 = x1[i2 - 1] - t3;
							x1[i7 - 1] = -x1[i2 - 1] - t3;
							x1[i4 - 1] = t2;
							t1 = x1[i1 - 1] + t5;
							x1[i6 - 1] = x1[i1 - 1] - t5;
							x1[i1 - 1] = t1;
							t1 = x1[i5 - 1] + t4;
							x1[i5 - 1] -= t4;
							x1[i2 - 1] = t1;
						}
						is = (id << 1) - n2;
						id <<= 2; /* id *=4; */
					} while (is < n);
				}
			}
			/******* Rerrange data as in program header, copy to y **********************/
			j = n >> 1; /* n/2 */
			y[0] = x1[0];
			y[1] = 0.0;
			y[n] = x1[j];
			y[n + 1] = 0.0;
			for (i = 1, k = 2; i < j; i++, k += 2) {
				y[k] = x1[i];
				y[k + 1] = x1[n - i];
			}
		}
	}

	// index <--> freq conversions
	public static double index2Freq(int i, double samples, int nFFT) {
		return (double) i * (samples / nFFT / 2.);
	}

	public static int freq2Index(double freq, double samples, int nFFT) {
		return (int) (freq / (samples / nFFT / 2.0));
	}

	// absulute values & scale to
	public double absScale(double[] vfft, int n) {
		return absScale(vfft, n, 100.);
	}

	public double absScale(double[] vfft, int n, double scale) {
		double max = Double.MIN_VALUE;
		for (int i = 0; i < n; i++) {
			double v = vfft[i];
			// if (v<0) v=-v;
			if (v < 0)
				v = 0; // remove neg. values
			if (v > max)
				max = v;
			vfft[i] = v;
		}
		if (max == 0)
			return 1;
		for (int i = 0; i < n; i++) {
			vfft[i] = scale * vfft[i] / max;
		}
		return max; // ??
	}

	private class Tpm {
		int p;
		double v;
		double ph;

		public void assign(Tpm t) {
			p = t.p;
			v = t.v;
			ph = t.ph;
		}
	} // formants pos.

	public void quickSortInDescendingOrder(Tpm[] numbers, int low, int high) {
		int i = low;
		int j = high;
		double middle = numbers[(low + high) / 2].v;

		while (i < j) {
			while (numbers[i].v > middle) {
				i++;
			}
			while (numbers[j].v < middle) {
				j--;
			}
			if (j >= i) {
				Tpm temp = new Tpm();
				temp.assign(numbers[i]);
				numbers[i].assign(numbers[j]);
				numbers[j].assign(temp);
				i++;
				j--;
			}
		}
		if (low < j) {
			quickSortInDescendingOrder(numbers, low, j);
		}
		if (i < high) {
			quickSortInDescendingOrder(numbers, i, high);
		}
	}

	// calculates 'n' formants of a fft value
	// needs a smooth fft vector.
	int formants(double[] vfft, double[] HzFrm, double[] PwrFrm,
			double[] phFrm, int n, int nform) {
		Tpm[] PosMax;
		int i;
		int p, ip = 0;

		if (nform == 0)
			return 0;

		PosMax = new Tpm[nform];
		for (i = 0; i < nform; i++)
			PosMax[i] = new Tpm();

		// after smooth we've got: 0..0..local_max1..0..0..local_max2....
		for (i = 3; i < n; i++) {
			while (vfft[i] == 0 && i < n - 3)
				i++; // skip ceros
			double max;
			for (max = Double.MIN_VALUE, p = -1; vfft[i] != 0 && i < n - 3; i++)
				// local max
				if (vfft[i] > max) {
					max = vfft[i];
					p = i;
				}
			if (p != -1) { // found something...?
				PosMax[ip].p = p;
				PosMax[ip].v = max;
				if (phFrm != null)
					PosMax[ip].ph = phFrm[p]; // save local max
				if (++ip >= nform)
					ip--;
			}
		}
		// find 'nform' greater max (formants)
		quickSortInDescendingOrder(PosMax, 0, PosMax.length - 1);

		if (ip < nform)
			nform = ip; // at least 'nform' values.
		for (i = 0; i < nform; i++) {
			HzFrm[i] = PosMax[i].p; // pos of fft to convert to Hz.
			PwrFrm[i] = PosMax[i].v; // already in % max power
			if (phFrm != null)
				phFrm[i] = PosMax[i].ph;
		}
		return nform;
	}

	int formants(double[] vfft, double[] HzFrm, double[] PwrFrm, int n,
			int nform) {
		return formants(vfft, HzFrm, PwrFrm, null, n, nform);
	}

	// ---------------------------------------------------------------------------
	// ProcessFFT
	// input: FFT vector.
	// 1.modulus, 2.max, 3. smooth, 4. log scale, 5. scale if != 0
	// 6.Hz conversion
	// ---------------------------------------------------------------------------
	public void processFFT(double[] vfft, int nFFT, int sampleRate) {
		processFFT(vfft, null, nFFT, true, false, 1, sampleRate, 0, null, null, null);
	}
	public int processFFT(double[] vfft, double[] vx, int n, // i/o vfft i/o vx
																// in Hz (if
																// !NULL)
			boolean smooth, boolean scalelog, // smooth?, log scale?
			double scale, int samples, // scale==0 don't scale, samples=SAMPLE
										// RATE
			int nform, // number of formants (must smooth!)
			double[] HzFrm, double[] PowFrm, // vector containing formants
			double[] phFrm) // scaled vfft values and samples for Hz conversion.
	{

		double sfx = (double) samples / n / 2., sfy;
		double s = 0, s2 = 0, stddev; // for coherence

		vfft[0] = vfft[1] = vfft[2] = 0; // first three values=0
		PowMax = Double.MIN_VALUE;
		PosMax = -1;
		// PowMax, modulus, log, x vaues in Hz
		for (int i = 3; i < n; i++) { // ignore 3 first values
			double va = vfft[i];
			if (va < 0)
				va = -va; // modulus fabs
			{
				s += va;
				s2 += va * va;
			} // for stddev in coherence calc.
			if (scalelog)
				va = (va != 0) ? Math.log(va) : 0;
			if (va > PowMax) {
				PowMax = va;
				PosMax = i;
			} // PowMax
			vfft[i] = va;
			if (vx != null)
				vx[i] = i * sfx; // Hz in x if not NULL
		}

		{
			double N = n - 3; // coherence calc.
			stddev = Math.sqrt((N * s2 - s * s) / (N * (N - 1))); // stddev calc
			coherence = (stddev != 0) ? 1 - (s / N) / stddev : 0; // coherence
																	// calc.
																	// mean/stddev.
		}

		HzMax = PosMax * sfx; // convert to Hz

		// scale
		if (scale != 0 && PowMax != 0) {
			sfy = scale / PowMax;
			PowMax *= sfy;
			for (int i = 3; i < n; i++)
				vfft[i] *= sfy;
		}

		// smooth fft.
		if (smooth) {
			double max10 = 0.05 * PowMax;
			for (int i = 3; i < n - 1; i++)
				if (vfft[i] < max10)
					vfft[i] = 0;
			for (int c = 0; c < 4; c++) {
				for (int i = 4; i < n - 1; i++) {
					if (vfft[i - 1] > vfft[i] && vfft[i] < vfft[i + 1])
						vfft[i] = (vfft[i - 1] + vfft[i + 1]) / 2;
				}
			}

			if (nform != 0) {
				// get formants
				nform = formants(vfft, HzFrm, PowFrm, phFrm, n, nform);
				// convert HzFrm to Hz
				for (int i = 0; i < nform; i++)
					HzFrm[i] *= (double) samples / n / 2.;
			}
		}
		this.nform = nform;
		return nform;
	}

	// calc dissonance from filtered spectrum, 1 - ratio between area of main freqs / total freq sum in %
	public static double dissonanceDistance(double[] vfft, int n) {
		// find PowMax and pos PowMax (px)
		double mx = -Double.MAX_VALUE, sum=0, maSum=0;
		int px = 0;
		for (int i = 0; i < n; i++) {
			if (vfft[i] > mx) {
				mx = vfft[i];
				px = i;
			}
			sum+=vfft[i];
		}
		// from 'px' find left and right margin to '0'
		int lm, rm;
		for (lm = px; lm != 0 && vfft[lm] != 0; lm--) maSum+=vfft[lm];
		for (rm = px; rm < n && vfft[rm] != 0; rm++) maSum+=vfft[rm];
		int dst = rm - lm;
		if (px != 0)
			mx = 100 * (1-maSum/sum);
		else
			mx = 0;
		return mx;
	}
	// dissonance as 1 - ratio of n. of freqs containing coefPwr (%) of power / n , in %
	public static double dissonancePower(double[] vfft, int n) {
		// find PowMax and pos PowMax (px)
		double mx = -Double.MAX_VALUE, sum=0, maSum=0;
		int px = 0;
		for (int i = 0; i < n; i++) {
			if (vfft[i] > mx) {
				mx = vfft[i];
				px = i;
			}
			sum+=vfft[i];
		}
		// from 'px' find distance of coefPwr of sum
		int lm, rm; double coefPwr=0.7, grpPower= coefPwr * sum;
		for (lm = rm = px; maSum < grpPower || (lm<0 && rm>=n); lm--, rm++) {
			if (lm>=0) maSum+=vfft[lm];
			if (rm<n && lm!=rm) maSum+=vfft[rm];
		}
		double dst = Math.min(rm, n) - Math.max(lm, 0);
		if (px != 0)
			mx = 100. *  dst/(double)n;
		else
			mx = 0;
		return mx;
	}

	// decibel calc
	public static double db(double range, double val) {
		if (val == 0)
			return -80;
		return 20. * Math.log10(val / range);
	}

	// for 16bit sound
	public static double db(double val) {
		if (val == 0)
			return -80;
		return 20. * Math.log10(val / 32767.);
	}

	public static double db100(double val) {
		if (val == 0)
			return -80;
		return 20. * Math.log10(val / 100.);
	}
}
