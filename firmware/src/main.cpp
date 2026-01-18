#include <Arduino.h>

#include "ble.h"
#include "display.h"
#include "io.h"
#include "game_state.h"

void setup()
{
	Serial.begin(115200);

	init_io();

	set_leds(1);
	
	init_ble();

	init_display();

	set_leds(0);
}

void loop()
{
	static int i = 0;
	
	/* update battery voltage characteristic every 10s */
	if (!i)
		update_ble_characteristics();
	i = (++i) % 100;

	if (deviceConnected) {
		update_game_screen();
		delay(100);
	} else {
		show_connect_screen();
		for (int j = 0; j < 600; j++) {
			delay(100);
			if (deviceConnected)
				break;
		}

		if (!deviceConnected) {
			show_standby_screen();
			deinit_ble();
			Serial.flush();
			Serial.end();
			
			enable_touch_wakeup_pin();
			esp_deep_sleep_start();
		}
	}
	return;
}
