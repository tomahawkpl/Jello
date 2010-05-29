#ifndef _CHILDINFO_H
#define _CHILDINFO_H

class BTreeElement;

struct ChildInfo {
	BTreeElement *child;
	~ChildInfo();
};
#endif
