#ifndef _PAGEIDS_H
#define _PAGEIDS_H

#include <jni.h>
#include <stdlib.h>

class PageIds {
	private:
		int count;
		int position;
		int *ids;
		JNIEnv *env;
		jobject policy;
		jmethodID midAcquire, midRelease;
	public:
		PageIds(JNIEnv *env, jobject spaceManagerPolicy);
		int get();
		void iterate();
		void iterationDone();
		void clear();
		void add(int pageId);
};

#endif
