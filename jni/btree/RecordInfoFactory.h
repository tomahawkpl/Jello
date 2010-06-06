#ifndef _RECORDINFOFACTORY_H
#define _RECORDINFOFACTORY_H

#include "NodeContentFactory.h"

class BTree;

class RecordInfoFactory : public NodeContentFactory {
	private:
		BTree *btree;
	public:
		RecordInfoFactory(BTree *btree);
		NodeContent *create();
};

#endif
