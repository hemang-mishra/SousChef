#include <NimBLEDevice.h>
#include <ESP32Servo.h>

// ================= BLE CONFIG =================
static const char* DEVICE_NAME = "SousChef";
static const char* SERVICE_UUID = "12345678-1234-1234-1234-123456789abc";
static const char* DISPENSE_CHAR_UUID = "87654321-4321-4321-4321-cba987654321";

// ================= STEPPER =================
#define IN1 14
#define IN2 27
#define IN3 26
#define IN4 25

int stepSequence[8][4] = {
  {1, 0, 0, 0},
  {1, 1, 0, 0},
  {0, 1, 0, 0},
  {0, 1, 1, 0},
  {0, 0, 1, 0},
  {0, 0, 1, 1},
  {0, 0, 0, 1},
  {1, 0, 0, 1}
};

int currentPosition = 0;
int stepsPerPosition = 819;

// ================= SERVO =================
Servo servo;
int servoPin = 13;

int motorStepIndex = 0;

// ================= MOTOR FUNCTIONS =================
void stepMotor(int steps) {
  int direction = (steps > 0) ? 1 : -1;
  steps = abs(steps);

  for (int i = 0; i < steps; i++) {
    motorStepIndex += direction;
    if (motorStepIndex > 7) motorStepIndex = 0;
    if (motorStepIndex < 0) motorStepIndex = 7;

    digitalWrite(IN1, stepSequence[motorStepIndex][0]);
    digitalWrite(IN2, stepSequence[motorStepIndex][1]);
    digitalWrite(IN3, stepSequence[motorStepIndex][2]);
    digitalWrite(IN4, stepSequence[motorStepIndex][3]);

    delay(2); // 2ms gives reliable torque for 28BYJ-48
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

void moveAndDispense(uint8_t compartmentId, uint8_t count) {

  Serial.println("➡️ Moving to position...");
  moveToSpice(compartmentId - 1);

  delay(500);

  // Turn OFF stepper
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, LOW);

  Serial.println("🛑 Stepper stopped");

  delay(200);

  Serial.println("🍽 Dispensing...");

  for (int i = 0; i < count; i++) {
    servo.write(90);
    delay(400);
    servo.write(0);
    delay(400);
  }

  Serial.println("✅ Done");
}

// ================= BLE SERVER CALLBACKS =================
class ServerCallbacks : public NimBLEServerCallbacks {
    void onConnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo) override {
        Serial.println("🟢 DEVICE CONNECTED!");
        // Update connection parameters if needed
        pServer->updateConnParams(connInfo.getConnHandle(), 24, 48, 0, 60);
    }

    void onDisconnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo, int reason) override {
        Serial.println("🔴 DEVICE DISCONNECTED!");
        // Restart advertising so Android can find it again
        NimBLEDevice::startAdvertising();
    }
    
    void onMTUChange(uint16_t MTU, NimBLEConnInfo& connInfo) override {
        Serial.print("⚙️ MTU Updated to: ");
        Serial.println(MTU);
    }
};

// ================= BLE CALLBACK =================
class DispenseCallback : public NimBLECharacteristicCallbacks {
  void onWrite(NimBLECharacteristic* pChar, NimBLEConnInfo& connInfo) override {

    Serial.println("🔥 WRITE RECEIVED");

    std::string v = pChar->getValue();

    Serial.print("Raw length: ");
    Serial.println(v.length());

    if (v.length() != 2) {
      Serial.println("❌ Invalid data length");
      return;
    }

    uint8_t compartmentId = (uint8_t)v[0];
    uint8_t count         = (uint8_t)v[1];

    Serial.print("Compartment: ");
    Serial.println(compartmentId);

    Serial.print("Count: ");
    Serial.println(count);

    if (compartmentId < 1 || compartmentId > 5) {
      Serial.println("❌ Invalid compartment");
      return;
    }

    if (count < 1 || count > 255) {
      Serial.println("❌ Invalid count");
      return;
    }

    pendingCompartmentId = compartmentId;
    pendingCount = count;
    pendingDispense = true;
    Serial.println("✅ Dispense queued for processing!");
  }
};

// ================= SETUP =================
void setup() {
  Serial.begin(115200);

  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);

  // Servo setup
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
    NIMBLE_PROPERTY::WRITE | NIMBLE_PROPERTY::WRITE_NR  // ⭐ IMPORTANT
  );

  dispenseChar->setCallbacks(new DispenseCallback());

  service->start();

  NimBLEAdvertising* adv = NimBLEDevice::getAdvertising();

  adv->addServiceUUID(SERVICE_UUID);
  adv->setName(DEVICE_NAME);

  NimBLEAdvertisementData scanData;
  scanData.setName(DEVICE_NAME);

  adv->setScanResponseData(scanData);

  adv->start();

  Serial.println("🚀 BLE Ready!");
}

// ================= LOOP =================
void loop() {
  if (pendingDispense) {
    uint8_t comp = pendingCompartmentId;
    uint8_t count = pendingCount;
    pendingDispense = false;
    moveAndDispense(comp, count);
  }
  delay(50);
}
