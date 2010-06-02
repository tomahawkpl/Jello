#ifndef _CHILDINFO_H
#define _CHILDINFO_H

class BTreeElement;

#include <stdlib.h>
#include "NodeContent.h"

class ChildInfo : public NodeContent {
	public:
		BTreeElement *child;
		~ChildInfo();

		int getLength();
		void toBytes(uint8_t *bytes);
		void fromBytes(uint8_t *bytes, int length, BTreeElement *parent);
};
#endif
