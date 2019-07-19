import audio.savable.*;

SavableAudio audio;

void setup() {
  audio = new SavableAudio();
  audio.loadAudio(sketchPath("Audio1.wav"));
  audio.appendAudio(sketchPath("Audio2.wav"));
  audio.saveAudio(sketchPath("output.mp3"));
  exit();
}
