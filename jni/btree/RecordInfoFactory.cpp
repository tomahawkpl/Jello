#include "RecordInfoFactory.h"

#include "RecordInfo.h"

NodeContent *RecordInfoFactory::create() {
	return new RecordInfo();
}
