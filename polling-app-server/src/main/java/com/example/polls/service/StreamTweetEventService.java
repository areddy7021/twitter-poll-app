package com.example.polls.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StreamTweetEventService {

	private final Logger log = LoggerFactory.getLogger(StreamTweetEventService.class);

	@Autowired
	private Twitter twitter;
	
	private Stream userStream;

	public void streamTweetEvent(List<SseEmitter> emitters) throws InterruptedException{

    	List<StreamListener> listeners = new ArrayList<StreamListener>();
    	
    	StreamListener streamListener = new StreamListener() {
			@Override
			public void onWarning(StreamWarningEvent warningEvent) {
			}

			@Override
			public void onTweet(Tweet tweet) {
				//log.info("User '{}', Tweeted : {}, from ; {}", tweet.getUser().getName() , tweet.getText(), tweet.getUser().getLocation());
				Integer connectedUsers =  emitters.size();
				
				//log.info("Streaming to :" + connectedUsers +" connected Users");
				
				if (connectedUsers!=0) {
					for (SseEmitter emiter : emitters) {
						try {
							emiter.send(SseEmitter.event().name("streamLocation").data(tweet.getUser().getLocation()));
							
							StringBuilder hashTag = new StringBuilder();
							
							List<HashTagEntity> hashTags = tweet.getEntities().getHashTags();
							for (HashTagEntity hash : hashTags) {
								hashTag.append("#"+hash.getText() + " ");
							}
							//System.out.println(hashTag);
							emiter.send(SseEmitter.event().name("streamHashtags").data(hashTag));
						} catch (IOException e) {
							System.out.println("User Disconnected from the Stream");
							//e.printStackTrace();
						}
					}
				}else{
					//Close Stream when all Users are disconnected.
					userStream.close();
					log.info("Zero Connected Users - Closing Stream");
				}
				
			}

			@Override
			public void onLimit(int numberOfLimitedTweets) {
			}

			@Override
			public void onDelete(StreamDeleteEvent deleteEvent) {
			}
		};
		//Start Stream when a User is connected
		if (emitters.size()==1) {
			listeners.add(streamListener);
			userStream = twitter.streamingOperations().sample(listeners);
		}

	}
}
