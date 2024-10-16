#include "LifeParallelImplementation.h"
#include <stdlib.h>
#include <mpi.h>
#include <algorithm>

LifeParallelImplementation::LifeParallelImplementation() { }

void LifeParallelImplementation::beforeFirstStep() {
    MPI_Status ms;
    int msize;
    int mrank;
    int segment;
    int first;
    int last;
    MPI_Comm_size(MPI_COMM_WORLD, &msize);
    MPI_Comm_rank(MPI_COMM_WORLD, &mrank);
    segment = size / msize;
    first = std::max(mrank * segment, 1);
    last = (mrank + 1) * segment - 1;
    if(mrank == msize - 1 ) last = size - 2;

    if (!mrank) {
        for (int index=last+1;index<size - 1;index++) {
            MPI_Send(cells[index], size, MPI_INT, std::min(index/segment, msize-1), index, MPI_COMM_WORLD);
            MPI_Send(pollution[index], size, MPI_INT, std::min(index/segment, msize-1), index, MPI_COMM_WORLD);
        }
    } else {
        for (int index=first;index<=last;index++) {
            MPI_Recv(cells[index], size, MPI_INT, 0, index, MPI_COMM_WORLD, &ms);
            MPI_Recv(pollution[index], size, MPI_INT, 0, index, MPI_COMM_WORLD, &ms);
        }
    }
}

void LifeParallelImplementation::realStep() {
    MPI_Status ms;
    int msize;
    int mrank;
    int segment;
    int first;
    int last;
    MPI_Comm_size(MPI_COMM_WORLD, &msize);
    MPI_Comm_rank(MPI_COMM_WORLD, &mrank);
    segment = size / msize;
    first = std::max(mrank * segment, 1);
    last = (mrank + 1) * segment - 1;
    if(mrank == msize - 1 ) last = size - 2;

    if (msize > 1) {
        if (mrank < msize - 1) {
            MPI_Send(cells[last], size, MPI_INT, mrank + 1, 0, MPI_COMM_WORLD);
            MPI_Send(pollution[last], size, MPI_INT, mrank + 1, 1, MPI_COMM_WORLD);
        }

        if (mrank > 0) {
            MPI_Recv(cells[first - 1], size, MPI_INT, mrank - 1, 0, MPI_COMM_WORLD, &ms);
            MPI_Recv(pollution[first - 1], size, MPI_INT, mrank - 1, 1, MPI_COMM_WORLD, &ms);

            MPI_Send(cells[first], size, MPI_INT, mrank - 1, 0, MPI_COMM_WORLD);
            MPI_Send(pollution[first], size, MPI_INT, mrank - 1, 1, MPI_COMM_WORLD);
        }

        if (mrank < msize - 1) {
            MPI_Recv(cells[last + 1], size, MPI_INT, mrank + 1, 0, MPI_COMM_WORLD, &ms);
            MPI_Recv(pollution[last + 1], size, MPI_INT, mrank + 1, 1, MPI_COMM_WORLD, &ms);
        }
    }

	int currentState, currentPollution;
	for (int row = first; row <= last; row++)
		for (int col = 1; col < size_1; col++)
		{
			currentState = cells[row][col];
			currentPollution = pollution[row][col];
			cellsNext[row][col] = rules->cellNextState(currentState, liveNeighbours(row, col),
													   currentPollution);
			pollutionNext[row][col] =
				rules->nextPollution(currentState, currentPollution, pollution[row + 1][col] + pollution[row - 1][col] + pollution[row][col - 1] + pollution[row][col + 1],
									 pollution[row - 1][col - 1] + pollution[row - 1][col + 1] + pollution[row + 1][col - 1] + pollution[row + 1][col + 1]);
		}
}

void LifeParallelImplementation::afterLastStep() {
    MPI_Status ms;
    int msize;
    int mrank;
    int segment;
    int first;
    int last;
    MPI_Comm_size(MPI_COMM_WORLD, &msize);
    MPI_Comm_rank(MPI_COMM_WORLD, &mrank);
    segment = size / msize;
    first = std::max(mrank * segment, 1);
    last = (mrank + 1) * segment - 1;
    if(mrank == msize - 1 ) last = size - 2;

    if (!mrank) {
        for (int index=last+1;index<size - 1;index++) {
            MPI_Recv(cells[index], size, MPI_INT, std::min(index/segment, msize-1), index, MPI_COMM_WORLD, &ms);
            MPI_Recv(pollution[index], size, MPI_INT, std::min(index/segment, msize-1), index, MPI_COMM_WORLD, &ms);
        }
    } else {
        for (int index=first;index<=last;index++) {
            MPI_Send(cells[index], size, MPI_INT, 0, index, MPI_COMM_WORLD);
            MPI_Send(pollution[index], size, MPI_INT, 0, index, MPI_COMM_WORLD);
        }
    }
}

void LifeParallelImplementation::oneStep() {
	realStep();
	swapTables();
}

int LifeParallelImplementation::numberOfLivingCells() {
	return sumTable( cells );
}

double LifeParallelImplementation::averagePollution() {
	return (double)sumTable( pollution ) / size_1_squared / rules->getMaxPollution();
}
