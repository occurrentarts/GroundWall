package processing.test.groundwall;

import java.io.IOException;

import netP5.NetAddress;
import processing.core.*; 
import oscP5.*;
import apwidgets.*;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import ketai.camera.*;

import android.os.Vibrator;

public class groundWall extends PApplet {
	
	APWidgetContainer widgetContainer; 
	APEditText indexField;
	NetAddress myRemoteLocation;
	
	KetaiCamera cam;
	//CameraSurfaceView gCamSurfView;
	//PImage gBuffer;
	
	APMediaPlayer player;
	
	int maxTouchEvents = 5;
	MultiTouch[] mt;  
	
	OscP5 oscP5;
	int myColor, toColor;
	int myIndex;
	int fallBackTimer, fallBackSpeed;
	
	PImage imgOne, imgTwo, imgThree;
	int installationMode;
	boolean displayMode;
	
	PImage fDiff, prevImage;
	boolean fadeOut, fadeIn;
	int fadeSpeed, fadeIndex;
	int[] previousFrame;
	float fThreshold, tThreshold;
	
	public Vibrator vibrator;
	
	public void setup() {
			    
		  //orientation(LANDSCAPE);
		  
		  oscP5 = new OscP5(this,8000);
		  myRemoteLocation = new NetAddress("255.255.255.255",9000); //fiberspace
		  //colorMode(HSB);
		  
		  myColor = color(255,255,255,255);
		  toColor = color(255,255,255,255);
		  
		  myIndex = 0;
		  
		  frameRate(30.0f);
		  
		  player = new APMediaPlayer(this); //create new APMediaPlayer
		  player.setMediaFile("audioSprite.4.mp3"); //set the file (files are in data folder)
		  player.setVolume(1.0f, 1.0f);
		  player.pause();
		  
		  //Populate our MultiTouch array that will track all of our touch-points:
		  mt = new MultiTouch[maxTouchEvents];
		  
		  for(int i=0; i < maxTouchEvents; i++) {
		    mt[i] = new MultiTouch();
		  }
		  
		  widgetContainer = new APWidgetContainer(this); //create new container for widgets
		  
		  indexField = new APEditText(width/2, 50, width/2, 50 );
		  widgetContainer.addWidget( indexField );
		  indexField.setInputType(InputType.TYPE_CLASS_NUMBER); //Set the input type to number
		  indexField.setCloseImeOnDone(true); //close the IME when done is pressed
		  
		  imgOne = loadImage("tempImg.1.png");
		  imgTwo = loadImage("tempImg.2.png");
		  imgThree = loadImage("tempImg.3.png");
		  
		  installationMode = 0;
		  displayMode = false;
		  fadeOut = false;
		  fadeIn = false;
		  fadeSpeed = 25;
		  fadeIndex = 0;
		  fThreshold = 18.0f;
		  tThreshold = 7000000;
		  
		  fallBackTimer = 0;
		  fallBackSpeed = 800;
		  
		  //vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		  
		  cam = new KetaiCamera(this, 320, 240, 24);
		  fDiff = createImage(cam.width, cam.height, RGB);
		  prevImage = createImage(cam.width, cam.height, RGB);
		  cam.setCameraID(1);
		  //gCamSurfView = new CameraSurfaceView(this.getApplicationContext(), this);
		  cam.start();
	}

	public int sketchWidth() { return displayWidth; }
	public int sketchHeight() { return displayHeight; }
	public String sketchRenderer() { return P3D; }

	public void onCameraPreviewEvent()
	{
	  cam.read();
	}

	public void exit() {
	  cam.stop();
	}
	
	public void onPause()
	{
	  super.onPause();
	  //Make sure to releae the camera when we go
	  //  to sleep otherwise it stays locked
	  if (cam != null && cam.isStarted())
	    cam.stop();
	}
		
	public void draw() {
	
		
      float myColA = (myColor >> 24) & 0xFF;
	  float myColR = (myColor >> 16) & 0xFF; // Like red(), but faster
	  float myColG = (myColor >> 8) & 0xFF;
	  float myColB = myColor & 0xFF;
	  
	  float toColR = (toColor >> 16) & 0xFF; // Like red(), but faster
	  float toColG = (toColor >> 8) & 0xFF;
	  float toColB = toColor & 0xFF;
	 
	  boolean changed = false;
	  
	  if( myColR != toColR) {
		  myColR += (toColR - myColR) / 2.0f;
		  changed = true;
	  }
	  
	  if( myColG != toColG) {
		  myColG += (toColG - myColG) / 2.0f;
		  changed = true;
	  }
	  
	  if( myColB != toColB) {
		  myColB += (toColB - myColB) / 2.0f;
		  changed = true;
	  }
	  
	  if(changed)
		  myColor = color(myColR, myColG, myColB, myColA);
	  
	  //if (displayMode) {
	  
	  noTint();
	  
	  switch(installationMode) {
	  	case 0:
	  	  background(255);
	  	  image(imgOne,0,0);
		  break;
	  	case 1:
	  	  background(255);
	  	  image(imgTwo,0,0);
	  	  break;
	  	case 2:
	  	  background(myColor,255);
	  	  rect(0,0,width,height);
	  	  image(imgThree,0,0);
	  	  break;
	  	case 3:
		  fill(255);
		  rect(0,0,width,height);
		  break;
	  	case 4:
		  fill(0);
		  rect(0,0,width,height);
		  break;
	  	default:
	  	  break;
	  }
		  
	  //} else {
		  
	  frameDifference();	
	   
	  if (fadeIn) {
		  fadeIndex -= fadeSpeed;
		  if (fadeIndex < 0 ) {
			  fadeIn = false;
			  fadeIndex = 0;
			  displayMode = true;
			  player.pause();
		  }
		  myColor = color( myColR, myColG, myColB, fadeIndex );
	  }
	  
	  if (fadeOut) {
		  fadeIndex += fadeSpeed;
		  if (fadeIndex > 255 ) {
			  fadeOut = false;
			  fadeIndex = 255;
			  displayMode = false;
			  player.pause();
		  }
		  myColor = color( myColR, myColG, myColB, fadeIndex );
	  }
	  
	  //fill(myColor);
	  //rect(0,0,width,height);
	  
	  //scale(1, -1); 
	  this.tint(255,alpha(myColor));
	  image(fDiff, 0, 0, width, height);
	  
	  /**
	  if(displayMode)	{
		  if(fallBackTimer > fallBackSpeed) {
			  this.switchState(false);
			  fallBackTimer = 0;
		  } else {
			  fallBackTimer++;
		  }
	  }
	  **/
	  
	}
	
	public void frameDifference() {
		
	        float myColA = (myColor >> 24) & 0xFF;
		    float myColR = (myColor >> 16) & 0xFF; // Like red(), but faster
		    int myColG = (myColor >> 8) & 0xFF;
		    int myColB = myColor & 0xFF;
					
		    int movementSum = 0; // Amount of movement in the frame
		    for (int i = 0; i < cam.height * cam.width; i++) { // For each pixel in the video frame...
		    	
		      int currColor = cam.pixels[i];
		      int prevColor = prevImage.pixels[i];
		      
		      // Extract the red, green, and blue components from current pixel
		      int currR = (currColor >> 16) & 0xFF; // Like red(), but faster
		      int currG = (currColor >> 8) & 0xFF;
		      int currB = currColor & 0xFF;
		      
		      // Extract red, green, and blue components from previous pixel
		      int prevR = (prevColor >> 16) & 0xFF;
		      int prevG = (prevColor >> 8) & 0xFF;
		      int prevB = prevColor & 0xFF;
		      
		      // Compute the difference of the red, green, and blue values
		      int diffR = abs(currR - prevR);
		      int diffG = abs(currG - prevG);
		      int diffB = abs(currB - prevB);
		      
		      // Add these differences to the running tally
		      int thisMovt = diffR + diffG + diffB;
		      movementSum += thisMovt;
		      
		      // The following line is much faster, but more confusing to read
		      //fDiff.pixels[i] = 0xff000000 | (diffR << 16) | (diffG << 8) | diffB;
		      
		      fDiff.pixels[i] = color( myColR - ((myColR + currR) / fThreshold), myColG - ((myColG + currG) / fThreshold), myColB - ((myColB + currB) / fThreshold), myColA);
		      
		      // Save the current color into the 'previous' buffer
		      prevImage.pixels[i] = currColor;
		    }
		    
		    fDiff.updatePixels();
		    prevImage.updatePixels();
		   
		    if (movementSum > 0) {
		    	
		    	
		    } 
		    
		    //19,584,510
		    if (movementSum > tThreshold) {
		    	switchState(false);
		    }
		    
	}

	//If setImeOptions(EditorInfo.IME_ACTION_DONE) has been called 
	//on a APEditText. onClickWidget will be called when done editing.
	public void onClickWidget(APWidget widget) {  

	      myIndex = (Integer.valueOf(indexField.getText())).intValue();
	      widgetContainer.hide();
	      
	      updateImages();
	}
	
	//The MediaPlayer must be released when the app closes
	public void onDestroy() {

	  super.onDestroy(); //call onDestroy on super class
	  if(player!=null) { //must be checked because or else crash when return from landscape mode
	    player.release(); //release the player

	  }
	}
	
	public void updateImages() {
		  
		  //PImage tmp = loadImage("faceUp.png");
		   
	      int xLoc = myIndex % 10 * width; //
		  int yLoc = myIndex / 10 * height; //0
	      
		  //imgOne = tmp.get(xLoc,yLoc,width,height);
		  
		  //PImage tmp2 = loadImage("magnum.png");
		  
		  //imgTwo = tmp2.get(xLoc,yLoc,width,height);
		  
		  //Log.d("images","updated");
	}
	
	public void switchState(boolean _vib) {
		
		if (fadeOut || fadeIn ) return;
		
		try {
			OscMessage myMessage = new OscMessage("/tg/tab/trigger");
			myMessage.add(myIndex);
			oscP5.send(myMessage, myRemoteLocation);
		} catch (Exception e) {
			Log.e("oscSend","error");
		}
		
		player.seekTo(myIndex * 2 * 1000);
		player.start();
		
		//if (_vib) vibrator.vibrate(200);
		
		if ( displayMode ) {
			fadeOut = true;
		} else {
			fadeIn = true;
		}
		
	}

	public void oscEvent(OscMessage theOscMessage) {
		
		  int myColA = (toColor >> 24) & 0xFF;
		  int myColR = (toColor >> 16) & 0xFF; // Like red(), but faster
	      int myColG = (toColor >> 8) & 0xFF;
	      int myColB = toColor & 0xFF;
	  
		  if(theOscMessage.checkAddrPattern("/tg/tab/" + myIndex + "/opacity")==true) {
			  toColor = color( myColR, myColG, myColB, (int)(theOscMessage.get(0).floatValue() * 255.0f) );
			  fadeIn = false;
			  fadeOut = false;
			  fadeIndex = (int)(theOscMessage.get(0).floatValue() * 255.0f);
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/" + myIndex + "/color/red")==true) {

			  int tmpVal = (int)(theOscMessage.get(0).floatValue() * 255.0f);
			  toColor = color( tmpVal, myColG, myColB, myColA );
			  //Log.d("colorChange","red yo");
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/" + myIndex + "/color/green")==true) {
			 
			  int tmpVal = (int)(theOscMessage.get(0).floatValue() * 255.0f);
			  toColor = color( myColR, tmpVal, myColB, myColA );
			  //Log.d("colorChange","green yo");
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/" + myIndex + "/color/blue")==true) {
			  
			  int tmpVal = (int)(theOscMessage.get(0).floatValue() * 255.0f);
			  toColor = color( myColR, myColG, tmpVal, myColA );
			  //Log.d("colorChange","blue yo");
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/" + myIndex + "/diffThresh")==true) {
			  
			  fThreshold = theOscMessage.get(0).floatValue();
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/" + myIndex + "/trigThresh")==true) {
			  
			  tThreshold = theOscMessage.get(0).floatValue();
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/color")==true) {
			  			  
			  int tmpR, tmpG, tmpB;
			  tmpR = theOscMessage.get(myIndex).intValue();
			  tmpG = theOscMessage.get(myIndex + 50).intValue();
			  tmpB = theOscMessage.get(myIndex + 100).intValue();
			  toColor = color( tmpR, tmpG, tmpB, myColA);
			  //Log.d("colorChange",tmpR + " " +  tmpG + " " + tmpB);
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/opacity")==true) {
  			  
			  float tmpA = theOscMessage.get(myIndex).floatValue();
			  toColor = color( myColR, myColG, myColB, tmpA * 255 );
			  //Log.d("colorChange",tmpR + " " +  tmpG + " " + tmpB);
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/mode")==true) {
			  
			  installationMode = theOscMessage.get(0).intValue();
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/trigThresh")==true) {
			  
			  tThreshold = theOscMessage.get(0).floatValue();
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/diffThresh")==true) {
			  
			  fThreshold = theOscMessage.get(0).floatValue();
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/fallBackSpeed")==true) {
			  
			  fallBackSpeed = theOscMessage.get(0).intValue();
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/ip")==true) {
			  
			  String ip = theOscMessage.get(0).stringValue();
			  myRemoteLocation = new NetAddress( ip, 9000 );
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/exit")==true) {
			  
			  int ind = theOscMessage.get(0).intValue();
			  if (myIndex == ind) finish();
			  
		  }
		  
		  if(theOscMessage.checkAddrPattern("/tg/tab/audio")==true) {
			  
			  float vol = theOscMessage.get(0).floatValue();
			  player.setVolume(vol,vol);
			  
		  }
		  
		  //myColor = toColor;
		  
	}

	
	public boolean surfaceTouchEvent(MotionEvent me) {
		  
		  
		  //number of pointers, like in Akeric's version, used only when pointers move
		  int numPointers = me.getPointerCount();
		  //integer representing the type of action, which can be pressing down, moving, releasing or other stuff
		  
		  final int action = me.getAction() & MotionEvent.ACTION_MASK;

		  int pointerIndex = (me.getAction() & MotionEvent.ACTION_POINTER_ID_MASK)
		                      >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		    
		  //get the pointer id for that index
		  int pointerId = me.getPointerId(pointerIndex);
		  
		  if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
		  {
			  	
			//displayMode = !displayMode;
			switchState(true);
			
		    if(pointerId == 0)
		    {
		        // do stuff that has to do with the first pointer being pressed
		    	mt[0].update(me,pointerIndex);  
		    	mt[0].touched = true;
		    	
		    }else if(pointerId == 1)
		    {
		       // do stuff that has to do with the second pointer being pressed
		       //perhaps set the first of your "pointer" objects to touched, so you can use
		       //that in your algos or whatever else   	
		    	mt[1].update(me,pointerIndex);
		    	mt[1].touched = true;
		    	//map.mapDisplay.manualCleanup(25);
		    	
		    }
		    else if(pointerId == 2)
		    {
		      // do stuff that has to do with the third pointer being pressed
		    	mt[2].update(me,pointerIndex);
		    	
		    }
		    else
		    if(pointerId == 3)
		    {
		         // do stuff that has to do with the fourth pointer being pressed
		    	mt[3].update(me,pointerIndex);

		    }
		    else
		    if(pointerId == 4)
		    {
		       // do stuff that has to do with the fifth pointer being pressed
		    	//background(255);
		    	mt[4].update(me,pointerId);
		    	
		    }
		    
		    
		  }
		  else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP)
		  {
			  
			  	
			if(pointerId == 0)
		    {
			  	//stuff having to do with pointer id 0 going up 
			  	//perhaps set your first pointer object to untouched
				mt[0].touched = false;
				float tmpDist = this.dist(mt[0].motionX, mt[0].motionY, mt[0].pmotionX, mt[0].pmotionY);
				
		    }else if(pointerId == 1)
		    {
		    	//stuff having to do with pointer id 1 going up 
		    	mt[1].touched = false;	
		    		        
		    }
		    else if(pointerId == 2)
		    {
		    	//stuff having to do with pointer id 2 going up 
		    	mt[2].touched = false;
		    	
		    }
		    else if(pointerId == 3)
		    {
		    	//stuff having to do with pointer id 3 going up 
		    	mt[3].touched = false;
		    	
		    }
		    else if(pointerId == 4)
		    {
		    	//stuff having to do with pointer id 4 going up 
		    	mt[4].touched = false;
		    	
		    }
		    
		  }
		  else
		  //if the mouse pointer moved
		  //this uses code similar to the blog post, because we have to know where all the current pointers are 
		  if(action == MotionEvent.ACTION_MOVE)
		  {
			
		    for(int i = 0; i<numPointers;i++)
		    {
		    	
		      int newId = me.getPointerId(i);  
		      
		      //mt[0].update(me,newId);
		      //you can get x,y positions with me.getX(i), me.getY(i)
		    	
		      if(newId == 0)
		      {
		    	  //do stuff having to do with pointer 0 having moved
		    	  //perhaps update the position details for your first pointer object so you know where it is  
		    	  mt[0].update(me,i);
		    	  mt[0].touched = true;
		    	  
		    	  float tmpDist = this.dist(mt[0].motionX, mt[0].motionY, mt[0].pmotionX, mt[0].pmotionY);
		    	 		    	  
		      }
		      else
		      if(newId == 1) // means that there's at least two fingers being detected
		      {
		    	  //do stuff having to do with pointer 1 having moved	  
		    	  mt[1].update(me,i);
		    	  mt[1].touched = true;
		    	  	    	  		    	  
		      }
		      else
		      if(newId == 2)
		      {
		    	  //do stuff having to do with pointer 2 having moved  
		    	  mt[2].update(me,i);
		    	  
		      }
		      else
		      if(newId == 3)
		      {
		          //do stuff having to do with pointer 3 having moved
		    	  mt[3].update(me,i);
		    	  
		      }
		      else
		      if(newId == 4)
		      {
		          //do stuff having to do with pointer 4 having moved
		    	  mt[4].update(me,i);

		      }
		    }
		    
		 }
		  	
		 return super.surfaceTouchEvent(me);
	}
	
	public void onResume() {
		  super.onResume();
		  println("onResume()!");
		  // Sete orientation here, before Processing really starts, or it can get angry:
		  orientation(LANDSCAPE);
		  
		  getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		  // Create our 'CameraSurfaceView' objects, that works the magic:
		  //gCamSurfView = new CameraSurfaceView(this.getApplicationContext(),this);
		  
		  StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		  StrictMode.setThreadPolicy(policy); 
		  

	}

	
}
