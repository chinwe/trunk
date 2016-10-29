#include "CalendarWidgetDemo.h"
#include "ui_CalendarWidgetDemo.h"
#include "CalendarWidget.h"

CCalendarWidgetDemo::CCalendarWidgetDemo(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::CCalendarWidgetDemo)
{
    ui->setupUi(this);

    CCalendarWidget* pCal = new CCalendarWidget(this);
    ui->gridLayout->addWidget(pCal);

    connect(pCal, SIGNAL(clicked(const QDate&)), this, SLOT(OnClick(const QDate&)));

}

CCalendarWidgetDemo::~CCalendarWidgetDemo()
{
    delete ui;
}

void CCalendarWidgetDemo::OnClick(const QDate &date)
{
    setWindowTitle(date.toString());
}
