package Signal;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

public class PlaySound {
	boolean isPlaying=false;

	private playSoundTask 	ps; 
	private int bs=1024; 
	private short []sbuff=new short[bs]; // stereo
	private int sampleRate=11025; // default
	WaveGen wg;
	private boolean prepared=false;

	public boolean isPlaying() {return isPlaying;}
	public void startPlaying() {
		if (prepared) {
			ps=new playSoundTask(); // need a new copy each time
			isPlaying=true;		ps.execute(); // generate the sound
		}
	}
	public void stopPlaying() {isPlaying=false;	if (ps!=null) ps.cancel(true); ps=null; }

	public void prepareSound(int sampleRate, double[]amp, double[]hz)  {// init wave gen n amp/hz waves with current params[nLine] 
		this.sampleRate=sampleRate;
		wg=new WaveGen(WaveGen.STEREO, sampleRate);
		wg.SetDif(1.618);
		wg.Set(amp, hz, null, amp.length);
		prepared=true;
	}

	class playSoundTask extends AsyncTask<Void, Void, Void> {
		AudioTrack track;
		int minSize;

		// init the audio system
		public void InitAudio() {
			minSize = AudioTrack.getMinBufferSize( sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT );        
			track = new AudioTrack( AudioManager.STREAM_MUSIC, sampleRate, 
					AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, 
					minSize, AudioTrack.MODE_STREAM);
			track.play();
		}
		@Override protected void onPreExecute() { // init track & wg
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
			InitAudio();
		}
		@Override protected void onPostExecute(Void result) {
		}
		@Override protected Void doInBackground(Void... params) { // gen & write
			while(isPlaying) {
				track.write( fillBuffer(), 0, bs );
			}
			return null;
		}
		private short[] fillBuffer() {
			wg.gen(sbuff);
			return sbuff;
		}
	}
}
