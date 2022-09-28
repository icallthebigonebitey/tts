package com.suydam.tts;

import org.apache.commons.cli.*;
import javax.sound.sampled.*;

//Imports the Google Cloud client library
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * An implementation of a Google Text to Speech client that can fetch
 * and play audio renditions of text 
 *
 */
public class GoogleTTS implements LineListener
{
	private static final int RESOURCE_POLLING_INTERVAL = 100; // milliseconds for polling interval during WAV playback
	private static final int RESOURCE_MP3_PAUSE_INTERVAL = 1500; // milliseconds for pauses during MP3 playback
	private static final String RESOURCE_LANGUAGE_DEFAULT = "default";
	private static final String RESOURCE_LANGUAGE_THAI = "th-TH";
	private static final String RESOURCE_LANGUAGE_AUSTRALIAN_ENGLISH = "en-AU";
	private static final double PLAYBACK_DEFAULT = 1.0;  // measured in seconds
	
	/*
	 * Google Cloud TTS
	 * https://cloud.google.com/text-to-speech/   (Web form to test out TTS on any words)
	 * https://cloud.google.com/text-to-speech/docs/libraries
	 * https://cloud.google.com/text-to-speech/docs/libraries#installing_the_client_library
	 * https://cloud.google.com/apis/docs/cloud-client-libraries
	 * https://cloud.google.com/apis/docs/client-libraries-explained
	 * https://cloud.google.com/java/docs/setup
	 * https://cloud.google.com/docs/authentication?_ga=2.163244284.-130985594.1660933210#api-keys
	 * https://cloud.google.com/text-to-speech/docs/reference/rpc/google.cloud.texttospeech.v1
	 * https://developers.google.com/protocol-buffers/docs/reference/java/index-all.html#I:P
	 * Quotas: https://console.cloud.google.com/iam-admin/quotas?project=watchful-idea-360015&supportedpurview=project
	 * 
	 * Audio playback
	 * https://www.baeldung.com/java-play-sound
	 * https://www.codejava.net/coding/how-to-play-back-audio-in-java-with-examples
	 * https://www.javatpoint.com/javafx-with-eclipse
	 * 
	 */
	private TextToSpeechClient textToSpeechClient;
	private Hashtable<String, ByteString> audioContentsCache = new Hashtable<String, ByteString>();
	private ArrayList<Phrase> audioPhrases = new ArrayList<Phrase>();
	private Hashtable<String, TextToSpeechTransform> transformer = new Hashtable<String, TextToSpeechTransform>();
	private AudioConfig englishAudioConfig;
	private AudioConfig nonEnglishAudioConfig;
	private double englishPlaybackSpeed = PLAYBACK_DEFAULT;
	private double nonEnglishPlaybackSpeed = PLAYBACK_DEFAULT;
	boolean playCompleted;
	
    public static void main( String[] args )
    {
    	String verbiage = "งาน";
    	// งาน  ม
    	// "ในวันฝนพรำเธอคิดถึงกันบ้างไหมในตอนที่ไม่ได้เจอแล้วเธอนั้นเป็นอย่างไร"
    	String language = RESOURCE_LANGUAGE_THAI;
    	
    	// define options
        Options options = new Options();
        options.addOption(new Option("v", "verbiage", true, "the verbiage to verbalize"));
        options.addOption(new Option("l", "language", true, "the language code to use"));
                
        CommandLineParser parser = new DefaultParser(); 
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null; 

        try {
            cmd = parser.parse(options, args);  //it will parse according to the options and parse option value
            
            if (cmd.hasOption("v")) {
            	verbiage = cmd.getOptionValue("verbiage");
            }
            
            if (cmd.hasOption("l")) {
            	language = cmd.getOptionValue("language");
            }
            
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Quiz", options); 

            System.exit(1);
        } 

        System.out.println("Verbalizing: " + verbiage);
        System.out.println("Language Code: " + language);
        
        try {
        	GoogleTTS client = new GoogleTTS();
        	
        	client.collectPhraseForPlayback("หลอด", RESOURCE_LANGUAGE_THAI, "straw", RESOURCE_LANGUAGE_AUSTRALIAN_ENGLISH);
        	client.collectPhraseForPlayback("ผนัง", RESOURCE_LANGUAGE_THAI, "wall", RESOURCE_LANGUAGE_AUSTRALIAN_ENGLISH);
        	client.generateMP3fromPhrases("tone_rule_words.mp3", 1500, false);
        	
        	//client.playAsWAV(verbiage, language);
        	//client.playAsWAV("ในวันฝนพรำเธอคิดถึงกันบ้างไหมในตอนที่ไม่ได้เจอแล้วเธอนั้นเป็นอย่างไร", language);
        	
        	// test that caching is working
        	//client.playAsWAV(verbiage, language);
        } catch (Exception e) {
        	e.printStackTrace();
        	System.exit(1);
        }
     
        System.exit(0);
    }
    
    public GoogleTTS() throws IOException {
    	this(PLAYBACK_DEFAULT);
    }
    
    public GoogleTTS(double nonEnglishPlaybackSpeed) throws IOException {
    	textToSpeechClient = TextToSpeechClient.create();
    	transformer.put(RESOURCE_LANGUAGE_DEFAULT, new GoogleTextToSpeechTransform());
    	transformer.put(RESOURCE_LANGUAGE_THAI, new GoogleTextToSpeechTransformThai());
    	this.nonEnglishPlaybackSpeed = nonEnglishPlaybackSpeed;
    }
    
    private TextToSpeechTransform fetchTransform(String language) {
    	if (language == null || language.length() == 0) {
    		return transformer.get(RESOURCE_LANGUAGE_DEFAULT);
    	}
    	TextToSpeechTransform transform = transformer.get(language);
    	return (transform == null) ? transformer.get(RESOURCE_LANGUAGE_DEFAULT) : transform;
    }
    
    // assumes AudioConfig objects have already been created
    private ByteString synthesize(String verbiage, String language) {
 
    	verbiage = fetchTransform(language).transform(verbiage);
    	
    	// Set the text input to be synthesized
    	SynthesisInput input = SynthesisInput.newBuilder().setSsml(verbiage).build();
    	
    	// Build the voice request, specify the language code and the ssml voice gender
    	// ("neutral")
    	VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode(language).
	           setSsmlGender(SsmlVoiceGender.NEUTRAL).build();
    	
    	// Perform the text-to-speech request on the text input with the selected voice parameters and
	    // audio file type
    	SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice,
    			language.startsWith("en") ? englishAudioConfig : nonEnglishAudioConfig);
    	
    	// Get the audio contents from the response
    	return response.getAudioContent();
    }

    private ByteString synthesizeSilence(int timeInMillis) {
    	String ssml = "<speak><break time=\"" + timeInMillis + "ms\"/></speak>"; 
    	
    	// Set the text input to be synthesized
    	SynthesisInput input = SynthesisInput.newBuilder().setSsml(ssml).build();
    	
    	// Build the voice request, specify the language code and the ssml voice gender
    	// ("neutral")
    	VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode(RESOURCE_LANGUAGE_AUSTRALIAN_ENGLISH).
	           setSsmlGender(SsmlVoiceGender.NEUTRAL).build();
    	
    	// Perform the text-to-speech request on the text input with the selected voice parameters and
	    // audio file type
    	AudioConfig audioCfg = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();
    	SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioCfg);
    	
    	// Get the audio contents from the response
    	return response.getAudioContent();
    }

    
    public void collectPhraseForPlayback(String verbiage1, String language1, String verbiage2, String language2) {
    	audioPhrases.add(new Phrase(verbiage1, language1, verbiage2, language2));
    }
    
    public boolean generateMP3fromPhrases(String fileName, int pauseInterval, boolean reverse) {
    	if (pauseInterval == 0) {
    		pauseInterval = RESOURCE_MP3_PAUSE_INTERVAL;
    	}
    	
    	englishAudioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).setSpeakingRate(englishPlaybackSpeed).build();
    	nonEnglishAudioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).setSpeakingRate(nonEnglishPlaybackSpeed).build();
    	
    	ByteString silenceContents = synthesizeSilence(pauseInterval);
    	ByteString audioContents;
    	
    	// Write the response to the output file.
    	try (OutputStream out = new FileOutputStream(fileName)) {
    		// Fetch all collected phrases
            for (Phrase phrase:audioPhrases) {
            	
            	/*
            	 * we don't bother using audioContentsCache right now because it's just an
            	 * in-memory cache and study track creation basically only uses the words once.
            	 * if the cache evolves to a disk-based implementation then it becomes useful
            	 * for the purposes of reducing Google cloud API consumption
            	 */
            	audioContents = synthesize(reverse ? phrase.verbiage2() : phrase.verbiage1(),
            			reverse ? phrase.language2() : phrase.language1());
    			
            	if (audioContents == null) {
    				System.out.println("Could not synthesize text to speech");
    				return false;
    			}
    			out.write(audioContents.toByteArray());
    			out.write(silenceContents.toByteArray());
    			
    			/*
            	 * we don't bother using audioContentsCache right now because it's just an
            	 * in-memory cache and study track creation basically only uses the words once.
            	 * if the cache evolves to a disk-based implementation then it becomes useful
            	 * for the purposes of reducing Google cloud API consumption
            	 */
    			audioContents = synthesize(reverse ? phrase.verbiage1() : phrase.verbiage2(),
    					reverse ? phrase.language1() : phrase.language2());
    			
    			if (audioContents == null) {
    				System.out.println("Could not synthesize text to speech");
    				return false;
    			}
    			out.write(audioContents.toByteArray());
    			out.write(silenceContents.toByteArray());
            }
    		
    		out.close();
    	    System.out.println("Audio content written to file " + fileName);
    	} catch (Exception e) {
    		System.out.println("Could not write audio content to file");
    		e.printStackTrace();
    		return false;
    	}
    	
		return true;
    }
    
    public boolean playAsWAV(String verbiage, String language) {
    	
    	String key = createVerbiageKey(verbiage, language);
    	ByteString audioContents = (ByteString) audioContentsCache.get(key);
    	
    	if (audioContents == null) {
	    	// Select the type of audio file you want returned
	    	// https://cloud.google.com/text-to-speech/docs/reference/rpc/google.cloud.texttospeech.v1#google.cloud.texttospeech.v1.AudioEncoding
	    	
        	englishAudioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).setSpeakingRate(englishPlaybackSpeed).build();
        	nonEnglishAudioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).setSpeakingRate(nonEnglishPlaybackSpeed).build();
	    	
	    	audioContents = synthesize(verbiage, language);
	    	if (audioContents == null) {
				System.out.println("Could not synthesize text to speech");
				return false;
			}
			
			audioContentsCache.put(key, audioContents);
    	} else {
    		//System.out.println("pulled from cache");
    	}
    	 
    	InputStream inputStream = audioContents.newInput();
    	try {
    		AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
	    	
	    	AudioFormat audioFormat = audioStream.getFormat();
	    	DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
	    	
	    	Clip audioClip = (Clip) AudioSystem.getLine(info);
	    	audioClip.addLineListener(this);
	    	audioClip.open(audioStream);
	    	audioClip.start();
	    	
	    	while (!playCompleted) {
                // wait for the playback to complete
                try {
                    Thread.sleep(RESOURCE_POLLING_INTERVAL );
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
             
            audioClip.close();
            audioStream.close();
            
            // set up client to play something new
            playCompleted = false;
    	} catch (Exception e) {
    		System.out.println("failed to playback TTS audio");
    		e.printStackTrace();
    		return false;
    	}
    	return true;
    }
    
    private String createVerbiageKey(String verbiage, String language) {
    	return verbiage + language;
    }
    
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();
         
        if (type == LineEvent.Type.STOP) {
            playCompleted = true;
            //System.out.println("Playback completed.");
        }
    }
    
    private class Phrase {
    	private String verbiage1;
    	private String language1;
    	private String verbiage2;
    	private String language2;
    	
    	public Phrase(String verbiage1, String language1, String verbiage2, String language2) {
    		this.verbiage1 = verbiage1;
    		this.language1 = language1;
    		this.verbiage2 = verbiage2;
    		this.language2 = language2;
    	}
    	
    	public String verbiage1() {
    		return verbiage1;
    	}
    	
    	public String language1() {
    		return language1;
    	}
    	
    	public String verbiage2() {
    		return verbiage2;
    	}
    	
    	public String language2() {
    		return language2;
    	}
    }
}

