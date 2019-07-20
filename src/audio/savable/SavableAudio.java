package audio.savable;

import processing.core.*;
import java.io.*;
import javax.sound.sampled.*;

//import java.io.File;
//import java.io.IOException;
//import java.io.ByteArrayInputStream;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.AudioFileFormat;
//import javax.sound.sampled.UnsupportedAudioFileException;

public class SavableAudio {

	public PApplet myParent;
	public final static String VERSION = "##library.prettyVersion##";
	public AudioInputStream ais;
	public int bytesPerFrame;
	public int sampleRate;
	public int channels;
	public int frameRate;
	public int bitsPerSample;
	public double volume = 1;

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the Library.
	 * 
	 * @param [path to the file]
	 */
	public SavableAudio() {
		ais = null;
	}

	public SavableAudio(String filePath) {
		loadAudio(filePath);
	}

	public SavableAudio(SavableAudio audio) {
		ais = audio.ais;
		loadInfo();
	}

	/**
	 * Loads the audio file into an Audio Input Stream. currently only works with
	 * .wav files.
	 * 
	 * @param filePath
	 */
	public void loadAudio(String filePath) {

		String extension = filePath.substring(filePath.lastIndexOf(".") + 1);
		// loads the audio file into an Audio Input String
		try {
			if (extension.equals("mp3")) {
				System.out.println("Cannot load audio from mp3 file\nConvert file to wav format\n");
				return;
			} else {
				ais = AudioSystem.getAudioInputStream(new File(filePath));
			}
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			System.out.println("Something went wrong with loading the song located at: " + filePath + "\n");
			System.out.println("make sure that the audio file is a .wav file");
			System.out.println("the easiest way to make one is through audacity -> export\n");
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(
					"File not found: Be sure that you provided the correct file path and that the file exists\n");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("an I/O exception occured when loading the audio\n");
			return;
		}

		loadInfo();
	}

	/**
	 * calculates information about the audio track
	 * 
	 */
	private void loadInfo() {
		try {
			AudioFormat aisFormat = ais.getFormat();

			bytesPerFrame = aisFormat.getFrameSize();
			if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
				// some audio formats may have unspecified frame size
				// in that case we may read any amount of bytes
				bytesPerFrame = 1;
			}
			sampleRate = (int) aisFormat.getSampleRate();
			channels = aisFormat.getChannels();
			frameRate = (int) aisFormat.getFrameRate();
			bitsPerSample = aisFormat.getSampleSizeInBits();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to load information from the audio stream\n");
			return;
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
			System.out.println("Unable to save audio: audio not yet loaded or not loaded properly.\n");
			return;
		}

		try {
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filePath));
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("No song to save, audio not loaded properly\n");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("an I/O exception occured when saving the audio\n");
			return;
		}
	}

	/**
	 * Appends audio to another audio based from a given file path. Currently only
	 * works with .wav files.
	 * 
	 * @param filePath
	 */
	public void appendAudio(SavableAudio audio2) {

		// check to see if any audio is loaded
		if (ais == null) {
			// if the audio is not yet loaded, load the file instead of appending it to
			// nothing
			ais = audio2.ais;
			loadInfo();
			return;
		}

		ais = appendAIS(ais, audio2.ais);
	}

	public void appendAudio(String filePath) {
		if (ais == null) {
			// if the audio is not yet loaded, load the file instead of appending it to
			// nothing
			loadAudio(filePath);
			return;
		}
		appendAudio(new SavableAudio(filePath));
	}

	private AudioInputStream appendAIS(AudioInputStream ais1, AudioInputStream ais2) {
		int buffer1 = (int) ais1.getFrameLength() * bytesPerFrame;
		int buffer2 = (int) ais2.getFrameLength() * bytesPerFrame;
		int buffer3 = buffer1 + buffer2;
		byte[] audioBytes1 = new byte[buffer1];
		byte[] audioBytes2 = new byte[buffer2];
		byte[] audioBytes3 = new byte[buffer3];

		try {
			ais1.read(audioBytes1);
			ais2.read(audioBytes2);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("problem with reading the Audio Input Streams into byte arrays\n");
			return ais1;
		}

		for (int i = 0; i < buffer3; i++) {
			if (i < buffer1) {
				audioBytes3[i] = audioBytes1[i];
			} else {
				audioBytes3[i] = audioBytes2[i - buffer1];
			}
		}

		return new AudioInputStream(new ByteArrayInputStream(audioBytes3), ais.getFormat(), buffer3 / bytesPerFrame);
	}

	/**
	 * Mixes two audio inputs together such that they both play at the same time
	 * (like adding background music to an audio track). Also includes the option to
	 * add a fade at the end of the overall audio.
	 * 
	 * @param filePath
	 * @param [fadeLength]
	 */
	public void mixAudio(SavableAudio bkgMusic, double fadeLength) {

		// check to see if any audio is loaded
		if (ais == null || bkgMusic.ais == null) {
			System.out.println("One of the audio classes are not yet loaded.");
			System.out.println("use the loadAudio function to work with audio");
			return;
		}

		// SavableAudio bkgMusic = new SavableAudio(filePath);

		if (bkgMusic.channels != channels) {
			System.out.println(
					"could not merge audio tracks, make sure the audio tracks have the same channel type (ie. both mono or both stereo).\n");
			return;
		}

		if (bkgMusic.sampleRate != sampleRate) {
			System.out.println(
					"Warning: Sample rate mismatched, background audio may sound slower or faster than the input audio");
		}

		// Set a buffer equal to the size of the base audio times the bytes per frame.
		int buffer = (int) ((int) ais.getFrameLength() + sampleRate * fadeLength) * bytesPerFrame;

		// create byte arrays with their sizes equal to the buffer
		byte[] bkgAudioBytes = new byte[buffer];
		byte[] audioBytes = new byte[buffer];

		// read the data from the audio input streams into the byte arrays
		try {
			bkgMusic.ais.read(bkgAudioBytes);
			ais.read(audioBytes);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("problem with reading the Audio Input Streams into byte arrays\n");
			return;
		}

		int fadeFrame = (int) (audioBytes.length - (fadeLength * sampleRate * bytesPerFrame));

		// combine the bytes into one array using byte addition
		byte[] combBytes = new byte[audioBytes.length];
		for (int i = 0; i < audioBytes.length; i += 2) {
			short buf1A = audioBytes[i + 1];
			short buf2A = audioBytes[i];
			buf1A = (short) ((buf1A & 0xff) << 8);
			buf2A = (short) (buf2A & 0xff);

			short buf1B = bkgAudioBytes[i + 1];
			short buf2B = bkgAudioBytes[i];
			buf1B = (short) ((buf1B & 0xff) << 8);
			buf2B = (short) (buf2B & 0xff);

			short buf1C;
			short buf2C;

			// (fadeFrame, audioBytes2.length) -> (1, 0)
			double dampenerA = volume;
			double dampenerB = bkgMusic.volume;
			if (i > fadeFrame) {
				dampenerA = mapRange(fadeFrame, audioBytes.length, volume, 0, i);
				dampenerB = mapRange(fadeFrame, audioBytes.length, bkgMusic.volume, 0, i);
			}
			buf1C = (short) ((buf1A * dampenerA) + (buf1B * dampenerB));
			buf2C = (short) ((buf2A * dampenerA) + (buf2B * dampenerB));

			short res = (short) (buf1C + buf2C);

			combBytes[i] = (byte) res;
			combBytes[i + 1] = (byte) (res >> 8);
		}

		ais = new AudioInputStream(new ByteArrayInputStream(combBytes), ais.getFormat(), buffer / bytesPerFrame);
	}

	public void mixAudio(String filePath) {
		mixAudio(filePath, 0);
	}

	public void mixAudio(SavableAudio background) {
		mixAudio(background, 0);
	}

	public void mixAudio(String filePath, double fadeLength) {
		mixAudio(new SavableAudio(filePath), fadeLength);
	}

	/**
	 * returns the length of the audio sample in seconds
	 * 
	 * @return double
	 */
	public double getLength() {
		return ais.getFrameLength() / (double) sampleRate;
	}

	/**
	 * helper function used to map one range to another. (a1, a2) -> (b1, b2), using
	 * s as the input. Formula was found here:
	 * https://rosettacode.org/wiki/Map_range.
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

	/**
	 * fades the audio track in and/or out
	 * 
	 * @param fadeInLength
	 * @param fadeOutLength
	 */
	public void fade(double fadeInLength, double fadeOutLength) {
		// check to see if any audio is loaded
		if (ais == null) {
			System.out.println("Audio not yet loaded.");
			System.out.println("use the loadAudio function to work with audio");
			return;
		}

		int buffer = (int) ais.getFrameLength() * bytesPerFrame;

		byte[] original = new byte[buffer];
		try {
			ais.read(original);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("problem with reading the Audio Input Streams into byte arrays\n");
			return;
		}

		byte[] faded = new byte[buffer];

		int fadeInFrame = (int) (fadeInLength * frameRate * bytesPerFrame);
		int fadeOutFrame = original.length - (int) (fadeOutLength * frameRate * bytesPerFrame);

		for (int i = 0; i < original.length; i += 2) {
			short buf1A = original[i + 1];
			short buf2A = original[i];
			buf1A = (short) ((buf1A & 0xff) << 8);
			buf2A = (short) (buf2A & 0xff);

			double dampener = 1;
			if (i < fadeInFrame) {
				dampener *= mapRange(0, fadeInFrame, 0, volume, i);
			}

			if (i > fadeOutFrame) {
				dampener *= mapRange(fadeOutFrame, original.length, volume, 0, i);
			}
			short buf1C = (short) (buf1A * dampener);
			short buf2C = (short) (buf2A * dampener);

			short res = (short) (buf1C + buf2C);

			faded[i] = (byte) res;
			faded[i + 1] = (byte) (res >> 8);
		}

		ais = new AudioInputStream(new ByteArrayInputStream(faded), ais.getFormat(), buffer / bytesPerFrame);
	}

	public void fade(String type, double fadeLength) {
		switch (type) {
		case "IN":
			fade(fadeLength, 0);
			break;
		case "in":
			fade(fadeLength, 0);
			break;
		case "OUT":
			fade(0, fadeLength);
			break;
		case "out":
			fade(0, fadeLength);
			break;
		default:
			System.out.println("Invalid fade type: " + type);
			break;
		}
	}

	/**
	 * Add pauses before and after the audio sample (in seconds)
	 * 
	 * @param frontPause
	 * @param backPause
	 */
	public void addPause(double frontPause, double backPause) {
		// check for negative numbers
		if (frontPause < 0 || backPause < 0) {
			System.out.println("Invalid pause inputs: cannot be negative numbers");
			return;
		}
		
		// calculate the frame length of the front and back pauses
		int frontPauseFrames = (int) (frontPause * sampleRate);
		int backPauseFrames = (int) (backPause * sampleRate);

		// using their frame lengths, create byte arrays that are the length of the
		// pauses
		byte[] frontPauseBytes = new byte[frontPauseFrames * bytesPerFrame];
		byte[] backPauseBytes = new byte[backPauseFrames * bytesPerFrame];

		// convert the byte arrays to AudioInputStreams
		AudioInputStream frontAIS = new AudioInputStream(new ByteArrayInputStream(frontPauseBytes), ais.getFormat(),
				frontPauseFrames);
		AudioInputStream backAIS = new AudioInputStream(new ByteArrayInputStream(backPauseBytes), ais.getFormat(),
				backPauseFrames);

		// append the front, middle, and end input streams together
		ais = appendAIS(frontAIS, ais);
		ais = appendAIS(ais, backAIS);
	}

	/**
	 * Trim audio off the front and end of the audio sample (in seconds);
	 * 
	 * @param frontTrim
	 * @param backTrim
	 */
	public void trim(double frontTrim, double backTrim) {
		// check to see if any audio is loaded
		if (ais == null) {
			System.out.println("Audio not yet loaded: No audio to trim.");
			return;
		}

		// check if the trim will trim more than the length of the audio sample
		if (getLength() < frontTrim + backTrim) {
			System.out.println(
					"You are trying to trim more than the audio sample, shorten the length you want to trim the front or back of the audio sample");
			System.out.println("Or delete the audio sample, which will have the same effect");
			return;
		}
		
		// check for negative numbers
		if (frontTrim < 0 || backTrim < 0) {
			System.out.println("Invalid trim inputs: cannot be negative numbers");
			return;
		}

		// load the audio sample into a byte array
		int buffer = (int) ais.getFrameLength() * bytesPerFrame;
		byte[] original = new byte[buffer];
		try {
			ais.read(original);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("problem with reading the Audio Input Streams into byte arrays\n");
			return;
		}

		// create a byte array with the frame length equal to the final trimmed audio
		// sample
		int trimmedFrameLength = (int) (ais.getFrameLength() - (frontTrim * sampleRate) - (backTrim * sampleRate));
		byte[] trimmedBytes = new byte[trimmedFrameLength * bytesPerFrame];

		// copy each byte (starting after the front byte length) into the trimmed byte
		// array
		int frontByteLength = (int) (frontTrim * sampleRate * bytesPerFrame);
		for (int i = 0; i < trimmedBytes.length; i++) {
			trimmedBytes[i] = original[i + frontByteLength];
		}

		// set the audio sample to this trimmed byte array
		ais = new AudioInputStream(new ByteArrayInputStream(trimmedBytes), ais.getFormat(), trimmedFrameLength);
	}
}
