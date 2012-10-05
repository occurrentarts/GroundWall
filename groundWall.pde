import oscP5.*;
import apwidgets.*;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;

APWidgetContainer widgetContainer; 
APEditText indexField, ipField, portField;

OscP5 oscP5;
int myColor;
int myIndex;

void setup() {
  
  //size(400,400, A3D);
  oscP5 = new OscP5(this,8000);
  myColor = color(255,255,255);
  myIndex = 0;
  
  widgetContainer = new APWidgetContainer(this); //create new container for widgets
  
  indexField = new APEditText(width/2, 50, width/2, 50 );
  widgetContainer.addWidget( indexField );
  indexField.setInputType(InputType.TYPE_CLASS_TEXT); //Set the input type to number
  indexField.setImeOptions(EditorInfo.IME_ACTION_NEXT); //Enables a next button, shifts to next field
  
  ipField = new APEditText(width/2, 75, width/2, 50 );
  widgetContainer.addWidget( ipField );
  ipField.setInputType(InputType.TYPE_CLASS_TEXT); //Set the input type to number
  ipField.setImeOptions(EditorInfo.IME_ACTION_NEXT); //Enables a next button, shifts to next field
  
  portField = new APEditText(width/2, 100, width/2, 50 );
  ipField.setNextEditText(portField); //Manually set which field to shift to next. Must be set AFTER the target is initialized
  widgetContainer.addWidget( portField );
  portField.setInputType(InputType.TYPE_CLASS_NUMBER); //Set the input type to number
  portField.setImeOptions(EditorInfo.IME_ACTION_DONE); //Enables a Done button
  portField.setCloseImeOnDone(true); //close the IME when done is pressed
  
}

public int sketchWidth() {
    return screenWidth;
}
 
public int sketchHeight() {
    return screenHeight;
}

public String sketchRenderer() {
  return A3D;
}

void draw() {
 
  background(myColor); 

}

//If setImeOptions(EditorInfo.IME_ACTION_DONE) has been called 
//on a APEditText. onClickWidget will be called when done editing.
void onClickWidget(APWidget widget) {  
  if(widget == portField){
    
      myRemoteLocation = new NetAddress( ipField.getText(), (Integer.valueOf(portField.getText())).intValue() );
      myIndex = (Integer.valueOf(indexField.getText())).intValue();
      widgetContainer.hide();
      
  }
}

void oscEvent(OscMessage theOscMessage) {
  //println(theOscMessage.addrPattern());
  if(theOscMessage.checkAddrPattern("/" + myIndex + "/color")==true) {

    myColor = theOscMessage.get(0).intValue();
      
  }
  
}

//
public void registerSelf() {
  
}


public String getLocalIpAddress() {
    try {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    return inetAddress.getHostAddress().toString();
                }
            }
        }
    } catch (SocketException ex) {
        Log.e(LOG_TAG, ex.toString());
    }
    return null;
}
