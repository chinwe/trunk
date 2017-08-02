#include <iostream>

using namespace std;

struct StringBuffer{
    unsigned int uLength;
    char pData[0];
};

StringBuffer* NewBuffer(unsigned int uSize) {
    unsigned int uMallocSize = uSize + sizeof(StringBuffer);
    StringBuffer* pBuffer = (StringBuffer*)malloc(uMallocSize);
    memset(pBuffer, 0, uMallocSize);
    pBuffer->uLength = uSize;

    return pBuffer;
}

int main(int argc, char** argv) {

    StringBuffer* pBuffer = NewBuffer(16);
    strcpy(pBuffer->pData, "0123456789012345");

    cout << "pBuffer: " << pBuffer << endl;
    cout << "pBuffer->pData: " << &pBuffer->pData << endl;
    cout << "pBuffer->uLength: " << pBuffer->uLength << endl;
    cout << "pBuffer->pData[15]: " << pBuffer->pData[15] << endl;
    return 0;
}