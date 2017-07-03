package com.mindBlender.app;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.cloud.speech.spi.v1.SpeechClient;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.common.io.Files;
import com.google.protobuf.ByteString;

/**
 * Handles requests for the application home page.
 */
@Controller
public class TestController {
	
	public File savedAudioFile;
	private SpeechClient speech;
	
	@RequestMapping(value = "/getAudiofile", method = RequestMethod.GET)
	public @ResponseBody String getAudio(File audioFile) {
		
		if(audioFile!=null){
			
			savedAudioFile = audioFile;
			
			return "Success";
		}else{
			return "Fail";
		}
	}
	
	@RequestMapping(value ="/postAudiofile", method = RequestMethod.POST)
		public @ResponseBody String postAudiofile(){
		
		String getText = null;
		//speech to text
        try {
            speech = SpeechClient.create();
        } catch (IOException e) {
        	System.err.printf("Speech create");
            e.printStackTrace();
        }

        // Reads the audio file into memory
        byte[] data = new byte[0];
        try {
            data = Files.toByteArray(savedAudioFile);
        } catch (IOException e) {
        	System.err.printf("Read the audio file into memory");
            e.printStackTrace();
        }
        ByteString audioBytes = ByteString.copyFrom(data);

        // Builds the sync recognize request
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode("ko-KR")
                .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(audioBytes)
                .build();


        // Performs speech recognition on the audio file
        RecognizeResponse response = speech.recognize(config, audio);

        List<SpeechRecognitionResult> results = response.getResultsList();

        for (SpeechRecognitionResult result : results) {
            List<SpeechRecognitionAlternative> alternatives = result.getAlternativesList();
            for (SpeechRecognitionAlternative alternative : alternatives) {
                //System.out.printf("Transcription: %s%n", alternative.getTranscript());
               getText = alternative.getTranscript();
            }
        }
        try {
            speech.close();
          
        } catch (Exception e) {
        	System.err.printf("Speech close");
            e.printStackTrace();
        }
    
		return getText;
	}
}

