package com.suydam.tts;

/*
 * This is a tricky class because it contains transforms that make
 * the ca-ES pronunciation approximate Swahili pronunciation!
 */
public class GoogleTextToSpeechTransformSwahili extends GoogleTextToSpeechTransform {

	/**
	 * 
	 */
	public GoogleTextToSpeechTransformSwahili() {
		this(0);
	}
	
	/**
	 * 
	 * @param delayMS number of milliseconds to inject for pauses
	 */
	public GoogleTextToSpeechTransformSwahili(int delayMS) {
		super(delayMS);
		
		registerRule(new TransformRule("lake", false, "laque"));
		registerRule(new TransformRule("mchana", false, "mtjana"));
		registerRule(new TransformRule("umeshindaje", false, "umessindatje"));
		
	}
}
