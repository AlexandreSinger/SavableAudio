/**
 * Two audio files can be appended then saved using the append function.
 * When the append function is used, the audio from the file is added to 
 * the original audio, directly following. So you will hear the first 
 * audio, then directly after the second.
 */
import audio.savable.*;

SavableAudio audio;

void setup() {
  audio = new SavableAudio(sketchPath("Audio1.wav"));
  audio.append(sketchPath("Audio2.wav"));
  audio.save(sketchPath("output.mp3"));
  exit();
}
