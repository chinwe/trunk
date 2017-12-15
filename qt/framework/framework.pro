#-------------------------------------------------
#
# Project created by QtCreator 2017-12-15T23:21:14
#
#-------------------------------------------------

QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = framework
TEMPLATE = app

# qmake automatically generates a bundle for your application.
# To disable this, add the following statement to your application's project file (.pro):
# CONFIG -= app_bundle

# The following define makes your compiler emit warnings if you use
# any feature of Qt which has been marked as deprecated (the exact warnings
# depend on your compiler). Please consult the documentation of the
# deprecated API in order to know how to port your code away from it.
DEFINES += QT_DEPRECATED_WARNINGS

# You can also make your code fail to compile if you use deprecated APIs.
# In order to do so, uncomment the following line.
# You can also select to disable deprecated APIs only up to a certain version of Qt.
#DEFINES += QT_DISABLE_DEPRECATED_BEFORE=0x060000    # disables all the APIs deprecated before Qt 6.0.0


SOURCES += \
        main.cpp \
        cframwworkwidget.cpp

HEADERS += \
        cframwworkwidget.h

FORMS += \
        cframwworkwidget.ui


macx: LIBS += -L$$PWD/../../qt/build-testcomponent-Desktop_Qt_5_9_3_clang_64bit-Release/ -ltestcomponent.1.0.0

INCLUDEPATH += $$PWD/../../qt/testcomponent
DEPENDPATH += $$PWD/../../qt/testcomponent
