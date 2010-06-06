#include "jni.h"

class BTreeElement;
class BTreeLeaf;
class BTreeNode;
class PageIds;
struct RecordInfo;

class BTree {
	private:
		BTreeElement *root;

		void mergeNode(BTreeElement *node);
		void removeNode(BTreeElement *node);
		void addToNode(BTreeNode *node, BTreeElement *child);
		PageIds *pageIds;
		int klassIndexPageId;
	public:
		BTree(short leafCapacity, short nodeCapacity, jobject pagedFile, jobject bTreePage,
				jobject spaceManagerPolicy, JNIEnv *env, int klassIndexPageId);
		~BTree();
		void initIDs();
		void add(int id, RecordInfo *record);
		void update(int id, RecordInfo *record);
		RecordInfo* find(int id);
		void remove(int id);
		void debug();
		bool load();
		void commit();
		PageIds *getPageIds();

		static short leafCapacity, nodeCapacity;
		static JNIEnv *env;
		static jobject pagedFile, spaceManagerPolicy, pagePoolProxy;
		static jfieldID fidPageId, fidPageData;
		static jmethodID midPagedFileReadPage, midPagedFileWritePage;
		static jmethodID midPagePoolProxyAcquire, midPagePoolProxyRelease;
};
