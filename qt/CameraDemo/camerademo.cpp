#include "camerademo.h"
#include "ui_camerademo.h"
#include <QCameraImageCapture>
#include <QCameraInfo>
#include <QFileDialog>

CameraDemo::CameraDemo(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::UiCameraDemo),
    m_pVideoWidget(nullptr),
    m_pCurrentCamera(nullptr)
{
    ui->setupUi(this);

    m_pVideoWidget = new QVideoWidget();

    ui->gridLayout->addWidget(m_pVideoWidget, 1, 0);

    InitCameraComboxBox();

    connect(ui->m_pPushButtonCapture, SIGNAL(clicked()),this, SLOT(OnCaptureBtnClicked()));
    connect(ui->m_pPushButtonSave, SIGNAL(clicked()),this, SLOT(OnSaveBtnClicked()));
}

CameraDemo::~CameraDemo()
{
    delete m_pVideoWidget;
    delete ui;
}

//初始化摄像机列表
void CameraDemo::InitCameraComboxBox()
{
    qDeleteAll(m_listCamera);

    disconnect(ui->m_pComboBoxCamera, SIGNAL(currentIndexChanged(int)), this, SLOT(OnCameraCboBoxIndexChaned(int)));
    ui->m_pComboBoxCamera->clear();

    QList<QCameraInfo> cameras = QCameraInfo::availableCameras();
    foreach (const QCameraInfo& cameraInfo, cameras)
    {
        ui->m_pComboBoxCamera->addItem(cameraInfo.description());

        m_listCamera.append(new QCamera(cameraInfo));
    }

    ui->m_pComboBoxCamera->setCurrentIndex(0);

    connect(ui->m_pComboBoxCamera, SIGNAL(currentIndexChanged(int)), this, SLOT(OnCameraCboBoxIndexChaned(int)));

    OnCameraCboBoxIndexChaned(0);
}

void CameraDemo::OnCameraCboBoxIndexChaned(int iIndex)
{
    m_pCurrentCamera = m_listCamera.at(iIndex);

    if (nullptr != m_pCurrentCamera)
    {
        m_pCurrentCamera->setViewfinder(m_pVideoWidget);

        m_pCurrentCamera->start();
    }
}

void CameraDemo::OnCaptureBtnClicked()
{
    if (nullptr != m_pCurrentCamera)
    {
        QCamera::State enumState = m_pCurrentCamera->state();
        if (QCamera::ActiveState == enumState)
        {
            m_pCurrentCamera->stop();

            ui->m_pPushButtonSave->setEnabled(false);
            ui->m_pPushButtonCapture->setText("Start");
        }
        else
        {
            m_pCurrentCamera->start();

            ui->m_pPushButtonSave->setEnabled(true);
            ui->m_pPushButtonCapture->setText("Stop");
        }
    }
}

void CameraDemo::OnSaveBtnClicked()
{
    /*
    QString strFileName = QFileDialog::getSaveFileName(this, "Save", "", "PNG(*.png)");

    if (!strFileName.isEmpty())
    {
        //QPixmap pixmap = m_pVideoWidget->grab();
        //pixmap.save(strFileName, "png");


    }*/

    QCameraImageCapture* pImageCapture = new QCameraImageCapture(m_pCurrentCamera);

    m_pCurrentCamera->setCaptureMode(QCamera::CaptureStillImage);
    pImageCapture->setCaptureDestination(QCameraImageCapture::CaptureToFile);

    //on half pressed shutter button
    m_pCurrentCamera->searchAndLock();

    QString strPwd = qApp->applicationDirPath();
    qDebug() << strPwd;

    //on shutter button pressed
    pImageCapture->capture();

    //on shutter button released
    m_pCurrentCamera->unlock();

    delete pImageCapture;
}
