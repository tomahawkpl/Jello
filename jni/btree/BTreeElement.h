#ifndef _BTREEELEMENT_H
#define _BTREEELEMENT_H

class BTreeNode;
class BTreeLeaf;

class BTreeElement {
	protected:
		BTreeNode *parent;
		int minId;
		int freeSpace;
		int count;
//		BTreeElement *left;
//		BTreeElement *right;
	public:
		virtual ~BTreeElement();
		static const int ELEMENT_NODE = 1;
		static const int ELEMENT_LEAF = 2;
		int type;

		BTreeNode *getParent();
		void setParent(BTreeNode *parent);
		int getMinId();
		int getFreeSpace();
		int getCount();

		void setMinId(int minId);
		void setFreeSpace(int freeSpace);
		void setCount(int count);

//		BTreeElement *getRight();
//		BTreeElement *getLeft();
//		void setRight(BTreeElement *leaf);
//		void setLeft(BTreeElement *leaf);

		virtual void join(BTreeElement *node) = 0;
		virtual void debug();
		virtual int commit() = 0;


};

#endif
