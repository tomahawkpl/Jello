#include "jni.h"

class BTreeElement;
class BTreeLeaf;
class BTreeNode;
struct RecordInfo;

class BTree {
	private:
		BTreeElement *root;
		short leafCapacity, nodeCapacity;

		void mergeNode(BTreeElement *node);
		void removeNode(BTreeElement *node);
		void addToNode(BTreeNode *node, BTreeElement *child);
	public:
		BTree(short leafCapacity, short nodeCapacity);
		~BTree();
		void add(int id, RecordInfo *record);
		void update(int id, RecordInfo *record);
		RecordInfo* find(int id);
		void remove(int id);
		void debug();
};
