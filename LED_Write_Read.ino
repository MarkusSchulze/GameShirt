#include <SoftwareSerial.h>
char command;
String string;
boolean ledon = false;
#define led 3
SoftwareSerial mySerial(6,5);

  void setup()
  {
    mySerial.begin(115200);
    pinMode(led, OUTPUT);
  }

  void loop()
  {
    if (mySerial.available() > 0) 
    {string = "";}
    
    while(mySerial.available() > 0)
    {
      command = ((char)mySerial.read());
      
      if(command == ':')
      {
        break;
      }
      
      else
      {
        string += command;
      }
      
      delay(1);
    }
    
    if(string =="TO")
    {
        ledOn();
        ledon = true;
    }
    
    if(string =="TF")
    {
        ledOff();
        ledon = false;
      //  mySerial.println(string);
    }
    
    if ((string.toInt()>=0)&&(string.toInt()<=255))
    {
      if (ledon==true)
      {
        analogWrite(led, string.toInt());
       // mySerial.println(string);
        delay(10);
      }
    }
 }
 
void ledOn()
   {
      analogWrite(led, 255);
      delay(10);
    }
 
 void ledOff()
 {
      analogWrite(led, 0);
      delay(10);
 }
 

    
