#include "AVLTree.h"

#include <stdlib.h>
#include <android/log.h>
#include "RecordInfo.h"
#include "ChildInfo.h"


inline int
MIN(int a, int b) { return  (a < b) ? a : b; }

inline int
MAX(int a, int b) { return  (a > b) ? a : b; }

template <typename T> void AVLTreeNode<T>::updateHeight() {
	int a = 0;
	balance = 0;
	if (this->left != NULL) {
		a = this->left->height;
		balance -= a;
	}
	int b = 0;
	if (this->right != NULL) {
		b = this->right->height;
		balance += b;
	}
	height = 1 + MAX(a,b);

}

template <typename T> AVLTree<T>::AVLTree() {
	count = 0;
	root = NULL;
}

template <typename T> void AVLTree<T>::freeNode(AVLTreeNode<T> *node) {
	if (node != NULL) {
		freeNode(node->left);
		freeNode(node->right);
		delete node->content;
		delete node;
	}

}

template <typename T> AVLTree<T>::~AVLTree() {
	freeNode(root);
}

template <typename T> void AVLTree<T>::add(int recordId, T *content) {
	count++;
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding to AVL tree node %d, new count: %d", recordId, count);
	AVLTreeNode<T> *node = new AVLTreeNode<T>();
	node->recordId = recordId;
	node->content = content;
	node->left = NULL;
	node->right = NULL;
	node->updateHeight();

	if (root == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "root is null, creating new");
		root = node;
		node->parent = NULL;
		return;
	}

	AVLTreeNode<T> *n = root;
	AVLTreeNode<T> *prev;

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "searching for a good place");
	while(n != NULL) {
		prev = n;
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "node: %d", n->recordId);

		if (recordId < n->recordId) {
			n = n->left;
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "left");
		} else {
			n = n->right;
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "right");
		}
	}
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "found");

	node->parent = prev;

	if (recordId < prev->recordId)
		prev->left = node;
	else
		prev->right = node;

	rebalance(prev);

}

template <typename T> bool AVLTree<T>::remove(int recordId) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "remove");
	AVLTreeNode<T> *node = findNode(recordId);
	if (node != NULL) {
		count--;
		removeNode(node);
		if (node->parent != NULL)
			rebalance(node->parent);
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "pre delete");
		delete node->content;
		delete node;
		return true;
	}

	return false;
}

template <typename T> void AVLTree<T>::removeNode(AVLTreeNode<T> *node) {
	if (node == root) {
		if (node->left == NULL && node->right == NULL) {
			root = NULL;
			return;
		}

		if (node->left == NULL) {
			root = node->right;
			return;
		} else if (node->right == NULL) {
			root = node->left;
			return;
		}

		AVLTreeNode<T> *prev, *n = node->left;

		while(n != NULL) {
			prev = n;
			n = n->right;
		}

		prev->parent = NULL;
		root = prev;

		return;
	}

	if (node->left == NULL && node->right == NULL) {
		if (node->recordId < node->parent->recordId)
			node->parent->left = NULL;
		else
			node->parent->right = NULL;

		return;
	}

	if (node->left == NULL) {
		if (node->recordId < node->parent->recordId)
			node->parent->left = node->right;
		else
			node->parent->right = node->right;
		return;
	} else if (node->right == NULL) {
		if (node->recordId < node->parent->recordId)
			node->parent->left = node->left;
		else
			node->parent->right = node->left;

		return;
	}

	AVLTreeNode<T> *prev, *n = node->left;

	while(n != NULL) {
		prev = n;
		n = n->right;
	}

	prev->parent = node->parent;
	if (node->recordId < node->parent->recordId)
		node->parent->left = prev;
	else
		node->parent->right = prev;
}

template <typename T> AVLTreeNode<T> *AVLTree<T>::getSmallest() {
	AVLTreeNode<T> *prev, *node = root;

	if (node == NULL)
		return NULL;

	while(node != NULL) {
		prev = node;
		node = node->left;
	}

	return prev;
	
}

template <typename T> AVLTreeNode<T> *AVLTree<T>::extractSmallest() {
	AVLTreeNode<T> *prev, *node = root;

	if (node == NULL)
		return NULL;

	while(node != NULL) {
		prev = node;
		node = node->left;
	}

	count--;
	removeNode(node);
	if (node->parent != NULL)
		rebalance(node->parent);

	return prev;
	
}

template <typename T> AVLTreeNode<T> *AVLTree<T>::findNode(int recordId) {
	AVLTreeNode<T> *node = root;

	if (node == NULL)
		return NULL;

	while(node->recordId != recordId) {
		if (recordId < node->recordId)
			node = node->left;
		else
			node = node->right;

		if (node == NULL)
			return NULL;
	}

	return node;
}

template <typename T> T *AVLTree<T>::find(int recordId) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "AVL find");
	AVLTreeNode<T> *node = findNode(recordId);

	if (node != NULL)
		return node->content;
	else
		return NULL;
}

template <typename T> void AVLTree<T>::update(int recordId, T *content, AVLTreeNode<T> *node) {
	delete node->content;
	node->content = content;
}


template <typename T> void AVLTree<T>::rebalance(AVLTreeNode<T> *node) {
	node->updateHeight();
	int b = node->balance;
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "rebalancing node %d, balance: %d", node->recordId, b);
	if (b < -1) {
		if (node->left->balance == 1)
			rotateRightTwice(node);
		else
			rotateRight(node);
	} else if (b > 1) {
		if (node->right->balance == -1)
			rotateLeftTwice(node);
		else
			rotateLeft(node);
	}
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "here");

	if(node->parent != NULL)
		rebalance(node->parent);
}

template <typename T> void AVLTree<T>::rotateLeft(AVLTreeNode<T> *&node) {
	AVLTreeNode<T> *oldRoot = node;

	node = node->right;
	oldRoot->right = node->left;
	node->left = oldRoot;

	oldRoot->updateHeight();
	node->updateHeight();

	if (oldRoot == root)
		root = node;
}

template <typename T> void AVLTree<T>::rotateLeftTwice(AVLTreeNode<T> *&node) {
	rotateRight(node->right);
	rotateLeft(node);
}

template <typename T> void AVLTree<T>::rotateRight(AVLTreeNode<T> *&node) {
	AVLTreeNode<T> *oldRoot = node;

	node = node->left;
	oldRoot->left = node->right;
	node->right = oldRoot;

	oldRoot->updateHeight();
	node->updateHeight();

	if (oldRoot == root)
		root = node;
}

template <typename T> void AVLTree<T>::rotateRightTwice(AVLTreeNode<T> *&node) {
	rotateLeft(node->left);
	rotateRight(node);
}



template class AVLTree<RecordInfo>;
template class AVLTree<ChildInfo>;

