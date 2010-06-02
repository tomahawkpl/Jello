#include "ChildInfoFactory.h"

#include "ChildInfo.h"

NodeContent *ChildInfoFactory::create() {
	return new ChildInfo();
}
