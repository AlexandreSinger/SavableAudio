package audio.savable;

import processing.core.*;
import java.io.*;
import javax.sound.sampled.*;

public class SavableAudio {

	public PApplet myParent;
	public final static String VERSION = "##library.prettyVersion##";
	public AudioInputStream ais;
	public int bytesPerFrame;

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the Library.
	 * 
	 * @param [path to the file]
	 */
	public SavableAudio() {
		ais = null;
		bytesPerFrame = -1;
	}

	public SavableAudio(String filePath) {
		loadAudio(filePath);

		// calculates the bytes per frame of the audio track
		try {
			bytesPerFrame = ais.getFormat().getFrameSize();
			if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
				// some audio formats may have unspecified frame size
				// in that case we may read any amount of bytes
				bytesPerFrame = 1;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public SavableAudio(SavableAudio audio) {
		ais = audio.ais;
		bytesPerFrame = audio.bytesPerFrame;
	}

	/**
	 * Loads the audio file into an Audio Input Stream. currently only works with
	 * .wav files.
	 * 
	 * @param filePath
	 */
	public void loadAudio(String filePath) {

		// loads the audio file into an Audio Input String
		try {
			ais = AudioSystem.getAudioInputStream(new File(filePath));
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			System.out.println("Something went wrong with loading the song located at: " + filePath + "\n");
			System.out.println("make sure that the audio file is a .wav file");
			System.out.println("the easiest way to make one is through audacity -> export\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("an I/O exception occured when loading the audio\n");
		}
	}

	/**
	 * Saves the Audio Input Stream as a file named by a given file path.
	 * 
	 * @param filePath
	 */
	public void saveAudio(String filePath) {

		// check to see if any audio is loaded
		if (ais == null) {
			System.out.println("Audio not yet loaded.");
			System.out.println("use the loadAudio function to work with audio");
			return;
		}

		try {
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filePath));
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("No song to save, audio not loaded properly\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("an I/O exception occured when saving the audio\n");
		}
	}

	/**
	 * Appends audio to another audio based from a given file path. Currently only
	 * works with .wav files.
	 * 
	 * @param filePath
	 */
	public void appendAudio(String filePath) {

		// check to see if any audio is loaded
		if (ais == null) {
			// if the audio is not yet loaded, load the file instead of appending it to
			// nothing
			loadAudio(filePath);
			return;
		}

		SavableAudio audio2 = new SavableAudio(filePath);

		ais = new AudioInputStream(new SequenceInputStream(ais, audio2.ais), ais.getFormat(),
				ais.getFrameLength() + audio2.ais.getFrameLength());
	}

	/**
	 * Mixes two audio inputs together such that they both play at the same time
	 * (like adding background music to an audio track). Also includes the option to
	 * add a fade at the end of the overall audio.
	 * 
	 * @param filePath
	 * @param [fadeLength]
	 */
	public void mixAudio(String filePath, int fadeLength) {

		// check to see if any audio is loaded
		if (ais == null) {
			System.out.println("Audio not yet loaded.");
			System.out.println("use the loadAudio function to work with audio");
			return;
		}

		SavableAudio bkgMusic = new SavableAudio(filePath);

		if (bkgMusic.bytesPerFrame != bytesPerFrame) {
			System.out.println("could not merge audio tracks, make sure the audio tracks have the same bitrate");
			System.out.println("base audio track had bytesPerFrame of       " + bytesPerFrame);
			System.out.println("background audio track had bytesPerFrame of " + bkgMusic.bytesPerFrame + "\n");
			return;
		}

		// Set a buffer equal to the size of the base audio times the bytes per frame.
		int buffer = ((int) ais.getFrameLength() + 22500 * fadeLength) * bytesPerFrame;

		// create byte arrays with their sizes equal to the buffer
		byte[] bkgAudioBytes = new byte[buffer];
		byte[] audioBytes2 = new byte[buffer];

		// read the data from the audio input streams into the byte arrays
		try {
			bkgMusic.ais.read(bkgAudioBytes);
			ais.read(audioBytes2);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("problem with reading the Audio Input Streams into byte arrays\n");
		}

		int fadeFrame = audioBytes2.length - (fadeLength * 22500 * bytesPerFrame);

		// combine the bytes into one array using byte addition
		byte[] combBytes = new byte[audioBytes2.length];
		for (int i = 0; i < audioBytes2.length; i += 2) {
			short buf1A = audioBytes2[i + 1];
			short buf2A = audioBytes2[i];
			buf1A = (short) ((buf1A & 0xff) << 8);
			buf2A = (short) (buf2A & 0xff);

			short buf1B = bkgAudioBytes[i + 1];
			short buf2B = bkgAudioBytes[i];
			buf1B = (short) ((buf1B & 0xff) << 8);
			buf2B = (short) (buf2B & 0xff);

			short buf1C;
			short buf2C;

			// (fadeFrame, audioBytes2.length) -> (1, 0)
			double dampener = 1;
			if (i > fadeFrame) {
				dampener = mapRange(fadeFrame, audioBytes2.length, 1, 0, i);
			}
			buf1C = (short) ((buf1A * dampener) / 3 + (buf1B * dampener) / 7);
			buf2C = (short) ((buf2A * dampener) / 3 + (buf2B * dampener) / 7);

			short res = (short) (buf1C + buf2C);

			combBytes[i] = (byte) res;
			combBytes[i + 1] = (byte) (res >> 8);
		}

		AudioFormat outFormat = new AudioFormat(22050, 16, 1, true, false);

		AudioInputStream comb = new AudioInputStream(new ByteArrayInputStream(combBytes),
				// ais.getFormat(),
				outFormat, buffer / bytesPerFrame);

		ais = comb;
	}

	public void mixAudio(String filePath) {
		mixAudio(filePath, 0);
	}

	/**
	 * returns the length of the audio sample in seconds
	 * 
	 * @return double
	 */
	public double getLength() {
		double soundLength = ais.getFrameLength() / 22500.0;
		return soundLength;
	}

	/**
	 * helper function used to map one range to another. Found here:
	 * https://rosettacode.org/wiki/Map_range
	 * 
	 * @param a1
	 * @param a2
	 * @param b1
	 * @param b2
	 * @param s
	 * @return
	 */
	private double mapRange(double a1, double a2, double b1, double b2, double s) {
		return b1 + ((s - a1) * (b2 - b1)) / (a2 - a1);
	}

	/**
	 * return the version of the Library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}
}
