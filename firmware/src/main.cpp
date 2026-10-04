#include <Arduino.h>

#include "ble.h"
#include "display.h"
#include "io.h"
#include "game_state.h"

#include "esp_pm.h"

static void configure_light_sleep(bool enable)
{
	esp_pm_config_t pm_config = { 0 };

	esp_pm_get_configuration(&pm_config);
	pm_config.light_sleep_enable = enable;

	Serial.printf("pm config: min %d max %d light sleep %d\n", pm_config.min_freq_mhz, pm_config.max_freq_mhz, pm_config.light_sleep_enable);

	ESP_ERROR_CHECK(esp_pm_configure(&pm_config));
}

void setup()
{
	Serial.begin(115200);

	init_io();

	set_leds(1);

	init_ble();

	init_display();

	set_leds(0);

	configure_light_sleep(true);
}

static int disconnectTimeout;

#define REFRESH_DELAY_MS 100

// time to wait after a device disconnected until going back to standby
// some phones kill the BT connection when the screen is off. Otherwise this
// wouldn't be needed.
#define DISCONNECT_TIMEOUT_MS (20 * 60 * 1000)

// how long the connect screen will wait for a connection
#define CONNECT_SCREEN_TIMEOUT_MS (2 * 60 * 1000)

void loop()
{
	static int i = 0;

	/* update battery voltage characteristic every 10s */
	if (!i)
		update_ble_characteristics();
	i = (++i) % 100;

	if (deviceConnected ||
	    disconnectTimeout > 0) {
		update_game_screen();
		delay(REFRESH_DELAY_MS);
		if (deviceConnected)
			disconnectTimeout = DISCONNECT_TIMEOUT_MS;
		else
			disconnectTimeout -= REFRESH_DELAY_MS;
	} else {
		show_connect_screen();
		disconnectTimeout = CONNECT_SCREEN_TIMEOUT_MS;
		while (disconnectTimeout > 0) {
			delay(REFRESH_DELAY_MS);
			disconnectTimeout -= REFRESH_DELAY_MS;

			if (deviceConnected)
				break;
		}

		if (!deviceConnected) {
			show_standby_screen();
			deinit_ble();

			// light sleep has to be turned off otherwise
			// deep sleep immediately returns for some reason
			configure_light_sleep(false);

			Serial.flush();
			Serial.end();

			enable_touch_wakeup_pin();

			esp_deep_sleep_start();
		}
	}
	return;
}
