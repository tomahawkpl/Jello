#ifndef _RECORDINFO_H
#define _RECORDINFO_H

#include <stdlib.h>
#include "NodeContent.h"

class BTreeElement;
class BTree;

class RecordInfo : public NodeContent {
	private:
		BTree *btree;
	public:
		RecordInfo(BTree* btree);
		uint8_t *data;
		int length;

		int getLength();
		void toBytes(uint8_t *bytes);
		void fromBytes(uint8_t *bytes, int length, BTreeElement *parent);
};

#endif
