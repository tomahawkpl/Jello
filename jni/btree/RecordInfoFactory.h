#ifndef _RECORDINFOFACTORY_H
#define _RECORDINFOFACTORY_H

#include "NodeContentFactory.h"

class RecordInfoFactory : public NodeContentFactory {
	NodeContent *create();
};

#endif
