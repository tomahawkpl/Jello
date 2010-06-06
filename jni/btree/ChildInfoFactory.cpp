#include "ChildInfoFactory.h"

#include "ChildInfo.h"

ChildInfoFactory::ChildInfoFactory(BTree *btree) {
	this->btree = btree;
}

NodeContent *ChildInfoFactory::create() {
	return new ChildInfo(btree);
}
