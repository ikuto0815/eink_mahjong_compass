#include <Arduino.h>

#include "io.h"
#include "game_state.h"

static const int touchPins[] = {7, 6, 5, 4};
static const int ledPins[] = {38, 37, 36, 35};
static const int buttonPins[] = {42, 41, 40, 39};

#define BATTERY_V_PIN 14

static void set_active_player(int i, int state)
{
	if (state) {
		digitalWrite(ledPins[i], 1);
		game_state.active = i;
	} else {
		digitalWrite(ledPins[i], 0);
		game_state.active = 0xff;
	}
}

static void got_touch(void *arg)
{
	int i = (int)arg;
	int state = touchInterruptGetLastStatus(touchPins[i]);

	Serial.printf("touch %d state %d\n", i, state);
	set_active_player(i, state);
}

static void button_isr(void *arg) {
	int i = (int)arg;
	int state = !digitalRead(buttonPins[i]);

	Serial.printf("button %d state %d\n", i, state);
	set_active_player(i, state);
}

void set_led(int led, int state)
{
	digitalWrite(ledPins[led], state);
}

void set_leds(int state)
{
	for (int i = 0; i < 4; i++)
		digitalWrite(ledPins[i], state);
}

/*
 * read the analog pin and convert the value back to the actual battery voltage.
 * (Voltage divider with 330k and 1000k resistors used)
 */
uint32_t get_battery_voltage(void)
{
	return analogReadMilliVolts(BATTERY_V_PIN)*(330+1000)/330;
}

void init_io(void)
{
	int touch_threshold = 40;

	touchSetDefaultThreshold(touch_threshold);

	for (int i = 0; i < 4; i++) {
		pinMode(buttonPins[i], INPUT_PULLUP);
		touchAttachInterruptArg(touchPins[i],  got_touch, (void*)i, 0);

		pinMode(ledPins[i], OUTPUT);
		digitalWrite(ledPins[i], 0);
		attachInterruptArg(buttonPins[i], button_isr, (void*)i, CHANGE);
	}

	pinMode(BATTERY_V_PIN, INPUT);

	// limit the voltage range. 4.5V battery voltage would be 1.12V on BATTERY_V_PIN
	analogSetAttenuation(ADC_6db);
}

/* set pin for bottom right corner to sleep wakeup
 * esp32-s3 seems to only support one touch wakeup pin sadly
 * having the interrupt still attached makes the wakeup not work sometimes
 */
void enable_touch_wakeup_pin(void)
{
	for (int i = 0; i < 4; i++)
		touchDetachInterrupt(touchPins[i]);
	touchSleepWakeUpEnable(touchPins[3], 40);
}
