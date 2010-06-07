#ifndef _AVLTREE_H
#define _AVLTREE_H

#include <stdlib.h>

class NodeContent;
class NodeContentFactory;
class BTreeElement;

struct AVLTreeNode {
	int recordId;

	NodeContent *content;

	AVLTreeNode *parent;

	AVLTreeNode *left;
	AVLTreeNode *right;

	int height;
	int balance;

	void updateHeight();
	
};

class AVLTree {
	private:
		int count;
		AVLTreeNode *root;
		NodeContentFactory *factory;

		void removeNode(AVLTreeNode *node);
		void freeNode(AVLTreeNode *node);

		void rebalance(AVLTreeNode *node);
		void rotateRight(AVLTreeNode *&node);
		void rotateLeft(AVLTreeNode *&node);
		void rotateRightTwice(AVLTreeNode *&node);
		void rotateLeftTwice(AVLTreeNode *&node);
	public:
		AVLTree(NodeContentFactory *factory);
		~AVLTree();
		void add(int recordId, NodeContent *node);
		bool remove(int recordId);
		AVLTreeNode *findNode(int recordId);
		AVLTreeNode *getSmallest();
		AVLTreeNode *extractSmallest();
		AVLTreeNode *findRightNode(int recordId);
		NodeContent *find(int recordId);
		NodeContent *findHigher(int recordId);
		NodeContent *findRight(int recordId);
		NodeContent *findLeft(int recordId);
		void update(int recordId, NodeContent *content, AVLTreeNode *node);
		int getCount();
		void debug(bool follow);
		void debugNode(AVLTreeNode *node);
		void printNode(AVLTreeNode *node);

		int nodeToBytes(uint8_t *bytes, AVLTreeNode *node);
		AVLTreeNode *nodeFromBytes(uint8_t *bytes, int &read, BTreeElement *parent);
		void commit(uint8_t *bytes);
		void fromBytes(uint8_t *bytes, BTreeElement *parent);


};

#endif
