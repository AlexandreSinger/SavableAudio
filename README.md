# Savable Audio Library For Processing

This library was made to allow processing to read audio files, manipulate them in some way, then save them into a file.

## Documentation:
### Constructor:
SavableAudio() <br>
SavableAudio(String pathToFile) <br>
SavableAudio(SavableAudio audio) <br>

### Methods
load(String pathToFile)
* Loads the file from the given file path into the SavableAudio class, overriding anything that was already there.

save(String pathToFile)
* Saves the audio sample that is in the SavableAudio class into a file located at the given pathToFile. In processing, it is most common to use: sketchPath(fileName.wav).

append(String pathToFile) <br>
append(SavableAudio audio)
* Appends the audio located either in a file or in another SavableAudio class onto the end of the audio sample. When appending two samples together, the first sample will play then the second sample will play directly after. 

mix(String pathToFile) <br>
mix(SavableAudio audio)
* Mixes the audio sample with the sample located either in a file or in another SavableAudio class. When mixing two samples together, both samples will play at the same time.

getLength()
* Returns the length of the audio track, in seconds.

getInfo()
* Returns a string that describes the format of the audio, ex: "PCM_SIGNED 22050.0 Hz, 16 bit, mono, 2 bytes/frame, little-endian".

fade(double fadeInLength, double fadeOutLength) <br>
fade(String type, double fadeLength)
* Adds fades to the beginning and end of the audio sample, with the fade in and out lengths being measured in seconds. To fade just the front or back, set the fade length equal to zero or set the "type" to either "IN" or "OUT" and then specify the fade length.

addPause(double frontPause, double backPause) <br>
addPause(String type, double pauseLength)
* Adds pauses to the front and back of the audio sample, with the pause lengths being measured in seconds. To pause just the front or back, set the pause length equal to zero or set the "type" to either "FRONT" or "BACK" and then specify the fade length.

trim(double frontTrimLength, double backTrimLength) <br>
trim(String type, double trimLength)
* Trims a specific length of audio (measured in seconds) off the front and back of the audio sample. To trim just the front or back, set the trim length equal to zero or set the "type" to either "FRONT" or "BACK" and then specify the trim length.

setVolume(double newVolumePercentage)
* Sets the volume of the audio track, in percentage, with 100% being the volume of the original audio sample and 0% being no sound.

getBytes()
* Returns the audio sample as a byte array.

monoToStereo() <br>
monoToStereo(boolean left, boolean right)
* Converts an audio sample from mono to stereo. By default, both left and right are true, therefore the audio can be heard from both speakers.

stereoToMono()
* Converts an audio sample from stereo to mono.

swapEndian()
* swaps the endian from big endian to little endian, or little endian to big endian. The current standard is little endian.
