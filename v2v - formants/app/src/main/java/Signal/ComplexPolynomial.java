package Signal;

import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Term;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Complex;

public class ComplexPolynomial {

	/**
	 * An implementation of the Durand-Kerner-Weierstrass method to
	 * determine the roots of a complex univariate polynomial, as described
	 * <a href="http://en.wikipedia.org/wiki/Durand-Kerner_method">here</a>.
	 *
	 * @author John B. Matthews; distribution per LGPL.
	 */

	private static final int MAX_COUNT = 999;
	private static final boolean DEBUG = false;
	private static double epsilon = 1E-15;

	private ComplexPolynomial() {}

	/**
	 * Create a complex polynomial from a number of Complex values.
	 * The array should have the highest order coefficient first.
	 *
	 * @param  a a variable number of Complex coefficients.
	 * @return a Polynomial having those Complex coefficients.
	 */
	public static Polynomial<Complex> create(Complex... a) {
		Variable<Complex> x = new Variable.Local<Complex>("x");
		Polynomial<Complex> px = Polynomial.valueOf(Complex.ZERO, x);
		for (int i = 0, e = a.length - 1; i < a.length; i++, e--) {
			px = px.plus(Polynomial.valueOf(a[i], Term.valueOf(x, e)));
		}
		return px;
	}

	/**
	 * Create a complex array from an array of double.
	 * The array should have the highest order coefficient first.
	 *
	 * @param  a a variable number of double coefficients.
	 * @return an array of the corresponding Complex coefficients.
	 */
	public static Complex[] complexArray(double... a) {
		Complex[] ca = new Complex[a.length];
		for (int i = 0; i < a.length; i++) {
			ca[i] = Complex.valueOf(a[i], 0);
		}
		return ca;
	}

	/**
	 * This implementation uses the Durand-Kerner-Weierstrass method
	 * to find the roots of a polynomial with complex coefficients.
	 * The method requires a monic polynomial; some error may occur
	 * when dividing by the leading coefficient.
	 * The array should have the highest order coefficient first.
	 *
	 * @param  ca a variable number of Complex polynomial coefficients.
	 * @return an array of the Complex roots of the polynomial.
	 */
	public static Complex[] roots(Complex[] ca) {
		Complex[] a0 = new Complex[ca.length - 1];
		Complex[] a1 = new Complex[ca.length - 1];

		// Divide by leading coefficient if not monic
		Complex leading = ca[0];
		if (!ca[0].equals(Complex.ONE)) {
			for (int i = 0; i < ca.length; i++) {
				ca[i] = ca[i].divide(leading);
			}
		}

		// Initialize a0
		Complex result = Complex.valueOf(0.4, 0.9);
		a0[0] = Complex.ONE;
		for (int i = 1; i < a0.length; i++) {
			a0[i] = a0[i - 1].times(result);
		}

		// Iterate
		int count = 0;
		while (true) {
			for (int i = 0; i < a0.length; i++) {
				result = Complex.ONE;
				for (int j = 0; j < a0.length; j++) {
					if (i != j) {
						result = a0[i].minus(a0[j]).times(result);
					}
				}
				a1[i] = a0[i].minus(ComplexPolynomial.
						eval(ca, a0[i]).divide(result));
			}
			count++;
			if (count > MAX_COUNT || done(a0, a1)) break;
			System.arraycopy(a1, 0, a0, 0, a1.length); // a0 := a1
		}
		if (DEBUG) {
			System.out.println("Iterations: " + count);
			for (Complex c : a0) System.out.println(c);
		}
		return a1;
	}

	// Determine if the components of two arrays are unchanging
	private static boolean done(Complex[] a, Complex[] b) {
		boolean unchanged = true;
		Complex delta;
		for (int i = 0; i < a.length; i++) {
			delta = a[i].minus(b[i]);
			unchanged &= Math.abs(delta.getReal()) < epsilon
					&& Math.abs(delta.getImaginary()) < epsilon;
		}
		return unchanged;
	}

	/**
	 * Evaluate the polynomial at x using
	 * <a href="http://en.wikipedia.org/wiki/Horner_scheme">Horner's scheme</a>.
	 * The array should have the highest order coefficient first.
	 *
	 * @param  ca an array of Complex polynomial coefficients.
	 * @param  x the function argument.
	 * @return the Complex value of the function at x.
	 */
	public static Complex eval(Complex[] ca, Complex x) {
		Complex result = ca[0];
		for (int i = 1; i < ca.length; i++) {
			result = result.times(x).plus(ca[i]);
		}
		return result;
	}

	/** Return the nominal tolerance value. */
	public static double getEpsilon() { return epsilon; }

	/** Set the nominal tolerance value. */
	public static void setEpsilon(double e) { epsilon = e; }
}
