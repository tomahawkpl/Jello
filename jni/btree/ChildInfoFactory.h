#ifndef _CHILDINFOFACTORY_H
#define _CHILDINFOFACTORY_H

#include "NodeContentFactory.h"

class ChildInfoFactory : public NodeContentFactory {
	NodeContent *create();
};

#endif
