#ifndef _CHILDINFOFACTORY_H
#define _CHILDINFOFACTORY_H

#include "NodeContentFactory.h"

class BTree;

class ChildInfoFactory : public NodeContentFactory {
	private:
		BTree* btree;
	public:
		ChildInfoFactory(BTree *btree);
		NodeContent *create();
};

#endif
