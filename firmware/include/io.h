#ifndef IO_H_
#define IO_H_

#include <inttypes.h>

void init_io(void);

void set_led(int led, int state);
void set_leds(int state);

uint32_t get_battery_voltage(void);

#endif // IO_H_
