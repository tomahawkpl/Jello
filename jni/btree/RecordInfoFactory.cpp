#include "RecordInfoFactory.h"

#include "RecordInfo.h"

RecordInfoFactory::RecordInfoFactory(BTree *btree) {
	this->btree = btree;
}

NodeContent *RecordInfoFactory::create() {
	return new RecordInfo(btree);
}
