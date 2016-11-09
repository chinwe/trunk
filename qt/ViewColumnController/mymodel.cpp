#include "mymodel.h"

MyModel::MyModel(QObject* pParent)
    : QStandardItemModel(pParent)
{
    QStringList strlistHeader;
    strlistHeader << "Col A" << "Col B" << "Col C";
    setHorizontalHeaderLabels(strlistHeader);
}

