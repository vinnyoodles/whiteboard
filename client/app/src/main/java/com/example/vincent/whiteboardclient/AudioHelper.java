package com.example.vincent.whiteboardclient;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import io.socket.client.Socket;

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

    public void startStream(final Socket socket) {
        // Don't start duplicate streams
        if (status)
            return;
        status = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int channelConfig = AudioFormat.CHANNEL_IN_MONO;
                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                int minBufSize = AudioRecord.getMinBufferSize(Constants.AUDIO_SAMPLE_RATE, channelConfig, audioFormat);
                byte[] audioBuffer = new byte[minBufSize];
                recorder = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, Constants.AUDIO_SAMPLE_RATE, channelConfig, audioFormat, minBufSize * 10);

                recorder.startRecording();

                while (status) {
                    int bytes = recorder.read(audioBuffer, 0, audioBuffer.length);
                    socket.emit(Constants.AUDIO_STREAM, audioBuffer, bytes);
                }
            }
        });
        thread.start();
    }
}
