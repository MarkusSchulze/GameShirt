#include <SoftwareSerial.h>
#include <CapacitiveSensor.h>

/*
 * CapitiveSense Library Demo Sketch
 * Paul Badger 2008
 * Uses a high value resistor e.g. 10 megohm between send pin and receive pin
 * Resistor effects sensitivity, experiment with values, 50 kilohm - 50 megohm. Larger resistor values yield larger sensor values.
 * Receive pin is the sensor pin - try different amounts of foil/metal on this pin
 * Best results are obtained if sensor foil and wire is covered with an insulator such as paper or plastic sheet
 */


CapacitiveSensor   cs_9_10 = CapacitiveSensor(9,10);        // 10 megohm resistor between pins 4 & 2, pin 2 is sensor pin, add wire, foil
CapacitiveSensor   cs_4_5 = CapacitiveSensor(4,5);        // 10 megohm resistor between pins 4 & 6, pin 6 is sensor pin, add wire, foil
CapacitiveSensor   cs_4_8 = CapacitiveSensor(4,8);        // 10 megohm resistor between pins 4 & 8, pin 8 is sensor pin, add wire, foil
char command;
String string;
boolean ledon = false;
long trigger;
#define led 3
SoftwareSerial mySerial(6,5);
//SoftwareSerial myCapacitiveSerial(10,9);

void setup()
{
    Serial.begin(115200);
    mySerial.begin(115200);
    //  myCapacitiveSerial.begin(115200);
    pinMode(led, OUTPUT);

    //cs_4_2.set_CS_AutocaL_Millis(0xFFFFFFFF);     // turn off autocalibrate on channel 1 - just as an example



  // setup mean
  if(true){
   int calibrate = 500;

   long sum = 0;
   for(int i=0;i<calibrate;i++){
      long total1 =  cs_9_10.capacitiveSensor(30);
      sum = sum + total1;

   }



    long mean = sum/calibrate;
    trigger = mean * 10;


    Serial.print("init:");       // check on performance in milliseconds
    Serial.print("\t mean: ");                    // tab character for debug window spacing
    Serial.print(mean);
    Serial.print("\t sum: ");
    Serial.print(sum);
    Serial.print("\t trigger: ");                    // tab character for debug window spacing
    Serial.println(trigger);
  }
}

void loop(){
    capacitive();
    // myCapacitiveSerial.listen();
    //  while(myCapacitiveSerial.available() > 0){}
    //mySerial.listen();

    if (mySerial.available() > 0) {
        string = "";
    }

    while(mySerial.available() > 0)
    {
        command = ((char)mySerial.read());

        if(command == ':'){
            break;
        }
        else{
            string += command;
        }
        delay(1);
    }

    if(string =="TO"){
        ledOn();
        ledon = true;
    }

    if(string =="TF"){
        ledOff();
        ledon = false;
        //  mySerial.println(string);
    }
    //mySerial.println("hellotuztu1347383");
    //Serial.print("loop");
    Serial.println(string);

    delay(1000);

    if ((string.toInt()>=0)&&(string.toInt()<=255)){
        if (ledon==true){
            analogWrite(led, string.toInt());
            // mySerial.println(string);
            delay(10);
        }
    }

}

void ledOn(){
    analogWrite(led, 255);
    delay(10);
}

void ledOff(){
    analogWrite(led, 0);
    delay(10);
}

void capacitive(){
    long start = millis();
    long total1 =  cs_9_10.capacitiveSensor(30);
    //long total2 =  cs_4_5.capacitiveSensor(30);
    //long total3 =  cs_4_8.capacitiveSensor(30);

    Serial.print(millis() - start);        // check on performance in milliseconds
    Serial.print("\t trigger: ");                    // tab character for debug window spacing
    Serial.print(trigger);
    Serial.print("\t gemessen: ");
    Serial.println(total1);                  // print sensor output 1

    if(total1 > trigger){  // threshold ermitteln, wenn gesamtwiderstand des garns fest steht
      Serial.println("hit1");
      mySerial.print("hit1,");
      mySerial.println(total1);
    }else{
      mySerial.println("nohit1");
    }
    //Serial.print("\t");
    //Serial.print(total2);                  // print sensor output 2
    //Serial.print("\t");
    //.println(total3);                // print sensor output 3

    delay(10);
}



