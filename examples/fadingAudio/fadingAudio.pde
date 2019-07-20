import audio.savable.*;

SavableAudio audio;

void setup() {
  audio = new SavableAudio(sketchPath("background.wav"));
  audio.fade(3, 7);        // or fade("IN", 3); fade("OUT", 7);
  audio.saveAudio(sketchPath("output.mp3"));
  exit();
}
