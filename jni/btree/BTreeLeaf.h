#ifndef _BTREELEAF_H
#define _BTREELEAF_H

#include "BTreeNode.h"

struct RecordInfo;
template<typename T> class AVLTree;

class BTreeLeaf : public BTreeElement {
	private:
	
		AVLTree<RecordInfo> *records;

	public:
		BTreeLeaf(short freeSpace);
		~BTreeLeaf();
		AVLTree<RecordInfo> *getAVLTree();
		bool add(int id, RecordInfo *record);
		bool update(int id, RecordInfo *record);
		void remove(int id);
		RecordInfo *get(int id);
		void split(BTreeLeaf *leaf);
		void join(BTreeElement *leaf);
		void setLeft(BTreeLeaf *leaf);
		void debug();

};

#endif
