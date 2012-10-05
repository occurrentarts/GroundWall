package processing.test.groundwall;

import android.view.*;

public class MultiTouch {
	
	// Public attrs that can be queried for each touch point:
	  float motionX, motionY;
	  float pmotionX, pmotionY;
	  float size, psize;
	  int id;
	  boolean touched = false;

	  // Executed when this index has been touched:
	  //void update(MotionEvent me, int index, int newId){
	  void update(MotionEvent me, int index) {
	    // me : The passed in MotionEvent being queried
	    // index : the index of the item being queried
	    // newId : The id of the pressed item.

	    // Tried querying these via' the 'historical' methods,
	    // but couldn't get consistent results.
	    pmotionX = motionX;
	    pmotionY = motionY;
	    psize = size; 

	    motionX = me.getX(index);
	    motionY = me.getY(index);
	    size = me.getSize(index);

	    id = me.getPointerId(index);
	    touched = true;
	  }

	  // Executed if this index hasn't been touched:
	  void update() {
	    pmotionX = motionX;
	    pmotionY = motionY;
	    psize = size;
	    //touched = false;
	  }
	  
	  void draw() {
		  
	  }
	
}
