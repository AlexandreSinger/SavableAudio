import audio.savable.*;

SavableAudio audio, background;

void setup() {
  audio = new SavableAudio(sketchPath("Audio1.wav"));
  background = new SavableAudio(sketchPath("background.wav"));
  
  audio.volume = 1.25;
  background.volume = 0.25;
  audio.mixAudio(background, 2);
  audio.saveAudio(sketchPath("output.mp3"));
  exit();
}
