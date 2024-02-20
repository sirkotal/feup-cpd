#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

 
void OnMult(int m_ar, int m_br) 
{
	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br)
{
	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

    double* pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	double* phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	double* phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

    Time1 = clock();

    for (i=0; i<m_ar; i++) {
        for (k=0; k<m_ar; k++) {
            for (j=0; j<m_br; j++) {
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
            }
        }
    }

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

    double* pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	double* phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	double* phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

    Time1 = clock();

    for (int b1 = 0; b1 < m_ar; b1 += bkSize) {
        for (int b3 = 0; b3 < m_ar; b3 += bkSize) {
            for (int b2 = 0; b2 < m_br; b2 += bkSize) {
                for (int i = b1; i < b1 + bkSize; i++) {
                    for (int k = b3; k < b3 + bkSize; k++) {
                        for (int j = b2; j < b2 + bkSize; j++) {
                            phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
                        }
                    }
                }
            }
        }
    }

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

void OnMultLineP1(int m_ar, int m_br)
{

	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

	#pragma omp parallel for
	for (i = 0; i < m_ar; i++) {	
		for (k = 0; k < m_ar; k++) {	
			for (j = 0; j < m_br; j++) {	
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
			}
		}
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++) {	
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
    
}

void OnMultLineP2(int m_ar, int m_br)
{

	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

	#pragma omp parallel for
	for (i = 0; i < m_ar; i++) {	
		for (k = 0; k < m_ar; k++) {	
			#pragma omp for
			for (j = 0; j < m_br; j++) {	
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
			}
		}
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++) {	
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
    
}



void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

enum Type {
    DEFAULT,
    LINE,
    BLOCK,
};

std::string shift_args(int *argc, char ***argv) {
    (*argc)--;
    return *((*argv)++);
}

void help_screen(std::string& prog_name) {
    std::cout << "Usage: " << prog_name << " [options]" << std::endl;
    std::cout << "Options:" << std::endl;
    std::cout << "  -t, --type <type>      Type of multiplication (default, line, block)" << std::endl;
    std::cout << "  -d, --dimension <dim>  Dimension of the matrix (default = 1024)" << std::endl;
    std::cout << "  -b, --block <size>     Block size for block multiplication" << std::endl;
}

int main (int argc, char *argv[])
{

	char c;
    int blockSize = 0;
	int lin, col = 1024;
    enum Type op = DEFAULT;
	
	int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;

    std::string prog_name = shift_args(&argc, &argv);

    if (argc == 0) {
        help_screen(prog_name);
        return 1;
    }

    while (argc != 0) {
        std::string input = shift_args(&argc, &argv);

        if (input == "-t" || input == "--type") {
            input = shift_args(&argc, &argv);

            if (input == "default") {
                op = DEFAULT;
            }
            else if (input == "line") {
                op = LINE;
            } else if (input == "block") {
                op = BLOCK;
            }
        } else if (input == "-d" || input == "--dimension") {
            input = shift_args(&argc, &argv);
            lin = std::stoi(input);
            col = std::stoi(input);
        } else if (input == "-b" || input == "--block") {
            input = shift_args(&argc, &argv);
            blockSize = std::stoi(input);
        } else if (input == "-h" || input == "--help") {
            help_screen(prog_name);
            return 0;
        } else {
            std::cerr << "Invalid option: " << input << std::endl;
            help_screen(prog_name);
            return 1;
        }
    }

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

    // Start counting
    ret = PAPI_start(EventSet);
    if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

    switch (op){
        case DEFAULT: 
            OnMult(lin, col);
        break;
        case LINE:
            OnMultLine(lin, col);  
        break;
        case BLOCK:
            if (!blockSize) {
                std::cerr << "Block size is required" << std::endl;
                help_screen(prog_name);
                return 1;
            }
            OnMultBlock(lin, col, blockSize);  
        break;

    }

    ret = PAPI_stop(EventSet, values);
    if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
    printf("L1 DCM: %lld \n",values[0]);
    printf("L2 DCM: %lld \n",values[1]);

    ret = PAPI_reset( EventSet );
    if ( ret != PAPI_OK )
        std::cout << "FAIL reset" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

}
