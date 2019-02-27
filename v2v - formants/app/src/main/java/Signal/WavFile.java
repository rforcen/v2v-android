package Signal;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;


public class WavFile {
	short nChannels, bpSample; 
	int sampleRate;

	int sbcs, byteRate; short audioFormat, blockAlign;
	int nSamples;
	LittleEndianDataInputStream in;
	BufferedOutputStream osw;
	boolean ok;
	int wavSize=0;
	String fName;
	float secs;

	enum Modes {read, write};
	Modes mode;
	Context context;
	public double[]dSamples;


	public boolean create(String fName, int nChannels, int bpSample, int sampleRate) { // create("name.wav", 2, 16, 22050); 
		this.fName=fName; this.nChannels=(short)nChannels; this.bpSample=(short)bpSample; this.sampleRate=sampleRate;
		try {
			mode=Modes.write;
			osw= new BufferedOutputStream(new FileOutputStream(fName));
			osw.write("RIFF".getBytes());
			osw.write(toBytes(0)); 								// Final file size not known yet, write 0 
			osw.write("WAVE".getBytes());
			osw.write("fmt ".getBytes());
			osw.write(toBytes(Integer.reverseBytes(16))); 		// Sub-chunk size, 16 for PCM
			osw.write(toBytes(Short.reverseBytes((short) 1))); 	// AudioFormat, 1 for PCM
			osw.write(toBytes(Short.reverseBytes(this.nChannels)));// Number of channels, 1 for mono, 2 for stereo
			osw.write(toBytes(Integer.reverseBytes(sampleRate))); // Sample rate
			osw.write(toBytes(Integer.reverseBytes(sampleRate*bpSample*nChannels/8))); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
			osw.write(toBytes(Short.reverseBytes((short)(nChannels*bpSample/8)))); // Block align, NumberOfChannels*BitsPerSample/8
			osw.write(toBytes(Short.reverseBytes(this.bpSample))); // Bits per sample
			osw.write("data".getBytes());
			osw.write(toBytes(0)); // Data chunk size not known yet, write 0
			ok=true;
			wavSize=0;
		} catch (IOException e) { e.printStackTrace();	ok=false;}
		return ok;
	}
	private byte[] toBytes(int i)	{ return new byte[]{ (byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) (i /*>> 0*/) };	}
	private byte[] toBytes(short s)	{ return new byte[]{ (byte) (s >> 8),  (byte) (s /*>> 0*/) }; }
	public void closeW() {
		try {
			osw.close();
		} catch (IOException e) {}
	}
	public void closeR() {
		try {
			in.close();
		} catch (IOException e) {}
	}
	public double[] readAssets(Context context, String name) {
		this.context=context;
		try {
			in=new LittleEndianDataInputStream(context.getAssets().open(name));
			if (readHeader(in))	dSamples = getSamplesD(in);
			in.close();
			ok=true;
		} catch (IOException e) { ok=false; e.printStackTrace();	}
		return dSamples;
	}
	public boolean isOk() {return ok;}
	private double[]getSamplesD(LittleEndianDataInputStream isr) { // get the mono or Left channel
		double[]ds=null;
		try {
			byte[]buff=new byte[wavSize];
			isr.read(buff);
			ds=getDSamples(buff);
			buff=null;
		} catch (IOException e) { ok=false; }
		return ds;
	}
	private double[] getDSamples(byte[] buff) {
		int bl=buff.length;
		double[]ds=null;

		switch (this.nChannels) {
		case 1: // mono
			switch (bpSample) {
			case 8:
				ds=new double[bl];
				for (int i=0; i<bl; i++) ds[i] = (double)buff[i]; 
				break;
			case 16:
				bl/=2; ds=new double[bl];
				for (int i=0; i<bl; i++) 	ds[i] = smp2double(buff[i*2+0], buff[i*2+1]);
				break;
			}
			break;
		case 2: // stereo
			bl/=2;
			switch (bpSample) {
			case 8:
				ds=new double[bl];
				for (int i=0; i<bl; i++) ds[i] = (double) ( ((int)buff[i*2+0]) + ((int)buff[i*2+1]) ) / 2.; 
				break;
			case 16:
				bl/=2; ds=new double[bl];
				for (int i=0; i<bl; i++) 	ds[i] = ( smp2double(buff[i*4+0], buff[i*4+1]) + smp2double(buff[i*4+2], buff[i*4+3]) ) / 2.;
				break;
			}
			break;
		}
		return ds;
	}
	private boolean readHeader(LittleEndianDataInputStream in) {
		byte[]b4=new byte[4];

		try {
			in.readFully(b4); ok=check( "RIFF" , b4);
			wavSize=in.readInt(); 		// total wav file len
			in.readFully(b4); ok=check( "WAVE" , b4);
			in.readFully(b4); ok=check( "fmt " , b4);
			sbcs=in.readInt(); 		// Sub-chunk size, 16 for PCM
			audioFormat=in.readShort();	// AudioFormat, 1 for PCM
			nChannels=in.readShort();
			sampleRate=in.readInt();
			byteRate=in.readInt();		// Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
			blockAlign=in.readShort();	// Block align, NumberOfChannels*BitsPerSample/8
			bpSample=in.readShort();	// bits per sample 8, 16
			in.readFully(b4); ok=check( "data" ,  b4);
			wavSize=in.readInt();		// Data chunk size in bytes	
			if (ok) {
				nSamples= (ok=(bpSample!=0)) ? wavSize / ((int)bpSample/8) : 0;
				secs=(float)nSamples / (float)sampleRate;
			}
		} catch (IOException e) { ok=false; e.printStackTrace();	}
		return ok;
	}
	private boolean check(String s, byte[]cb) {
		boolean eq=true;
		for (int i=0; i<cb.length & eq; i++) eq=cb[i]==s.charAt(i); 
		return eq;
	}
	double smp2double(byte lo, byte hi) {
		short smp = (short) (((hi & 0xff) << 8) | (lo & 0xff));
		return (double) smp;
	}
	public int getSampleRate() {
		return sampleRate;
	}
	public double[] readSamplesFromWavFile(String fWav) {
		try {
			in =new LittleEndianDataInputStream(new FileInputStream(new File(fWav)));
			if (readHeader(in))	dSamples = getSamplesD(in);
			in.close();
			ok=true;
		} catch (IOException e) { ok=false; e.printStackTrace();	}
		return dSamples;
	}
}