package com.suydam.tts;

public class GoogleTextToSpeechTransformAustralian extends GoogleTextToSpeechTransform {

	/**
	 * 
	 */
	public GoogleTextToSpeechTransformAustralian() {
		this(0);
	}
	
	/**
	 * 
	 * @param delayMS number of milliseconds to inject for pauses
	 */
	public GoogleTextToSpeechTransformAustralian(int delayMS) {
		super(delayMS);
		
		registerRule(new TransformRule("tuk tuk", true, "took took"));
	}
	
	public static void main(String[] args) {
		TextToSpeechTransform g = new GoogleTextToSpeechTransformAustralian();
		System.out.println(g.transform("tuk tuk"));
	}
}
