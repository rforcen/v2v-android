package Signal;

public class MusicalMatrix {

	static int nFFT;
	static double sampleRate;
	static double[]yfft;
	
	
	// ---------------- MUSICAL NOTE TABLE 10 octaves x 12 notes
	static int[][] NoteOctTable;
	static int[] NoteTable, OctaveTable, noZeroOctaveList, noZeroOctaveTable;
	static final int nOctaves = 18, nNotes = 12, octOffset = 12;
	static int firstNoZeroOctave, lastNoZeroOctave, octMax, noteMax, noteMin;

	public static int weakNote(int _nFFT, double _sampleRate, double[]_yfft) {
		nFFT=_nFFT; sampleRate=_sampleRate; yfft=_yfft;
		doNoteOctTable();
		return noteMin;
	}
	public static double index2Freq(int ix) {
		return FFT.index2Freq(ix, sampleRate, nFFT);
	}

	static int inRange(int i, int n) {
		if (i < 0 || i >= n)
			return 0;
		else
			return i;
	}

	static void doNoteOctTable() {

		NoteOctTable = new int[nOctaves][nNotes]; // 10 x 12 note weight
		NoteTable = new int[nNotes]; //
		OctaveTable = new int[nOctaves];
		double[][] dOctTab = new double[nOctaves][nNotes];
		firstNoZeroOctave = lastNoZeroOctave = 0;

		for (int i = 0; i < nFFT; i++) { // create the table
			double freq = index2Freq(i);
			int oct = MusicFreq.Freq2Oct(freq) + octOffset, note = MusicFreq
					.Freq2Note(freq);
			note = inRange(note, nNotes);
			oct = inRange(oct, nOctaves);
			dOctTab[oct][note] += yfft[i];
		}
		double max = -Double.MAX_VALUE, min=-max, scale = 100; // scale 0..100
		for (int i = 0; i < nOctaves; i++)
			for (int j = 0; j < nNotes; j++) {
				if (dOctTab[i][j] > max) {
					max = dOctTab[i][j];
					octMax = i;
					noteMax = j;
				}
			}
		// find the weak note in octMax
		for (int i=0; i<nNotes; i++) if (dOctTab[octMax][i] < min) { noteMin=i; min=dOctTab[octMax][i]; }
		scale = 100 / (max == 0 ? 1 : max);
		for (int i = 0; i < nOctaves; i++)
			for (int j = 0; j < nNotes; j++)
				NoteOctTable[i][j] = (int) (scale * dOctTab[i][j]);
		for (int i = 0; i < nOctaves; i++)
			for (int j = 0; j < nNotes; j++) {
				NoteTable[j] += NoteOctTable[i][j];
				OctaveTable[i] += NoteOctTable[i][j];
			}
		for (int i = 0; i < nOctaves; i++)
			if (OctaveTable[i] != 0) {
				firstNoZeroOctave = i;
				break;
			}
		for (int i = nOctaves - 1; i > firstNoZeroOctave & OctaveTable[i] == 0; i--)
			lastNoZeroOctave = i;
		if (firstNoZeroOctave < lastNoZeroOctave) { // create the octave list
													// with no zero content
			noZeroOctaveList = new int[(lastNoZeroOctave - firstNoZeroOctave + 0)
					* nNotes];
			noZeroOctaveTable = new int[(lastNoZeroOctave - firstNoZeroOctave + 0)];
			for (int i = firstNoZeroOctave; i < lastNoZeroOctave; i++) {
				noZeroOctaveTable[i - firstNoZeroOctave] = OctaveTable[i];
				for (int j = 0; j < nNotes; j++)
					noZeroOctaveList[(i - firstNoZeroOctave) * nNotes + j] = NoteOctTable[i][j];
			}
		}
	}

	public static String freqOct(int pos) { // oct hz in hz or khz.1 format
		double hz = MusicFreq.NoteOct2Freq(0, pos + firstNoZeroOctave
				- octOffset);
		if (hz < 1000)
			return String.format("%3.0f", hz);
		else if (hz < 10000)
			return String.format("%2.1fk", hz / 1000);
		else
			return String.format("%5.0fk", hz / 1000);
	}
}
