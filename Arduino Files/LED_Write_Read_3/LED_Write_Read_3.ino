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
int led_phase_time = 1250; // how long each LED phase lasts
int hitcooldown_length = 0; //how long hits wont be detected after a hit, in milli secs
int calibrate = 3; //how often should we measure the mean value


//LED 1 States
int led1on = 0; //3 = on bright, 2 = on weak, 1 = blinking, 0 = off
int led1_timer = 0;
boolean led1_blink_state = false;

//LED 2 States
int led2on = 0; //3 = on bright, 2 = on weak, 1 = blinking, 0 = off
int led2_timer = 0;
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
    string = "";
    if(led1on > 0 or true){
      capacitive();
    }
    if(led2on > 0 or true){
      capacitive2();
    }
    
    //global delay per cycle
    delay(cycle_delay);
    
    //small deactivation of zone after hitting it, to prevent one hit counting twice
    if(hitcooldown > 0) hitcooldown = hitcooldown - cycle_delay;
    
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

    //zone control from app
    int z1on_matches = 0;
    int z1off_matches = 0;
    int z2on_matches = 0;
    int z2off_matches = 0;
    if(string.length() >= 4) {
      for(int i = 0; i<=3; i++){
        if(string[i] == '0'){
          z1on_matches++;
        }
        if(string[i] == '5'){
          z1off_matches++;
        }
        if(string[i] == '1'){
          z2on_matches++;
        }
        if(string[i] == '6'){
          z2off_matches++;
        }
      }
    }
    if(z1on_matches >= 2){
        Serial.println("phase zone 1: 3");
        led1On();
    }
    if(z2on_matches >= 2){
        Serial.println("phase zone 2: 3");
        led2On();
    }
    /* cant occur anymore
    if(z1off_matches >= 2){
        Serial.print("1 off");
        led1Off_countdown();
    }
    if(z2off_matches >= 2){
        Serial.print("2 off");
        led2Off_countdown();
    }
    */
    
    /*
    if(string == "0000"){
        led1On();
    }
    if(string == "1111"){
        led2On();
    }
    if(string == "5555"){
        led1Off_countdown();
    }
    if(string == "6666"){
        led2Off_countdown();
    }
    */
    //mySerial.println("hello");
    Serial.print(string);
    
    //start new phase when a phase ends: 3 -> 2 -> 1 -> 0
    //LED 1
    if (led1on > 0){
      led1_timer = led1_timer - cycle_delay;
      if(led1_timer <= 0){
        led1on = led1on - 1; //change phase
        led1_timer = led_phase_time;
        Serial.print("phase zone 1: ");
        Serial.println(led1on);
        if(led1on == 2){
          led1OnWeak();
        }
      }
      if(led1on == 1){
        if(led1_blink_state){
          led1_blink_state = false;
          analogWrite(led1, 0);
        }else{
          led1_blink_state = true;
          analogWrite(led1, 255);
        }
      }
    }else{
      led1Off();
    }
    //LED 2
    if (led2on > 0){
      led2_timer = led2_timer - cycle_delay;
      if(led2_timer <= 0){
        led2on = led2on - 1; //change phase
        led2_timer = led_phase_time;
        Serial.print("phase zone 2: ");
        Serial.println(led2on);
        if(led2on == 2){
          led2OnWeak();
        }
      }
      if(led2on == 1){
        if(led2_blink_state){
          led2_blink_state = false;
          analogWrite(led2, 0);
        }else{
          led2_blink_state = true;
          analogWrite(led2, 255);
        }
      }
    }else{
      led2Off();
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
    led1on = 3;
    led1_timer = led_phase_time;
}

void led1OnWeak(){
    analogWrite(led1, 200);
    led1_timer = led_phase_time;
}

void led1Off(){
    analogWrite(led1, 0);
    led1_timer = 0;
    led1on = 0;
}

void led2On(){
    analogWrite(led2, 255);
    led2on = 3;
    led2_timer = led_phase_time;
}

void led2OnWeak(){
    analogWrite(led2, 200);
    led2_timer = led_phase_time;
}

void led2Off(){
    analogWrite(led2, 0);
    led2_timer = 0;
    led2on = 0;
}

void capacitive(){
    long start = millis();
    long total1 =  cs_9_10.capacitiveSensor(30);
    //long total2 =  cs_4_5.capacitiveSensor(30);
    //long total3 =  cs_4_8.capacitiveSensor(30);

    if(true){
      //Serial.print(millis() - start);        // check on performance in milliseconds
      //Serial.print("\t trigger: ");                    // tab character for debug window spacing
      //Serial.print(trigger);   
      //Serial.print("\t gemessen: ");
      //Serial.println(total1);                  // print sensor output 1
    }
    
    if(total1 > trigger && led1on > 0 && hitcooldown <= 0){  // threshold ermitteln, wenn gesamtwiderstand des garns fest steht
      Serial.println("hit1");
      if(led1on == 3){
        mySerial.println("h3");
      }
      if(led1on == 2){
        mySerial.println("h2");
      }
      if(led1on == 1){
        mySerial.println("h1");
      }
      
      hitcooldown = hitcooldown_length;
      led1Off();
    }else{
      //mySerial.println("no hit");
    }
    //Serial.print("\t");
    //Serial.print(total2);                  // print sensor output 2
    //Serial.print("\t");
    //.println(total3);                // print sensor output 3

    //delay(500);
    //delay(cycle_delay/2);
}
void capacitive2(){
    long start = millis();
    long total1 =  cs_3_4.capacitiveSensor(30);
    //long total2 =  cs_4_5.capacitiveSensor(30);
    //long total3 =  cs_4_8.capacitiveSensor(30);

    if(true){
      //Serial.print(millis() - start);        // check on performance in milliseconds
      //Serial.print("\t trigger2: ");                    // tab character for debug window spacing
      //Serial.print(trigger2);   
      //Serial.print("\t gemessen: ");
      //Serial.println(total1);                  // print sensor output 1
    }
    //Serial.print("hitcooldown ");
    //Serial.print(hitcooldown);
    //Serial.print("led2on state ");
    //Serial.print(led2on);
    if(total1 > trigger2  && led2on > 0 && hitcooldown <= 0){  // threshold ermitteln, wenn gesamtwiderstand des garns fest steht
      Serial.println("hit2");
      if(led1on == 3){
        mySerial.println("h3");
      }
      if(led1on == 2){
        mySerial.println("h2");
      }
      if(led1on == 1){
        mySerial.println("h1");
      }
      hitcooldown = hitcooldown_length;
      led2Off();
    }else{
      //mySerial.println("no hit2");
    }
    //Serial.print("\t");
    //Serial.print(total2);                  // print sensor output 2
    //Serial.print("\t");
    //.println(total3);                // print sensor output 3

    //delay(500);
    //delay(cycle_delay/2);
}
    
    

