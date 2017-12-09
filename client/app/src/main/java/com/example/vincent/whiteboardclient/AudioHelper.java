package com.example.vincent.whiteboardclient;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by vincent on 12/9/17.
 */

public class AudioHelper {
    private byte[] audioBuffer;
    private AudioRecord recorder;
    private boolean status;

    public AudioHelper() {
        status = false;
    }

    public void stopStream() {
        if (!status || recorder == null)
            return;
        status = false;
        recorder.release();
        recorder = null;
    }

    public void startStream() {
        // Don't start duplicate streams
        if (status)
            return;
        status = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int sampleRate = 16000;
                int channelConfig = AudioFormat.CHANNEL_IN_MONO;
                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                byte[] audioBuffer = new byte[minBufSize];
                recorder = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufSize * 10);

                recorder.startRecording();

                while (status) {
                    int bytes = recorder.read(audioBuffer, 0, audioBuffer.length);
                    Log.d("audio", "read " + bytes + " bytes");
                }
            }
        });
        thread.start();
    }
}
