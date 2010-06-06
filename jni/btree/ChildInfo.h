#ifndef _CHILDINFO_H
#define _CHILDINFO_H

class BTreeElement;
class BTree;

#include <stdlib.h>
#include "NodeContent.h"

class ChildInfo : public NodeContent {
	public:
		ChildInfo(BTree *btree);
		BTreeElement *child;
		~ChildInfo();

		int getLength();
		void toBytes(uint8_t *bytes);
		void fromBytes(uint8_t *bytes, int length, BTreeElement *parent);
};
#endif
