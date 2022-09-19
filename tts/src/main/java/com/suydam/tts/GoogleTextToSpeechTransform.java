package com.suydam.tts;

import java.util.ArrayList;

public class GoogleTextToSpeechTransform implements TextToSpeechTransform {
	
	// default amount of time for injected pauses
	private static final int PAUSE_LENGTH_DEFAULT = 500;
	// character for pause insertion
	private static final String RESOURCE_CHAR_PAUSE = "/";
	// xml code for GoogleTTS to inject a delay (where / must be replaced by a numeric value)
	private static final String RESOURCE_PAUSE_CODE = "<break time=\"" + RESOURCE_CHAR_PAUSE + "ms\"/>";
	
	// transform rules
	private ArrayList<TransformRule> rules = new ArrayList<>();
    
	/**
	 * 
	 */
	public GoogleTextToSpeechTransform() {
		this(PAUSE_LENGTH_DEFAULT);
	}
	
	/**
	 * 
	 * @param delayMS number of milliseconds to inject for pauses
	 */
	public GoogleTextToSpeechTransform(int delayMS) {
		// insert pauses anywhere we see a forward slash in the english meaning
		registerRule(new TransformRule(RESOURCE_CHAR_PAUSE, false, assignPauseDelay(delayMS)));
		
		// workaround mispronunciations from Google TTS
		
		// standalone consonants
		registerRule(new TransformRule("บ", true, "บ ใบไม"));
		registerRule(new TransformRule("น", true, "นอ หนู"));
		registerRule(new TransformRule("ถ", true, "ท่อ ถุง"));
		registerRule(new TransformRule("ผ", true, "พ่อ ผึ้ง"));
		registerRule(new TransformRule("ธ", true, "ท่อ ธง"));
		registerRule(new TransformRule("ฅ", true, "ขอ คน"));
		registerRule(new TransformRule("ฌ", true, "ชอ เฌอ"));
		
		// standalone vowels
		
		// rule for trimming enclosing brackets < >
		// rule for TTS speed
	}
	
	/**
	 * Register a transform rule
	 */
	protected boolean registerRule(TransformRule rule) {
		if (rule != null) {
			rules.add(rule);
			return true;
		}
		return false;
	}
	
	/**
	 * @param delayMS number of milliseconds to inject for pauses
	 */
	private String assignPauseDelay(int delayMS) {
		StringBuffer verbiage = new StringBuffer();
		int index = RESOURCE_PAUSE_CODE.indexOf(RESOURCE_CHAR_PAUSE);
		if (index != -1) {
			verbiage.append(RESOURCE_PAUSE_CODE.substring(0, index));
			verbiage.append(delayMS);
			verbiage.append(RESOURCE_PAUSE_CODE.substring(index +1));
		}
		return verbiage.toString();
	}
	
	@Override
	/**
	 * @param text the verbiage to vocalize
	 * @return the modified string to submit to a TTS service
	 */
	public String transform(String text) {
		StringBuffer verbiage = new StringBuffer(text);
		
		int index;
		String matchString;
		for (TransformRule rule:rules) {
			matchString = rule.matchString();
            if (rule.mustMatchEntireString()) {
            	if (matchString.equals(text)) {
            		verbiage.replace(0, verbiage.length(), rule.replacementString());
            		break;
            	} else {
            		continue;
            	}
            }
            
            index = verbiage.indexOf(matchString);
            while (index != -1) {
            	verbiage.replace(index, index+matchString.length(), rule.replacementString());
            	index = verbiage.indexOf(matchString, index + rule.replacementString().length());
            }
        }
		
		verbiage.insert(0, "<speak>");
		verbiage.append("</speak>");
		return verbiage.toString();
	}
	
	/**
     * A simple description of a transform rule
     * 
     */
	protected class TransformRule {
		private String matchString;
		private boolean mustMatchEntireString;
		private String replacementString;
		
		/**
		 * @param mustMatchEntireString if true, the transformation is only requested when
		 * the supplied verbiage exactly matches the predicate of the transform rule 
		 */
		TransformRule(String matchString, boolean mustMatchEntireString, String replacementString) {
			this.matchString = matchString;
			this.mustMatchEntireString = mustMatchEntireString;
			this.replacementString = replacementString;
		}
		
		String matchString() {
			return matchString;
		}

		boolean mustMatchEntireString() {
			return mustMatchEntireString;
		}
		
		String replacementString() {
			return replacementString;
		}
	}
	
	public static void main(String[] args) {
		GoogleTextToSpeechTransform g = new GoogleTextToSpeechTransform();
		System.out.println(g.transform("to stay"));
		System.out.println(g.transform("to stay / to be at"));
		System.out.println(g.transform("to stay / to be at / like spanish estar"));
		System.out.println(g.transform("บ"));
		System.out.println(g.transform("ชอบ"));
		System.out.println(g.transform("น"));
		System.out.println(g.transform("นอน"));
	}
}
