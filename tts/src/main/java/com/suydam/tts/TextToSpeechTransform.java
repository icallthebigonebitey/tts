package com.suydam.tts;

public interface TextToSpeechTransform {

	/**
	 * 
	 * @param text the verbiage to vocalize
	 * @return the modified string to submit to a TTS service
	 */
	public String transform(String text);
	
}
