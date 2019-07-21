/**
 * The trim function can be used to remove a certain length of the beginning or
 * end of an audio sample. This is used in the example below to remove the first
 * word of the audio and the dead space at the end.
 */
import audio.savable.*;

SavableAudio audio;

void setup() {
  audio = new SavableAudio(sketchPath("Audio1.wav"));

  println("The length of the audio sample before trimming: " + String.format("%.2f", audio.getLength()) + " seconds");
  audio.trim(0.5, 0.25);
  println("the length after: " + String.format("%.2f", audio.getLength()) + " seconds");

  audio.save(sketchPath("output.mp3"));
  exit();
}
