package Signal;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

import java.util.ArrayList;

public class Rec {
    public boolean isRecording = false;
    public short[] samplesBuffer;
    public int iSamples = 0, lSampBuff, iSampProcess;
    protected boolean recDataOk;
    ArrayList<AsyncListener> listListener; // list of listeners when data chuck is ready
    int[] rateList = {Conf.sampleRate, 11025, 16000, 8000, 44100}; // getfirst in desider order
    int sread;
    boolean audioOk = false;
    private int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;            // mono
    private int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;    // record in a 16 bit/sample
    private AudioRecord recorder = null;
    private int bufferSize;
    private short[] recBuff;    // temp buffer to hold 1 sample read
    private Thread recordingThread = null;
    private int sampleRate;
    private boolean isSound;

    public Rec() {
        Conf.sampleRate = sampleRate = getFirstSampleRate();
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
            audioOk = true;
            recBuff = new short[bufferSize];
        }
        listListener = new ArrayList<AsyncListener>(); // list of listeners
    }

    private int getFirstSampleRate() {
        for (int rate : rateList) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
            if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                return rate;
            }
        }
        return 0;
    }

    private void allocSamplesBuffer() {
        samplesBuffer = new short[Conf.maxSecs * sampleRate];
        if (samplesBuffer != null) {
            iSamples = 0;
            lSampBuff = samplesBuffer.length;
        } else audioOk = false;
    }

    protected void addSamples() { // circ. buffer
        for (int i = 0; i < sread; i++) {
            samplesBuffer[(iSamples++) % lSampBuff] = recBuff[i];
        }
    }

    private boolean isSound() {
        int s = 0;
        for (int i = 0; i < sread; i++) s += (recBuff[i] & 0x7fff); // abs
        return isSound = (s > ((sread * Short.MAX_VALUE) / 100) * 7);
    }

    public boolean startRecording() {
        if (audioOk) {
            recorder = new AudioRecord(AudioSource.MIC, sampleRate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
            recorder.startRecording();
            isRecording = true;
            allocSamplesBuffer();
            recordingThread = new Thread(new Runnable() { // record from MIC in a thread
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                    while (isRecording) {
                        sread = recorder.read(recBuff, 0, bufferSize);
                        recDataOk = (AudioRecord.ERROR_INVALID_OPERATION != sread);
                        if (recDataOk) {
                            if (isSound()) {
                                addSamples();
                                dispatchListeners(sread);
                            }
                        }
                    }
                }

                private void dispatchListeners(int sread) {
                    if ((iSampProcess += sread) >= Conf.nFFT) { // nFFT samples chunk ready?
                        iSampProcess = 0;
                        Signal.hasData = true;
                        for (AsyncListener ll : listListener) ll.onDataReady(); // dispatch all
                    }
                }
            }, "AudioRecorder Thread");
            recordingThread.start();
        } else isRecording = false;
        return isRecording;
    }

    public void stopRecording() {
        if (null != recorder & audioOk) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null; // stop thread!
        }
    }

    public void addListener(AsyncListener listener) {
        listListener.add(listener);
    }

    public void resetListener(AsyncListener listener) {
        listListener.clear();
    }

    // getters
    public short[] getSamplesBuffer() {
        return samplesBuffer;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public boolean dataOk() {
        return recDataOk;
    }

    public boolean isSilence() {
        return !isSound;
    }
}
