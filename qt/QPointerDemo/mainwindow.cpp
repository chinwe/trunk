#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QPointer>
#include <QWeakPointer>
#include <QDebug>
#include <QLabel>
#include <QSharedPointer>
#include <QDialog>

class CLabel : public QLabel
{
public:
    CLabel(QWidget* pParent = nullptr) : QLabel(pParent)
    {
        qDebug() << "CLabel Construct()";
    }
    ~CLabel()
    {
        qDebug() << "CLabel Destruct()";
    }
};

class CDialog : public QDialog
{
public:
    CDialog(QWidget* pParent = nullptr) : QDialog(pParent)
    {
        qDebug() << "CDialog Construct()";
    }
    ~CDialog()
    {
        qDebug() << "CDialog Destruct()";
    }
};

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    connect(ui->pushButton, &QPushButton::clicked,
            this, &MainWindow::OnBtnClicked);
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::OnBtnClicked()
{
    {
    CDialog wnd;

    ShowTip(&wnd);

    wnd.setFixedSize(200, 60);
    wnd.exec();
    }
    //qDebug() << "isActiveWindow=" << wnd.isActiveWindow();

    ShowTip(this);
}

void MainWindow::ShowTip(QWidget* pWidget, bool bNew)
{
    qDebug() << "ShowTip" << pWidget;

    static QPointer<CLabel> pPLabel;
    if (pPLabel.isNull())
    {
        pPLabel = new CLabel(pWidget);
    }
    else
    {
        pPLabel->setParent(pWidget);
    }

    qDebug() << "parent" << pPLabel->parent();

    pPLabel->setText("show");
    pPLabel->show();
}
