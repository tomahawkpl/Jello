#include "misc.h"

void intToBytes(int i, uint8_t *data) {
	data[0] = i >> 0 & 0xFF;
	data[1] = i >> 8 & 0xFF;
	data[2] = i >> 16 & 0xFF;
	data[3] = i >> 24 & 0xFF;
}

void bytesToInt(int &i, uint8_t *data) {
	i = 0;
	i = data[3];
	i = (i << 8) | data[2];
	i = (i << 8) | data[1];
	i = (i << 8) | data[0];
}
