#include <NimBLEDevice.h>
#include <ESP32Servo.h>

// ================= BLE CONFIG =================
static const char* DEVICE_NAME = "SousChef";
static const char* SERVICE_UUID = "12345678-1234-1234-1234-123456789abc";
static const char* DISPENSE_CHAR_UUID = "87654321-4321-4321-4321-cba987654321";
static const char* BUTTON_CHAR_UUID   = "87654321-4321-4321-4321-cba987654322"; // Characteristic for button

// ================= STEPPER =================
#define STEP_PIN 18
#define DIR_PIN 19
#define EN_PIN 25

int currentPosition = 0;
// 200 steps per revolution / 5 compartments = 40 steps (72 degrees)
// Since it's moving very little, your driver is in a microstepping mode!
// Let's try 1/16th microstepping: 40 * 16 = 640 steps per position
int stepsPerPosition = 640;

Servo servo;
int servoPin = 26; // Safe PWM pin for ESP32-WROOM-32D

// ================= MOTOR FUNCTIONS =================
void stepMotor(int steps) {
  if (steps == 0) return;

  // Wake up motor driver
  digitalWrite(EN_PIN, LOW);
  delay(1);

  // Set direction
  digitalWrite(DIR_PIN, (steps > 0) ? HIGH : LOW);

  steps = abs(steps);

  for (int i = 0; i < steps; i++) {
    digitalWrite(STEP_PIN, HIGH);
    delayMicroseconds(700);
    digitalWrite(STEP_PIN, LOW);
    delayMicroseconds(700);
  }
}

void moveToSpice(int target) {
  int stepsToMove = (target - currentPosition) * stepsPerPosition;
  Serial.print("Moving steps: ");
  Serial.println(stepsToMove);

  stepMotor(stepsToMove);
  currentPosition = target;
}

// ================= STATE =================
uint8_t pendingCompartmentId = 0;
uint8_t pendingCount = 0;
bool pendingDispense = false;

// ================= COOKING-MODE BUTTONS =================
// Three physical buttons that mirror the on-screen Cooking Mode controls:
//   PREV     → byte 1 (previous step)
//   NEXT     → byte 2 (next step / finish)
//   DISPENSE → byte 3 (dispense the current step's ingredient)
//
// Wiring (active-HIGH): each button bridges a 3.3V (or logic-level-shifted)
// source to its GPIO pin. INPUT_PULLDOWN keeps the line LOW when idle;
// pressing the button drives it HIGH (rising edge = press).
//
// ⚠️ ESP32 GPIOs are 3.3V tolerant only. Drive the buttons from the
//    board's 3V3 rail (or use a voltage divider / level shifter if you
//    insist on 5V) — feeding raw 5V to a GPIO can damage the chip.
//
// Pin choice notes (ESP32-WROOM-32D):
//   - GPIOs 23, 16, 17, 4 are general-purpose digital I/O with internal
//     pull-down support — ideal for active-HIGH buttons.
//   - GPIOs 34/35/36/39 are input-only with NO internal pull resistors,
//     so they'd need external resistors. We avoid them here.
#define BUTTON_PREV_PIN     23
#define BUTTON_NEXT_PIN     17
#define BUTTON_DISPENSE_PIN 16

// Debounce window — typical tactile switches settle within 30–50 ms.
const unsigned long DEBOUNCE_MS = 40;

struct ButtonState {
  uint8_t pin;
  uint8_t payload;        // Byte sent to the app on a confirmed press.
  bool    lastReading;    // Most recent raw read (HIGH/LOW).
  bool    stableState;    // Debounced state (HIGH/LOW). LOW = idle.
  unsigned long lastChangeMs;
};

// Idle state is LOW (pulled down); a confirmed press transitions to HIGH.
ButtonState buttons[3] = {
  { BUTTON_PREV_PIN,     1, LOW, LOW, 0 },
  { BUTTON_NEXT_PIN,     2, LOW, LOW, 0 },
  { BUTTON_DISPENSE_PIN, 3, LOW, LOW, 0 }
};

NimBLECharacteristic* pButtonCharacteristic = NULL;

void moveAndDispense(uint8_t compartmentId, uint8_t count) {

  Serial.println(" Moving to position...");
  moveToSpice(compartmentId - 1);

  delay(500);

  // Turn OFF stepper to stop holding torque and silence the motor
  digitalWrite(EN_PIN, HIGH);

  Serial.println(" Stepper stopped");

  delay(200);

  Serial.println(" Dispensing...");

  for (int i = 0; i < count; i++) {
    servo.write(90);
    delay(400);
    servo.write(0);
    delay(400);
  }

  Serial.println(" Done");
}

// ================= BLE SERVER CALLBACKS =================
class ServerCallbacks : public NimBLEServerCallbacks {
    void onConnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo) override {
        Serial.println(" DEVICE CONNECTED!");
        // Update connection parameters if needed
        pServer->updateConnParams(connInfo.getConnHandle(), 24, 48, 0, 60);
    }

    void onDisconnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo, int reason) override {
        Serial.println(" DEVICE DISCONNECTED!");
        // Restart advertising so Android can find it again
        NimBLEDevice::startAdvertising();
    }

    void onMTUChange(uint16_t MTU, NimBLEConnInfo& connInfo) override {
        Serial.print(" MTU Updated to: ");
        Serial.println(MTU);
    }
};

// ================= BLE CALLBACK =================
class DispenseCallback : public NimBLECharacteristicCallbacks {
  void onWrite(NimBLECharacteristic* pChar, NimBLEConnInfo& connInfo) override {

    Serial.println(" WRITE RECEIVED");

    std::string v = pChar->getValue();

    Serial.print("Raw length: ");
    Serial.println(v.length());

    if (v.length() != 2) {
      Serial.println(" Invalid data length");
      return;
    }

    uint8_t compartmentId = (uint8_t)v[0];
    uint8_t count         = (uint8_t)v[1];

    Serial.print("Compartment: ");
    Serial.println(compartmentId);

    Serial.print("Count: ");
    Serial.println(count);

    if (compartmentId < 1 || compartmentId > 5) {
      Serial.println(" Invalid compartment");
      return;
    }

    if (count < 1 || count > 255) {
      Serial.println(" Invalid count");
      return;
    }

    pendingCompartmentId = compartmentId;
    pendingCount = count;
    pendingDispense = true;
    Serial.println("Dispense queued for processing!");
  }
};

// ================= SETUP =================
void setup() {
  Serial.begin(115200);

  // Configure all three cooking-mode buttons with internal pull-downs so
  // each button can drive its pin HIGH (active-HIGH) when pressed.
  for (uint8_t i = 0; i < 3; i++) {
    pinMode(buttons[i].pin, INPUT_PULLDOWN);
  }

  pinMode(STEP_PIN, OUTPUT);
  pinMode(DIR_PIN, OUTPUT);
  pinMode(EN_PIN, OUTPUT);
  digitalWrite(EN_PIN, HIGH); // Start with motor turned off

  // Servo setup
  ESP32PWM::allocateTimer(0);
  ESP32PWM::allocateTimer(1);
  ESP32PWM::allocateTimer(2);
  ESP32PWM::allocateTimer(3);
  servo.setPeriodHertz(50);
  servo.attach(servoPin, 500, 2400);
  servo.write(0);

  // BLE setup
  NimBLEDevice::init(DEVICE_NAME);

  NimBLEServer* server = NimBLEDevice::createServer();
  server->setCallbacks(new ServerCallbacks());

  NimBLEService* service = server->createService(SERVICE_UUID);

  NimBLECharacteristic* dispenseChar = service->createCharacteristic(
    DISPENSE_CHAR_UUID,
    NIMBLE_PROPERTY::WRITE | NIMBLE_PROPERTY::WRITE_NR  //  IMPORTANT
  );

  dispenseChar->setCallbacks(new DispenseCallback());

  // Configure characteristic to notify the app
  pButtonCharacteristic = service->createCharacteristic(
    BUTTON_CHAR_UUID,
    NIMBLE_PROPERTY::READ | NIMBLE_PROPERTY::NOTIFY
  );

  service->start();

  NimBLEAdvertising* adv = NimBLEDevice::getAdvertising();

  adv->addServiceUUID(SERVICE_UUID);
  adv->setName(DEVICE_NAME);

  NimBLEAdvertisementData scanData;
  scanData.setName(DEVICE_NAME);

  adv->setScanResponseData(scanData);

  adv->start();

  Serial.println(" BLE Ready!");
}

// ================= LOOP =================
void loop() {
  if (pendingDispense) {
    uint8_t comp = pendingCompartmentId;
    uint8_t count = pendingCount;
    pendingDispense = false;
    moveAndDispense(comp, count);
  }

  // Poll all three cooking-mode buttons. We debounce in software: a press
  // is registered only when the line stays HIGH for at least DEBOUNCE_MS.
  unsigned long now = millis();
  for (uint8_t i = 0; i < 3; i++) {
    ButtonState &b = buttons[i];
    bool reading = digitalRead(b.pin);

    if (reading != b.lastReading) {
      b.lastChangeMs = now;
      b.lastReading = reading;
    }

    if ((now - b.lastChangeMs) >= DEBOUNCE_MS && reading != b.stableState) {
      b.stableState = reading;
      // Rising edge (LOW → HIGH) = press (active-HIGH wiring).
      if (b.stableState == HIGH) {
        Serial.print("Hardware Button Pressed: payload=");
        Serial.println(b.payload);
        if (pButtonCharacteristic != NULL) {
          pButtonCharacteristic->setValue(&b.payload, 1);
          pButtonCharacteristic->notify();
        }
      }
    }
  }
}