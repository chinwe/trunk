#ifndef CAMERADEMO_H
#define CAMERADEMO_H

#include <QWidget>
#include <QCamera>
#include <QVideoWidget>

namespace Ui {
class UiCameraDemo;
}

class CameraDemo : public QWidget
{
    Q_OBJECT

public:
    explicit CameraDemo(QWidget *parent = 0);
    ~CameraDemo();

private:
    //初始化摄像机列表
    void InitCameraComboxBox();

private slots:
    void OnCameraCboBoxIndexChaned(int iIndex);

    void OnCaptureBtnClicked();

    void OnSaveBtnClicked();

private:
    Ui::UiCameraDemo *ui;
    QVideoWidget* m_pVideoWidget;
    QList<QCamera*> m_listCamera;
    QCamera* m_pCurrentCamera;
};

#endif // CAMERADEMO_H
