#include "RecordInfo.h"

int RecordInfo::getLength() {
	return length;
}

void RecordInfo::toBytes(uint8_t *bytes) {
	memcpy(bytes, data, length);
}

void RecordInfo::fromBytes(uint8_t *bytes, int length, BTreeElement *parent) {
	data = (uint8_t*)malloc(length);
	memcpy(data, bytes, length);
	this->length = length;
}
