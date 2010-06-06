#ifndef _NODECONTENT_H
#define _NODECONTENT_H

#include "stdlib.h"

class BTreeElement;
class BTree;

class NodeContent {
	protected:
		BTree *btree;
	public:
		NodeContent(BTree *btree);
		virtual int getLength() = 0;
		virtual void toBytes(uint8_t *bytes) = 0;
		virtual void fromBytes(uint8_t *bytes, int length, BTreeElement *parent) = 0;
};

#endif
