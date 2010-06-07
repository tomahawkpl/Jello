#include "AVLTree.h"

#include <stdlib.h>
#include <android/log.h>
#include "RecordInfo.h"
#include "ChildInfo.h"
#include "BTreeElement.h"
#include "misc.h"
#include "NodeContentFactory.h"


inline int
MIN(int a, int b) { return  (a < b) ? a : b; }

inline int
MAX(int a, int b) { return  (a > b) ? a : b; }


void AVLTreeNode::updateHeight() {
	int oldHeight = height;
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

	if (parent != NULL && oldHeight != height)
		parent->updateHeight();

}

AVLTree::AVLTree(NodeContentFactory *factory) {
	count = 0;
	root = NULL;
	this->factory = factory;
}

void AVLTree::freeNode(AVLTreeNode *node) {
	if (node != NULL) {
		freeNode(node->left);
		freeNode(node->right);
		delete node->content;
		delete node;
	}

}

AVLTree::~AVLTree() {
	freeNode(root);
}

void AVLTree::add(int recordId, NodeContent *content) {
	count++;
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]adding to AVL tree node %d, new count: %d",
//			recordId, count);
	AVLTreeNode *node = new AVLTreeNode();
	node->recordId = recordId;
	node->content = content;
	node->left = NULL;
	node->right = NULL;
	node->updateHeight();

	if (root == NULL) {
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]root is null, creating new");
		root = node;
		node->parent = NULL;
		return;
	}

	AVLTreeNode *n = root;
	AVLTreeNode *prev = NULL;

//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]searching for a good place");
	while(n != NULL) {
		prev = n;
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]node: %d", n->recordId);

		if (recordId < n->recordId) {
			n = n->left;
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]left");
		} else {
			n = n->right;
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]right");
		}
	}
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]found");

	node->parent = prev;

	if (recordId < prev->recordId)
		prev->left = node;
	else
		prev->right = node;

	prev->updateHeight();

	rebalance(prev);

}

bool AVLTree::remove(int recordId) {
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]remove %d", recordId);
	AVLTreeNode *node = findNode(recordId);
	if (node != NULL) {
		count--;
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]removing node");
		removeNode(node);
		if (node->parent != NULL)
			rebalance(node->parent);
		delete node->content;
		delete node;
		return true;
	}

	return false;
}

void AVLTree::removeNode(AVLTreeNode *node) {
	if (node == root) {
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]removeNode (root): %d", node->recordId);
		if (node->left == NULL && node->right == NULL) {
			root = NULL;
			return;
		}

		if (node->left == NULL) {
			root = node->right;
			root->parent = NULL;
			return;
		} else if (node->right == NULL) {
			root = node->left;
			root->parent = NULL;
			return;
		}

		AVLTreeNode *prev = NULL, *n = node->left;

		while(n != NULL) {
			prev = n;
			n = n->right;
		}

		removeNode(prev);

		prev->parent = NULL;
		prev->right = node->right;
		prev->left = node->left;

		if (prev->left != NULL)
			prev->left->parent = prev;
		if (prev->right != NULL)
			prev->right->parent = prev;

		prev->updateHeight();

		root = prev;

		return;
	}

	if (node->left == NULL && node->right == NULL) {
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] both leaves are NULL");
		if (node->recordId < node->parent->recordId)
			node->parent->left = NULL;
		else
			node->parent->right = NULL;
		
		node->parent->updateHeight();
		return;
	}

	if (node->left == NULL) {
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] left is NULL");
		if (node->recordId < node->parent->recordId)
			node->parent->left = node->right;
		else
			node->parent->right = node->right;
		node->right->parent = node->parent;
		node->parent->updateHeight();
		return;
	} else if (node->right == NULL) {
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] right is NULL");
		if (node->recordId < node->parent->recordId)
			node->parent->left = node->left;
		else
			node->parent->right = node->left;
		node->left->parent = node->parent;
		node->parent->updateHeight();
		return;
	}

	AVLTreeNode *prev = NULL, *n = node->left;

	while(n != NULL) {
		prev = n;
		n = n->right;
	}

//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] both are not NULL, substituting with %d", prev);

	removeNode(prev);
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] substitution removed");

	prev->parent = node->parent;
	prev->right = node->right;
	prev->left = node->left;
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] 1");

	if (prev->left != NULL)
		prev->left->parent = prev;
	if (prev->right != NULL)
		prev->right->parent = prev;
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] 2");

	if (prev->recordId < node->parent->recordId)
		node->parent->left = prev;
	else
		node->parent->right = prev;
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] 3");

	prev->updateHeight();
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL] remove done");
}

AVLTreeNode *AVLTree::getSmallest() {
	AVLTreeNode *prev = NULL, *node = root;

	if (node == NULL)
		return NULL;

	while(node != NULL) {
		prev = node;
		node = node->left;
	}

	return prev;

}

AVLTreeNode *AVLTree::extractSmallest() {
	AVLTreeNode *prev = NULL, *node = root;

	if (node == NULL)
		return NULL;

	while(node != NULL) {
		prev = node;
		node = node->left;
	}

	node = prev;

	count--;
	removeNode(node);
	if (node->parent != NULL)
		rebalance(node->parent);

	return prev;

}

AVLTreeNode *AVLTree::findNode(int recordId) {
	AVLTreeNode *node = root;

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

NodeContent *AVLTree::find(int recordId) {
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]AVL find");
	AVLTreeNode *node = findNode(recordId);

	if (node != NULL)
		return node->content;
	else
		return NULL;
}

NodeContent *AVLTree::findHigher(int recordId) {
	AVLTreeNode *prev = NULL, *node = root;

	if (node == NULL) {
		return NULL;
	}

	while(node != NULL) {
		if (node->recordId <= recordId)
			prev = node;
		if (node->recordId > recordId)
			node = node->left;
		else
			node = node->right;
	}

	node = prev;

	if (node != NULL)
		return node->content;
	else
		return NULL;

}

NodeContent *AVLTree::findLeft(int recordId) {
	AVLTreeNode *prev = NULL, *node = root;

	if (node == NULL) {
		return NULL;
	}

	while(node != NULL) {
		if (node->recordId < recordId) {
			prev = node;
			node = node->right;
		} else
			node = node->left;
	}

	node = prev;

	if (node != NULL)
		return node->content;
	else
		return NULL;
}

AVLTreeNode *AVLTree::findRightNode(int recordId) {
	AVLTreeNode *prev = NULL, *node = root;

	if (node == NULL) {
		return NULL;
	}

	while(node != NULL) {
		if (node->recordId > recordId) {
			prev = node;
			node = node->left;
		} else
			node = node->right;
	}

	return prev;
}

NodeContent *AVLTree::findRight(int recordId) {
	AVLTreeNode *prev = NULL, *node = root;

	if (node == NULL) {
		return NULL;
	}

	while(node != NULL) {
		if (node->recordId > recordId) {
			prev = node;
			node = node->left;
		} else
			node = node->right;
	}

	node = prev;

	if (node != NULL)
		return node->content;
	else
		return NULL;
}

void AVLTree::update(int recordId, NodeContent *content, AVLTreeNode *node) {
	delete node->content;
	node->content = content;
}


void AVLTree::rebalance(AVLTreeNode *node) {
	int b = node->balance;
	AVLTreeNode *parent = node->parent;
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]rebalancing node %d, balance: %d", node->recordId, b);
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

	if(parent != NULL) {
	//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "[AVL]rebalancing parent");
		rebalance(parent);
	}
}

void AVLTree::rotateLeft(AVLTreeNode *&node) {
	AVLTreeNode *oldRoot = node;

	node = node->right;
	oldRoot->right = node->left;

	if (oldRoot->right != NULL)
		oldRoot->right->parent = oldRoot;

	node->left = oldRoot;

	oldRoot->updateHeight();
	node->updateHeight();

	node->parent = oldRoot->parent;
	oldRoot->parent = node;

	if (node->parent != NULL)
		if (node->parent->recordId > node->recordId)
			node->parent->left = node;
		else
			node->parent->right = node;

	if (oldRoot == root)
		root = node;
}

void AVLTree::rotateLeftTwice(AVLTreeNode *&node) {
	rotateRight(node->right);
	rotateLeft(node);
}

void AVLTree::rotateRight(AVLTreeNode *&node) {
	AVLTreeNode *oldRoot = node;

	node = node->left;
	oldRoot->left = node->right;

	if (oldRoot->left != NULL)
		oldRoot->left->parent = oldRoot;

	node->right = oldRoot;

	oldRoot->updateHeight();
	node->updateHeight();

	node->parent = oldRoot->parent;
	oldRoot->parent = node;

	if (node->parent != NULL)
		if (node->parent->recordId > node->recordId)
			node->parent->left = node;
		else
			node->parent->right = node;

	if (oldRoot == root)
		root = node;
}

void AVLTree::rotateRightTwice(AVLTreeNode *&node) {
	rotateLeft(node->left);
	rotateRight(node);
}

int AVLTree::nodeToBytes(uint8_t *bytes, AVLTreeNode *node) {
	int len = 0;

	if (node == NULL) {
		intToBytes(-1, bytes);
		return 4;
	}

	intToBytes(node->recordId, bytes);
	intToBytes(node->height, bytes + 4);
	intToBytes(node->balance, bytes + 8);
	intToBytes(node->content->getLength(), bytes + 12);
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "content->getLength(): %d", node->content->getLength());
	                                  
	node->content->toBytes(bytes + 16);

	len += 16 + node->content->getLength();

	len += nodeToBytes(bytes + len, node->left);
	len += nodeToBytes(bytes + len, node->right);

	return len;
}

void AVLTree::commit(uint8_t *bytes) {
	nodeToBytes(bytes, root);
}

AVLTreeNode *AVLTree::nodeFromBytes(uint8_t *bytes, int &read, BTreeElement *parent) {
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "nodeFromBytes (%d)", bytes);
	int id;
	bytesToInt(id, bytes);

	if (id == -1) {
		read = 4;
		return NULL;
	}

	count++;
	AVLTreeNode *node = new AVLTreeNode();
	node->content = factory->create();
	node->recordId = id;
	bytesToInt(node->height, bytes + 4);
	bytesToInt(node->balance, bytes + 8);
	int len;
	bytesToInt(len, bytes + 12);
	node->content->fromBytes(bytes+16, len, parent);

	read = 16 + len;

	int r;
	AVLTreeNode *left = nodeFromBytes(bytes + read, r, parent);
	read += r;

	if (left != NULL) {
		node->left = left;
		left->parent = node;
	} else
		node->left = NULL;

	AVLTreeNode *right = nodeFromBytes(bytes + read, r, parent);
	read += r;

	if (right != NULL) {
		node->right = right;
		right->parent = node;
	} else
		node->right = NULL;

	return node;


}

void AVLTree::fromBytes(uint8_t *bytes, BTreeElement *parent) {
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "fromBytes (%d)", bytes);
	int r;
	root = nodeFromBytes(bytes, r, parent);

	if (root != NULL)
		root->parent = NULL;
}

void AVLTree::debug(bool follow) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "   AVLTree contents (count: %d)", count);

	if (root != NULL)
		printNode(root);
	else
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "   root is NULL");

	if (!follow)
		return;

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "   Children >>>", count);

	if (root != NULL)
		debugNode(root);

}


void AVLTree::printNode(AVLTreeNode *node) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "   (%d) recordId: %d, parent: %d, height:%d, content:%d", node,
			node->recordId, node->parent, node->height, node->content);
	if (node->left != NULL)
		printNode(node->left);
	if (node->right != NULL)
		printNode(node->right);

}

void AVLTree::debugNode(AVLTreeNode *node) {
	((ChildInfo*)node->content)->child->debug();
	if (node->left != NULL)
		debugNode(node->left);
	if (node->right != NULL)
		debugNode(node->right);

}


