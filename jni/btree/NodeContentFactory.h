#ifndef _NODECONTENTFACTORY_H
#define _NODECONTENTFACTORY_H

class NodeContent;

class NodeContentFactory {
	public:
		virtual NodeContent *create() = 0;
};

#endif
