#include "jni.h"

class BTreeElement;
class BTreeLeaf;
struct RecordInfo;

class BTree {
	private:
		BTreeElement *root;
		short leafCapacity, nodeCapacity;

		void mergeLeaf(BTreeLeaf *leaf);
	public:
		BTree(short leafCapacity, short nodeCapacity);
		void add(int id, RecordInfo *record);
		void update(int id, RecordInfo *record);
		RecordInfo* find(int id);
		void remove(int id);
};
