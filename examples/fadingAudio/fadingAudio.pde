/**
 * Audio files can be faded using the fade function. By declaring
 * the length you want the audio to fade for (in seconds), the volume
 * will gradually get louder up to the time you specify or will gradually
 * get lower. The times you input are the duration of the fade.
 */
import audio.savable.*;

SavableAudio audio;

void setup() {
  audio = new SavableAudio(sketchPath("background.wav"));
  audio.fade(3, 7);        // or fade("IN", 3); fade("OUT", 7);
  audio.save(sketchPath("output.mp3"));
  exit();
}
