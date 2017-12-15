#include "cframwworkwidget.h"
#include "ui_cframwworkwidget.h"

CFramwWorkWidget::CFramwWorkWidget(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::CFramwWorkWidget)
{
    ui->setupUi(this);
}

CFramwWorkWidget::~CFramwWorkWidget()
{
    delete ui;
}
