/**
 * Mixing audio is another method of joining two audio tracks. In this case,
 * when the mix function is called, the two audio files are played on top of
 * one another. This is perfect for adding background music to audio files.
 * 
 */
import audio.savable.*;

SavableAudio audio, background;

void setup() {
  audio = new SavableAudio(sketchPath("Audio1.wav"));
  background = new SavableAudio(sketchPath("background.wav"));

  // using the setVolume function to understand the voice better
  audio.setVolume(125);
  background.setVolume(50);
  audio.mix(background);
  audio.save(sketchPath("output.mp3"));
  exit();
}
