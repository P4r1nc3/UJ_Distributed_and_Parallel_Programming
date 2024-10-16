
#ifndef LIFEPARALLEL
#define LIFEPARALLEL

#include "Life.h"

class LifeParallelImplementation: public Life {
protected:
	void realStep();
public:
	LifeParallelImplementation();
	int numberOfLivingCells();
	double averagePollution();
	void beforeFirstStep();
	void oneStep();
	void afterLastStep();
};

#endif
