package com.suydam.tts;

public class GoogleTextToSpeechTransformEnglish extends GoogleTextToSpeechTransform {

	/**
	 * 
	 */
	public GoogleTextToSpeechTransformEnglish() {
		this(0);
	}
	
	/**
	 * 
	 * @param delayMS number of milliseconds to inject for pauses
	 */
	public GoogleTextToSpeechTransformEnglish(int delayMS) {
		super(delayMS);
		
		registerRule(new TransformRule("tuk tuk", false, "took took"));
	}
	
	public static void main(String[] args) {
		TextToSpeechTransform g = new GoogleTextToSpeechTransformEnglish();
		System.out.println(g.transform("take a tuk tuk"));
	}
}
