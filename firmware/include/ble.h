#ifndef BLE_H_
#define BLE_H_

extern bool deviceConnected;

void init_ble(void);
void deinit_ble(void);
void update_ble_characteristics(void);

#endif // BLE_H_
