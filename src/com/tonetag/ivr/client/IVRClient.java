package com.tonetag.ivr.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.sound.sampled.*;

public class IVRClient {
	private static final String TAG = IVRClient.class.getSimpleName();

	private static final int PORT = 0; // copy Port here shared in mail
	private static final String IP = "Copy IP shared in Mail here";

	private DataOutputStream out;
	private Socket clientSock;
	private InputStream sockis;

	// the line from which audio data is captured
	TargetDataLine line;
	
	private static float sampleRate = 8000;

	public static void main(String[] args) {

		System.out.println("Enter Phone Number:");
		Scanner scanner = new Scanner(System.in);
		
		String phone = scanner.nextLine();
		
		System.out.println("Enter Audio Sampling Rate:");
		
		sampleRate = Float.parseFloat(scanner.nextLine().trim());
		
		
		String toSend = "{\"phone_num\":\""+phone+"\",\"sample_rate\":\""+sampleRate+"\"}";
		
		System.out.println("Data to send : " + toSend);
		
		IVRClient client = new IVRClient();
		client.execute(toSend);
		scanner.close();

	}

	private void execute(String tosend) {
		try {
			clientSock = new Socket(IP, PORT);
			L.e(TAG, "Socket Connected " + clientSock.toString());
			
			OutputStream outToServer = clientSock.getOutputStream();
			sockis = clientSock.getInputStream();

			PrintWriter pw = new PrintWriter(outToServer, true);

			pw.println(tosend);

			out = new DataOutputStream(outToServer);

			// recordAudio();
			startRecorder();

			if (!clientSock.isClosed()) {

				Scanner in = new Scanner(sockis);
				String event = in.nextLine();

				L.e(TAG, "Server Detection Success! ;)");

				if (event.contains("stop")) {
					closeLine();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Defines an audio format
	 */
	AudioFormat getAudioFormat() {
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		return format;
	}

	/**
	 * Captures the sound and Stream
	 */
	void startRecorder() {
		try {

			AudioFormat format = getAudioFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

			// checks if system supports the data line
			if (!AudioSystem.isLineSupported(info)) {
				L.e(TAG, "Line not supported");
				System.exit(0);
			}
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();

			L.e(TAG, "Start capturing...");

			final AudioInputStream ais = new AudioInputStream(line);

			new Thread(new Runnable() {

				@Override
				public void run() {
					byte[] buff = new byte[372];
					int read = 0;
					L.e(TAG, "Start recording...");

					try {

						synchronized (this) {
							while ((read = ais.read(buff, 0, buff.length)) != -1) {

								if (line.isActive() && line.isOpen() && !clientSock.isClosed()
										&& !clientSock.isOutputShutdown()) {
									out.write(buff, 0, buff.length);
								} else {
									break;
								}
							}
						}

					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						closeConnection();
					}
				}
			}).start();

		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Closes the target data line to finish capturing and recording
	 */
	void closeLine() {

		line.stop();
		line.close();
		L.e(TAG, "line closed");
	}

	void closeConnection() {
		try {
			out.flush();
			out.close();
			clientSock.close();
			L.e(TAG, "Socket closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
