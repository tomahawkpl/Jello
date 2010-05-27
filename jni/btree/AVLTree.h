#ifndef _AVLTREE_H
#define _AVLTREE_H

template<typename T>
struct AVLTreeNode {
	int recordId;
	T *content;

	AVLTreeNode *parent;

	AVLTreeNode *left;
	AVLTreeNode *right;

	int height;
	int balance;

	void updateHeight();
	
};

template<typename T>
class AVLTree {
	private:
		int count;
		AVLTreeNode<T> *root;

		void removeNode(AVLTreeNode<T> *node);
		void freeNode(AVLTreeNode<T> *node);

		void rebalance(AVLTreeNode<T> *node);
		void rotateRight(AVLTreeNode<T> *&node);
		void rotateLeft(AVLTreeNode<T> *&node);
		void rotateRightTwice(AVLTreeNode<T> *&node);
		void rotateLeftTwice(AVLTreeNode<T> *&node);
	public:
		AVLTree();
		~AVLTree();
		void add(int recordId, T *node);
		bool remove(int recordId);
		AVLTreeNode<T> *findNode(int recordId);
		AVLTreeNode<T> *getSmallest();
		AVLTreeNode<T> *extractSmallest();
		T *find(int recordId);
		T *findHigher(int recordId);
		void update(int recordId, T *content, AVLTreeNode<T> *node);
		int getCount();
		void debug(bool follow);
		void debugNode(AVLTreeNode<T> *node);
		void printNode(AVLTreeNode<T> *node);


};

#endif
