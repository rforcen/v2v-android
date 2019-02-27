package Signal;


public class LPC {
	private static int length;
	private static double[] delayLine;
	private static double[] impulseResponse;
	private static int count = 0;
	static double[]coefs;

	public static double[] filter(double[]y ) {	for (int i=0; i<y.length; i++) y[i]=getOutputSample(y[i]); return y;	}
	public static double[] eval(double[]y )   {
		double yy, yp;
		for (int i=0; i<y.length; i++) {
			yp=0; yy=y[i];
			for (int j=0; j<coefs.length; j++) yp += coefs[j] * Math.pow(yy, j);
			y[i]=yp;
		}
		return y;	
	}

	public static double[] calcCoeff(int p, short x[]){
		coefs=getCoefficients(p, x);
		init();
		return coefs;
	}
	public static double[] calcCoeff(int p, double x[]){
		coefs=getCoefficients(p, x);
		init();
		return coefs;
	}
	private static void init() {
		length = coefs.length;
		impulseResponse = coefs;
		delayLine = new double[length];
		count = 0;		
	}
	public static double getOutputSample(double inputSample) { // evaluate conv
		delayLine[count] = inputSample;
		double result = 0.0;
		int index = count;
		for (int i = 0; i < length; i++) {
			result += impulseResponse[i] * delayLine[index--];
			if (index < 0)	index = length - 1;
		}
		if (++count >= length)	count = 0;
		return result;
	}
	private static double[] getCoefficients(int p, short x[]){
		double r[]=new double[p+1]; //size = 11
		int N=x.length;             //size = 256
		for(int T=0;T<r.length;T++){
			for(int t=0;t<N-T;t++){
				r[T] += (double)x[t] * (double)x[t+T];
			}
		}
		double e=r[0], e1=0.0, k=0.0;
		double alpha_new[]=new double[p+1], alpha_old[]=new double[p+1];
		alpha_new[0]=alpha_old[0]=1.0;
		for(int h=1;h<=p;h++)	alpha_new[h]=alpha_old[h]=0.0;
		double sum=0.0;
		for(int i=1;i<=p;i++){
			sum=0;
			for(int j=1;j<=i-1;j++)		sum+=alpha_old[j]*(r[i-j]);
			k=((r[i])-sum)/e;
			alpha_new[i]=k;
			for(int c=1;c<=i-1;c++)		alpha_new[c]=alpha_old[c]-(k*alpha_old[i-c]);
			e1=(1-(k*k))*e;
			for(int g=0;g<=i;g++)		alpha_old[g]=alpha_new[g];
			e=e1;
		}
		for(int a=1;a<alpha_new.length;a++)	alpha_new[a]=-1*alpha_new[a];
		return alpha_new;
	}
	public static double[] getCoefficients(int p, double x[]){
		double r[]=new double[p+1]; //size = 11
		int N=x.length;             //size = 256
		for(int T=0;T<r.length;T++){
			for(int t=0;t<N-T;t++){
				r[T] += x[t] * x[t+T];
			}
		}
		double e=r[0], e1=0.0, k=0.0;
		double alpha_new[]=new double[p+1], alpha_old[]=new double[p+1];
		alpha_new[0]=alpha_old[0]=1.0;
		for(int h=1;h<=p;h++)	alpha_new[h]=alpha_old[h]=0.0;
		double sum=0.0;
		for(int i=1;i<=p;i++){
			sum=0;
			for(int j=1;j<=i-1;j++)		sum+=alpha_old[j]*(r[i-j]);
			k=((r[i])-sum)/e;
			alpha_new[i]=k;
			for(int c=1;c<=i-1;c++)		alpha_new[c]=alpha_old[c]-(k*alpha_old[i-c]);
			e1=(1-(k*k))*e;
			for(int g=0;g<=i;g++)		alpha_old[g]=alpha_new[g];
			e=e1;
		}
		for(int a=1;a<alpha_new.length;a++)	alpha_new[a]=-1*alpha_new[a];
		return alpha_new;
	}
	public static void printArray(double arr[]){
		for(int i=0;i<arr.length;i++)
			System.out.print(arr[i]+"\t");
		System.out.println();
	}
	public static void test() {
		short x[]=new short[256];
		for(int i=0;i<x.length;i++)	x[i]=(short)(16000*Math.sin(2.*Math.PI*8000.*i/44100.));
		double[]alpha=getCoefficients(10,x);
		printArray(alpha);
	}

}
