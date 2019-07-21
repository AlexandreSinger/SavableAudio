/**
 * Using the addPause function, you can add empty sound to the beginning and end
 * of the audio sample. To add a pause just to one end of the sample, replace
 * one of the numbers with a zero. This is usefull when adding fade into an audio 
 * track that has been mixed with background music.
 */
import audio.savable.*;

SavableAudio audio, background;

void setup() {
  audio = new SavableAudio(sketchPath("Audio1.wav"));
  background = new SavableAudio(sketchPath("background.wav"));

  println("The length of the audio sample before adding the pauses: " + String.format("%.2f", audio.getLength()) + " seconds");
  audio.addPause(2.0, 4.0);
  println("the length after: " + String.format("%.2f", audio.getLength()) + " seconds");
  audio.mix(background);
  audio.fade(2.0, 4.0);

  audio.save(sketchPath("output.wav"));
  exit();
}
