#ifndef _BTREEELEMENT_H
#define _BTREEELEMENT_H

class BTreeNode;


class BTreeElement {
	protected:
		BTreeNode *parent;
		int minId;
	public:
		static const int ELEMENT_NODE = 1;
		static const int ELEMENT_LEAF = 2;
		int type;

		int getMinId();

};

#endif
