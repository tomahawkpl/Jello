#include "PageIds.h"

PageIds::PageIds(JNIEnv *env, jobject spaceManagerPolicy) {
	count = 0;
	ids = NULL;

	this->env = env;

	this->policy = spaceManagerPolicy;

	jclass klass;

	klass = env->FindClass("com/atteo/jello/space/SpaceManagerPolicy");
	if (klass == NULL)
		return;

	midAcquire = env->GetMethodID(klass, "acquirePage", "()I");
	if (midAcquire == NULL)
		return;

	midRelease = env->GetMethodID(klass, "releasePage", "(I)V");
	if (midRelease == NULL)
		return;

}

void PageIds::add(int pageId) {
	count++;
	ids = (int*)realloc(ids, count * 4);
	ids[count - 1] = pageId;
}

void PageIds::iterate() {
	position = 0;
}

void PageIds::iterationDone() {
	count = position;
	for (int i=position; i<count; i++) {
		env->CallVoidMethod(policy, midRelease, ids[i]);
	}

	ids = (int*) realloc(ids, count * 4);


}

int PageIds::get() {
	if (count > position) {
		position++;
		return ids[position-1];
	}

	position++;
	count++;
	int id = env->CallIntMethod(policy, midAcquire);
	ids = (int*)realloc(ids, count * 4);
	ids[count - 1] = id;

	return id;
}

void PageIds::clear() {
	count = 0;
	ids = (int*)realloc(ids,0);
}
