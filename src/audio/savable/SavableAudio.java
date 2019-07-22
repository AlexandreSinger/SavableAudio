package audio.savable;

//import processing.core.*;
//import java.io.*;
//import javax.sound.sampled.*;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A class that allows audio files to be read, manipulated, then saved back into
 * a file
 *
 */
public class SavableAudio {
	public AudioInputStream ais;
	public AudioFormat aisFormat;
	public int bytesPerFrame;
	private double volume = 1;

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
		load(filePath);
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
	public void load(String filePath) {
		String extension = filePath.substring(filePath.lastIndexOf(".") + 1);
		try {
			if (extension.equals("mp3")) {
				System.out.println("Cannot load audio from mp3 file\nConvert file to wav format\n");
				ais = null;
				return;
			} else {
				ais = AudioSystem.getAudioInputStream(new File(filePath));
			}
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			System.out.println("Something went wrong with loading the song located at: " + filePath + "\n");
			System.out.println("make sure that the audio file is a .wav file");
			System.out.println("the easiest way to make one is through audacity -> export\n");
			ais = null;
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(
					"File not found: Be sure that you provided the correct file path and that the file exists\n");
			ais = null;
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("an I/O exception occured when loading the audio\n");
			ais = null;
			return;
		}

		loadInfo();
	}

	/**
	 * calculates information about the audio track
	 * 
	 */
	public String getInfo() {
		return aisFormat.toString();
	}
	
	private void loadInfo() {
		aisFormat = ais.getFormat();

		bytesPerFrame = aisFormat.getFrameSize();
		if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
			// some audio formats may have unspecified frame size
			// in that case we may read any amount of bytes
			bytesPerFrame = 1;
		}
	}

	/**
	 * Saves the Audio Input Stream as a file named by a given file path.
	 * 
	 * @param filePath
	 */
	public void save(String filePath) {
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
	public void append(String filePath) {
		append(new SavableAudio(filePath));
	}

	public void append(SavableAudio audio2) {
		// check to see if any audio is loaded
		if (ais == null) {
			// if the audio is not yet loaded, load the file instead of appending
			ais = audio2.ais;
			loadInfo();
			return;
		}
		if (audio2.ais == null) {
			return;
		}

		ais = appendAIS(ais, audio2.ais);
	}

	private AudioInputStream appendAIS(AudioInputStream ais1, AudioInputStream ais2) {
		// set buffers that will be the size of the byte arrays
		int buffer1 = (int) ais1.getFrameLength() * bytesPerFrame;
		int buffer2 = (int) ais2.getFrameLength() * bytesPerFrame;
		int buffer3 = buffer1 + buffer2;

		// declare byte arrays
		byte[] audioBytes1 = getBytes(ais1, buffer1);
		byte[] audioBytes2 = getBytes(ais2, buffer2);
		byte[] audioBytes3 = new byte[buffer3];

		// write the bytes into the third array one after the other
		for (int i = 0; i < buffer3; i++) {
			if (i < buffer1) {
				audioBytes3[i] = audioBytes1[i];
			} else {
				audioBytes3[i] = audioBytes2[i - buffer1];
			}
		}

		// return the AudioInputStream version of the byte array
		return new AudioInputStream(new ByteArrayInputStream(audioBytes3), aisFormat, buffer3 / bytesPerFrame);
	}

	/**
	 * Mixes two audio inputs together such that they both play at the same time
	 * (like adding background music to an audio track).
	 * 
	 * @param filePath
	 */
	public void mix(String filePath) {
		mix(new SavableAudio(filePath));
	}

	public void mix(SavableAudio bkgMusic) {
		// check to see if any audio is loaded
		if (ais == null || bkgMusic.ais == null) {
			System.out.println("One of the audio classes are not yet loaded or was not loaded properly.");
			return;
		}

		// check if both audio tracks are mono or stereo
		if (bkgMusic.aisFormat.getChannels() != aisFormat.getChannels()) {
			System.out.println(
					"could not merge audio tracks, make sure the audio tracks have the same channel type (ie. both mono or both stereo).\n");
			return;
		}

		// provide a warning if the sample rates do not match
		if (bkgMusic.aisFormat.getSampleRate() != aisFormat.getSampleRate()) {
			System.out.println(
					"Warning: Sample rates do not match, background audio may sound slower or faster than the input audio");
		}

		// Set a buffer equal to the size of the base audio times the bytes per frame.
		int buffer = (int) ais.getFrameLength() * bytesPerFrame;

		// load the audio samples into byte arrays
		byte[] bkgAudioBytes = getBytes(bkgMusic.ais, buffer);
		byte[] audioBytes = getBytes(ais, buffer);

		// combine the bytes into one array using byte addition
		// note: must be done two bytes at a time
		byte[] combBytes = new byte[audioBytes.length];
		for (int i = 0; i < audioBytes.length; i += 2) {
			// Combine bytes from both arrays into shorts, then add those together
			short res = (short) (combineBytes(audioBytes[i + 1], audioBytes[i])
					+ combineBytes(bkgAudioBytes[i + 1], bkgAudioBytes[i]));

			// Split the short into two bytes and save into combined bytes array
			combBytes[i] = (byte) res;
			combBytes[i + 1] = (byte) (res >> 8);
		}

		// set the byte array to the audio input stream
		ais = new AudioInputStream(new ByteArrayInputStream(combBytes), aisFormat, buffer / bytesPerFrame);
	}

	/**
	 * returns the length of the audio sample in seconds
	 * 
	 * @return double
	 */
	public double getLength() {
		return ais.getFrameLength() / (double) (aisFormat.getSampleRate() * aisFormat.getChannels());
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
			System.out.println("Audio not yet loaded, cannot fade");
			return;
		}

		// set a buffer for the size of the audio sample
		int buffer = (int) ais.getFrameLength() * bytesPerFrame;

		// load the audio sample into a byte array
		byte[] original = getBytes(ais, buffer);

		// create empty byte array to hold the final audio
		byte[] faded = new byte[buffer];

		// calculate the fade in and fade out frames
		int fadeInFrame = (int) (fadeInLength * aisFormat.getFrameRate() * bytesPerFrame);
		int fadeOutFrame = original.length - (int) (fadeOutLength * aisFormat.getFrameRate() * bytesPerFrame);

		// load all the bytes, two at a time, and change their volumes based on the fade
		// in and out frames
		for (int i = 0; i < original.length; i += 2) {
			// Declaring a dampener that fades depending on the frame
			double dampener = 1;
			if (i < fadeInFrame) {
				dampener *= mapRange(0, fadeInFrame, 0, volume, i);
			}
			if (i > fadeOutFrame) {
				dampener *= mapRange(fadeOutFrame, original.length, volume, 0, i);
			}

			// Combine two bytes to a short and multiply by the dampener
			short res = (short) (dampener * combineBytes(original[i + 1], original[i]));

			// Split the short into two bytes and store in new byte array
			faded[i] = (byte) res;
			faded[i + 1] = (byte) (res >> 8);
		}

		// load the faded byte array into the Audio Input Stream
		ais = new AudioInputStream(new ByteArrayInputStream(faded), aisFormat, buffer / bytesPerFrame);
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

	private double mapRange(double a1, double a2, double b1, double b2, double s) {
		return b1 + ((s - a1) * (b2 - b1)) / (a2 - a1);
	}

	/**
	 * Adds pauses before and after the audio sample (in seconds)
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
		int frontPauseFrames = (int) (frontPause * aisFormat.getSampleRate());
		int backPauseFrames = (int) (backPause * aisFormat.getSampleRate());

		// using their frame lengths, create byte arrays that are the length of the
		// pauses
		byte[] frontPauseBytes = new byte[frontPauseFrames * bytesPerFrame];
		byte[] backPauseBytes = new byte[backPauseFrames * bytesPerFrame];

		// convert the byte arrays to AudioInputStreams
		AudioInputStream frontAIS = new AudioInputStream(new ByteArrayInputStream(frontPauseBytes), aisFormat,
				frontPauseFrames);
		AudioInputStream backAIS = new AudioInputStream(new ByteArrayInputStream(backPauseBytes), aisFormat,
				backPauseFrames);

		// append the front, middle, and end input streams together
		ais = appendAIS(frontAIS, ais);
		ais = appendAIS(ais, backAIS);
	}

	public void addPause(String type, double pauseLength) {
		switch (type) {
		case "FRONT":
			addPause(pauseLength, 0);
			break;
		case "front":
			addPause(pauseLength, 0);
			break;
		case "BACK":
			addPause(0, pauseLength);
			break;
		case "back":
			addPause(0, pauseLength);
			break;
		default:
			System.out.println("Invalid pause type: " + type);
			break;
		}
	}

	/**
	 * Trims audio off the front and end of the audio sample (in seconds);
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
		byte[] original = getBytes(ais, buffer);

		// create a byte array with the frame length equal to the final trimmed audio
		// sample
		int trimmedFrameLength = (int) (ais.getFrameLength() - (frontTrim * aisFormat.getSampleRate()) - (backTrim * aisFormat.getSampleRate()));
		byte[] trimmedBytes = new byte[trimmedFrameLength * bytesPerFrame];

		// copy each byte (starting after the front byte length) into the trimmed byte
		// array
		int frontByteLength = (int) (frontTrim * aisFormat.getSampleRate() * bytesPerFrame);

		// front Byte Length needs to be an even number (has to do with most audio
		// having 2 bytesPerFrame)
		if (frontByteLength % 2 == 1) {
			frontByteLength -= 1;
		}

		for (int i = 0; i < trimmedBytes.length; i++) {
			trimmedBytes[i] = original[i + frontByteLength];
		}

		// set the audio sample to this trimmed byte array
		ais = new AudioInputStream(new ByteArrayInputStream(trimmedBytes), aisFormat, trimmedFrameLength);
	}

	public void trim(String type, double trimLength) {
		switch (type) {
		case "FRONT":
			addPause(trimLength, 0);
			break;
		case "front":
			addPause(trimLength, 0);
			break;
		case "BACK":
			addPause(0, trimLength);
			break;
		case "back":
			addPause(0, trimLength);
			break;
		default:
			System.out.println("Invalid pause type: " + type);
			break;
		}
	}

	/**
	 * Sets the volume of the audio sample, with the input being a percentage with
	 * 100% being normal.
	 * 
	 * @param newVolume
	 */
	public void setVolume(double newVolume) {
		// check to see if any audio is loaded
		if (ais == null) {
			System.out.println("Audio not yet loaded, cannot set the volume");
			return;
		}

		newVolume = newVolume / 100;

		volume = newVolume;

		// set a buffer for the size of the audio sample
		int buffer = (int) ais.getFrameLength() * bytesPerFrame;

		// load the audio sample into a byte array
		byte[] original = getBytes(ais, buffer);

		// create empty byte array to hold the final audio
		byte[] changed = new byte[buffer];

		// Go through each of the bytes, two at a time, and change their volume
		for (int i = 0; i < original.length; i += 2) {
			// Combine the two bytes into one short, multiplying by the new volume
			short res = (short) (volume * combineBytes(original[i + 1], original[i]));

			// Split short into two bytes and put them into a new byte array.
			changed[i] = (byte) res;
			changed[i + 1] = (byte) (res >> 8);
		}

		// load the changed byte array into the audio input stream
		ais = new AudioInputStream(new ByteArrayInputStream(changed), aisFormat, buffer / bytesPerFrame);
	}

	/**
	 * Converts the audio sample from mono to stereo.
	 */
	public void monoToStereo() {
		monoToStereo(true, true);
	}
	
	public void monoToStereo(boolean left, boolean right) {
		// check to see if any audio is loaded
		if (ais == null) {
			System.out.println("Audio not yet loaded, cannot convert from mono to stereo");
			return;
		}

		// check to see if the audio is mono
		if (aisFormat.getChannels() != 1) {
			System.out.println("Audio sample not mono, cannot convert from mono to stereo");
			return;
		}

		// Set a buffer equal to the length of the audio in bytes
		int buffer = (int) ais.getFrameLength() * bytesPerFrame;

		// Declare two byte arrays, the original and the stereo
		byte[] original = getBytes(ais, buffer);
		byte[] stereo = new byte[buffer * 2];

		// create a temporary variable to move through the stereo array
		int j = 0;
		for (int i = 0; i < original.length; i += 2) {
			// Combine the bytes from the original, two at a time
			short res = combineBytes(original[i + 1], original[i]);

			// Set the bytes of the stereo array for left ear
			if (left) {
				stereo[j] = (byte) res;
				stereo[j + 1] = (byte) (res >> 8);
			}
			
			// Set the bytes of the stereo array for the right ear
			if (right) {
				stereo[j + 2] = (byte) res;
				stereo[j + 3] = (byte) (res >> 8);
			}

			// Iterate the temporary variable by 4
			j += 4;
		}

		// Create a new format for stereo based off the original
		AudioFormat oldFormat = aisFormat;
		AudioFormat newFormat = new AudioFormat(oldFormat.getEncoding(), oldFormat.getSampleRate(), 16, 2, 4,
				oldFormat.getFrameRate(), oldFormat.isBigEndian());

		// Load the stereo array into an Audio Input Stream and reload the info
		ais = new AudioInputStream(new ByteArrayInputStream(stereo), newFormat, (buffer * 2) / bytesPerFrame);
		loadInfo();
	}

	/**
	 * Converts audio sample from stereo to mono
	 */
	public void stereoToMono() {
		// check to see if any audio is loaded
		if (ais == null) {
			System.out.println("Audio not yet loaded, cannot convert from stereo to mono");
			return;
		}

		// check to see if the audio is stereo
		if (aisFormat.getChannels() != 2) {
			System.out.println("Audio sample not stereo, cannot convert from stereo to mono");
			return;
		}

		// Set a buffer equal to the length of the audio in bytes
		int buffer = (int) ais.getFrameLength() * bytesPerFrame;

		// Declare two byte arrays, the original and the stereo
		byte[] original = getBytes(ais, buffer);
		byte[] mono = new byte[buffer / 2];

		// create a temporary variable to move through the mono array
		int j = 0;
		for (int i = 0; i < original.length; i += 4) {
			// Combine the bytes from the original, 2 at a time, then add those together
			short res = (short) (combineBytes(original[i + 1], original[i])
					+ combineBytes(original[i + 3], original[i + 2]));

			// Set the bytes of the mono array by splitting the short
			mono[j] = (byte) (res/2);
			mono[j + 1] = (byte) ((res/2) >> 8);

			// Iterate the temporary variable by 4
			j += 2;
		}

		// Create a new format for mono based off the original
		AudioFormat oldFormat = aisFormat;
		AudioFormat newFormat = new AudioFormat(oldFormat.getEncoding(), oldFormat.getSampleRate(), 16, 1, 2,
				oldFormat.getFrameRate(), oldFormat.isBigEndian());

		// Load the mono array into an Audio Input Stream and reload the info
		ais = new AudioInputStream(new ByteArrayInputStream(mono), newFormat, (buffer / 2) / bytesPerFrame);
		loadInfo();
	}

	/**
	 * Gets the audio sample as a byte array
	 * 
	 * @return byteArray
	 */
	public byte[] getBytes() {
		// check to see if any audio is loaded
		if (ais == null) {
			System.out.println("Audio not yet loaded, cannot get Bytes");
			return null;
		}

		// set a buffer for the size of the audio sample
		int buffer = (int) ais.getFrameLength() * bytesPerFrame;

		// load the audio sample into a byte array
		byte[] original = new byte[buffer];
		try {
			ais.read(original);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("problem with reading the Audio Input Streams into byte arrays\n");
			return null;
		}

		return original;
	}

	private byte[] getBytes(AudioInputStream aisInput, int buffer) {
		// load the audio sample into a byte array
		byte[] original = new byte[buffer];
		try {
			aisInput.read(original);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("problem with reading an Audio Input Streams into a byte array\n");
			return null;
		}

		return original;
	}

	private short combineBytes(byte byte1, byte byte2) {
		// Both bytes need to be bitwise and-ed to 0xff to remove extra leading data.
		// The first byte needs to be shifted to the left by 8 bits, then they are added
		// together to make a short
		return (short) (((byte1 & 0xff) << 8) + (byte2 & 0xff));
	}
}
