package processing.test.groundwall;

import processing.core.*;
import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

// Object that accesses the camera, and updates our image data
// Using ideas pulled from 'Android Wireless Application Development', page 340

PImage gBuffer, fDiff;
SurfaceHolder mHolder;
Camera cam = null;
Camera.Size prevSize;
groundWall p;
int[] previousFrame;
int TOLERANCE = 5;


// SurfaceView Constructor:  : ---------------------------------------------------
CameraSurfaceView(Context context, groundWall _parent) {
  super(context);
  
  p = _parent;
  
  // Processing PApplets come with their own SurfaceView object which can be accessed
  // directly via its object name, 'surfaceView', or via the below function:
  // mHolder = surfaceView.getHolder();
  mHolder = p.getSurfaceHolder();
  // Add this object as a callback:
  mHolder.addCallback(this);
}

// SurfaceHolder.Callback stuff: ------------------------------------------------------
public void surfaceCreated (SurfaceHolder holder) {
  // When the SurfaceHolder is created, create our camera, and register our
  // camera's preview callback, which will fire on each frame of preview:
  cam = Camera.open(1);
  
  cam.setPreviewCallback(this);

  Camera.Parameters parameters = cam.getParameters();
  
  // Find our preview size, and init our global PImage:
  prevSize = parameters.getPreviewSize();
  gBuffer = p.createImage(prevSize.width, prevSize.height, p.RGB);
  fDiff = p.createImage(prevSize.width, prevSize.height, p.RGB);
  
  previousFrame = new int[prevSize.width * prevSize.height];
}  

public PImage getImage() {
	return gBuffer;
}

public PImage diffImage() {
	return fDiff;
}

public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
  // Start our camera previewing:
  cam.startPreview();
}

public void surfaceDestroyed (SurfaceHolder holder) {
  // Give the cam back to the phone:
  cam.stopPreview();
  cam.release();
  cam = null;
}

//  Camera.PreviewCallback stuff: ------------------------------------------------------
public void onPreviewFrame(byte[] data, Camera cam) {
  // This is called every frame of the preview.  Update our global PImage.
  gBuffer.loadPixels();
  // Decode our camera byte data into RGB data:
  decodeYUV420SP(gBuffer.pixels, data, prevSize.width, prevSize.height);
    
  //gBuffer.updatePixels();
}

  //  Byte decoder : ---------------------------------------------------------------------
  public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
    // Pulled directly from:
    // http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
    final int frameSize = width * height;
    int movementSum = 0;
    
    for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
      for (int i = 0; i < width; i++, yp++) {
        int y = (0xff & ((int) yuv420sp[yp])) - 16;
        if (y < 0)
          y = 0;
        if ((i & 1) == 0) {
          v = (0xff & yuv420sp[uvp++]) - 128;
          u = (0xff & yuv420sp[uvp++]) - 128;
        }

        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        if (r < 0)
           r = 0;
        else if (r > 262143)
           r = 262143;
        if (g < 0)
           g = 0;
        else if (g > 262143)
           g = 262143;
        if (b < 0)
           b = 0;
        else if (b > 262143)
           b = 262143;

        rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        
        int currColor = rgb[yp];
        int prevColor = previousFrame[yp];
        // Extract the red, green, and blue components from current pixel
        int currR = (currColor >> 16) & 0xFF; 
        int currG = (currColor >> 8) & 0xFF;
        int currB = currColor & 0xFF;
        // Extract red, green, and blue components from previous pixel
        int prevR = (prevColor >> 16) & 0xFF;
        int prevG = (prevColor >> 8) & 0xFF;
        int prevB = prevColor & 0xFF;
        // Compute the difference of the red, green, and blue values
        int diffR = p.abs(currR - prevR);
        int diffG = p.abs(currG - prevG);
        int diffB = p.abs(currB - prevB);
        // Add these differences to the running tally
        movementSum = diffR + diffG + diffB;
        //fDiff.pixels[yp] = p.color(diffR, diffG, diffB);
        // Save the current color into the 'previous' buffer
        fDiff.pixels[yp] = p.color(diffR, diffG, diffB);
        
        if (movementSum > TOLERANCE) {
        	
        	fDiff.updatePixels();
          //fDiff.updatePixels();
          // Render the difference image to the screen
          //fDiff.pixels[yp] = 0xff000000 | (prevR << 16) | (prevG << 8) | prevB; 
        }
                
        previousFrame[yp] = currColor;
        
      }
    }
  }
}
