package Signal;

import android.graphics.Color;


public class MusicFreq {
	
	// const values
	private static double MUSICAL_INC 		= 1.0594630943593; // 2^(1/12)
	private static double LOG_MUSICAL_INC    = 0.0577622650466;
	private static double baseC0 			    = 261.62556530061;  // 440 * MUSICAL_INC^(-9)
	private static double LOG_baseC0 		    = 5.5669143414923;
	private static double LOG2 				= 0.6931471805599;
	private static String	note_str[]={"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	
	// parameters
	private static int note;
	private static int oct;
	private static String NoteOct;
	private static double err;
	
	//---------------------------------------------------------------------------
	// Freq2Color, convert freq to color
	//---------------------------------------------------------------------------
	public static int Freq2Color(double freq)
	{
		int oct=Freq2Oct(freq); // get note and freq. err
	    double f0=NoteOct2Freq(0,oct),
	           fz=NoteOct2Freq(0,oct+1),
	           ratio=(freq-f0)/(fz-f0);
	           
	    // ratio between RED, VIOLET
	    int col=ColorScale.interpolateColor(0xffff0000,0xffff00ff,(float)ratio);
	    
	    return col;
	}
	
	
	// FreqInOctave. convert 'f' to octave 'o'
	public static double FreqInOctave(double f, int o)
	{
	  double fb,ft;
	  fb=NoteOct2Freq(0, o); ft=NoteOct2Freq(11, o); // rage freq in octave
	  if (f>fb) for (;f!=0 && f>fb && !(f>=fb && f<=ft); f/=2);  // in octave
	  else      for (;f!=0 && f<ft && !(f>=fb && f<=ft); f*=2);
	  return f;
	}
	
	public static String NoteString(int i)
	{
	   if (i>=0 && i<=12) return note_str[i];
	   else return "";
	}
	
	public static String NoteString(double freq)
	{
		int i=Freq2Note(freq);
		if (i>=0 && i<=12) return note_str[i];
	   else return "";
	}

	//---------------------------------------------------------------------------
	// NoteOct2Freq, convert Note/oct to freq
	//---------------------------------------------------------------------------
	public static double NoteOct2Freq(int note, int oct)
	{
		return (baseC0*Math.pow(MUSICAL_INC,note+12.*oct));
	}

	// String note of a freq
	public static String Freq2StrNote(double freq)
	{
		return NoteString(Freq2Note(freq));
	}
	//---------------------------------------------------------------------------
    //		Freq2NoteOct, convert Freq to note/oct and error
	// 0,  1  2   3  4  5   6  7   8  9  10 11
	// C, C#, D, D#, E, F, F#, G, G#, A, A#, B
	//---------------------------------------------------------------------------
	
	
	
	public static int Freq2NoteOct( double freq )
	{
		return Freq2NoteOct(freq );
	}
	
	public static String Freq2NoteOctStr( double freq )
	{	   
	   Freq2NoteOctInt( freq );
	   return NoteString(note)+Integer.toString(oct);
	}

	// note, oct, NoteOct -> by ref
	public static int Freq2NoteOctInt( double freq)
	{
	   int n,o; String no; 
	   Freq2NoteOctErr( freq ); n=note; o=oct; no=NoteOct;
	   if (++n>11) {n=0; o++;} // freq of next note
	   if (NoteOct2Freq( n, o ) - freq < err) { note=n; oct=o; NoteOct=NoteString(n); } // next note
	   return oct*12+((oct>=0)?note:-note);   
	}

	public static double NoteFit(double hz)
	{
	 Freq2NoteOct( hz );
	 return NoteOct2Freq( note, oct );
	}


	// note, oct, NoteOct, err -> reff
	public static int Freq2NoteOctErr( double freq )
	{
		if (freq<=0) { note=oct=0; err=0;	return 0;  }
	   // oct = floor( log2(freq/baseC0) )
	   // note = baseC0 * MUSICAl_INC ^(note+12*oct)
	   double lfB=Math.log(freq)-LOG_baseC0;
	   oct  = (int)Math.floor( lfB/LOG2 );
	   note = (int)( lfB/LOG_MUSICAL_INC - oct*12. );
	   // string
	   if (NoteOct!=null) 
		   NoteOct=(note_str[note])+((oct<0)?Integer.toString(oct):"+"+Integer.toString(oct));
	   // error
	   err = freq - NoteOct2Freq(note,oct);
	   return oct*12+((oct>=0)?note:-note);
	}

	// Hz 2 octave
	public static int Freq2Oct( double freq )
	{
	   if (freq<=0) return 0;
	   return (int)Math.floor( (Math.log(freq)-LOG_baseC0)/LOG2 );
	}

	// Hz 2 note
	public static int Freq2Note( double freq )
	{
	  if (freq<=0) return 0;
	  return (int)( (Math.log(freq)-LOG_baseC0)/LOG_MUSICAL_INC - Freq2Oct(freq)*12. );
	}

	// Error in note calc
	public static double ErrInNote(double freq)	{
	  return freq - NoteOct2Freq(Freq2Note(freq),Freq2Oct(freq));
	}

	// limit of freq in octave range
	public static boolean FreqInOctRange(double freq, int octDown, int octUp)	{
		if (freq<0 || freq>Double.MAX_VALUE/2)
	   	return false;
	 	int o=Freq2Oct(freq);
	    return (o>=octDown) && (o<=octUp);
	}
	// convert a freq. to a element freq in octave -4
	public static double Freq2Element(double freq)	{
	   double fb=NoteOct2Freq(11, -4); // B(-4) upper limit
	   if (freq==0) freq=0.01;
	   if (freq>fb) for (; freq>fb; freq/=2.);
	   else			for (; freq<fb/2; freq*=2.);
	   return freq;
	}

	// convert Hzfrm freq to 'oct'
	public static void Frm2OCtaveRange(double[] HzFrm,  int nform, int oct)	{
		for (int i=0; i<nform; i++)
	    	HzFrm[i]=FreqInOctave(HzFrm[i], oct);
	}
	public static String[] getNote_str() { 	return note_str;	}	
}
