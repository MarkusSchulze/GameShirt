#include <SoftwareSerial.h>
#include <CapacitiveSensor.h>

/*
 * CapitiveSense Library Demo Sketch
 * Paul Badger 2008
 * Uses a high value resistor e.g. 10 megohm between send pin and receive pinledon
 * Resistor effects sensitivity, experiment with values, 50 kilohm - 50 megohm. Larger resistor values yield larger sensor values.
 * Receive pin is the sensor pin - try different amounts of foil/metal on this pin
 * Best results are obtained if sensor foil and wire is covered with an insulator such as paper or plastic sheet
 */

//Globale Variablen
CapacitiveSensor   cs_9_10 = CapacitiveSensor(9,10);        // 10 megohm resistor between pins 4 & 2, pin 2 is sensor pin, add wire, foil
CapacitiveSensor   cs_3_4 = CapacitiveSensor(3,4);        // 10 megohm resistor between pins 4 & 6, pin 6 is sensor pin, add wire, foil
//CapacitiveSensor   cs_4_8 = CapacitiveSensor(4,8);        // 10 megohm resistor between pins 4 & 8, pin 8 is sensor pin, add wire, foil
char command;
String string;
int hitcooldown = 0;
#define led1 A0
#define led2 A1
SoftwareSerial mySerial(6,5);

//Globale Einstellungen
int cycle_delay = 100; //delay pro schleifen durchlauf, in milli secs
int led_shutdown_blink_time = 1000; // how long should a LED blink after receiving shutdown message, in milli secs
int hitcooldown_length = 2000; //how long hits wont be detected after a hit, in milli secs
int calibrate = 3; //how often should we measure the mean value


//LED 1 States
int led1on = 0; //2 = on, 1 = blinking, 0 = off
int led1_blink_timer = 0;
boolean led1_blink_state = false;

//LED 2 States
int led2on = 0; //2 = on, 1 = blinking, 0 = off
int led2_blink_timer = 0;
boolean led2_blink_state = false;

//Schwellenwerte fuer Hit Detection
long trigger;
long trigger2;


//SoftwareSerial myCapacitiveSerial(10,9);

void setup()
{
    Serial.begin(115200); //faster when off?
    mySerial.begin(115200);
    //  myCapacitiveSerial.begin(115200);
    pinMode(led1, OUTPUT);
    pinMode(led2,OUTPUT);

    //cs_4_2.set_CS_AutocaL_Millis(0xFFFFFFFF);     // turn off autocalibrate on channel 1 - just as an example

  
  
  // setup mean
  if(true){
   //Serial.print("init:"); 
   long sum = 0;
   long sum2 = 0;
   for(int i=0;i<calibrate;i++){
      long total1 =  cs_9_10.capacitiveSensor(30);
      long total2 = cs_3_4.capacitiveSensor(30);
      sum = sum + total1;
      sum2 = sum2 + total2;
       //Serial.print(sum);
   }

     
    
    long mean = sum/calibrate;
    long mean2 = sum2/calibrate;
    if(mean <= 0) mean = 1;
    if(mean2 <= 0) mean2 = 1;
    trigger = mean * 10 + 500;
    trigger2 = mean2 * 10 + 500;

  
          // check on performance in milliseconds
    if(true){
      Serial.print("\t mean: ");                    // tab character for debug window spacing
      Serial.print(mean);   
      Serial.print("\t sum: ");
      Serial.print(sum);
      Serial.print("\t trigger: ");                    // tab character for debug window spacing
      Serial.println(trigger);  
      Serial.print("\t trigger2: ");
      Serial.println(trigger2);
    }
  }
}

void loop(){
    capacitive();
    capacitive2();

    //global delay per cycle
    //delay(cycle_delay);
    
    //small deactivation of zone after hitting it, to prevent one hit counting twice
    if(hitcooldown > 0) hitcooldown = hitcooldown - cycle_delay;
    
    // myCapacitiveSerial.listen();
    //  while(myCapacitiveSerial.available() > 0){}
    //mySerial.listen();

    //if (mySerial.available() > 0 or true) {
        string = "";
    //}

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

    //debug LED tests
    /*
    if(string =="TO"){
        led1On();
        led2On();
        led1on = true;
    }

    if(string =="TF"){
        led1Off();
        led2Off();
        led1on = false;
        //  mySerial.println(string);
    }
    */

    //zone control from app
    if(string =="1n"){
        led1On();
    }
    if(string =="2n"){
        led2On();
    }
    if(string =="1f"){
        led1Off_countdown();
    }
    if(string =="2f"){
        led2Off_countdown();
    }
    //mySerial.println("hello");
    //Serial.print("loop");
    if(string.length() > 0) {
      //Serial.println(string);
    }
    
    //make LEDs blink for 1 sec once they are shut down by app
    if (led1_blink_timer > 0){
      led1_blink_timer = led1_blink_timer - cycle_delay;
      if(led1on == 1){
        if(led1_blink_state){
          led1_blink_state = false;
          analogWrite(led1, 0);
        }else{
          led1_blink_state = true;
          analogWrite(led1, 255);
        }
        if(led1_blink_timer <= 0){
          led1Off();
        }
      }
    }
    if (led2_blink_timer > 0){
      led2_blink_timer = led2_blink_timer - cycle_delay;
      if(led2on == 1){
        if(led2_blink_state){
          led2_blink_state = false;
          analogWrite(led2, 0);
        }else{
          led2_blink_state = true;
          analogWrite(led2, 255);
        }
        if(led2_blink_timer <= 0){
          led2Off();
        }
      }
    }

    //bluetooth debug message
    //mySerial.println("hit1");
    
  /*  if ((string.toInt()>=0)&&(string.toInt()<=255)){
        if (ledon==true){
            analogWrite(led, string.toInt());
            // mySerial.println(string);
            delay(10);
        }
    }*/
    
}

void led1On(){
    analogWrite(led1, 255);
    delay(10);
    //Serial.println("zone 1 on");
    led1on = 2;
}

void led1Off(){
    analogWrite(led1, 0);
    delay(10);
    //Serial.println("zone 1 off");
    led1on = 0;
}

void led1Off_countdown(){
  if(led1_blink_timer <= 0){
    led1on = 1;
    led1_blink_timer = led_shutdown_blink_time;
  }
}

void led2On(){
    analogWrite(led2, 255);
    delay(10);
    //Serial.println("zone 2 on");
    led2on = 2;
}

void led2Off(){
    analogWrite(led2, 0);
    delay(10);
    //Serial.println("zone 2 not off");
    led2on = 0;
}

void led2Off_countdown(){
    if(led2_blink_timer <= 0){
      led2on = 1;
      led2_blink_timer = led_shutdown_blink_time;
    }
}

void capacitive(){
    long start = millis();
    long total1 =  cs_9_10.capacitiveSensor(30);
    //long total2 =  cs_4_5.capacitiveSensor(30);
    //long total3 =  cs_4_8.capacitiveSensor(30);

    if(true){
      //Serial.print(millis() - start);        // check on performance in milliseconds
      Serial.print("\t trigger: ");                    // tab character for debug window spacing
      Serial.print(trigger);   
      Serial.print("\t gemessen: ");
      Serial.println(total1);                  // print sensor output 1
    }
    
    if(total1 > trigger && led1on > 0 && hitcooldown <= 0){  // threshold ermitteln, wenn gesamtwiderstand des garns fest steht
      Serial.println("hit1");
      mySerial.println("hit1");
      hitcooldown = hitcooldown_length;
    }else{
      //mySerial.println("no hit");
    }
    //Serial.print("\t");
    //Serial.print(total2);                  // print sensor output 2
    //Serial.print("\t");
    //.println(total3);                // print sensor output 3

    //delay(500);
    delay(cycle_delay/2);
}
void capacitive2(){
    long start = millis();
    long total1 =  cs_3_4.capacitiveSensor(30);
    //long total2 =  cs_4_5.capacitiveSensor(30);
    //long total3 =  cs_4_8.capacitiveSensor(30);

    if(true){
      //Serial.print(millis() - start);        // check on performance in milliseconds
      Serial.print("\t trigger2: ");                    // tab character for debug window spacing
      Serial.print(trigger2);   
      Serial.print("\t gemessen: ");
      Serial.println(total1);                  // print sensor output 1
    }
    //Serial.print("hitcooldown ");
    //Serial.print(hitcooldown);
    //Serial.print("led2on state ");
    //Serial.print(led2on);
    if(total1 > trigger2  && led2on > 0 && hitcooldown <= 0){  // threshold ermitteln, wenn gesamtwiderstand des garns fest steht
      Serial.println("hit2");
      mySerial.println("hit2");
      hitcooldown = hitcooldown_length;
      
    }else{
      //mySerial.println("no hit2");
    }
    //Serial.print("\t");
    //Serial.print(total2);                  // print sensor output 2
    //Serial.print("\t");
    //.println(total3);                // print sensor output 3

    //delay(500);
    delay(cycle_delay/2);
}
    
    

